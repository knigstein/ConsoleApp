package server.commands;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.ResponseStatus;
import common.dto.SaveCommandDTO;
import io.FileManager;
import server.ServerCommand;

/**
 * Серверная команда сохранения коллекции.
 * Вызывается автоматически после команд, изменяющих коллекцию.
 */
public class SaveServerCommand implements ServerCommand {

    private final FileManager fileManager;

    public SaveServerCommand(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, FileManager fileManager) {
        if (!(dto instanceof SaveCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для SaveServerCommand");
        }

        try {
            fileManager.save(collectionManager.getCollection());
            return new CommandResponseDTO(ResponseStatus.SUCCESS, "Коллекция сохранена на сервер.", null);
        } catch (Exception e) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "Ошибка при сохранении: " + e.getMessage(), null);
        }
    }
}
