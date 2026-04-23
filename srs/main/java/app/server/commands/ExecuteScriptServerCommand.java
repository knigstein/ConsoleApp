package server.commands;
import server.CommandExecutionContext;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.ExecuteScriptCommandDTO;
import common.dto.ResponseStatus;
import server.ServerCommand;

public class ExecuteScriptServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, CommandExecutionContext context) {
        return new CommandResponseDTO(
                ResponseStatus.ERROR,
                "Команда execute_script должна выполняться на стороне клиента, а не сервера.",
                null
        );
    }
}