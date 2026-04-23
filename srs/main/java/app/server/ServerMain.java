package server;

import auth.AuthManager;
import collection.CollectionManager;
import common.SerializationUtils;
import common.dto.*;
import database.DatabaseManager;
import database.StudyGroupRepository;
import database.UserRepository;
import server.commands.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ServerMain {

    private static final int DEFAULT_PORT = 5555;
    private static final int BUFFER_SIZE = 64 * 1024;
    private static final int CHUNK_SIZE = 8 * 1024;
    private static final int WINDOW_SIZE = 4;
    private static final int MAX_RETRIES = 3;
    private static final int ACK_TIMEOUT_MS = 1500;

private static final AtomicBoolean running = new AtomicBoolean(false);
    private static final ReentrantLock responseLock = new ReentrantLock();

    private static long transactionCounter = 0;
    private static ForkJoinPool readPool;
    private static ExecutorService processPool;
    private static CommandManager commandManager;
    private static CollectionManager collectionManager;
    private static StudyGroupRepository studyGroupRepository;
    private static UserRepository userRepository;
    private static volatile DatagramChannel serverChannel;
    private static volatile Selector serverSelector;

    public static void main(String[] args) {
        if (args.length < 1) {
            ServerLog.error("Usage: java -jar server.jar <db_login> [db_password] [port]");
            return;
        }

        String dbLogin = args[0];
        String dbPassword = "";
        int port = DEFAULT_PORT;

        if (args.length >= 2) {
            if (isNumeric(args[1])) {
                port = Integer.parseInt(args[1]);
            } else {
                dbPassword = args[1];
                if (args.length >= 3) {
                    port = Integer.parseInt(args[2]);
                }
            }
        }

        try {
            DatabaseManager.init(dbLogin, dbPassword);
            ServerLog.info("Database connected");

            studyGroupRepository = new StudyGroupRepository();
            userRepository = new UserRepository();
            collectionManager = new CollectionManager(studyGroupRepository);

            commandManager = new CommandManager(collectionManager);
            registerCommands(commandManager);

            readPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
            processPool = Executors.newFixedThreadPool(4);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                ServerLog.info("Shutdown hook triggered");
                running.set(false);
                shutdown();
            }));

            serverChannel = DatagramChannel.open();
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);

            serverSelector = Selector.open();
            serverChannel.register(serverSelector, SelectionKey.OP_READ);

            running.set(true);
            ServerLog.info("Server started on port " + port);

            while (running.get()) {
                int ready = serverSelector.select(250);
                if (!running.get()) break;

                if (ready == 0) continue;

                Iterator<SelectionKey> it = serverSelector.selectedKeys().iterator();
                while (it.hasNext() && running.get()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (key.isReadable() && key.channel() == serverChannel) {
                        handleRequest();
                    }
                }
            }

        } catch (Exception e) {
            ServerLog.error("Server error: " + e.getMessage(), e);
        } finally {
            shutdown();
        }
    }

    private static void handleRequest() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        SocketAddress clientAddress = serverChannel.receive(buffer);
        if (clientAddress == null) return;

        buffer.flip();
        final byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        readPool.submit(() -> {
            try {
                Object packet = deserializeAny(data);
                if (packet == null) return;

                if (packet instanceof AckPacket) {
                    handleAck((AckPacket) packet);
                    return;
                }

                if (packet instanceof Packet) {
                    handleDataPacket((Packet) packet, clientAddress);
                    return;
                }

                if (packet instanceof CommandDTO) {
                    CommandDTO command = (CommandDTO) packet;
                    processPool.submit(() -> {
                        CommandResponseDTO response = processCommand(command);
                        String transactionId = generateTransactionId();
                        new Thread(() -> {
                            try {
                                sendWithSlidingWindow(clientAddress, transactionId, response);
                            } catch (IOException e) {
                                ServerLog.error("Error sending response: " + e.getMessage());
                            }
                        }).start();
                    });
                }
            } catch (Exception e) {
                ServerLog.error("Error: " + e.getMessage());
            }
        });
    }

    private static void handleAck(AckPacket ack) {
        ServerLog.info("ACK received for " + ack.getTransactionId() + " packet " + ack.getAcknowledgedIndex());
    }

    private static void handleDataPacket(Packet packet, SocketAddress clientAddress) throws IOException {
        if (packet.getPacketType() == PacketType.RESEND) {
            ServerLog.info("RESEND requested for " + packet.getTransactionId());
            return;
        }

        CommandDTO command;
        try {
            command = deserializeCommand(packet.getData());
        } catch (Exception e) {
            ServerLog.error("Failed to deserialize command: " + e.getMessage());
            return;
        }
        processPool.submit(() -> {
            CommandResponseDTO response = processCommand(command);
            final String transactionId = packet.getTransactionId();
            new Thread(() -> {
                try {
                    sendWithSlidingWindow(clientAddress, transactionId, response);
                } catch (IOException e) {
                    ServerLog.error("Error sending response: " + e.getMessage());
                }
            }).start();
        });
    }

    private static CommandResponseDTO processCommand(CommandDTO dto) {
        if (dto instanceof RegisterCommandDTO) {
            RegisterCommandDTO r = (RegisterCommandDTO) dto;
            String login = normalizeLogin(r.getLogin());
            String password = r.getPassword();

            if (login == null) {
                return new CommandResponseDTO(ResponseStatus.ERROR, "Логин не может быть пустым.", null);
            }
            if (password == null || password.isEmpty()) {
                return new CommandResponseDTO(ResponseStatus.ERROR, "Пароль не может быть пустым.", null);
            }

            try {
                if (userRepository.findByLogin(login).isPresent()) {
                    return new CommandResponseDTO(ResponseStatus.ERROR, "Этот логин уже занят.", null);
                }
                AuthManager.register(login, password);
                return new CommandResponseDTO(ResponseStatus.SUCCESS, "Регистрация выполнена успешно.", null);
            } catch (Exception e) {
                return new CommandResponseDTO(ResponseStatus.ERROR, "Ошибка регистрации: " + e.getMessage(), null);
            }
        }

        if (dto instanceof LoginCommandDTO) {
            LoginCommandDTO l = (LoginCommandDTO) dto;
            String login = normalizeLogin(l.getLogin());
            String password = l.getPassword();
            if (login == null || password == null) {
                return new CommandResponseDTO(ResponseStatus.ERROR, "Укажите логин и пароль.", null);
            }
            try {
                var user = AuthManager.login(login, password);
                if (user.isPresent()) {
                    return new CommandResponseDTO(ResponseStatus.SUCCESS, "Вход выполнен успешно.", null);
                }
                return new CommandResponseDTO(ResponseStatus.ERROR, "Неверный логин или пароль.", null);
            } catch (Exception e) {
                return new CommandResponseDTO(ResponseStatus.ERROR, "Ошибка входа: " + e.getMessage(), null);
            }
        }

        if (dto instanceof AuthCommandDTO) {
            AuthCommandDTO a = (AuthCommandDTO) dto;
            String login = normalizeLogin(a.getLogin());
            String password = a.getPassword();
            if (login == null || password == null) {
                return new CommandResponseDTO(ResponseStatus.ERROR, "Укажите логин и пароль.", null);
            }
            try {
                var userOpt = AuthManager.login(login, password);
                if (userOpt.isEmpty()) {
                    return new CommandResponseDTO(ResponseStatus.ERROR, "Неверный логин или пароль.", null);
                }
                return new CommandResponseDTO(ResponseStatus.SUCCESS, "Аутентификация успешна.", null);
            } catch (Exception e) {
                return new CommandResponseDTO(ResponseStatus.ERROR, "Ошибка аутентификации: " + e.getMessage(), null);
            }
        }

        if (dto instanceof CommandWithUser) {
            CommandWithUser wrapped = (CommandWithUser) dto;
            String login = normalizeLogin(wrapped.getLogin());
            String password = wrapped.getPassword();
            if (login == null || password == null) {
                return new CommandResponseDTO(ResponseStatus.ERROR, "Требуется авторизация (логин и пароль).", null);
            }
            try {
                var userOpt = AuthManager.login(login, password);
                if (userOpt.isEmpty()) {
                    return new CommandResponseDTO(ResponseStatus.ERROR, "Неверный логин или пароль.", null);
                }
                return commandManager.handle(wrapped.getOriginalCommand(), userOpt.get().getId());
            } catch (Exception e) {
                return new CommandResponseDTO(ResponseStatus.ERROR, "Ошибка аутентификации: " + e.getMessage(), null);
            }
        }

        return new CommandResponseDTO(ResponseStatus.ERROR, "Требуется авторизация.", null);
    }

    private static String normalizeLogin(String login) {
        if (login == null) {
            return null;
        }
        String normalized = login.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static void registerCommands(CommandManager m) {
        m.register(InfoCommandDTO.class, new InfoServerCommand());
        m.register(ShowCommandDTO.class, new ShowServerCommand());
        m.register(AddCommandDTO.class, new AddServerCommand());
        m.register(RemoveByIdCommandDTO.class, new RemoveByIdServerCommand());
        m.register(RemoveFirstCommandDTO.class, new RemoveFirstServerCommand());
        m.register(ClearCommandDTO.class, new ClearServerCommand());
        m.register(UpdateCommandDTO.class, new UpdateServerCommand());
        m.register(AddIfMinCommandDTO.class, new AddIfMinServerCommand());
        m.register(RemoveLowerCommandDTO.class, new RemoveLowerServerCommand());
        m.register(FilterContainsNameCommandDTO.class, new FilterContainsNameServerCommand());
        m.register(FilterGreaterThanSemesterCommandDTO.class, new FilterGreaterThanSemesterServerCommand());
        m.register(PrintFieldDescendingGroupAdminCommandDTO.class, new PrintFieldDescendingGroupAdminServerCommand());
    }

    private static String generateTransactionId() {
        return "tx-" + System.currentTimeMillis() + "-" + (++transactionCounter);
    }

    private static void sendWithSlidingWindow(SocketAddress addr, String tid, CommandResponseDTO resp) throws IOException {
        byte[] responseData = SerializationUtils.serialize(resp);
        List<byte[]> chunks = chunkData(responseData, CHUNK_SIZE);
        int total = chunks.size();

        if (total == 1) {
            Packet p = new Packet(tid, 0, total, PacketType.DATA, chunks.get(0));
            serverChannel.send(ByteBuffer.wrap(SerializationUtils.serialize(p)), addr);
            ServerLog.info("Sent packet 1/1");
        } else {
            for (int i = 0; i < total; i++) {
                Packet p = new Packet(tid, i, total, PacketType.DATA, chunks.get(i));
                serverChannel.send(ByteBuffer.wrap(SerializationUtils.serialize(p)), addr);
                ServerLog.info("Sent packet " + (i + 1) + "/" + total);
            }
        }

        ServerLog.info("All packets sent for " + tid);
    }

    private static int waitForAck(SocketAddress addr, String transactionId, int fromIndex, int toIndex) throws IOException {
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_READ);

        long startTime = System.currentTimeMillis();
        int remaining = ACK_TIMEOUT_MS;

        while (System.currentTimeMillis() - startTime < ACK_TIMEOUT_MS) {
            long elapsed = System.currentTimeMillis() - startTime;
            remaining = (int) (ACK_TIMEOUT_MS - elapsed);
            if (remaining <= 0) break;

            int ready = selector.select(Math.max(1, remaining));
            if (ready == 0) continue;

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();

                if (key.isReadable()) {
                    ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
                    SocketAddress from = serverChannel.receive(buf);
                    if (from == null) continue;

                    buf.flip();
                    byte[] data = new byte[buf.remaining()];
                    buf.get(data);

                    try {
                        Object obj = deserializeAny(data);
                        if (obj instanceof AckPacket) {
                            AckPacket ack = (AckPacket) obj;
                            if (ack.getTransactionId().equals(transactionId)) {
                                int ackIdx = ack.getAcknowledgedIndex();
                                if (ackIdx >= fromIndex && ackIdx <= toIndex) {
                                    selector.close();
                                    return ackIdx;
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        selector.close();
        return -1;
    }

    private static List<byte[]> chunkData(byte[] data, int size) {
        List<byte[]> list = new ArrayList<>();
        for (int i = 0; i < data.length; i += size) {
            int end = Math.min(i + size, data.length);
            byte[] chunk = new byte[end - i];
            System.arraycopy(data, i, chunk, 0, chunk.length);
            list.add(chunk);
        }
        return list;
    }

    private static Object deserializeAny(byte[] data) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return ois.readObject();
        }
    }

    private static CommandDTO deserializeCommand(byte[] data) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return (CommandDTO) ois.readObject();
        }
    }

    private static void shutdown() {
        ServerLog.info("Shutting down server...");

        if (readPool != null) {
            readPool.shutdown();
            try {
                if (!readPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    readPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                readPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (processPool != null) {
            processPool.shutdown();
            try {
                if (!processPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    processPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                processPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (serverSelector != null && serverSelector.isOpen()) {
            try {
                serverSelector.close();
            } catch (IOException e) {
                ServerLog.error("Error closing selector: " + e.getMessage());
            }
        }

        if (serverChannel != null && serverChannel.isOpen()) {
            try {
                serverChannel.close();
            } catch (IOException e) {
                ServerLog.error("Error closing channel: " + e.getMessage());
            }
        }

        DatabaseManager.closeInstance();

        ServerLog.info("Server shutdown complete");
    }

    private static boolean isNumeric(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}