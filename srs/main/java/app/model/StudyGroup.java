package model;

import java.time.LocalDate;

/**
 * Класс, представляющий учебную группу.
 * Содержит основную информацию о группе: идентификатор, название, координаты,
 * дату создания, количество студентов, количество отчисленных и переведённых студентов,
 * семестр обучения и администратора группы.
 *
 * Реализует интерфейс {@link Comparable}, что позволяет сравнивать объекты
 * {@code StudyGroup} между собой. Сравнение выполняется сначала по количеству студентов,
 * а при равенстве этого показателя — по идентификатору группы.
 *
 * Класс является частью доменной модели приложения для управления коллекцией учебных групп.
 */
public class StudyGroup implements Comparable<StudyGroup> {
    private Integer id;
    private String name;
    private Coordinates coordinates;
    private java.time.LocalDate creationDate;
    private int studentsCount;
    private Long expelledStudents;
    private int transferredStudents;
    private Semester semesterEnum;
    private Person groupAdmin;

    /**
     * Создаёт новый объект учебной группы с указанными параметрами.
     *
     * @param id уникальный идентификатор группы, не может быть {@code null} и должен быть больше 0
     * @param name название группы, не может быть {@code null} и пустой строкой
     * @param coordinates координаты группы, не могут быть {@code null}
     * @param creationDate дата создания группы, не может быть {@code null}
     * @param studentsCount количество студентов в группе, должно быть больше 0
     * @param expelledStudents количество отчисленных студентов, должно быть больше 0, может быть {@code null}
     * @param transferredStudents количество переведённых студентов, должно быть больше 0
     * @param semesterEnum семестр обучения, может быть {@code null}, если не задан
     * @param groupAdmin администратор группы, не может быть {@code null}
     * @throws IllegalArgumentException если нарушены ограничения на значения полей
     */
    public StudyGroup(Integer id, String name, Coordinates coordinates, LocalDate creationDate, int studentsCount, Long expelledStudents, int transferredStudents, Semester semesterEnum, Person groupAdmin) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id cannot be null and must be more than 0");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }

        if (coordinates == null) {
            throw new IllegalArgumentException("coordinates cannot be null");
        }

        if (creationDate == null) {
            throw new IllegalArgumentException("creationDate cannot be null");
        }

        if (studentsCount <= 0) {
            throw new IllegalArgumentException("studentsCount must be more than 0");
        }

        if (expelledStudents <= 0) {
            throw new IllegalArgumentException("expelledStudents must be more than 0, but can be nullable");
        }

        if (transferredStudents <= 0) {
            throw new IllegalArgumentException("transferredStudents must be more than 0, but can be nullable"); 
        }

        if (groupAdmin == null) {
            throw new IllegalArgumentException("groupAdmin connot be null");
        }

        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.studentsCount = studentsCount;
        this.expelledStudents = expelledStudents;
        this.transferredStudents = transferredStudents;
        this.semesterEnum = semesterEnum;
        this.groupAdmin = groupAdmin;

    }

    /**
     * Сравнивает текущую учебную группу с другой группой.
     * Сначала сравнивается количество студентов, а если оно одинаково,
     * то сравнение производится по идентификатору группы.
     *
     * @param other другая учебная группа, с которой выполняется сравнение
     * @return отрицательное число, если текущая группа "меньше" другой;
     *         ноль, если группы равны по количеству студентов и идентификатору;
     *         положительное число, если текущая группа "больше" другой
     */
    @Override
    public int compareTo(StudyGroup other) {
        int result = Integer.compare(this.studentsCount, other.studentsCount);
        if (result == 0) {
            return Integer.compare(this.id, other.id);
        }
        return result;
    }

    /**
     * Возвращает идентификатор группы.
     *
     * @return идентификатор группы, всегда больше 0
     */
    public Integer getId() {
        return id;
    }

    /**
     * Возвращает название группы.
     *
     * @return название группы, не бывает {@code null} или пустым
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает координаты группы.
     *
     * @return объект с координатами группы
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }
    
    /**
     * Возвращает дату создания группы.
     *
     * @return дата создания группы
     */
    public LocalDate getCreationDate() {
        return creationDate;
    }

    /**
     * Возвращает количество студентов в группе.
     *
     * @return количество студентов, положительное целое число
     */
    public int getStudentsCount() {
        return studentsCount;
    }

    /**
     * Возвращает количество отчисленных студентов.
     *
     * @return количество отчисленных студентов или {@code null}, если не задано
     */
    public Long getExpelledStudents() {
        return expelledStudents;
    }

    /**
     * Возвращает количество переведённых студентов.
     *
     * @return количество переведённых студентов, положительное целое число
     */
    public int getTransferredStudents() {
        return transferredStudents;
    }

    /**
     * Возвращает значение семестра обучения.
     *
     * @return семестр обучения или {@code null}, если не задан
     */
    public Semester getSemesterEnum() {
        return semesterEnum;
    }

    /**
     * Возвращает администратора группы.
     *
     * @return объект администратора группы, не бывает {@code null}
     */
    public Person getGroupAdmin() {
        return groupAdmin;
    }

    /**
     * Устанавливает идентификатор группы.
     * Используется в коллекции для сохранения уникальности объекта при обновлении.
     *
     * @param id идентификатор группы, должен быть больше 0
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Возвращает строковое представление учебной группы,
     * содержащее основные поля объекта (id, имя, количество студентов, семестр).
     *
     * @return строковое представление объекта {@code StudyGroup}
     */
    @Override
    public String toString() {
        return "StudyGroup{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", studentsCount=" + studentsCount +
                ", semesterEnum=" + semesterEnum +
                '}';
    }

}
