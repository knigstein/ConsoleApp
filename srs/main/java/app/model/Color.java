package model;

import java.io.Serializable;

/**
 * Перечисление возможных цветов глаз администратора учебной группы.
 * Используется в классе {@link Person} для описания цвета глаз.
 */
public enum Color implements Serializable {
    BLACK,
    BLUE,
    ORANGE,
    WHITE,
    GREEN,
    BROWN;

}