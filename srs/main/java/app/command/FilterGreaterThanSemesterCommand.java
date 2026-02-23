package command;

import collection.CollectionManager;
import model.Semester;

/**
 * Команда {@code filter_greater_than_semester_enum}.
 * Выводит элементы коллекции, у которых значение поля {@code semesterEnum}
 * строго больше указанного значения перечисления {@link Semester}.
 *
 * Реализует интерфейс {@link Command}.
 */
public class FilterGreaterThanSemesterCommand implements Command {

    private final CollectionManager collectionManager;

    /**
     * Создаёт команду фильтрации по значению семестра.
     *
     * @param collectionManager менеджер коллекции, над которой выполняется фильтрация
     */
    public FilterGreaterThanSemesterCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Выполняет команду фильтрации по семестру.
     * Ожидает, что во втором аргументе команды передано строковое представление
     * значения перечисления {@link Semester}.
     *
     * @param args аргументы команды, где {@code args[1]} — значение enum {@link Semester}
     */
    @Override
    public void execute(String[] args) {

        if (args.length < 2) {
            System.out.println("Не указан семестр");
            return;
        }

        try {
            Semester semester = Semester.valueOf(args[1]);
            collectionManager.filterGreaterThanSemester(semester);
        } catch (Exception e) {
            System.out.println("Некорректный семестр");
        }
    }

    /**
     * Возвращает краткое описание команды.
     *
     * @return строка с описанием назначения команды {@code filter_greater_than_semester_enum}
     */
    @Override
    public String getDescription() {
        return "Вывести элементы с semesterEnum больше заданного";
    }
}
