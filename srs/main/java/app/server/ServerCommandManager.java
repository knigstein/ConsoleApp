package server;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;

public class ServerCommandManager {

    private final CommandManager commandManager;

    public ServerCommandManager(CollectionManager cm) {
        this.commandManager = new CommandManager(cm);
    }

    public void register(Class<? extends CommandDTO> type, ServerCommand cmd) {
        commandManager.register(type, cmd);
    }

    public CommandResponseDTO handle(CommandDTO dto) {
        return commandManager.handle(dto);
    }
}