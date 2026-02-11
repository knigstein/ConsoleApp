package command;

import collection.CollectionManager;

/**
 * Команда {@code filter_contains_name}.
 * Выводит элементы коллекции, имя которых содержит указанную подстроку.
 *
 * Реализует интерфейс {@link Command}.
 */
public class FilterContainsNameCommand implements Command {

    private final CollectionManager collectionManager;

    /**
     * Создаёт команду фильтрации по подстроке имени.
     *
     * @param collectionManager менеджер коллекции, над которой выполняется фильтрация
     */
    public FilterContainsNameCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Выполняет команду фильтрации.
     * Ожидает, что во втором аргументе команды передана подстрока для поиска.
     *
     * @param args аргументы команды, где {@code args[1]} — подстрока для поиска в имени
     */
    @Override
    public void execute(String[] args) {

        if (args.length < 2) {
            System.out.println("Не указана подстрока.");
            return;
        }

        collectionManager.filterContainsName(args[1]);
    }

    /**
     * Возвращает краткое описание команды.
     *
     * @return строка с описанием назначения команды {@code filter_contains_name}
     */
    @Override
    public String getDescription() {
        return "вывести элементы, имя которых содержит подстроку";
    }
}
