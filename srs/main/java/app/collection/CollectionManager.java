package collection;

import database.StudyGroupRepository;
import model.Semester;
import model.StudyGroup;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.sql.SQLException;

public class CollectionManager {

    private PriorityQueue<StudyGroup> collection;
    private LocalDateTime initializationDate;
    private final StudyGroupRepository repository;
    private final ReentrantLock lock = new ReentrantLock();

    public CollectionManager(StudyGroupRepository repository) {
        this.repository = repository;
        this.collection = new PriorityQueue<>();
        this.initializationDate = LocalDateTime.now();
        loadFromDatabase();
    }

    public void loadFromDatabase() {
        lock.lock();
        try {
            collection = repository.findAll();
            initializationDate = LocalDateTime.now();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load collection from database", e);
        } finally {
            lock.unlock();
        }
    }

    public PriorityQueue<StudyGroup> getCollection() {
        lock.lock();
        try {
            return new PriorityQueue<>(collection);
        } finally {
            lock.unlock();
        }
    }

    public PriorityQueue<StudyGroup> getCollectionSnapshot() {
        return new PriorityQueue<>(collection);
    }

    public LocalDateTime getInitializationDate() {
        return initializationDate;
    }

    public int size() {
        lock.lock();
        try {
            return collection.size();
        } finally {
            lock.unlock();
        }
    }

    public void add(StudyGroup group, Integer ownerId) {
        lock.lock();
        try {
            Integer id = repository.create(group, ownerId);
            group.setId(id);
            collection.add(group);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add group", e);
        } finally {
            lock.unlock();
        }
    }

    public void add(StudyGroup group) {
        add(group, null);
    }

    public boolean addIfMin(StudyGroup group) {
        return addIfMin(group, null);
    }

    public void clear() {
        clear(null);
    }

    public StudyGroup removeFirst() {
        return removeFirst(null).orElse(null);
    }

    public boolean removeById(Integer id) {
        return removeById(id, null);
    }

    public int removeLower(StudyGroup group) {
        return removeLower(group, null);
    }

    public boolean update(Integer id, StudyGroup group) {
        return update(id, group, null);
    }

    /**
     * Удаляет из БД все группы, принадлежащие {@code ownerId}, и перезагружает коллекцию в памяти.
     *
     * @return снимок удалённых групп (пустой список, если {@code ownerId == null} или нечего удалять)
     */
    public List<StudyGroup> clear(Integer ownerId) {
        lock.lock();
        try {
            if (ownerId == null) {
                return List.of();
            }
            List<StudyGroup> removed = new ArrayList<>(repository.findByOwner(ownerId));
            for (StudyGroup g : removed) {
                repository.delete(g.getId(), ownerId);
            }
            reloadCollection();
            return removed;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear collection", e);
        } finally {
            lock.unlock();
        }
    }

    public Optional<StudyGroup> removeFirst(Integer ownerId) {
        lock.lock();
        try {
            Optional<StudyGroup> removed = repository.deleteFirst(ownerId);
            if (removed.isPresent()) {
                collection = repository.findAll();
            }
            return removed;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove first", e);
        } finally {
            lock.unlock();
        }
    }

    public String getInfo() {
        lock.lock();
        try {
            return "Тип коллекции: " + collection.getClass().getName() +
                    "\nДата инициализации: " + initializationDate +
                    "\nКоличество элементов: " + collection.size();
        } finally {
            lock.unlock();
        }
    }

    public boolean update(Integer id, StudyGroup newGroup, Integer ownerId) {
        lock.lock();
        try {
            boolean updated = repository.update(newGroup, ownerId);
            if (updated) {
                collection = repository.findAll();
            }
            return updated;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update group", e);
        } finally {
            lock.unlock();
        }
    }

    public boolean removeById(Integer id, Integer ownerId) {
        lock.lock();
        try {
            boolean removed = repository.delete(id, ownerId);
            if (removed) {
                reloadCollection();
            }
            return removed;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove group", e);
        } finally {
            lock.unlock();
        }
    }

    public boolean addIfMin(StudyGroup group, Integer ownerId) {
        lock.lock();
        try {
            List<StudyGroup> ownerGroups = repository.findByOwner(ownerId);
            Optional<StudyGroup> min = ownerGroups.stream()
                    .min(StudyGroup::compareTo);

            if (min.isEmpty() || group.compareTo(min.get()) < 0) {
                Integer id = repository.create(group, ownerId);
                group.setId(id);
                collection.add(group);
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add if min", e);
        } finally {
            lock.unlock();
        }
    }

    public int removeLower(StudyGroup group, Integer ownerId) {
        lock.lock();
        try {
            int studentsCount = group.getStudentsCount();
            List<StudyGroup> removed = repository.deleteLower(ownerId, studentsCount);
            reloadCollection();
            return removed.size();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove lower", e);
        } finally {
            lock.unlock();
        }
    }

    public List<StudyGroup> filterContainsName(String substring) {
        lock.lock();
        try {
            return collection.stream()
                    .filter(group -> group.getName().contains(substring))
                    .toList();
        } finally {
            lock.unlock();
        }
    }

    public List<StudyGroup> filterGreaterThanSemester(Semester semester) {
        lock.lock();
        try {
            return collection.stream()
                    .filter(group ->
                            group.getSemesterEnum() != null &&
                            group.getSemesterEnum().compareTo(semester) > 0)
                    .toList();
        } finally {
            lock.unlock();
        }
    }

    public List<String> printAdminsDescending() {
        lock.lock();
        try {
            return collection.stream()
                    .map(group -> group.getGroupAdmin().getName())
                    .sorted((a, b) -> b.compareTo(a))
                    .toList();
        } finally {
            lock.unlock();
        }
    }

    public StudyGroup getById(Integer id) {
        lock.lock();
        try {
            return collection.stream()
                    .filter(g -> id.equals(g.getId()))
                    .findFirst()
                    .orElse(null);
        } finally {
            lock.unlock();
        }
    }

    private void reloadCollection() throws SQLException {
        collection = repository.findAll();
    }

    public ReentrantLock getLock() {
        return lock;
    }
}