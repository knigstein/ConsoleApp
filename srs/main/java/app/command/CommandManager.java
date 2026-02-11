package command;

import java.util.HashMap;
import java.util.Map;

import java.util.Scanner;

/**
 * Класс, управляющий доступными командами приложения.
 * Хранит отображение имени команды в соответствующий объект {@link Command},
 * предоставляет методы для регистрации команд, их выполнения и вывода справки.
 *
 * Используется как центральная точка маршрутизации пользовательских запросов
 * к конкретным реализациям команд.
 */
public class CommandManager {

    private final Map<String, Command> commands = new HashMap<>();

    /**
     * Регистрирует новую команду в менеджере.
     *
     * @param name имя команды, по которому она будет вызываться из консоли
     * @param command объект, реализующий интерфейс {@link Command}
     */
    public void register(String name, Command command) {
        commands.put(name, command);
    }

    /**
     * Выполняет команду на основе введённой пользователем строки.
     * Используется в интерактивном режиме работы приложения.
     *
     * @param input полная строка пользовательского ввода
     * @return {@code true}, если обработка команды завершена (даже при ошибке),
     *         что позволяет продолжать основной цикл работы программы
     */
    public boolean execute(String input) {

        if (input == null || input.trim().isEmpty()) {
            return true;
        }

        String[] parts = input.trim().split("\\s+");
        String commandName = parts[0];

        Command command = commands.get(commandName);

        if (command == null) {
            System.out.println("Неизвестная команда. Введите help.");
            return true;
        }

        command.execute(parts);

        return true;
    }

    /**
     * Выводит в стандартный поток вывода список всех зарегистрированных команд
     * с их кратким описанием.
     */
    public void printHelp() {
        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue().getDescription());
        }
    }

    /**
     * Выполняет команду в режиме чтения из скрипта.
     * В этом режиме, если команда реализует интерфейс {@link ScriptAware},
     * ей передаётся внешний {@link Scanner} для продолжения чтения из файла.
     *
     * @param input полная строка команды, считанная из скрипта
     * @param scanner сканер, используемый для продолжения чтения строк скрипта
     * @return {@code true}, если команда была обработана
     */
    public boolean execute(String input, Scanner scanner) {

        if (input == null || input.trim().isEmpty()) {
            return true;
        }

        String[] parts = input.trim().split("\\s+");
        String commandName = parts[0];

        Command command = commands.get(commandName);

        if (command == null) {
            System.out.println("Неизвестная команда: " + commandName);
            return true;
        }

        if (command instanceof ScriptAware) {
            ((ScriptAware) command).setScanner(scanner);
        }

        command.execute(parts);

        return true;
    }

}
