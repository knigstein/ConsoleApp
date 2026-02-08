package model;

/**
 * Класс координат.
 */
public class Coordinates {

    private int x;
    private Double y; // не null

    public Coordinates(int x, Double y) {

        if (y == null) {
            throw new IllegalArgumentException("y cannot be null!");
        }

        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
