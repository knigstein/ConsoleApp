package model;

/**
 * Класс координат.
 * Описывает двумерные координаты с целочисленным значением по оси X
 * и вещественным значением по оси Y.
 *
 * Используется как составная часть доменной модели учебной группы.
 */
public class Coordinates {

    private int x;
    private Double y; // не null

    /**
     * Создаёт объект координат с указанными значениями.
     *
     * @param x значение координаты по оси X
     * @param y значение координаты по оси Y, не может быть {@code null}
     * @throws IllegalArgumentException если {@code y} равен {@code null}
     */
    public Coordinates(int x, Double y) {

        if (y == null) {
            throw new IllegalArgumentException("y cannot be null!");
        }

        this.x = x;
        this.y = y;
    }

    /**
     * Возвращает значение координаты по оси X.
     *
     * @return целочисленное значение X
     */
    public int getX() {
        return x;
    }

    /**
     * Возвращает значение координаты по оси Y.
     *
     * @return значение Y, не бывает {@code null}
     */
    public Double getY() {
        return y;
    }

    /**
     * Возвращает строковое представление координат в формате
     * {@code Coordinates{x=..., y=...}}.
     *
     * @return строковое представление объекта {@code Coordinates}
     */
    @Override
    public String toString() {
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
