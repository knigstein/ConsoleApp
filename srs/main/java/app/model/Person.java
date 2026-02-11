package model;

import java.util.Date;

/**
 * Класс, представляющий администратора учебной группы.
 * Содержит основные персональные данные администратора, включая имя, дату рождения,
 * цвет глаз и национальность.
 *
 * Класс используется как часть доменной модели приложения для управления коллекцией
 * объектов {@link StudyGroup}.
 */
public class Person {
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Date birthday; //Поле не может быть null
    private Color eyeColor; //Поле может быть null
    private Country nationality; //Поле может быть null

    /**
     * Создаёт новый объект администратора группы с указанными параметрами.
     *
     * @param name имя администратора, не может быть {@code null} и не может быть пустой строкой
     * @param birthday дата рождения администратора, не может быть {@code null}
     * @param eyeColor цвет глаз администратора, может быть {@code null}, если не задан
     * @param nationality национальность администратора, может быть {@code null}, если не задана
     * @throws IllegalArgumentException если нарушены ограничения на поля (имя или дата рождения некорректны)
     */
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

    /**
     * Возвращает имя администратора группы.
     *
     * @return имя администратора, никогда не бывает {@code null} или пустым
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает дату рождения администратора.
     *
     * @return дата рождения администратора, не бывает {@code null}
     */
    public Date getBirthday() {
        return birthday;
    }

    /**
     * Возвращает цвет глаз администратора.
     *
     * @return цвет глаз администратора или {@code null}, если не задан
     */
    public Color getEyeColor() {
        return eyeColor;
    }

    /**
     * Возвращает национальность администратора.
     *
     * @return национальность администратора или {@code null}, если не задана
     */
    public Country getNationality() {
        return nationality;
    }
}
