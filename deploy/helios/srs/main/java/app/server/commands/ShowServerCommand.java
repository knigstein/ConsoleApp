package server.commands;
import io.FileManager;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.ResponseStatus;
import common.dto.ShowCommandDTO;
import model.StudyGroup;
import server.ServerCommand;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Серверная реализация команды show.
 * Возвращает коллекцию учебных групп, отсортированную по местоположению
 * (сравнение по координатам).
 */
public class ShowServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, FileManager fileManager) {
        if (!(dto instanceof ShowCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для ShowServerCommand");
        }

        List<StudyGroup> sorted = collectionManager.getCollection()
                .stream()
                .sorted(Comparator
                        .comparing((StudyGroup g) -> g.getCoordinates().getX())
                        .thenComparing(g -> g.getCoordinates().getY()))
                .collect(Collectors.toList());

        String message = "Элементов в коллекции: " + sorted.size();

        return new CommandResponseDTO(ResponseStatus.SUCCESS, message, sorted);
    }
}

