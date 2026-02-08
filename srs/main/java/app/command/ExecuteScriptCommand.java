package command;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class ExecuteScriptCommand implements Command {

    private final CommandManager commandManager;

    private static final Set<String> executingScripts = new HashSet<>();

    public ExecuteScriptCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

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

    @Override
    public String getDescription() {
        return "выполнить команды из файла";
    }
}
