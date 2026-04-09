package server.commands;
import io.FileManager;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.FilterContainsNameCommandDTO;
import common.dto.ResponseStatus;
import model.StudyGroup;
import server.ServerCommand;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Серверная команда filter_contains_name.
 */
public class FilterContainsNameServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, FileManager fileManager) {
        if (!(dto instanceof FilterContainsNameCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для FilterContainsNameServerCommand");
        }

        String substring = ((FilterContainsNameCommandDTO) dto).getSubstring();
        if (substring == null || substring.isEmpty()) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "Подстрока не должна быть пустой", null);
        }

        List<StudyGroup> result = collectionManager.getCollection()
                .stream()
                .filter(g -> g.getName() != null && g.getName().contains(substring))
                .sorted(Comparator
                        .comparing((StudyGroup g) -> g.getCoordinates().getX())
                        .thenComparing(g -> g.getCoordinates().getY()))
                .collect(Collectors.toList());

        String message = "Найдено элементов: " + result.size();
        return new CommandResponseDTO(ResponseStatus.SUCCESS, message, result);
    }
}

