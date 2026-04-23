package server;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;

public interface ServerCommand {

    CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, CommandExecutionContext context);

    default boolean modifiesCollection() {
        return false;
    }
}