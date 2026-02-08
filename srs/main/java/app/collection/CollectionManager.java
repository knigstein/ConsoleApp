package collection;

import model.Semester;
import model.StudyGroup;

import java.time.LocalDateTime;
import java.util.PriorityQueue;

import io.FileManager;

/**
 * Класс для управления коллекцией StudyGroup.
 */

public class CollectionManager {

    private PriorityQueue<StudyGroup> collection;
    private LocalDateTime initializationDate;

    public CollectionManager() {
        collection = new PriorityQueue<>();
        initializationDate = LocalDateTime.now();
    }

    public PriorityQueue<StudyGroup> getCollection() {
        return collection;
    }

    public LocalDateTime getInitializationDate() {
        return initializationDate;
    }

    public int size() {
        return collection.size();
    }

    public void add(StudyGroup group) {
        collection.add(group);
    }

    public void clear() {
        collection.clear();
    }

    public StudyGroup removeFirst() {
        return collection.poll();
    }

    public String getInfo() {
        return "Тип коллекции: " + collection.getClass().getName() +
                "\nДата инициализации: " + initializationDate +
                "\nКоличество элементов: " + collection.size();
    }

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

    public boolean removeById(Integer id) {
        return collection.removeIf(group -> group.getId().equals(id));
    }

    public void save(FileManager fileManager) {
        fileManager.save(collection);
    }

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

    public int removeLower(StudyGroup group) {

        int before = collection.size();

        collection.removeIf(existing -> existing.compareTo(group) < 0);

        return before - collection.size();
    }

    public void filterContainsName(String substring) {

        collection.stream()
                .filter(group -> group.getName().contains(substring))
                .forEach(System.out::println);
    }

    public void filterGreaterThanSemester(Semester semester) {

        collection.stream()
                .filter(group ->
                        group.getSemesterEnum() != null &&
                        group.getSemesterEnum().compareTo(semester) > 0)
                .forEach(System.out::println);
    }

    public void printAdminsDescending() {

        collection.stream()
                .map(group -> group.getGroupAdmin().getName())
                .sorted((a, b) -> b.compareTo(a))
                .forEach(System.out::println);
    }

}
