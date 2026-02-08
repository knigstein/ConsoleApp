package command;

import collection.CollectionManager;
import model.StudyGroup;
import util.StudyGroupBuilder;

import java.util.Scanner;

public class UpdateCommand implements Command, ScriptAware {

    private final CollectionManager collectionManager;
    private Scanner scanner;

    public UpdateCommand(CollectionManager collectionManager,
                         Scanner scanner) {
        this.collectionManager = collectionManager;
        this.scanner = scanner;
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

        try {
            Integer id = Integer.parseInt(args[1]);

            StudyGroupBuilder builder = new StudyGroupBuilder(scanner);
            StudyGroup newGroup = builder.build();

            boolean updated = collectionManager.updateById(id, newGroup);

            if (updated) {
                System.out.println("Элемент обновлён.");
            } else {
                System.out.println("Элемент с таким id не найден.");
            }

        } catch (NumberFormatException e) {
            System.out.println("id должен быть числом.");
        }
    }

    @Override
    public String getDescription() {
        return "обновить элемент по id";
    }
}
