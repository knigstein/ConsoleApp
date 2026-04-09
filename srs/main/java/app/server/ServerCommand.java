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

    /**
     * Возвращает {@code true}, если команда изменяет коллекцию
     * (добавление, удаление, обновление, очистка).
     * По умолчанию — {@code false} (команда только читает данные).
     *
     * @return {@code true} если требуется автосохранение после выполнения команды
     */
    default boolean modifiesCollection() {
        return false;
    }
}

