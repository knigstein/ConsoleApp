package command;

import collection.CollectionManager;

/**
 * Команда {@code clear}.
 * Очищает коллекцию учебных групп, удаляя все элементы.
 *
 * Реализует интерфейс {@link Command}.
 */
public class ClearCommand implements Command {

    private final CollectionManager collectionManager;

    /**
     * Создаёт команду очистки коллекции.
     *
     * @param collectionManager менеджер коллекции, которая будет очищена
     */
    public ClearCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Выполняет команду очистки коллекции и выводит подтверждение в консоль.
     *
     * @param args аргументы команды (не используются)
     */
    @Override
    public void execute(String[] args) {
        collectionManager.clear();
        System.out.println("Коллекция очищена");
    }

    /**
     * Возвращает краткое описание команды.
     *
     * @return строка с описанием назначения команды {@code clear}
     */
    @Override
    public String getDescription() {
        return "Очищение коллекции";
    }
}
