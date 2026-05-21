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

/**
 * Точка входа серверного приложения.
 *
 * <p>Сервер принимает UDP-запросы, выполняет аутентификацию пользователя,
 * диспетчеризует команды и отправляет пакетизированные ответы клиенту.
 * Для обработки запросов используется многопоточная модель:
 * чтение (ForkJoinPool), обработка (Fixed thread pool), отправка (new Thread).</p>
 */
public class ServerMain {

    private static final int DEFAULT_PORT = 5555;
    private static final int BUFFER_SIZE = 64 * 1024;
    private static final AtomicBoolean running = new AtomicBoolean(false);

    private static long transactionCounter = 0;
    private static ForkJoinPool readPool;
    private static ExecutorService processPool;
    private static ExecutorService sendPool;
    private static CommandManager commandManager;
    private static CollectionManager collectionManager;
    private static StudyGroupRepository studyGroupRepository;
    private static UserRepository userRepository;
    private static volatile DatagramChannel serverChannel;
    private static volatile Selector serverSelector;

    /** JDBC Connection не потокобезопасен — все команды выполняются последовательно. */
    private static final Object JDBC_COMMAND_LOCK = new Object();

    /**
     * Запускает сервер и инициализирует инфраструктуру приложения.
     *
     * @param args аргументы запуска: {@code <db_login> [port]} — пароль к PostgreSQL в аргументах не передаётся
     *             (аутентификация к БД как на стенде Helios: без пароля в командной строке).
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            ServerLog.error("Usage: java -jar server.jar <db_login> [port]");
            return;
        }

        String dbLogin = args[0];
        int port = DEFAULT_PORT;

        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                ServerLog.error("Usage: java -jar server.jar <db_login> [port] — второй аргумент должен быть номером порта");
                return;
            }
        }

        try {
            DatabaseManager.init(dbLogin, "");
            ServerLog.info("Database connected");

            studyGroupRepository = new StudyGroupRepository();
            userRepository = new UserRepository();
            collectionManager = new CollectionManager(studyGroupRepository);

            commandManager = new CommandManager(collectionManager);
            registerCommands(commandManager);

            readPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
            processPool = Executors.newFixedThreadPool(8);
            sendPool = Executors.newFixedThreadPool(8);

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
                        readPool.execute(() -> {
                            try {
                                handleRequest();
                            } catch (IOException e) {
                                ServerLog.error("Read task failed: " + e.getMessage());
                            }
                        });
                    }
                }
            }

        } catch (Exception e) {
            ServerLog.error("Server error: " + e.getMessage(), e);
        } finally {
            shutdown();
        }
    }

    /**
     * Принимает один UDP-пакет и передает его на асинхронную обработку в пул чтения.
     *
     * @throws IOException при ошибках чтения из сокета
     */
    private static void handleRequest() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        SocketAddress clientAddress = serverChannel.receive(buffer);
        if (clientAddress == null) return;

        buffer.flip();
        final byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

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
                    startResponseSenderThread(clientAddress, transactionId, response);
                });
            }
        } catch (Exception e) {
            ServerLog.error("Error: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает подтверждение получения пакета от клиента.
     *
     * @param ack ACK-пакет
     */
    private static void handleAck(AckPacket ack) {
        int zeroBasedIndex = ack.getAcknowledgedIndex();
        int oneBasedIndex = zeroBasedIndex + 1;
        ResponseTransfer.onAck(ack.getTransactionId(), zeroBasedIndex);
        ServerLog.info(
            "ACK received for " + ack.getTransactionId()
                + " packet " + oneBasedIndex
                + " (index=" + zeroBasedIndex + ")"
        );
    }

    /**
     * Обрабатывает входящий транспортный пакет с командой.
     *
     * @param packet входящий пакет
     * @param clientAddress адрес клиента
     * @throws IOException при ошибках сетевого ввода/вывода
     */
    private static void handleDataPacket(Packet packet, SocketAddress clientAddress) throws IOException {
        if (packet.getPacketType() == PacketType.RESEND) {
            ServerLog.info("RESEND requested for " + packet.getTransactionId());
            return;
        }

        ServerLog.info("Request " + packet.getTransactionId() + " from " + clientAddress);

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
            startResponseSenderThread(clientAddress, transactionId, response);
        });
    }

    private static void startResponseSenderThread(
        SocketAddress clientAddress,
        String transactionId,
        CommandResponseDTO response
    ) {
        sendPool.submit(() -> {
            try {
                ResponseTransfer.send(serverChannel, clientAddress, transactionId, response);
            } catch (IOException e) {
                ServerLog.error("Error sending response " + transactionId + ": " + e.getMessage());
            }
        });
    }

    /**
     * Выполняет бизнес-обработку входящей команды.
     *
     * @param dto входная команда
     * @return результат выполнения команды
     */
    private static CommandResponseDTO processCommand(CommandDTO dto) {
        synchronized (JDBC_COMMAND_LOCK) {
            return processCommandLocked(dto);
        }
    }

    private static CommandResponseDTO processCommandLocked(CommandDTO dto) {
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
                var user = AuthManager.register(login, password);
                return new CommandResponseDTO(ResponseStatus.SUCCESS, "Регистрация выполнена успешно.", null, user.getId());
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
                    return new CommandResponseDTO(ResponseStatus.SUCCESS, "Вход выполнен успешно.", null, user.get().getId());
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
                return new CommandResponseDTO(ResponseStatus.SUCCESS, "Аутентификация успешна.", null, userOpt.get().getId());
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

    /**
     * Нормализует логин (trim + проверка на пустую строку).
     *
     * @param login исходное значение логина
     * @return нормализованный логин или {@code null}
     */
    private static String normalizeLogin(String login) {
        if (login == null) {
            return null;
        }
        String normalized = login.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Регистрирует серверные обработчики DTO-команд.
     *
     * @param m менеджер команд
     */
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
        m.register(ExecuteScriptCommandDTO.class, new ExecuteScriptServerCommand());
    }

    /**
     * Генерирует уникальный идентификатор транспортной транзакции.
     *
     * @return строковый transaction id
     */
    private static String generateTransactionId() {
        return "tx-" + System.currentTimeMillis() + "-" + (++transactionCounter);
    }

    /**
     * Десериализует массив байт в произвольный объект.
     *
     * @param data сериализованные данные
     * @return десериализованный объект
     * @throws Exception если десериализация завершилась ошибкой
     */
    private static Object deserializeAny(byte[] data) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return ois.readObject();
        }
    }

    /**
     * Десериализует массив байт в объект команды.
     *
     * @param data сериализованные данные команды
     * @return DTO команды
     * @throws Exception если десериализация завершилась ошибкой
     */
    private static CommandDTO deserializeCommand(byte[] data) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return (CommandDTO) ois.readObject();
        }
    }

    /**
     * Корректно завершает работу сервера и освобождает ресурсы.
     */
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

        if (sendPool != null) {
            sendPool.shutdown();
            try {
                if (!sendPool.awaitTermination(10, TimeUnit.SECONDS)) {
                    sendPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                sendPool.shutdownNow();
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
}