package server.commands;
import server.CommandExecutionContext;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.FilterContainsNameCommandDTO;
import common.dto.ResponseStatus;
import server.ServerCommand;

import java.util.List;

public class FilterContainsNameServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, CommandExecutionContext context) {
        if (!(dto instanceof FilterContainsNameCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для FilterContainsNameServerCommand");
        }

        String substring = ((FilterContainsNameCommandDTO) dto).getSubstring();
        if (substring == null || substring.isEmpty()) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "Подстрока не должна быть пустой", null);
        }

        var result = collectionManager.filterContainsName(substring);

        String message = "Найдено элементов: " + result.size();
        return new CommandResponseDTO(ResponseStatus.SUCCESS, message, (List<model.StudyGroup>) (List<?>) result);
    }
}