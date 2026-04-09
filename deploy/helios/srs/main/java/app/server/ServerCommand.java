package server;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import io.FileManager;

/**
 * Базовый интерфейс серверной команды.
 * Каждая команда получает на вход объект {@link CommandDTO}
 * и возвращает результат в виде {@link CommandResponseDTO}.
 */
public interface ServerCommand {

    CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, FileManager fileManager);
}

