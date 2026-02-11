package command;

import collection.CollectionManager;
import model.StudyGroup;
import util.StudyGroupBuilder;

import java.util.Scanner;

/**
 * Команда {@code remove_lower}.
 * Удаляет из коллекции все элементы, которые меньше заданного объекта
 * согласно методу {@link model.StudyGroup#compareTo(model.StudyGroup)}.
 *
 * Реализует интерфейсы {@link Command} и {@link ScriptAware}.
 */
public class RemoveLowerCommand implements Command, ScriptAware {

    private final CollectionManager collectionManager;
    private Scanner scanner;

    /**
     * Создаёт команду удаления элементов, меньших заданного.
     *
     * @param collectionManager менеджер коллекции, в которой выполняется удаление
     * @param scanner сканер для чтения пользовательского ввода
     */
    public RemoveLowerCommand(CollectionManager collectionManager,
                              Scanner scanner) {
        this.collectionManager = collectionManager;
        this.scanner = scanner;
    }

    /**
     * Устанавливает сканер, который будет использоваться при работе команды
     * в режиме выполнения скрипта.
     *
     * @param scanner внешний {@link Scanner}, связанный с файлом-скриптом
     */
    @Override
    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Выполняет команду удаления всех элементов, меньших заданного.
     * Сначала формируется опорный объект {@link model.StudyGroup}, затем по нему
     * проводится фильтрация коллекции.
     *
     * @param args аргументы команды (не используются)
     */
    @Override
    public void execute(String[] args) {

        StudyGroupBuilder builder = new StudyGroupBuilder(scanner);
        StudyGroup group = builder.build();

        int removed = collectionManager.removeLower(group);

        System.out.println("Удалено элементов: " + removed);
    }

    /**
     * Возвращает краткое описание команды.
     *
     * @return строка с описанием назначения команды {@code remove_lower}
     */
    @Override
    public String getDescription() {
        return "удалить элементы меньше заданного";
    }
}
