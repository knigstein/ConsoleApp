package command;

import java.io.File;
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
            System.out.println("Рекурсия запрещена.");
            return;
        }

        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println("Файл не найден.");
            return;
        }

        executingScripts.add(fileName);

        try (Scanner fileScanner = new Scanner(file)) {

            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                commandManager.execute(line, fileScanner);
            }

        } catch (Exception e) {
            System.out.println("Ошибка выполнения скрипта.");
        } finally {
            executingScripts.remove(fileName);
        }
    }

    @Override
    public String getDescription() {
        return "выполнить скрипт";
    }
}