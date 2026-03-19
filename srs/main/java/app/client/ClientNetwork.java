package client;

import common.SerializationUtils;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * Сетевой слой клиента.
 * Отправляет сериализованные команды по UDP и ждёт ответов в неблокирующем режиме.
 */
public class ClientNetwork implements AutoCloseable {

    private static final int BUFFER_SIZE = 64 * 1024;
    private static final int TIMEOUT_MS = 2000;
    private static final int MAX_RETRIES = 3;

    private final DatagramChannel channel;
    private final SocketAddress serverAddress;

    public ClientNetwork(String host, int port) throws IOException {
        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
        this.serverAddress = new InetSocketAddress(host, port);
    }

    public CommandResponseDTO sendAndReceive(CommandDTO dto) throws IOException, ClassNotFoundException {
        byte[] data = SerializationUtils.serialize(dto);

        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            attempt++;

            ByteBuffer out = ByteBuffer.wrap(data);
            channel.send(out, serverAddress);

            try (Selector selector = Selector.open()) {
                channel.register(selector, SelectionKey.OP_READ);

                int ready = selector.select(TIMEOUT_MS);
                if (ready == 0) {
                    System.out.println("Сервер не отвечает, попытка " + attempt + " из " + MAX_RETRIES);
                    continue;
                }

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (key.isReadable()) {
                        ByteBuffer in = ByteBuffer.allocate(BUFFER_SIZE);
                        SocketAddress from = channel.receive(in);
                        if (from == null) {
                            continue;
                        }
                        in.flip();
                        byte[] respBytes = new byte[in.remaining()];
                        in.get(respBytes);
                        return deserializeResponse(respBytes);
                    }
                }
            }
        }

        throw new IOException("Сервер временно недоступен после " + MAX_RETRIES + " попыток.");
    }

    private CommandResponseDTO deserializeResponse(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            Object obj = ois.readObject();
            return (CommandResponseDTO) obj;
        }
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}

