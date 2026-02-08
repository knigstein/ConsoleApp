package command;

import collection.CollectionManager;
import model.StudyGroup;
import util.StudyGroupBuilder;

import java.util.Scanner;

public class AddIfMinCommand implements Command, ScriptAware {

    private final CollectionManager collectionManager;
    private Scanner scanner;

    public AddIfMinCommand(CollectionManager collectionManager, Scanner scanner) {
        this.collectionManager = collectionManager;
        this.scanner = scanner;
    }

    @Override
    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void execute(String[] args) {

        StudyGroupBuilder builder = new StudyGroupBuilder(scanner);
        StudyGroup group = builder.build();

        boolean added = collectionManager.addIfMin(group);

        if (added) {
            System.out.println("Элемент добавлен (он меньше минимального).");
        } else {
            System.out.println("Элемент не добавлен (он не меньше минимального).");
        }
    }

    @Override
    public String getDescription() {
        return "добавить элемент, если он меньше минимального";
    }
}
