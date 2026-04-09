package command;

import collection.CollectionManager;

/**
 * Команда {@code print_field_descending_group_admin}.
 * Выводит значения поля {@code groupAdmin} (имена администраторов групп)
 * в порядке убывания (обратный лексикографический порядок).
 *
 * Реализует интерфейс {@link Command}.
 */
public class PrintFieldDescendingGroupAdminCommand implements Command {

    private final CollectionManager collectionManager;

    /**
     * Создаёт команду вывода имён администраторов в порядке убывания.
     *
     * @param collectionManager менеджер коллекции, из которой берутся данные
     */
    public PrintFieldDescendingGroupAdminCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Выполняет команду, выводя имена администраторов групп в порядке убывания.
     *
     * @param args аргументы команды (не используются)
     */
    @Override
    public void execute(String[] args) {
        collectionManager.printAdminsDescending();
    }

    /**
     * Возвращает краткое описание команды.
     *
     * @return строка с описанием назначения команды {@code print_field_descending_group_admin}
     */
    @Override
    public String getDescription() {
        return "Вывести groupAdmin в порядке убывания";
    }
}
