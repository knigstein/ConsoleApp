package server.commands;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.ExecuteScriptCommandDTO;
import common.dto.ResponseStatus;
import server.ServerCommand;

/**
 * Серверная заглушка для команды execute_script.
 * 
 * Эта команда должна обрабатываться только на стороне клиента.
 * Если сервер получил такую команду, возвращается ошибка.
 */
public class ExecuteScriptServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager) {
        return new CommandResponseDTO(
                ResponseStatus.ERROR,
                "Команда execute_script должна выполняться на стороне клиента, а не сервера.",
                null
        );
    }
}
