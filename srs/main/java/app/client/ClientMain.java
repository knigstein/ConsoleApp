package client;

import common.dto.*;
import input.InputHandler;
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

    /**
     * Набор имён файлов скриптов, которые в данный момент находятся в процессе выполнения.
     * Используется для предотвращения прямой и косвенной рекурсии при вызове {@code execute_script}.
     */
    private static final Set<String> executingScripts = new HashSet<>();

    public static void main(String[] args) {

        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        System.out.println("Клиент подключается к " + host + ":" + port);

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

                    CommandResponseDTO response = network.sendAndReceive(dto);
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

        System.out.println(response.getStatus() + ": " + response.getMessage());

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

                    CommandResponseDTO response = network.sendAndReceive(dto);
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
}

