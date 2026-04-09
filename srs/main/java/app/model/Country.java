package model;

import java.io.Serializable;

/**
 * Перечисление стран, которые могут использоваться как национальность администратора.
 * Связано с классом {@link Person} в доменной модели приложения.
 */
public enum Country implements Serializable {
    RUSSIA,
    GERMANY,
    USA,
    SPAIN;

    private static final long serialVersionUID = 1L;
}
