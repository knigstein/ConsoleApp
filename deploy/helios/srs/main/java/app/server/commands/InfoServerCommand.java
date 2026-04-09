package server.commands;
import io.FileManager;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.InfoCommandDTO;
import common.dto.ResponseStatus;
import server.ServerCommand;

/**
 * Серверная реализация команды info.
 */
public class InfoServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, FileManager fileManager) {
        if (!(dto instanceof InfoCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для InfoServerCommand");
        }
        String info = collectionManager.getInfo();
        return new CommandResponseDTO(ResponseStatus.SUCCESS, info, null);
    }
}

