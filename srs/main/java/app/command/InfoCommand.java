package command;

import collection.CollectionManager;

/**
 * Команда {@code info}.
 * Выводит информацию о коллекции: тип, дату инициализации и количество элементов.
 *
 * Реализует интерфейс {@link Command}.
 */
public class InfoCommand implements Command {

    private final CollectionManager collectionManager;

    /**
     * Создаёт команду вывода информации о коллекции.
     *
     * @param collectionManager менеджер коллекции, из которого берутся сведения
     */
    public InfoCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Выполняет команду, выводя текстовую информацию о текущем состоянии коллекции.
     *
     * @param args аргументы команды (не используются)
     */
    @Override
    public void execute(String[] args) {
        System.out.println(collectionManager.getInfo());
    }

    /**
     * Возвращает краткое описание команды.
     *
     * @return строка с описанием назначения команды {@code info}
     */
    @Override
    public String getDescription() {
        return "вывести информацию о коллекции";
    }
}
