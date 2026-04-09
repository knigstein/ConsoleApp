package server;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import io.FileManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Регистратор и диспетчер серверных команд.
 * Сопоставляет классы DTO конкретным обработчикам.
 */
public class ServerCommandManager {

    private final Map<Class<? extends CommandDTO>, ServerCommand> commands = new HashMap<>();
    private final CollectionManager collectionManager;
    private final FileManager fileManager;

    public ServerCommandManager(CollectionManager collectionManager, FileManager fileManager) {
        this.collectionManager = collectionManager;
        this.fileManager = fileManager;
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
        CommandResponseDTO response = command.execute(dto, collectionManager, fileManager);

        // Автосохранение после команд, изменяющих коллекцию
        if (command.modifiesCollection()) {
            try {
                collectionManager.save(fileManager);
                ServerLog.info("Коллекция автоматически сохранена после команды: {}", dto.getClass().getSimpleName());
            } catch (Exception e) {
                ServerLog.warn("Ошибка автосохранения: {}", e.getMessage());
            }
        }

        return response;
    }
}

