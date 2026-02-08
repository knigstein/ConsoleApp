package command;

import collection.CollectionManager;
import model.StudyGroup;

public class ShowCommand implements Command {

    private final CollectionManager collectionManager;

    public ShowCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(String[] args) {

        if (collectionManager.size() == 0) {
            System.out.println("Коллекция пуста.");
            return;
        }

        for (StudyGroup group : collectionManager.getCollection()) {
            System.out.println(group);
        }
    }

    @Override
    public String getDescription() {
        return "вывести все элементы коллекции";
    }
}
