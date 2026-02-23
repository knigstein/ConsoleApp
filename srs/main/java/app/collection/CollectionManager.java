package collection;

import model.Semester;
import model.StudyGroup;

import java.time.LocalDateTime;
import java.util.PriorityQueue;

import io.FileManager;

/**
 * Класс для управления коллекцией объектов {@link StudyGroup}.
 * Хранит коллекцию в виде приоритетной очереди и предоставляет методы
 * для добавления, удаления, фильтрации и сохранения элементов.
 *
 * Используется командами приложения для выполнения операций над коллекцией.
 */
public class CollectionManager {

    private PriorityQueue<StudyGroup> collection;
    private LocalDateTime initializationDate;

    /**
     * Создаёт новый менеджер коллекции с пустой приоритетной очередью.
     * Фиксирует время инициализации коллекции.
     */
    public CollectionManager() {
        collection = new PriorityQueue<>();
        initializationDate = LocalDateTime.now();
    }

    /**
     * Возвращает внутреннюю коллекцию учебных групп.
     *
     * @return приоритетная очередь с объектами {@link StudyGroup}
     */
    public PriorityQueue<StudyGroup> getCollection() {
        return collection;
    }

    /**
     * Возвращает дату и время инициализации коллекции.
     *
     * @return дата и время создания менеджера коллекции
     */
    public LocalDateTime getInitializationDate() {
        return initializationDate;
    }

    /**
     * Возвращает количество элементов в коллекции.
     *
     * @return текущее количество элементов
     */
    public int size() {
        return collection.size();
    }

    /**
     * Добавляет новую учебную группу в коллекцию.
     *
     * @param group добавляемый объект {@link StudyGroup}
     */
    public void add(StudyGroup group) {
        collection.add(group);
    }

    /**
     * Очищает коллекцию учебных групп.
     */
    public void clear() {
        collection.clear();
    }

    /**
     * Удаляет и возвращает первый (минимальный согласно {@link StudyGroup#compareTo(StudyGroup)})
     * элемент коллекции.
     *
     * @return удалённый элемент или {@code null}, если коллекция пуста
     */
    public StudyGroup removeFirst() {
        return collection.poll();
    }

    /**
     * Возвращает текстовую информацию о коллекции: тип, дату инициализации и размер.
     *
     * @return строка с описанием коллекции
     */
    public String getInfo() {
        return "Тип коллекции: " + collection.getClass().getName() +
                "\nДата инициализации: " + initializationDate +
                "\nКоличество элементов: " + collection.size();
    }

    /**
     * Заменяет элемент с указанным идентификатором на новый объект.
     *
     * @param id идентификатор существующей группы
     * @param newGroup новый объект {@link StudyGroup}, который должен заменить старый
     * @return {@code true}, если элемент был найден и заменён, иначе {@code false}
     */
    public boolean update(int id, StudyGroup newGroup) {
        for (StudyGroup group : collection) {
            if (group.getId() == id) {
                collection.remove(group);
                collection.add(newGroup);
                return true;
            }
        }
        return false;
    }

    /**
     * Удаляет элемент с указанным идентификатором из коллекции.
     *
     * @param id идентификатор группы для удаления
     * @return {@code true}, если элемент был найден и удалён, иначе {@code false}
     */
    public boolean removeById(Integer id) {
        return collection.removeIf(group -> group.getId().equals(id));
    }

    /**
     * Сохраняет текущую коллекцию в файл с помощью {@link FileManager}.
     *
     * @param fileManager объект, выполняющий сериализацию и запись коллекции в файл
     */
    public void save(FileManager fileManager) {
        fileManager.save(collection);
    }

    /**
     * Обновляет элемент с указанным идентификатором, сохраняя сам id.
     *
     * @param id идентификатор обновляемой группы
     * @param newGroup новый объект {@link StudyGroup}, который заменит старый
     * @return {@code true}, если элемент был найден и обновлён, иначе {@code false}
     */
    public boolean updateById(Integer id, StudyGroup newGroup) {

        StudyGroup existing = null;

        for (StudyGroup group : collection) {
            if (group.getId().equals(id)) {
                existing = group;
                break;
            }
        }

        if (existing == null) {
            return false;
        }

        collection.remove(existing);

        // Сохраняем старый id
        newGroup.setId(id);

        collection.add(newGroup);

        return true;
    }

    /**
     * Добавляет элемент в коллекцию, если он меньше текущего минимального элемента.
     * Сравнение выполняется с использованием метода {@link StudyGroup#compareTo(StudyGroup)}.
     *
     * @param group добавляемый объект {@link StudyGroup}
     * @return {@code true}, если элемент был добавлен, иначе {@code false}
     */
    public boolean addIfMin(StudyGroup group) {

        if (collection.isEmpty()) {
            collection.add(group);
            return true;
        }

        StudyGroup first = collection.peek();

        if (group.compareTo(first) < 0) {
            collection.add(group);
            return true;
        }

        return false;
    }

    /**
     * Удаляет из коллекции все элементы, которые меньше указанного объекта.
     * Сравнение выполняется с помощью {@link StudyGroup#compareTo(StudyGroup)}.
     *
     * @param group объект, относительно которого выполняется сравнение
     * @return количество удалённых элементов
     */
    public int removeLower(StudyGroup group) {

        int before = collection.size();

        collection.removeIf(existing -> existing.compareTo(group) < 0);

        return before - collection.size();
    }

    /**
     * Выводит в стандартный поток вывода все элементы,
     * название которых содержит указанную подстроку.
     *
     * @param substring подстрока для поиска в названии группы
     */
    public void filterContainsName(String substring) {

        collection.stream()
                .filter(group -> group.getName().contains(substring))
                .forEach(System.out::println);
    }

    /**
     * Выводит в стандартный поток вывода все элементы, у которых
     * значение {@link Semester} строго больше указанного.
     *
     * @param semester семестр, с которым сравниваются элементы
     */
    public void filterGreaterThanSemester(Semester semester) {

        collection.stream()
                .filter(group ->
                        group.getSemesterEnum() != null &&
                        group.getSemesterEnum().compareTo(semester) > 0)
                .forEach(System.out::println);
    }

    /**
     * Выводит имена администраторов групп в порядке убывания (обратный лексикографический порядок).
     */
    public void printAdminsDescending() {

        collection.stream()
                .map(group -> group.getGroupAdmin().getName())
                .sorted((a, b) -> b.compareTo(a))
                .forEach(System.out::println);
    }

    public StudyGroup getById(Integer id) {
        if (id == null) return null;

        for (StudyGroup group : collection) {
            if (group.getId().equals(id)) {
                return group;
            }
        }

        return null;
    }

}
