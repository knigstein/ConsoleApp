package server.commands;
import io.FileManager;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.RemoveLowerCommandDTO;
import common.dto.ResponseStatus;
import model.StudyGroup;
import server.ServerCommand;

/**
 * Серверная команда remove_lower.
 */
public class RemoveLowerServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, FileManager fileManager) {
        if (!(dto instanceof RemoveLowerCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для RemoveLowerServerCommand");
        }

        StudyGroup base = ((RemoveLowerCommandDTO) dto).getStudyGroup();
        if (base == null) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "Базовый объект не задан", null);
        }

        int removed = collectionManager.removeLower(base);
        String message = "Удалено элементов: " + removed;
        return new CommandResponseDTO(ResponseStatus.SUCCESS, message, null);
    }
}

