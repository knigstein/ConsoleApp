package command;

import collection.CollectionManager;

public class ClearCommand implements Command {

    private final CollectionManager collectionManager;

    public ClearCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(String[] args) {
        collectionManager.clear();
        System.out.println("Коллекция очищена.");
    }

    @Override
    public String getDescription() {
        return "очистить коллекцию";
    }
}
