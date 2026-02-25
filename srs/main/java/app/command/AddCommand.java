package command;

import collection.CollectionManager;
import input.InputHandler;
import model.*;
import util.IdGenerator;
import java.time.LocalDate;
import java.util.Date;
import java.util.Scanner;

/**
 * Команда {@code add}, добавляющая новый элемент в коллекцию учебных групп.
 * Работает как в интерактивном режиме (чтение с консоли), так и в режиме
 * выполнения скрипта, получая ввод через {@link InputHandler}.
 *
 * Для построения объекта {@link StudyGroup} запрашивает у пользователя все необходимые поля:
 * координаты, числовые характеристики, семестр и администратора группы.
 * Валидация пользовательского ввода и обработка ошибок делегируется {@link InputHandler} и
 * конструкторам доменных моделей, которые могут выбрасывать {@link IllegalArgumentException}.
 */
public class AddCommand implements Command, ScriptAware {

    /**
     * Менеджер коллекции, в который будет добавлен созданный объект {@link StudyGroup}.
     */
    private final CollectionManager collectionManager;

    /**
     * Источник ввода, используемый для чтения данных (консоль или файл-скрипт).
     * Устанавливается через {@link #setScanner(Scanner)} при выполнении команды
     * в режиме скрипта.
     */
    private Scanner scanner;

    /**
     * Создаёт команду добавления, связанную с указанным менеджером коллекции.
     *
     * @param collectionManager менеджер коллекции, в который будут добавляться новые элементы
     */
    public AddCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Устанавливает сканер, из которого команда будет читать ввод пользователя
     * или содержимое скрипта.
     *
     * @param scanner внешний {@link Scanner}, связанный с источником ввода
     */
    @Override
    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Выполняет добавление нового элемента в коллекцию.
     * Пошагово запрашивает у пользователя (или из скрипта) значения всех полей
     * создаваемой учебной группы и, в случае успешной валидации, добавляет
     * объект в {@link CollectionManager}.
     *
     * Особенности обработки ошибок:
     * <ul>
     *     <li>Любые ошибки валидации или формата ввода (включая исключения,
     *     генерируемые конструкторами доменных объектов) перехватываются,</li>
     *     <li>пользователю выводится сообщение с текстом ошибки,</li>
     *     <li>элемент в этом случае не добавляется в коллекцию.</li>
     * </ul>
     *
     * @param args аргументы команды, в текущей реализации не используются
     */
    @Override
    public void execute(String[] args) {

        InputHandler input = new InputHandler(scanner, false);

        try {
            String name = input.readString("Введите название группы:", false, null);
            int x = input.readInt("Введите X:", Integer.MIN_VALUE, null);
            int yInt = input.readInt("Введите Y (>0):", 0, null);

            Coordinates coordinates = new Coordinates(x, (double) yInt);

            int studentsCount = input.readInt("Введите колличество студентов:", 0, null);
            Long expelledStudents = (long) input.readInt("Введите колличество отчисленных:", -1, 0);
            int transferredStudents = input.readInt("Введите колличество переведённых:", 0, null);

            Semester semester = input.readEnum("Введите семестр:",
                    Semester.class, true, null);

            String adminName = input.readString("Имя админа:", false, null);
            Date birthday = input.readDate("Дата рождения:", false, null);

            Person admin = new Person(adminName, birthday, null, null);

            StudyGroup group = new StudyGroup(
                    IdGenerator.generateId(),
                    name,
                    coordinates,
                    LocalDate.now(),
                    studentsCount,
                    expelledStudents,
                    transferredStudents,
                    semester,
                    admin
            );

            collectionManager.add(group);
            System.out.println("Группа добавлена.");

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    /**
     * Возвращает краткое текстовое описание команды для вывода в справке.
     *
     * @return строка с описанием назначения команды
     */
    @Override
    public String getDescription() {
        return "Добавить элемент";
    }
}