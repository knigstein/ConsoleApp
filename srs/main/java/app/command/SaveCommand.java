package command;

import collection.CollectionManager;
import io.FileManager;

public class SaveCommand implements Command {

    private final CollectionManager collectionManager;
    private final FileManager fileManager;

    public SaveCommand(CollectionManager collectionManager,
                       FileManager fileManager) {
        this.collectionManager = collectionManager;
        this.fileManager = fileManager;
    }

    @Override
    public void execute(String[] args) {
        try {
            fileManager.save(collectionManager.getCollection());
            System.out.println("Коллекция сохранена.");
        } catch (Exception e) {
            System.out.println("Ошибка при сохранении файла.");
        }
    }

    @Override
    public String getDescription() {
        return "сохранить коллекцию в файл";
    }
}
