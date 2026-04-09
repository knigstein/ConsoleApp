package common.dto;

import model.Semester;

/**
 * Команда фильтрации элементов по значению {@link Semester},
 * строго большему заданного.
 */
public class FilterGreaterThanSemesterCommandDTO implements CommandDTO {

    private static final long serialVersionUID = 1L;

    private final Semester semester;

    public FilterGreaterThanSemesterCommandDTO(Semester semester) {
        this.semester = semester;
    }

    public Semester getSemester() {
        return semester;
    }
}

