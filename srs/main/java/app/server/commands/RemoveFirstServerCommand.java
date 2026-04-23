package server.commands;
import server.CommandExecutionContext;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.RemoveFirstCommandDTO;
import common.dto.ResponseStatus;
import model.StudyGroup;
import server.ServerCommand;

public class RemoveFirstServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, CommandExecutionContext context) {
        if (!context.isAuthorized()) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "Не авторизован", null);
        }

        if (!(dto instanceof RemoveFirstCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для RemoveFirstServerCommand");
        }

        StudyGroup removed = collectionManager.removeFirst(context.getUserId()).orElse(null);
        String message = removed == null ? "Коллекция пуста." : "Первый элемент удалён.";
        return new CommandResponseDTO(ResponseStatus.SUCCESS, message, null);
    }

    @Override
    public boolean modifiesCollection() {
        return true;
    }
}