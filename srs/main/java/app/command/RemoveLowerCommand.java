package command;

import collection.CollectionManager;
import model.StudyGroup;
import util.StudyGroupBuilder;

import java.util.Scanner;

public class RemoveLowerCommand implements Command, ScriptAware {

    private final CollectionManager collectionManager;
    private Scanner scanner;

    public RemoveLowerCommand(CollectionManager collectionManager,
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

        StudyGroupBuilder builder = new StudyGroupBuilder(scanner);
        StudyGroup group = builder.build();

        int removed = collectionManager.removeLower(group);

        System.out.println("Удалено элементов: " + removed);
    }

    @Override
    public String getDescription() {
        return "удалить элементы меньше заданного";
    }
}
