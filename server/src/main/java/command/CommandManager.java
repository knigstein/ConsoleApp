package command;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandManager {

    private final Map<String, Command> commands = new HashMap<>();

    public void register(String name, Command command) {
        commands.put(name, command);
    }

    public boolean execute(String input, Scanner scanner) {

        if (input == null || input.trim().isEmpty()) {
            return true;
        }

        String[] parts = input.trim().split("\\s+");
        String commandName = parts[0];

        Command command = commands.get(commandName);

        if (command == null) {
            System.out.println("Неизвестная команда.");
            return true;
        }

        if (command instanceof ScriptAware) {
            ((ScriptAware) command).setScanner(scanner);
        }

        command.execute(parts);
        return true;
    }

    public void printHelp() {
        commands.forEach((name, cmd) ->
                System.out.println(name + " : " + cmd.getDescription()));
    }
}