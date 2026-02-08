package command;

import collection.CollectionManager;

public class RemoveByIdCommand implements Command {

    private final CollectionManager collectionManager;

    public RemoveByIdCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(String[] args) {

        if (args.length < 2) {
            System.out.println("Не указан id.");
            return;
        }

        try {
            Integer id = Integer.parseInt(args[1]);

            boolean removed = collectionManager.removeById(id);

            if (removed) {
                System.out.println("Элемент удалён.");
            } else {
                System.out.println("Элемент с таким id не найден.");
            }

        } catch (NumberFormatException e) {
            System.out.println("id должен быть числом.");
        }
    }

    @Override
    public String getDescription() {
        return "удалить элемент по id";
    }
}
