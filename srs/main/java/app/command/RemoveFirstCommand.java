package command;

import collection.CollectionManager;
import model.StudyGroup;

public class RemoveFirstCommand implements Command {

    private final CollectionManager collectionManager;

    public RemoveFirstCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(String[] args) {

        StudyGroup removed = collectionManager.removeFirst();

        if (removed == null) {
            System.out.println("Коллекция пуста.");
        } else {
            System.out.println("Удалён элемент: " + removed);
        }
    }

    @Override
    public String getDescription() {
        return "удалить первый элемент коллекции";
    }
}
