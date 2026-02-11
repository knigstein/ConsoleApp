package command;

import collection.CollectionManager;
import io.FileManager;

/**
 * Команда {@code save}.
 * Сохраняет текущую коллекцию учебных групп в файл с помощью {@link FileManager}.
 *
 * Реализует интерфейс {@link Command}.
 */
public class SaveCommand implements Command {

    private final CollectionManager collectionManager;
    private final FileManager fileManager;

    /**
     * Создаёт команду сохранения коллекции в файл.
     *
     * @param collectionManager менеджер коллекции, содержимое которой сохраняется
     * @param fileManager менеджер файла, выполняющий запись данных
     */
    public SaveCommand(CollectionManager collectionManager,
                       FileManager fileManager) {
        this.collectionManager = collectionManager;
        this.fileManager = fileManager;
    }

    /**
     * Выполняет команду сохранения коллекции в файл.
     * В случае ошибки выводит сообщение об ошибке.
     *
     * @param args аргументы команды (не используются)
     */
    @Override
    public void execute(String[] args) {
        try {
            fileManager.save(collectionManager.getCollection());
            System.out.println("Коллекция сохранена.");
        } catch (Exception e) {
            System.out.println("Ошибка при сохранении файла.");
        }
    }

    /**
     * Возвращает краткое описание команды.
     *
     * @return строка с описанием назначения команды {@code save}
     */
    @Override
    public String getDescription() {
        return "сохранить коллекцию в файл";
    }
}
