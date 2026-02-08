package command;

import collection.CollectionManager;

public class InfoCommand implements Command {

    private final CollectionManager collectionManager;

    public InfoCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(String[] args) {
        System.out.println(collectionManager.getInfo());
    }

    @Override
    public String getDescription() {
        return "вывести информацию о коллекции";
    }
}
