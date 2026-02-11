package command;

import collection.CollectionManager;
import model.StudyGroup;
import util.StudyGroupBuilder;

import java.util.Scanner;

/**
 * Команда {@code add}.
 * Добавляет новый объект {@link StudyGroup} в коллекцию, запрашивая все необходимые
 * данные у пользователя через {@link StudyGroupBuilder}.
 *
 * Реализует интерфейсы {@link Command} и {@link ScriptAware}, поэтому может
 * использоваться как в интерактивном режиме, так и при выполнении скриптов.
 */
public class AddCommand implements Command, ScriptAware {

    private final CollectionManager collectionManager;
    private Scanner scanner;

    /**
     * Создаёт команду добавления элемента в коллекцию.
     *
     * @param collectionManager менеджер коллекции, в которую будет добавлен новый элемент
     * @param scanner сканер для чтения пользовательского ввода
     */
    public AddCommand(CollectionManager collectionManager, Scanner scanner) {
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
     * Выполняет команду добавления.
     * Пошагово запрашивает у пользователя данные для новой группы и добавляет
     * созданный объект в коллекцию.
     *
     * @param args аргументы команды (в данном случае не используются)
     */
    @Override
    public void execute(String[] args) {

        StudyGroupBuilder builder = new StudyGroupBuilder(scanner);
        StudyGroup group = builder.build();

        collectionManager.add(group);

        System.out.println("Элемент добавлен.");
    }

    /**
     * Возвращает краткое текстовое описание команды.
     *
     * @return строка с описанием назначения команды {@code add}
     */
    @Override
    public String getDescription() {
        return "добавить новый элемент в коллекцию";
    }
}
