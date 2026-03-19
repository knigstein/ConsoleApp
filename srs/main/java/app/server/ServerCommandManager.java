package server;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * Регистратор и диспетчер серверных команд.
 * Сопоставляет классы DTO конкретным обработчикам.
 */
public class ServerCommandManager {

    private final Map<Class<? extends CommandDTO>, ServerCommand> commands = new HashMap<>();
    private final CollectionManager collectionManager;

    public ServerCommandManager(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public void register(Class<? extends CommandDTO> type, ServerCommand command) {
        commands.put(type, command);
    }

    public CommandResponseDTO handle(CommandDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Команда не может быть null");
        }
        ServerCommand command = commands.get(dto.getClass());
        if (command == null) {
            throw new IllegalArgumentException("Неизвестный тип команды: " + dto.getClass().getName());
        }
        return command.execute(dto, collectionManager);
    }
}

