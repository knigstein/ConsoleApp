package model;

import java.time.LocalDate;


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

    @Override
    public int compareTo(StudyGroup other) {
        int result = Integer.compare(this.studentsCount, other.studentsCount);
        if (result == 0) {
            return Integer.compare(this.id, other.id);
        }
        return result;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }
    
    public LocalDate getCreationDate() {
        return creationDate;
    }

    public int getStudentsCount() {
        return studentsCount;
    }

    public Long getExpelledStudents() {
        return expelledStudents;
    }

    public int getTransferredStudents() {
        return transferredStudents;
    }

    public Semester getSemesterEnum() {
        return semesterEnum;
    }

    public Person getGroupAdmin() {
        return groupAdmin;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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
