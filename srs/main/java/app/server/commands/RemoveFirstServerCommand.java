package server.commands;
import io.FileManager;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.RemoveFirstCommandDTO;
import common.dto.ResponseStatus;
import model.StudyGroup;
import server.ServerCommand;

/**
 * Серверная команда remove_first.
 */
public class RemoveFirstServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, FileManager fileManager) {
        if (!(dto instanceof RemoveFirstCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для RemoveFirstServerCommand");
        }

        StudyGroup removed = collectionManager.removeFirst();
        String message = removed == null ? "Коллекция пуста." : "Первый элемент удалён.";
        return new CommandResponseDTO(ResponseStatus.SUCCESS, message, null);
    }

    @Override
    public boolean modifiesCollection() {
        return true;
    }
}

