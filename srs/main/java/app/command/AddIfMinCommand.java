package command;

import collection.CollectionManager;
import model.StudyGroup;
import util.StudyGroupBuilder;

import java.util.Scanner;

/**
 * Команда {@code add_if_min}.
 * Добавляет новый элемент в коллекцию, если он меньше текущего минимального элемента
 * согласно методу {@link model.StudyGroup#compareTo(model.StudyGroup)}.
 *
 * Реализует интерфейсы {@link Command} и {@link ScriptAware}.
 */
public class AddIfMinCommand implements Command, ScriptAware {

    private final CollectionManager collectionManager;
    private Scanner scanner;

    /**
     * Создаёт команду условного добавления элемента (если он минимальный).
     *
     * @param collectionManager менеджер коллекции, в которую добавляется элемент
     * @param scanner сканер для чтения пользовательского ввода
     */
    public AddIfMinCommand(CollectionManager collectionManager, Scanner scanner) {
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
     * Выполняет команду добавления элемента, если он меньше минимального.
     * Запрашивает у пользователя данные для новой группы и сравнивает её
     * с текущим минимальным элементом коллекции.
     *
     * @param args аргументы команды (не используются)
     */
    @Override
    public void execute(String[] args) {

        StudyGroupBuilder builder = new StudyGroupBuilder(scanner);
        StudyGroup group = builder.build();

        boolean added = collectionManager.addIfMin(group);

        if (added) {
            System.out.println("Элемент добавлен (он меньше минимального).");
        } else {
            System.out.println("Элемент не добавлен (он не меньше минимального).");
        }
    }

    /**
     * Возвращает краткое описание команды.
     *
     * @return строка с описанием назначения команды {@code add_if_min}
     */
    @Override
    public String getDescription() {
        return "добавить элемент, если он меньше минимального";
    }
}
