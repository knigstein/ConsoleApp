package command;

import collection.CollectionManager;
import model.StudyGroup;

/**
 * Команда {@code remove_first}.
 * Удаляет первый (минимальный) элемент коллекции и выводит информацию о нём.
 *
 * Реализует интерфейс {@link Command}.
 */
public class RemoveFirstCommand implements Command {

    private final CollectionManager collectionManager;

    /**
     * Создаёт команду удаления первого элемента коллекции.
     *
     * @param collectionManager менеджер коллекции, из которой удаляется элемент
     */
    public RemoveFirstCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Выполняет команду удаления первого элемента.
     * Если коллекция пуста, выводит соответствующее сообщение.
     *
     * @param args аргументы команды (не используются)
     */
    @Override
    public void execute(String[] args) {

        StudyGroup removed = collectionManager.removeFirst();

        if (removed == null) {
            System.out.println("Коллекция пуста");
        } else {
            System.out.println("Удалён элемент: " + removed);
        }
    }

    /**
     * Возвращает краткое описание команды.
     *
     * @return строка с описанием назначения команды {@code remove_first}
     */
    @Override
    public String getDescription() {
        return "Удалить первый элемент коллекции";
    }
}
