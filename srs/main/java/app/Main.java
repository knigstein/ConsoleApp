import collection.CollectionManager;
import command.*;
import io.ConsoleManager;
import io.FileManager;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Не указано имя файла.");
            return;
        }

        String fileName = args[0];

        CollectionManager collectionManager = new CollectionManager();
        FileManager fileManager = new FileManager(fileName);

        // Загрузка данных
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
