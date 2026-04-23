package server.commands;
import server.CommandExecutionContext;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.RemoveByIdCommandDTO;
import common.dto.ResponseStatus;
import server.ServerCommand;

public class RemoveByIdServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, CommandExecutionContext context) {
        if (!context.isAuthorized()) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "Не авторизован", null);
        }

        if (!(dto instanceof RemoveByIdCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для RemoveByIdServerCommand");
        }

        Integer id = ((RemoveByIdCommandDTO) dto).getId();
        if (id == null) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "id не должен быть null", null);
        }

        boolean removed = collectionManager.removeById(id, context.getUserId());
        String message = removed ? "Элемент удалён." : "Элемент с таким id не найден.";
        return new CommandResponseDTO(removed ? ResponseStatus.SUCCESS : ResponseStatus.ERROR, message, null);
    }

    @Override
    public boolean modifiesCollection() {
        return true;
    }
}