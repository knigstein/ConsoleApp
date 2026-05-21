package server.commands;
import server.CommandExecutionContext;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.RemoveLowerCommandDTO;
import common.dto.ResponseStatus;
import model.StudyGroup;
import server.ServerCommand;

public class RemoveLowerServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, CommandExecutionContext context) {
        if (!context.isAuthorized()) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "Не авторизован", null);
        }

        if (!(dto instanceof RemoveLowerCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для RemoveLowerServerCommand");
        }

        StudyGroup base = ((RemoveLowerCommandDTO) dto).getStudyGroup();
        if (base == null) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "Базовый объект не задан", null);
        }

        int removed = collectionManager.removeLower(base, context.getUserId());
        String message = "Удалено элементов: " + removed;
        return new CommandResponseDTO(ResponseStatus.SUCCESS, message, null);
    }

    @Override
    public boolean modifiesCollection() {
        return true;
    }
}