package server.commands;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.RemoveByIdCommandDTO;
import common.dto.ResponseStatus;
import server.ServerCommand;

/**
 * Серверная команда remove_by_id.
 */
public class RemoveByIdServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager) {
        if (!(dto instanceof RemoveByIdCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для RemoveByIdServerCommand");
        }

        Integer id = ((RemoveByIdCommandDTO) dto).getId();
        if (id == null) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "id не должен быть null", null);
        }

        boolean removed = collectionManager.removeById(id);
        String message = removed ? "Элемент удалён." : "Элемент с таким id не найден.";
        return new CommandResponseDTO(ResponseStatus.SUCCESS, message, null);
    }
}

