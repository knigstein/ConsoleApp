package model;

import java.io.Serializable;

/**
 * Перечисление учебных семестров.
 * Используется в классе {@link StudyGroup} для указания текущего семестра обучения группы.
 */
public enum Semester implements Serializable {
    FIRST,
    SECOND,
    THIRD,
    FOURTH,
    FIFTH,
    SIXTH,
    SEVENTH,
    EIGHTH;

}
