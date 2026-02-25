package command;

import collection.CollectionManager;
import input.InputHandler;
import model.*;

import java.util.Scanner;

/**
 * Команда {@code update}, обновляющая существующий элемент коллекции по его идентификатору.
 * Работает с коллекцией учебных групп, хранящейся в {@link CollectionManager}, и позволяет
 * изменить часть полей выбранного объекта.
 *
 * Команда поддерживает взаимодействие как с консолью, так и с режимом скрипта,
 * получая ввод через {@link InputHandler}, который использует установленный {@link Scanner}.
 * Все ограничения доменной модели (валидность полей {@link StudyGroup}, {@link Person},
 * {@link model.Coordinates} и т.п.) соблюдаются за счёт проверок в соответствующих классах.
 */
public class UpdateCommand implements Command, ScriptAware {

    /**
     * Менеджер коллекции, в которой выполняется поиск и обновление элемента.
     */
    private final CollectionManager collectionManager;

    /**
     * Источник ввода данных (консоль или файл-скрипт), передаваемый через
     * {@link #setScanner(Scanner)} в режиме скрипта.
     */
    private Scanner scanner;

    /**
     * Создаёт команду обновления, связанную с указанным менеджером коллекции.
     *
     * @param collectionManager менеджер коллекции, в которой будет производиться обновление
     */
    public UpdateCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Устанавливает источник ввода, из которого будут считываться новые значения полей
     * обновляемого элемента.
     *
     * @param scanner внешний {@link Scanner}, связанный с консолью или файлом-скриптом
     */
    @Override
    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Обновляет элемент коллекции с указанным идентификатором.
     * Ожидается, что второй аргумент ({@code args[1]}) содержит целочисленный id.
     * Если элемент с таким id не найден, выводится соответствующее сообщение.
     *
     * Логика обновления:
     * <ul>
     *     <li>По id извлекается существующий объект {@link StudyGroup}.</li>
     *     <li>С помощью {@link InputHandler} считываются новые значения для части полей
     *     (имя, координаты, администратор), при этом можно использовать текущие значения
     *     в качестве значений по умолчанию.</li>
     *     <li>Создаётся новый объект {@link StudyGroup} с сохранением старой даты создания
     *     и остальных числовых характеристик.</li>
     *     <li>Менеджер коллекции обновляет элемент, заменяя старый объект на новый.</li>
     * </ul>
     *
     * Ошибки пользовательского ввода (включая неверный формат id и полей) перехватываются,
     * и их текст выводится пользователю; при этом элемент не обновляется.
     *
     * @param args массив аргументов, где {@code args[1]} — идентификатор обновляемого элемента
     */
    @Override
    public void execute(String[] args) {

        if (args.length < 2) {
            System.out.println("Не указан id.");
            return;
        }

        Integer id = Integer.parseInt(args[1]);
        StudyGroup existing = collectionManager.getById(id);

        if (existing == null) {
            System.out.println("Элемент не найден.");
            return;
        }

        InputHandler input = new InputHandler(scanner, false);

        try {
            String name = input.readString("Новое имя:", false, existing.getName());

            Coordinates coordinates = new Coordinates(
                    input.readInt("Новый X:", Integer.MIN_VALUE, existing.getCoordinates().getX()),
                    (double) input.readInt("Новый Y:", 0,
                            existing.getCoordinates().getY().intValue())
            );

            Person admin = new Person(
                    input.readString("Имя админа:", false,
                            existing.getGroupAdmin().getName()),
                    input.readDate("Дата рождения:", false,
                            existing.getGroupAdmin().getBirthday()),
                    null,
                    null
            );

            StudyGroup updated = new StudyGroup(
                    id,
                    name,
                    coordinates,
                    existing.getCreationDate(),
                    existing.getStudentsCount(),
                    existing.getExpelledStudents(),
                    existing.getTransferredStudents(),
                    existing.getSemesterEnum(),
                    admin
            );

            collectionManager.update(id, updated);
            System.out.println("Элемент обновлён.");

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
        return "обновить элемент";
    }
}