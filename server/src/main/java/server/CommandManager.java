package server;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandWithUser;
import common.dto.CommandResponseDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * Центральный реестр и диспетчер обработчиков серверных команд.
 */
public class CommandManager {

    private final Map<Class<? extends CommandDTO>, ServerCommand> commands = new HashMap<>();
    private final CollectionManager collectionManager;

    /**
     * Создает менеджер команд, связанный с менеджером коллекции.
     *
     * @param collectionManager общий менеджер коллекции
     */
    public CommandManager(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Регистрирует обработчик команды по типу DTO.
     *
     * @param type класс DTO
     * @param command обработчик команды
     */
    public void register(Class<? extends CommandDTO> type, ServerCommand command) {
        commands.put(type, command);
    }

    /**
     * Выполняет команду с явным контекстом пользователя.
     *
     * @param dto входной DTO команды
     * @param userId идентификатор авторизованного пользователя, может быть null
     * @return результат выполнения команды
     */
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

    /**
     * Выполняет команду без явного контекста пользователя.
     *
     * @param dto входной DTO команды
     * @return результат выполнения команды
     */
    public CommandResponseDTO handle(CommandDTO dto) {
        return handle(dto, null);
    }
}