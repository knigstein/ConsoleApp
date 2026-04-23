package server.commands;
import server.CommandExecutionContext;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.ResponseStatus;
import common.dto.SaveCommandDTO;
import server.ServerCommand;

public class SaveServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, CommandExecutionContext context) {
        return new CommandResponseDTO(
                ResponseStatus.ERROR,
                "Команда save не требуется при использовании базы данных",
                null
        );
    }
}