import collection.CollectionManager;
import command.*;
import io.ConsoleManager;
import io.FileManager;

import java.util.Scanner;

/**
 * Точка входа в консольное приложение для управления коллекцией учебных групп.
 * Инициализирует менеджер коллекции, загружает данные из файла, регистрирует команды
 * и запускает интерактивный цикл обработки пользовательского ввода.
 *
 * Класс использует инфраструктуру команд из пакета {@code command} и классы
 * {@link collection.CollectionManager}, {@link FileManager} и {@link ConsoleManager}.
 */
public class Main {

    /**
     * Точка входа в программу.
     * Ожидает, что в аргументах командной строки будет передано имя файла для загрузки и сохранения коллекции.
     *
     * @param args аргументы командной строки, где {@code args[0]} — имя XML-файла с данными
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Не указано имя файла.");
            return;
        }

        String fileName = args[0];

        CollectionManager collectionManager = new CollectionManager();
        FileManager fileManager = new FileManager(fileName);

        try {
            collectionManager.getCollection().addAll(fileManager.load());
        } catch (Exception e) {
            System.out.println("Ошибка загрузки файла.");
        }

        CommandManager commandManager = new CommandManager();
        Scanner scanner = new Scanner(System.in);

        // Регистрация команд
        commandManager.register("help", new HelpCommand(commandManager));
        commandManager.register("info", new InfoCommand(collectionManager));
        commandManager.register("show", new ShowCommand(collectionManager));
        commandManager.register("add", new AddCommand(collectionManager, scanner));
        commandManager.register("remove_by_id", new RemoveByIdCommand(collectionManager));
        commandManager.register("remove_first", new RemoveFirstCommand(collectionManager));
        commandManager.register("clear", new ClearCommand(collectionManager));
        commandManager.register("save", new SaveCommand(collectionManager, fileManager));
        commandManager.register("exit", new ExitCommand());
        commandManager.register("update", new UpdateCommand(collectionManager, scanner));
        commandManager.register("add_if_min", new AddIfMinCommand(collectionManager, scanner));
        commandManager.register("remove_lower", new RemoveLowerCommand(collectionManager, scanner));
        commandManager.register("filter_contains_name", new FilterContainsNameCommand(collectionManager));
        commandManager.register("filter_greater_than_semester_enum", new FilterGreaterThanSemesterCommand(collectionManager));
        commandManager.register("print_field_descending_group_admin", new PrintFieldDescendingGroupAdminCommand(collectionManager));
        commandManager.register("execute_script", new ExecuteScriptCommand(commandManager));

        ConsoleManager console = new ConsoleManager();

        // Интерактивный режим
        while (true) {
            System.out.print("> ");
            String input = console.readLine();
            commandManager.execute(input);
        }
    }
}
