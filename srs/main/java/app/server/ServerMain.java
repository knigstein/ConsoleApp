package server;

import collection.CollectionManager;
import common.SerializationUtils;
import common.dto.*;
import io.FileManager;
import server.commands.*;

import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * Точка входа серверного приложения.
 * Работает в однопоточном режиме: принимает сериализованные объекты-команды по UDP
 * и отправляет обратно результаты их выполнения.
 */
public class ServerMain {

    private static final int DEFAULT_PORT = 5555;
    private static final int BUFFER_SIZE = 64 * 1024;

    public static void main(String[] args) {
        if (args.length == 0) {
            ServerLog.error("Не указан файл коллекции.");
            return;
        }

        String fileName = args[0];
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        CollectionManager collectionManager = new CollectionManager();
        FileManager fileManager = new FileManager(fileName);

        try {
            collectionManager.getCollection().addAll(fileManager.load());
        } catch (Exception e) {
            ServerLog.error("Ошибка загрузки файла: {}", e.getMessage());
        }

        ServerCommandManager commandManager = new ServerCommandManager(collectionManager, fileManager);
        registerCommands(commandManager, fileManager);

        try (DatagramChannel channel = DatagramChannel.open();
             Selector selector = Selector.open()) {

            channel.bind(new InetSocketAddress(port));
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);

            ServerLog.info("Сервер запущен на порту {}", port);
            ServerLog.info("Локальные команды сервера: save, exit");

            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                // 1) Обрабатываем локальные команды сервера (не блокируя).
                try {
                    if (console.ready()) {
                        String line = console.readLine();
                        if (line != null) {
                            String cmd = line.trim();
                            if ("save".equalsIgnoreCase(cmd)) {
                                collectionManager.save(fileManager);
                                ServerLog.info("Коллекция сохранена (server-only команда).");
                            } else if ("exit".equalsIgnoreCase(cmd)) {
                                ServerLog.info("Завершение сервера по команде exit.");
                                ServerLog.info("Сохраняем коллекцию перед завершением...");
                                collectionManager.save(fileManager);
                                return;
                            }
                        }
                    }
                } catch (IOException e) {
                    ServerLog.warn("Ошибка чтения команды с консоли сервера: {}", e.getMessage());
                }

                // 2) Ждём сетевые события (не блокируясь надолго).
                selector.select(250);

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (key.isReadable()) {
                        handleRequest(channel, key, commandManager, collectionManager);
                    }
                }
            }

        } catch (IOException e) {
            ServerLog.error("Ошибка сервера: " + e.getMessage(), e);
        } finally {
            // Однопоточное завершение: при нормальном выходе (через return из main)
            // сохранение уже выполнено по команде exit. Здесь — страховка для исключений.
            try {
                collectionManager.save(fileManager);
            } catch (Exception ignored) {
            }
        }
    }

    private static void registerCommands(ServerCommandManager manager, FileManager fileManager) {
        manager.register(InfoCommandDTO.class, new InfoServerCommand());
        manager.register(ShowCommandDTO.class, new ShowServerCommand());
        manager.register(AddCommandDTO.class, new AddServerCommand());
        manager.register(RemoveByIdCommandDTO.class, new RemoveByIdServerCommand());
        manager.register(RemoveFirstCommandDTO.class, new RemoveFirstServerCommand());
        manager.register(ClearCommandDTO.class, new ClearServerCommand());
        manager.register(UpdateCommandDTO.class, new UpdateServerCommand());
        manager.register(AddIfMinCommandDTO.class, new AddIfMinServerCommand());
        manager.register(RemoveLowerCommandDTO.class, new RemoveLowerServerCommand());
        manager.register(FilterContainsNameCommandDTO.class, new FilterContainsNameServerCommand());
        manager.register(FilterGreaterThanSemesterCommandDTO.class, new FilterGreaterThanSemesterServerCommand());
        manager.register(PrintFieldDescendingGroupAdminCommandDTO.class, new PrintFieldDescendingGroupAdminServerCommand());
        manager.register(ExecuteScriptCommandDTO.class, new ExecuteScriptServerCommand());
        manager.register(SaveCommandDTO.class, new SaveServerCommand(fileManager));
    }

    private static void handleRequest(DatagramChannel channel,
                                      SelectionKey key,
                                      ServerCommandManager commandManager,
                                      CollectionManager collectionManager) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            SocketAddress clientAddress = channel.receive(buffer);
            if (clientAddress == null) {
                return;
            }
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            ServerLog.info("Получен запрос от {}", clientAddress);
            CommandDTO command = deserializeCommand(data);
            CommandResponseDTO response;
            try {
                response = commandManager.handle(command);
            } catch (Exception e) {
                response = new CommandResponseDTO(
                        ResponseStatus.ERROR,
                        "Ошибка обработки команды: " + e.getMessage(),
                        null
                );
            }

            byte[] respBytes = SerializationUtils.serialize(response);
            ByteBuffer out = ByteBuffer.wrap(respBytes);
            channel.send(out, clientAddress);
            ServerLog.info("Ответ отправлен {}", clientAddress);

        } catch (Exception e) {
            ServerLog.error("Ошибка при обработке запроса: " + e.getMessage(), e);
        }
    }

    private static CommandDTO deserializeCommand(byte[] data) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            Object obj = ois.readObject();
            return (CommandDTO) obj;
        }
    }
}

