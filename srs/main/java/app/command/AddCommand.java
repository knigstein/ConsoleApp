package command;

import collection.CollectionManager;
import model.StudyGroup;
import util.StudyGroupBuilder;

import java.util.Scanner;

public class AddCommand implements Command, ScriptAware {

    private final CollectionManager collectionManager;
    private Scanner scanner;

    public AddCommand(CollectionManager collectionManager, Scanner scanner) {
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

        collectionManager.add(group);

        System.out.println("Элемент добавлен.");
    }

    @Override
    public String getDescription() {
        return "добавить новый элемент в коллекцию";
    }
}
