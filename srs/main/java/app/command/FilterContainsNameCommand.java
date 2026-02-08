package command;

import collection.CollectionManager;

public class FilterContainsNameCommand implements Command {

    private final CollectionManager collectionManager;

    public FilterContainsNameCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(String[] args) {

        if (args.length < 2) {
            System.out.println("Не указана подстрока.");
            return;
        }

        collectionManager.filterContainsName(args[1]);
    }

    @Override
    public String getDescription() {
        return "вывести элементы, имя которых содержит подстроку";
    }
}
