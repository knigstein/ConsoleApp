package server;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandWithUser;
import common.dto.CommandResponseDTO;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {

    private final Map<Class<? extends CommandDTO>, ServerCommand> commands = new HashMap<>();
    private final CollectionManager collectionManager;

    public CommandManager(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public void register(Class<? extends CommandDTO> type, ServerCommand command) {
        commands.put(type, command);
    }

    public CommandResponseDTO handle(CommandDTO dto, Integer userId) {
        if (dto == null) {
            throw new IllegalArgumentException("Команда не может быть null");
        }

        ServerCommand command = commands.get(dto.getClass());
        if (command == null) {
            throw new IllegalArgumentException("Неизвестный тип команды: " + dto.getClass().getName());
        }

        CommandExecutionContext context = new CommandExecutionContext(userId);
        return command.execute(dto, collectionManager, context);
    }

    public CommandResponseDTO handle(CommandDTO dto) {
        return handle(dto, null);
    }
}