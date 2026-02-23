package util;

import java.time.LocalDate;
import java.util.Date;
import java.util.Scanner;

import model.Color;
import model.Coordinates;
import model.Country;
import model.Person;
import model.Semester;
import model.StudyGroup;

/**
 * Класс для пошагового создания объектов {@link StudyGroup} через консольный ввод.
 * Запрашивает у пользователя значения всех полей учебной группы и связанных сущностей
 * (координаты, администратор, семестр и т.д.), выполняя базовую валидацию входных данных.
 *
 * Используется командами, реализующими добавление и изменение элементов коллекции.
 */
public class StudyGroupBuilder {

    private final Scanner scanner;

    /**
     * Создаёт новый билдер для построения объектов {@link StudyGroup}.
     *
     * @param scanner объект {@link Scanner}, используемый для чтения пользовательского ввода
     */
    public StudyGroupBuilder(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Пошагово запрашивает у пользователя данные и создаёт новый объект {@link StudyGroup}.
     *
     * @return сконструированный объект {@link StudyGroup} с валидными полями
     */
    public StudyGroup build() {

        String name = readName();
        Coordinates coordinates = readCoordinates();
        int studentsCount = readStudentsCount();
        Long expelledStudents = readExpelledStudents();
        int transferredStudents = readTransferredStudents();
        Semester semester = readSemester();
        Person admin = readPerson();

        return new StudyGroup(IdGenerator.generateId(), name, coordinates, LocalDate.now(), studentsCount, expelledStudents, transferredStudents, semester, admin);
    }

    /**
     * Запрашивает у пользователя название учебной группы до тех пор,
     * пока не будет введена непустая строка.
     *
     * @return корректное название группы
     */
    private String readName() {
        while (true) {
            System.out.println("Enter group name: ");
            String input = scanner.nextLine();

            if (input == null || input.trim().isEmpty()) {
                System.out.println("Name of group cannot be null");

            } else {
                return input.trim();
            }
        }
    }

    /**
     * Запрашивает у пользователя количество студентов в группе.
     * Проверяет, что введено целое число больше нуля.
     *
     * @return количество студентов в группе
     */
    private int readStudentsCount() {
        while (true) {
            
            try {
                System.out.println("Введите количество студентов (>0)");
                int value = Integer.parseInt(scanner.nextLine());

                if (value <= 0) {
                    throw new IllegalArgumentException("Количество студентов должно быть >0");
                }

                return value;

            } catch (Exception e) {
                System.out.println("Ошибка ввода");
            }
        }
    }

    /**
     * Запрашивает у пользователя количество отчисленных студентов.
     * Разрешает пустой ввод (в этом случае возвращается {@code null}).
     * При наличии значения проверяет, что оно больше нуля.
     *
     * @return количество отчисленных студентов или {@code null}, если значение не задано
     */
    private Long readExpelledStudents() {
        while (true) {
            try {
                System.out.print("Введите количество отчисленных (>0, пусто если null): ");
                String input = scanner.nextLine();

                if (input.trim().isEmpty()) {
                    return null;
                }

                Long value = Long.parseLong(input);

                if (value <= 0) {
                    throw new IllegalArgumentException();
                }

                return value;

            } catch (Exception e) {
                System.out.println("Ошибка ввода. Повторите.");
            }
        }
    }

    /**
     * Запрашивает у пользователя значение семестра из перечисления {@link Semester}.
     * Разрешает пустой ввод (в этом случае возвращается {@code null}).
     *
     * @return выбранный семестр или {@code null}, если значение не задано
     */
    private Semester readSemester() {
        while (true) {
            try {
                System.out.println("Доступные семестры:");
                for (Semester s : Semester.values()) {
                    System.out.println("- " + s);
                }

                System.out.print("Введите семестр (пусто если null): ");
                String input = scanner.nextLine();

                if (input.trim().isEmpty()) {
                    return null;
                }

                return Semester.valueOf(input.trim());

            } catch (Exception e) {
                System.out.println("Ошибка ввода. Повторите.");
            }
        }
    }

    /**
     * Запрашивает у пользователя координаты X и Y.
     * Проверяет корректность числового ввода и создаёт объект {@link Coordinates}.
     *
     * @return объект координат с введёнными значениями
     */
    private Coordinates readCoordinates() {
        while (true) {
            try {
                System.out.print("Введите x: ");
                int x = Integer.parseInt(scanner.nextLine());

                System.out.print("Введите y (не null): ");
                Double y = Double.parseDouble(scanner.nextLine());

                return new Coordinates(x, y);

            } catch (Exception e) {
                System.out.println("Ошибка ввода.");
            }
        }
    }

    /**
     * Запрашивает у пользователя количество переведённых студентов.
     * Проверяет, что введено целое число больше нуля.
     *
     * @return количество переведённых студентов
     */
    private int readTransferredStudents() {
        while (true) {
            
            try {
                System.out.println("Введите количество переведенных студентов");
                int value = Integer.parseInt(scanner.nextLine());

                if (value <= 0) {
                    throw new IllegalArgumentException();
                }

                return value;

            } catch (Exception e) {
                System.out.println("Ошибка ввода");
            }
        }        
    }

    /**
     * Запрашивает у пользователя данные администратора группы:
     * имя, дату рождения (в миллисекундах), цвет глаз и национальность.
     *
     * @return объект {@link Person} с введёнными пользователем данными
     */
    private Person readPerson() {

        String name;
        while (true) {
            System.out.print("Введите имя администратора: ");
            name = scanner.nextLine();

            if (name.trim().isEmpty()) {
                System.out.println("Это поле не может быть пустым.");
            } else break;
        }

        Date birthday;
        while (true) {
            try {
                System.out.print("Введите дату рождения (Формат: 13062007): ");
                long millis = Long.parseLong(scanner.nextLine());
                birthday = new Date(millis);
                break;
            } catch (Exception e) {
                System.out.println("Ошибка ввода");
            }
        }

        Color eyeColor = null;
        System.out.println("Доступные цвета глаз:");
        for (Color c : Color.values()) {
            System.out.println("- " + c);
        }
        System.out.print("Введите цвет: ");
        String eye = scanner.nextLine();
        if (!eye.trim().isEmpty()) {
            eyeColor = Color.valueOf(eye.trim());
        }

        Country nationality = null;
        System.out.println("Доступные страны: ");
        for (Country c : Country.values()) {
            System.out.println("- " + c);
        }
        System.out.print("Введите страну: ");
        String nat = scanner.nextLine();
        if (!nat.trim().isEmpty()) {
            nationality = Country.valueOf(nat.trim());
        }

        return new Person(name, birthday, eyeColor, nationality);
    }
}
