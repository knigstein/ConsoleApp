package server.commands;
import server.CommandExecutionContext;

import collection.CollectionManager;
import common.dto.ClearCommandDTO;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.ResponseStatus;
import model.StudyGroup;
import server.ServerCommand;

import java.util.List;

public class ClearServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, CommandExecutionContext context) {
        if (!context.isAuthorized()) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "Не авторизован", null);
        }

        if (!(dto instanceof ClearCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для ClearServerCommand");
        }
        List<StudyGroup> removed = collectionManager.clear(context.getUserId());
        String message;
        if (removed.isEmpty()) {
            message = "У вас не было учебных групп для удаления.";
        } else {
            message = "Удалено ваших учебных групп: " + removed.size()
                + ". Полный список удалённых элементов ниже.";
        }
        return new CommandResponseDTO(ResponseStatus.SUCCESS, message, removed);
    }

    @Override
    public boolean modifiesCollection() {
        return true;
    }
}