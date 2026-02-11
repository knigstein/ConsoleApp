package command;

import collection.CollectionManager;
import model.StudyGroup;
import util.StudyGroupBuilder;

import java.util.Scanner;

/**
 * Команда {@code update}.
 * Обновляет элемент коллекции с указанным идентификатором, заменяя его
 * на новый объект {@link StudyGroup}, созданный с помощью {@link StudyGroupBuilder}.
 *
 * Реализует интерфейсы {@link Command} и {@link ScriptAware}, поэтому может
 * использоваться как в интерактивном режиме, так и при выполнении скриптов.
 */
public class UpdateCommand implements Command, ScriptAware {

    private final CollectionManager collectionManager;
    private Scanner scanner;

    /**
     * Создаёт команду обновления элемента по идентификатору.
     *
     * @param collectionManager менеджер коллекции, в которой выполняется обновление
     * @param scanner сканер для чтения пользовательского ввода
     */
    public UpdateCommand(CollectionManager collectionManager,
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
     * Выполняет команду обновления элемента.
     * Ожидает, что во втором аргументе команды передан идентификатор элемента.
     * При успешном обновлении выводит сообщение, иначе сообщает об ошибке.
     *
     * @param args аргументы команды, где {@code args[1]} — идентификатор элемента
     */
    @Override
    public void execute(String[] args) {

        if (args.length < 2) {
            System.out.println("Не указан id.");
            return;
        }

        try {
            Integer id = Integer.parseInt(args[1]);

            StudyGroupBuilder builder = new StudyGroupBuilder(scanner);
            StudyGroup newGroup = builder.build();

            boolean updated = collectionManager.updateById(id, newGroup);

            if (updated) {
                System.out.println("Элемент обновлён.");
            } else {
                System.out.println("Элемент с таким id не найден.");
            }

        } catch (NumberFormatException e) {
            System.out.println("id должен быть числом.");
        }
    }

    /**
     * Возвращает краткое описание команды.
     *
     * @return строка с описанием назначения команды {@code update}
     */
    @Override
    public String getDescription() {
        return "обновить элемент по id";
    }
}
