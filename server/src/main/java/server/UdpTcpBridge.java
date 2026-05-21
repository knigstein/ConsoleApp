package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Мост TCP ↔ UDP с кадрированием (4 байта длины + payload).
 * <p>
 * Режим {@code udp-lsn}: одно постоянное TCP к SSH — запросы и ACK идут без очереди 12 с.<br>
 * Режим {@code tcp-lsn}: одна TCP-сессия за раз к одному UDP-сокету сервера.
 */
public final class UdpTcpBridge {

    private static final int BUFFER_SIZE = 64 * 1024;
    private static final boolean VERBOSE = "1".equals(System.getenv("LAB5_BRIDGE_VERBOSE"));
    private UdpTcpBridge() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }
        try {
            String mode = args[0];
            switch (mode) {
                case "tcp-lsn" -> runTcpListen(
                        parseInt(args, 1, 15555),
                        args.length > 2 ? args[2] : "127.0.0.1",
                        parseInt(args, 3, 5555)
                );
                case "udp-lsn" -> {
                    UdpListenArgs parsed = parseUdpListenArgs(args);
                    runUdpListenPersist(parsed.udpBind(), parsed.udpPort(), parsed.tcpHost(), parsed.tcpPort());
                }
                default -> {
                    printUsage();
                    System.exit(1);
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid arguments: " + e.getMessage());
            printUsage();
            System.exit(1);
        }
    }

    private static UdpListenArgs parseUdpListenArgs(String[] args) {
        String udpBind = "127.0.0.1";
        int cursor = 1;

        if (args.length > cursor && !isInteger(args[cursor])) {
            udpBind = args[cursor];
            cursor++;
        }

        int udpPort = args.length > cursor ? parseIntStrict(args[cursor], "udpPort") : 5555;
        cursor++;

        String tcpHost = args.length > cursor ? args[cursor] : "127.0.0.1";
        cursor++;

        int tcpPort = args.length > cursor ? parseIntStrict(args[cursor], "tcpPort") : 15555;
        cursor++;

        if (args.length > cursor) {
            throw new IllegalArgumentException("too many arguments for udp-lsn");
        }

        return new UdpListenArgs(udpBind, udpPort, tcpHost, tcpPort);
    }

    private record UdpListenArgs(String udpBind, int udpPort, String tcpHost, int tcpPort) {
    }

    private static boolean isInteger(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        int start = value.charAt(0) == '-' ? 1 : 0;
        if (start == value.length()) {
            return false;
        }
        for (int i = start; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static int parseIntStrict(String value, String name) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be an integer: " + value, e);
        }
    }

    private static int parseInt(String[] args, int index, int defaultValue) {
        if (args.length <= index) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(args[index]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("argument at index " + index + " must be integer: " + args[index], e);
        }
    }

    private static void printUsage() {
        System.err.println("Usage:");
        System.err.println("  tcp-lsn <tcpPort> [udpHost] [udpPort]   — Helios");
        System.err.println("  udp-lsn [udpBind] [udpPort] [tcpHost] [tcpPort] — Arch (persistent TCP)");
    }

    /** Helios: одна TCP-сессия (SSH -L) ↔ один connected UDP к серверу. */
    private static void runTcpListen(int tcpPort, String udpHost, int udpPort) throws IOException {
        System.out.printf("Lab5 bridge [tcp-lsn]: TCP 127.0.0.1:%d <-> UDP %s:%d (framed, one session)%n", tcpPort, udpHost, udpPort);
        try (ServerSocket server = new ServerSocket()) {
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress("127.0.0.1", tcpPort));
            while (true) {
                Socket tcp = server.accept();
                tcp.setTcpNoDelay(true);
                System.out.println("TCP connected (tunnel)");
                relayTcpSession(tcp, udpHost, udpPort);
                System.out.println("TCP disconnected (tunnel)");
            }
        }
    }

    /** Arch: UDP GUI ↔ TCP к локальному порту SSH (с автопереподключением). */
    private static void runUdpListenPersist(String udpBind, int udpPort, String tcpHost, int tcpBridge) throws IOException {
        System.out.printf("Lab5 bridge [udp-lsn]: UDP %s:%d <-> TCP %s:%d (auto-reconnect)%n", udpBind, udpPort, tcpHost, tcpBridge);
        AtomicReference<Socket> tcpRef = new AtomicReference<>();

        try (DatagramChannel udp = DatagramChannel.open()) {
            udp.bind(new InetSocketAddress(udpBind, udpPort));
            AtomicBoolean running = new AtomicBoolean(true);
            AtomicReference<InetSocketAddress> lastClient = new AtomicReference<>();
            Queue<byte[]> pendingToClient = new ConcurrentLinkedQueue<>();
            Object tcpWriteLock = new Object();

            Thread tcpReader = new Thread(() -> {
                while (running.get()) {
                    Socket tcp = null;
                    try {
                        tcp = ensureTcpConnected(tcpRef, tcpHost, tcpBridge);
                        try (InputStream in = tcp.getInputStream()) {
                            while (running.get() && tcp == tcpRef.get()) {
                                byte[] frame = StreamFramer.readFrame(in);
                                if (frame == null) {
                                    throw new IOException("TCP stream closed by peer");
                                }
                                forwardTcpFrameToClient(udp, lastClient, pendingToClient, frame);
                            }
                        }
                    } catch (IOException e) {
                        if (running.get()) {
                            System.err.println("TCP reader reconnect: " + e.getMessage());
                        }
                        closeCurrentTcp(tcpRef, tcp);
                        sleepQuietly(250);
                    }
                }
            }, "lab5-tcp-reader");
            tcpReader.setDaemon(true);
            tcpReader.start();

            byte[] buf = new byte[BUFFER_SIZE];
            while (running.get()) {
                ByteBuffer bb = ByteBuffer.wrap(buf);
                InetSocketAddress from = (InetSocketAddress) udp.receive(bb);
                int len = bb.position();
                if (len <= 0) {
                    continue;
                }
                lastClient.set(from);
                if (VERBOSE) {
                    System.out.printf("UDP->TCP %s bytes=%d%n", from, len);
                }
                flushPendingToClient(udp, from, pendingToClient);
                byte[] payload = Arrays.copyOfRange(buf, 0, len);
                int attempts = 0;
                while (attempts < 2 && running.get()) {
                    attempts++;
                    Socket tcp = ensureTcpConnected(tcpRef, tcpHost, tcpBridge);
                    try {
                        synchronized (tcpWriteLock) {
                            StreamFramer.writeFrame(tcp.getOutputStream(), payload);
                        }
                        break;
                    } catch (IOException writeError) {
                        if (attempts >= 2) {
                            throw writeError;
                        }
                        System.err.println("TCP write failed, reconnecting: " + writeError.getMessage());
                        closeCurrentTcp(tcpRef, tcp);
                        sleepQuietly(200);
                    }
                }
            }
        } finally {
            closeCurrentTcp(tcpRef, null);
        }
    }

    private static void relayTcpSession(Socket tcp, String udpHost, int udpPort) throws IOException {
        InetSocketAddress serverAddr = new InetSocketAddress(udpHost, udpPort);
        try (DatagramChannel udp = DatagramChannel.open()) {
            udp.configureBlocking(false);
            udp.bind(new InetSocketAddress(udpHost, 0));
            AtomicBoolean running = new AtomicBoolean(true);

            Thread toUdp = new Thread(() -> {
                try {
                    tcpToUdp(tcp, udp, serverAddr, running);
                } catch (IOException ignored) {
                    // closed
                } finally {
                    running.set(false);
                }
            }, "tcp->udp");
            Thread toTcp = new Thread(() -> {
                try {
                    udpToTcp(udp, tcp, running);
                } catch (IOException ignored) {
                    // closed
                } finally {
                    running.set(false);
                }
            }, "udp->tcp");
            toUdp.setDaemon(true);
            toTcp.setDaemon(true);
            toUdp.start();
            toTcp.start();
            toUdp.join();
            toTcp.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                tcp.close();
            } catch (IOException ignored) {
                // closed
            }
        }
    }

    private static void tcpToUdp(Socket tcp, DatagramChannel udp, InetSocketAddress serverAddr, AtomicBoolean running)
            throws IOException {
        try (InputStream in = tcp.getInputStream()) {
            while (running.get()) {
                byte[] frame = StreamFramer.readFrame(in);
                if (frame == null) {
                    break;
                }
                if (VERBOSE) {
                    System.out.printf("TCP->UDP frame bytes=%d%n", frame.length);
                }
                udp.send(ByteBuffer.wrap(frame), serverAddr);
            }
        }
    }

    private static void udpToTcp(DatagramChannel udp, Socket tcp, AtomicBoolean running) throws IOException {
        try (OutputStream out = tcp.getOutputStream()) {
            ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
            while (running.get()) {
                buf.clear();
                if (udp.receive(buf) == null) {
                    sleepQuietly(10);
                    continue;
                }
                int n = buf.position();
                if (n <= 0) {
                    continue;
                }
                if (VERBOSE) {
                    System.out.printf("UDP->TCP response bytes=%d%n", n);
                }
                StreamFramer.writeFrame(out, Arrays.copyOfRange(buf.array(), 0, n));
            }
        }
    }

    private static void forwardTcpFrameToClient(
            DatagramChannel udp,
            AtomicReference<InetSocketAddress> lastClient,
            Queue<byte[]> pendingToClient,
            byte[] frame
    ) throws IOException {
        InetSocketAddress dest = lastClient.get();
        if (dest != null) {
            if (VERBOSE) {
                System.out.printf("TCP->UDP to client %s bytes=%d%n", dest, frame.length);
            }
            udp.send(ByteBuffer.wrap(frame), dest);
            return;
        }
        pendingToClient.offer(frame);
        if (pendingToClient.size() > 32) {
            pendingToClient.poll();
            System.err.println("WARN: pending TCP->UDP queue overflow, dropping oldest frame");
        }
    }

    private static void flushPendingToClient(
            DatagramChannel udp,
            InetSocketAddress dest,
            Queue<byte[]> pendingToClient
    ) throws IOException {
        byte[] frame;
        while ((frame = pendingToClient.poll()) != null) {
            udp.send(ByteBuffer.wrap(frame), dest);
        }
    }

    private static Socket connectTcpWithRetry(String host, int port, int attempts, long pauseMs)
            throws IOException {
        IOException last = null;
        for (int i = 0; i < attempts; i++) {
            try {
                return new Socket(host, port);
            } catch (IOException e) {
                last = e;
                try {
                    Thread.sleep(pauseMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while connecting to " + host + ":" + port, ie);
                }
            }
        }
        throw last != null ? last : new IOException("Cannot connect to " + host + ":" + port);
    }

    private static Socket ensureTcpConnected(AtomicReference<Socket> tcpRef, String host, int port) throws IOException {
        Socket existing = tcpRef.get();
        if (isTcpAlive(existing)) {
            return existing;
        }
        synchronized (tcpRef) {
            Socket current = tcpRef.get();
            if (isTcpAlive(current)) {
                return current;
            }
            closeQuietly(current);
            Socket fresh = connectTcpWithRetry(host, port, 45, 1000);
            fresh.setTcpNoDelay(true);
            fresh.setKeepAlive(true);
            tcpRef.set(fresh);
            System.out.println("TCP connected to " + host + ":" + port);
            return fresh;
        }
    }

    private static boolean isTcpAlive(Socket socket) {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    private static void closeCurrentTcp(AtomicReference<Socket> tcpRef, Socket expected) {
        synchronized (tcpRef) {
            Socket current = tcpRef.get();
            if (expected != null && current != expected) {
                return;
            }
            tcpRef.set(null);
            closeQuietly(current);
        }
    }

    private static void closeQuietly(Socket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException ignored) {
            // ignored
        }
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

}

