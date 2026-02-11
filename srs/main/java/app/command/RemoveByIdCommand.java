package command;

import collection.CollectionManager;

/**
 * Команда {@code remove_by_id}.
 * Удаляет элемент коллекции по его идентификатору.
 *
 * Реализует интерфейс {@link Command}.
 */
public class RemoveByIdCommand implements Command {

    private final CollectionManager collectionManager;

    /**
     * Создаёт команду удаления элемента по идентификатору.
     *
     * @param collectionManager менеджер коллекции, в которой выполняется удаление
     */
    public RemoveByIdCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Выполняет команду удаления элемента по указанному идентификатору.
     * Ожидает, что во втором аргументе команды передано целое число.
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

            boolean removed = collectionManager.removeById(id);

            if (removed) {
                System.out.println("Элемент удалён.");
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
     * @return строка с описанием назначения команды {@code remove_by_id}
     */
    @Override
    public String getDescription() {
        return "удалить элемент по id";
    }
}
