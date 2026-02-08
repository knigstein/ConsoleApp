package command;

import collection.CollectionManager;

public class PrintFieldDescendingGroupAdminCommand implements Command {

    private final CollectionManager collectionManager;

    public PrintFieldDescendingGroupAdminCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(String[] args) {
        collectionManager.printAdminsDescending();
    }

    @Override
    public String getDescription() {
        return "вывести groupAdmin в порядке убывания";
    }
}
