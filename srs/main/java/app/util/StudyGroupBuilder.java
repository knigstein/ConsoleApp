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
 * Класс для пошагового создания StudyGroup через консоль.
 */
public class StudyGroupBuilder {

    private final Scanner scanner;

    public StudyGroupBuilder(Scanner scanner) {
        this.scanner = scanner;
    }

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

    private int readStudentsCount() {
        while (true) {
            
            try {
                System.out.println("Enter quantity of students");
                int value = Integer.parseInt(scanner.nextLine());

                if (value <= 0) {
                    throw new IllegalArgumentException();
                }

                return value;

            } catch (Exception e) {
                System.out.println("Enter error");
            }
        }
    }

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
                System.out.println("Некорректное значение. Повторите.");
            }
        }
    }

    private Coordinates readCoordinates() {
        while (true) {
            try {
                System.out.print("Введите x: ");
                int x = Integer.parseInt(scanner.nextLine());

                System.out.print("Введите y (не null): ");
                Double y = Double.parseDouble(scanner.nextLine());

                return new Coordinates(x, y);

            } catch (Exception e) {
                System.out.println("Ошибка ввода координат.");
            }
        }
    }

    private int readTransferredStudents() {
        while (true) {
            
            try {
                System.out.println("Enter quantity of transferred students");
                int value = Integer.parseInt(scanner.nextLine());

                if (value <= 0) {
                    throw new IllegalArgumentException();
                }

                return value;

            } catch (Exception e) {
                System.out.println("Enter error");
            }
        }        
    }

    private Person readPerson() {

        String name;
        while (true) {
            System.out.print("Введите имя администратора: ");
            name = scanner.nextLine();

            if (name.trim().isEmpty()) {
                System.out.println("Имя не может быть пустым.");
            } else break;
        }

        Date birthday;
        while (true) {
            try {
                System.out.print("Введите дату рождения (миллисекунды): ");
                long millis = Long.parseLong(scanner.nextLine());
                birthday = new Date(millis);
                break;
            } catch (Exception e) {
                System.out.println("Ошибка ввода даты.");
            }
        }

        Color eyeColor = null;
        System.out.println("Доступные цвета глаз:");
        for (Color c : Color.values()) {
            System.out.println("- " + c);
        }
        System.out.print("Введите цвет (пусто если null): ");
        String eye = scanner.nextLine();
        if (!eye.trim().isEmpty()) {
            eyeColor = Color.valueOf(eye.trim());
        }

        Country nationality = null;
        System.out.println("Доступные страны:");
        for (Country c : Country.values()) {
            System.out.println("- " + c);
        }
        System.out.print("Введите страну (пусто если null): ");
        String nat = scanner.nextLine();
        if (!nat.trim().isEmpty()) {
            nationality = Country.valueOf(nat.trim());
        }

        return new Person(name, birthday, eyeColor, nationality);
    }

}
