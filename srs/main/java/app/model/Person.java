package model;

import java.util.Date;

/**
 * Класс, представляющий человека (администратора группы).
 */

public class Person {
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Date birthday; //Поле не может быть null
    private Color eyeColor; //Поле может быть null
    private Country nationality; //Поле может быть null

    public Person(String name, java.util.Date birthday, Color eyeColor, Country nationality) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }

        if (birthday == null) {
            throw new IllegalArgumentException("birthday cannot be null");
        }

        this.name = name;
        this.birthday = birthday;
        this.eyeColor = eyeColor;
        this.nationality = nationality;

    }

    public String getName() {
        return name;
    }

    public Date getBirthday() {
        return birthday;
    }

    public Color getEyeColor() {
        return eyeColor;
    }

    public Country getNationality() {
        return nationality;
    }
}
