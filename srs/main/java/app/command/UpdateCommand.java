package command;

import collection.CollectionManager;
import input.InputHandler;
import model.*;

import java.util.Scanner;

public class UpdateCommand implements Command, ScriptAware {

    private final CollectionManager collectionManager;
    private Scanner scanner;

    public UpdateCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void execute(String[] args) {

        if (args.length < 2) {
            System.out.println("Не указан id.");
            return;
        }

        Integer id = Integer.parseInt(args[1]);
        StudyGroup existing = collectionManager.getById(id);

        if (existing == null) {
            System.out.println("Элемент не найден.");
            return;
        }

        InputHandler input = new InputHandler(scanner, false);

        try {
            String name = input.readString("Новое имя:", false, existing.getName());

            Coordinates coordinates = new Coordinates(
                    input.readInt("Новый X:", Integer.MIN_VALUE, existing.getCoordinates().getX()),
                    (double) input.readInt("Новый Y:", 0,
                            existing.getCoordinates().getY().intValue())
            );

            Person admin = new Person(
                    input.readString("Имя админа:", false,
                            existing.getGroupAdmin().getName()),
                    input.readDate("Дата рождения:", false,
                            existing.getGroupAdmin().getBirthday()),
                    null,
                    null
            );

            StudyGroup updated = new StudyGroup(
                    id,
                    name,
                    coordinates,
                    existing.getCreationDate(),
                    existing.getStudentsCount(),
                    existing.getExpelledStudents(),
                    existing.getTransferredStudents(),
                    existing.getSemesterEnum(),
                    admin
            );

            collectionManager.update(id, updated);
            System.out.println("Элемент обновлён.");

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "обновить элемент";
    }
}