package client;

import common.dto.*;
import model.*;
import util.StudyGroupBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Клиентское приложение.
 * Читает команды из консоли, строит объекты-команды (DTO),
 * отправляет их на сервер по UDP и выводит результаты.
 */
public class ClientMain {

    private static final int DEFAULT_PORT = 5555;
    private static final String DEFAULT_HOST = "localhost";

    private static String currentLogin = null;
    private static String currentPassword = null;

    /**
     * Набор имён файлов скриптов, которые в данный момент находятся в процессе выполнения.
     * Используется для предотвращения прямой и косвенной рекурсии при вызове {@code execute_script}.
     */
    private static final Set<String> executingScripts = new HashSet<>();

    public static void main(String[] args) {

        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        System.out.println("Клиент подключается к " + host + ":" + port);
        System.out.println("===========================================");
        System.out.println("ВНИМАНИЕ: Для выполнения серверных команд требуется авторизация.");
        System.out.println("Сначала выполните: register <логин> <пароль>");
        System.out.println("Или: login <логин> <пароль>");
        System.out.println("===========================================");

        try (ClientNetwork network = new ClientNetwork(host, port);
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                String cmd = parts[0];

                if ("exit".equals(cmd)) {
                    System.out.println("Завершение клиентского приложения.");
                    break;
                }

                if ("help".equals(cmd)) {
                    printHelp();
                    continue;
                }

                if ("save".equals(cmd)) {
                    System.out.println("Команда save недоступна на клиенте. Сохранение выполняет только сервер.");
                    continue;
                }

                if ("logout".equals(cmd)) {
                    if (currentLogin == null) {
                        System.out.println("Вы не авторизованы.");
                    } else {
                        System.out.println("Вы вышли из аккаунта: " + currentLogin);
                        currentLogin = null;
                        currentPassword = null;
                    }
                    continue;
                }

                if ("execute_script".equals(cmd)) {
                    executeScript(parts, network, scanner);
                    continue;
                }

                try {
                    CommandDTO dto = buildCommandDTO(cmd, parts, scanner);
                    if (dto == null) {
                        System.out.println("Неизвестная команда. Введите help для списка команд.");
                        continue;
                    }

                    CommandDTO toSend = dto;
                    if (shouldAttachCredentials(dto)) {
                        if (currentLogin == null || currentPassword == null) {
                            System.out.println("Для этой команды нужна авторизация. Сначала выполните login или register.");
                            continue;
                        }
                        toSend = CommandWithUser.wrap(dto, currentLogin, currentPassword);
                    }

                    CommandResponseDTO response = network.sendAndReceive(toSend);
                    if (response.getStatus() == ResponseStatus.SUCCESS) {
                        if (dto instanceof RegisterCommandDTO reg) {
                            currentLogin = normalizeLogin(reg.getLogin());
                            currentPassword = reg.getPassword();
                        } else if (dto instanceof LoginCommandDTO log) {
                            currentLogin = normalizeLogin(log.getLogin());
                            currentPassword = log.getPassword();
                        }
                    }

                    handleResponse(response);

                } catch (IOException e) {
                    System.out.println("Ошибка сети: " + e.getMessage());
                } catch (ClassNotFoundException e) {
                    System.out.println("Ошибка десериализации ответа от сервера.");
                } catch (Exception e) {
                    System.out.println("Ошибка: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("Не удалось инициализировать сетевое подключение: " + e.getMessage());
        }
    }

    private static CommandDTO buildCommandDTO(String cmd, String[] parts, Scanner scanner) {

        switch (cmd) {
            case "register": {
                if (parts.length < 3) {
                    System.out.println("Использование: register <логин> <пароль>");
                    return null;
                }
                String regLogin = normalizeLogin(parts[1]);
                if (regLogin == null) {
                    System.out.println("Логин не может быть пустым.");
                    return null;
                }
                if (parts[2].isEmpty()) {
                    System.out.println("Пароль не может быть пустым.");
                    return null;
                }
                return new RegisterCommandDTO(regLogin, parts[2]);
            }
            case "login": {
                if (parts.length < 3) {
                    System.out.println("Использование: login <логин> <пароль>");
                    return null;
                }
                String logLogin = normalizeLogin(parts[1]);
                if (logLogin == null) {
                    System.out.println("Логин не может быть пустым.");
                    return null;
                }
                if (parts[2].isEmpty()) {
                    System.out.println("Пароль не может быть пустым.");
                    return null;
                }
                return new LoginCommandDTO(logLogin, parts[2]);
            }
            case "info":
                return new InfoCommandDTO();
            case "show":
                return new ShowCommandDTO();
            case "clear":
                return new ClearCommandDTO();
            case "remove_first":
                return new RemoveFirstCommandDTO();
            case "remove_by_id": {
                if (parts.length < 2) {
                    System.out.println("Не указан id.");
                    return null;
                }
                try {
                    Integer id = Integer.parseInt(parts[1]);
                    return new RemoveByIdCommandDTO(id);
                } catch (NumberFormatException e) {
                    System.out.println("id должен быть числом.");
                    return null;
                }
            }
            case "filter_contains_name": {
                if (parts.length < 2) {
                    System.out.println("Не указана подстрока.");
                    return null;
                }
                return new FilterContainsNameCommandDTO(parts[1]);
            }
            case "filter_greater_than_semester_enum": {
                if (parts.length < 2) {
                    System.out.println("Не указан семестр.");
                    return null;
                }
                try {
                    Semester sem = Semester.valueOf(parts[1]);
                    return new FilterGreaterThanSemesterCommandDTO(sem);
                } catch (Exception e) {
                    System.out.println("Некорректный семестр.");
                    return null;
                }
            }
            case "print_field_descending_group_admin":
                return new PrintFieldDescendingGroupAdminCommandDTO();
            case "add": {
                StudyGroup group = buildStudyGroupInteractive(scanner);
                return new AddCommandDTO(group);
            }
            case "add_if_min": {
                StudyGroup group = buildStudyGroupInteractive(scanner);
                return new AddIfMinCommandDTO(group);
            }
            case "remove_lower": {
                StudyGroup group = buildStudyGroupInteractive(scanner);
                return new RemoveLowerCommandDTO(group);
            }
            case "update": {
                if (parts.length < 2) {
                    System.out.println("Не указан id.");
                    return null;
                }
                try {
                    Integer id = Integer.parseInt(parts[1]);
                    System.out.println("Ввод данных для новой версии элемента с id=" + id);
                    StudyGroup updated = buildStudyGroupInteractive(scanner);
                    return new UpdateCommandDTO(id, updated);
                } catch (NumberFormatException e) {
                    System.out.println("id должен быть числом.");
                    return null;
                }
            }
            default:
                return null;
        }
    }

    private static StudyGroup buildStudyGroupInteractive(Scanner scanner) {
        // Для клиента используем тот же билдер, но id и дату сервер всё равно переопределит.
        StudyGroupBuilder builder = new StudyGroupBuilder(scanner);
        return builder.build();
    }

    private static void handleResponse(CommandResponseDTO response) {
        if (response == null) {
            System.out.println("Пустой ответ от сервера.");
            return;
        }

        String statusLabel = response.getStatus() == ResponseStatus.SUCCESS ? "Успех" : "Ошибка";
        System.out.println(statusLabel + ": " + response.getMessage());

        List<StudyGroup> collection = response.getCollection();
        if (collection != null && !collection.isEmpty()) {
            for (StudyGroup group : collection) {
                System.out.println(group);
            }
        }
    }

    /**
     * Выполняет команды из файла-скрипта.
     * Каждая строка файла интерпретируется как пользовательский ввод.
     * 
     * @param parts аргументы команды (parts[1] - имя файла)
     * @param network сетевой клиент для отправки команд на сервер
     * @param scanner основной сканер консоли
     */
    private static void executeScript(String[] parts, ClientNetwork network, Scanner scanner) {
        if (parts.length < 2) {
            System.out.println("Не указано имя файла.");
            return;
        }

        String fileName = parts[1];
        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println("Файл не найден.");
            return;
        }

        if (executingScripts.contains(fileName)) {
            System.out.println("Рекурсия запрещена.");
            return;
        }

        executingScripts.add(fileName);

        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] cmdParts = line.split("\\s+");
                String cmd = cmdParts[0];

                if ("execute_script".equals(cmd)) {
                    System.out.println("Вложенный вызов execute_script из скрипта: " + line);
                    String nestedFileName = cmdParts.length > 1 ? cmdParts[1] : "";
                    if (executingScripts.contains(nestedFileName)) {
                        System.out.println("Рекурсия запрещена.");
                        continue;
                    }
                    executeScript(new String[]{"execute_script", nestedFileName}, network, scanner);
                    continue;
                }

                if ("exit".equals(cmd)) {
                    System.out.println("Команда exit в скрипте игнорируется.");
                    continue;
                }

                if ("save".equals(cmd)) {
                    System.out.println("Команда save недоступна на клиенте.");
                    continue;
                }

                if ("logout".equals(cmd)) {
                    if (currentLogin == null) {
                        System.out.println("Вы не авторизованы.");
                    } else {
                        System.out.println("Вы вышли из аккаунта: " + currentLogin);
                        currentLogin = null;
                        currentPassword = null;
                    }
                    continue;
                }

                if ("help".equals(cmd)) {
                    printHelp();
                    continue;
                }

                try {
                    CommandDTO dto = buildCommandDTO(cmd, cmdParts, fileScanner);
                    if (dto == null) {
                        System.out.println("Неизвестная команда в скрипте: " + cmd);
                        continue;
                    }

                    CommandDTO toSend = dto;
                    if (shouldAttachCredentials(dto)) {
                        if (currentLogin == null || currentPassword == null) {
                            System.out.println("Для этой команды нужна авторизация. Сначала выполните login или register.");
                            continue;
                        }
                        toSend = CommandWithUser.wrap(dto, currentLogin, currentPassword);
                    }

                    CommandResponseDTO response = network.sendAndReceive(toSend);
                    if (response.getStatus() == ResponseStatus.SUCCESS) {
                        if (dto instanceof RegisterCommandDTO reg) {
                            currentLogin = normalizeLogin(reg.getLogin());
                            currentPassword = reg.getPassword();
                        } else if (dto instanceof LoginCommandDTO log) {
                            currentLogin = normalizeLogin(log.getLogin());
                            currentPassword = log.getPassword();
                        }
                    }

                    handleResponse(response);

                } catch (IOException e) {
                    System.out.println("Ошибка сети при выполнении скрипта: " + e.getMessage());
                    break;
                } catch (ClassNotFoundException e) {
                    System.out.println("Ошибка десериализации ответа от сервера.");
                    break;
                } catch (Exception e) {
                    System.out.println("Ошибка при выполнении команды в скрипте: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка чтения файла скрипта.");
        } finally {
            executingScripts.remove(fileName);
        }
    }

    private static void printHelp() {
        System.out.println("Доступные команды клиента:");
        System.out.println("  register <логин> <пароль>   - Регистрация нового пользователя");
        System.out.println("  login <логин> <пароль>      - Авторизация пользователя");
        System.out.println("  logout                       - Выйти из текущего аккаунта");
        System.out.println("  info");
        System.out.println("  show");
        System.out.println("  add");
        System.out.println("  update <id>");
        System.out.println("  remove_by_id <id>");
        System.out.println("  remove_first");
        System.out.println("  clear");
        System.out.println("  add_if_min");
        System.out.println("  remove_lower");
        System.out.println("  filter_contains_name <substring>");
        System.out.println("  filter_greater_than_semester_enum <SEMESTER>");
        System.out.println("  print_field_descending_group_admin");
        System.out.println("  execute_script <filename>");
        System.out.println("  exit");
        System.out.println("Команда save доступна только на сервере и из клиента не отправляется.");
    }

    private static boolean shouldAttachCredentials(CommandDTO dto) {
        return !(dto instanceof RegisterCommandDTO) &&
               !(dto instanceof LoginCommandDTO) &&
               !(dto instanceof common.dto.AuthCommandDTO);
    }

    private static String normalizeLogin(String login) {
        if (login == null) {
            return null;
        }
        String trimmed = login.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

