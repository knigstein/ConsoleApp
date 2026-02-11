package command;

import collection.CollectionManager;
import model.StudyGroup;

/**
 * Команда {@code show}.
 * Выводит в стандартный поток вывода все элементы коллекции учебных групп.
 *
 * Реализует интерфейс {@link Command}.
 */
public class ShowCommand implements Command {

    private final CollectionManager collectionManager;

    /**
     * Создаёт команду вывода элементов коллекции.
     *
     * @param collectionManager менеджер коллекции, чьи элементы будут выводиться
     */
    public ShowCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Выполняет команду вывода всех элементов.
     * Если коллекция пуста, выводит соответствующее сообщение.
     *
     * @param args аргументы команды (не используются)
     */
    @Override
    public void execute(String[] args) {

        if (collectionManager.size() == 0) {
            System.out.println("Коллекция пуста.");
            return;
        }

        for (StudyGroup group : collectionManager.getCollection()) {
            System.out.println(group);
        }
    }

    /**
     * Возвращает краткое описание команды.
     *
     * @return строка с описанием назначения команды {@code show}
     */
    @Override
    public String getDescription() {
        return "вывести все элементы коллекции";
    }
}
