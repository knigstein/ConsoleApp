package command;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Команда {@code execute_script}.
 * Последовательно выполняет команды, считанные из указанного файла-скрипта.
 * Поддерживает защиту от рекурсивного вызова одного и того же скрипта.
 *
 * Реализует интерфейс {@link Command}.
 */
public class ExecuteScriptCommand implements Command {

    private final CommandManager commandManager;

    private static final Set<String> executingScripts = new HashSet<>();

    /**
     * Создаёт команду выполнения скрипта.
     *
     * @param commandManager менеджер команд, через который будут выполняться строки скрипта
     */
    public ExecuteScriptCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * Выполняет команды из указанного файла-скрипта.
     * Ожидает, что во втором аргументе команды передано имя файла.
     * При обнаружении рекурсии (повторного запуска того же файла) выполнение прерывается.
     *
     * @param args аргументы команды, где {@code args[1]} — имя файла со скриптом
     */
    @Override
    public void execute(String[] args) {

        if (args.length < 2) {
            System.out.println("Не указано имя файла.");
            return;
        }

        String fileName = args[1];

        if (executingScripts.contains(fileName)) {
            System.out.println("Обнаружена рекурсия! Скрипт уже выполняется.");
            return;
        }

        try (Scanner fileScanner = new Scanner(new File(fileName))) {

            executingScripts.add(fileName);

            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                System.out.println(">> " + line);
                commandManager.execute(line, fileScanner);
            }

            executingScripts.remove(fileName);

        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден.");
        }
    }

    /**
     * Возвращает краткое описание команды.
     *
     * @return строка с описанием назначения команды {@code execute_script}
     */
    @Override
    public String getDescription() {
        return "выполнить команды из файла";
    }
}
