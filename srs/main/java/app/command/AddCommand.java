package command;

import collection.CollectionManager;
import input.InputHandler;
import model.*;
import util.IdGenerator;
import java.time.LocalDate;
import java.util.Date;
import java.util.Scanner;

public class AddCommand implements Command, ScriptAware {

    private final CollectionManager collectionManager;
    private Scanner scanner;

    public AddCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void execute(String[] args) {

        InputHandler input = new InputHandler(scanner, false);

        try {
            String name = input.readString("Введите название группы:", false, null);
            int x = input.readInt("Введите X:", Integer.MIN_VALUE, null);
            int yInt = input.readInt("Введите Y (>0):", 0, null);

            Coordinates coordinates = new Coordinates(x, (double) yInt);

            int studentsCount = input.readInt("Введите колличество студентов:", 0, null);
            Long expelledStudents = (long) input.readInt("Введите колличество отчисленных:", -1, 0);
            int transferredStudents = input.readInt("Введите колличество переведённых:", 0, null);

            Semester semester = input.readEnum("Введите семестр:",
                    Semester.class, true, null);

            String adminName = input.readString("Имя админа:", false, null);
            Date birthday = input.readDate("Дата рождения:", false, null);

            Person admin = new Person(adminName, birthday, null, null);

            StudyGroup group = new StudyGroup(
                    IdGenerator.generateId(),
                    name,
                    coordinates,
                    LocalDate.now(),
                    studentsCount,
                    expelledStudents,
                    transferredStudents,
                    semester,
                    admin
            );

            collectionManager.add(group);
            System.out.println("Группа добавлена.");

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "добавить элемент";
    }
}