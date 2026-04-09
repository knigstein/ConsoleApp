package server.commands;
import io.FileManager;

import collection.CollectionManager;
import common.dto.ClearCommandDTO;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.ResponseStatus;
import server.ServerCommand;

/**
 * Серверная команда clear.
 */
public class ClearServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, FileManager fileManager) {
        if (!(dto instanceof ClearCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для ClearServerCommand");
        }
        collectionManager.clear();
        return new CommandResponseDTO(ResponseStatus.SUCCESS, "Коллекция очищена.", null);
    }
}

