package command;

import collection.CollectionManager;
import model.Coordinates;
import model.Person;
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
            System.out.println(formatGroup(group));
        }
    }

    private String formatGroup(StudyGroup group) {
        StringBuilder sb = new StringBuilder();

        sb.append("StudyGroup").append('\n');
        sb.append("  id: ").append(group.getId()).append('\n');
        sb.append("  name: ").append(group.getName()).append('\n');
        sb.append("  creationDate: ").append(group.getCreationDate()).append('\n');
        sb.append("  studentsCount: ").append(group.getStudentsCount()).append('\n');
        sb.append("  expelledStudents: ").append(group.getExpelledStudents()).append('\n');
        sb.append("  transferredStudents: ").append(group.getTransferredStudents()).append('\n');
        sb.append("  semesterEnum: ").append(group.getSemesterEnum()).append('\n');

        Coordinates c = group.getCoordinates();
        sb.append("  coordinates:").append('\n');
        sb.append("    x: ").append(c != null ? c.getX() : null).append('\n');
        sb.append("    y: ").append(c != null ? c.getY() : null).append('\n');

        Person admin = group.getGroupAdmin();
        sb.append("  groupAdmin:").append('\n');
        sb.append("    name: ").append(admin != null ? admin.getName() : null).append('\n');
        sb.append("    birthday: ").append(admin != null ? admin.getBirthday() : null).append('\n');
        sb.append("    eyeColor: ").append(admin != null ? admin.getEyeColor() : null).append('\n');
        sb.append("    nationality: ").append(admin != null ? admin.getNationality() : null).append('\n');

        return sb.toString();
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
