package collection;

import model.Semester;
import model.StudyGroup;

import java.time.LocalDateTime;
import java.util.PriorityQueue;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;

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
        return replaceById(id, newGroup);
    }

    /**
     * Удаляет элемент с указанным идентификатором из коллекции.
     *
     * @param id идентификатор группы для удаления
     * @return {@code true}, если элемент был найден и удалён, иначе {@code false}
     */
    public boolean removeById(Integer id) {
        return removeAndReturnById(id).isPresent();
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
        if (id == null || newGroup == null) {
            return false;
        }
        newGroup.setId(id);
        return replaceById(id, newGroup);
    }

    /**
     * Добавляет элемент в коллекцию, если он меньше текущего минимального элемента.
     * Сравнение выполняется с использованием метода {@link StudyGroup#compareTo(StudyGroup)}.
     *
     * @param group добавляемый объект {@link StudyGroup}
     * @return {@code true}, если элемент был добавлен, иначе {@code false}
     */
    public boolean addIfMin(StudyGroup group) {
        Optional<StudyGroup> min = collection.stream().min(Comparator.naturalOrder());
        if (min.isEmpty() || group.compareTo(min.get()) < 0) {
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
        List<StudyGroup> toRemove = collection.stream()
                .filter(existing -> existing.compareTo(group) < 0)
                .toList();

        collection.removeAll(toRemove);
        return toRemove.size();
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
        if (id == null) {
            return null;
        }
        return collection.stream()
                .filter(g -> id.equals(g.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Заменяет элемент с указанным id на новый объект.
     *
     * @param id идентификатор заменяемого элемента
     * @param newGroup новый объект, который будет сохранён в коллекции
     * @return {@code true}, если элемент с таким id найден и заменён
     */
    public boolean replaceById(Integer id, StudyGroup newGroup) {
        if (id == null || newGroup == null) {
            return false;
        }

        Optional<StudyGroup> existing = collection.stream()
                .filter(g -> id.equals(g.getId()))
                .findFirst();

        if (existing.isEmpty()) {
            return false;
        }

        collection.remove(existing.get());
        collection.add(newGroup);
        return true;
    }

    /**
     * Удаляет элемент по id.
     *
     * @param id идентификатор
     * @return удалённый элемент, если он существовал
     */
    public Optional<StudyGroup> removeAndReturnById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        Optional<StudyGroup> existing = collection.stream()
                .filter(g -> id.equals(g.getId()))
                .findFirst();
        existing.ifPresent(collection::remove);
        return existing;
    }

}
