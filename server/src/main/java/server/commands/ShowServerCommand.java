package server.commands;
import server.CommandExecutionContext;

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

public class ShowServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, CommandExecutionContext context) {
        if (!(dto instanceof ShowCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для ShowServerCommand");
        }

        List<StudyGroup> sorted = collectionManager.getCollection()
                .stream()
                .sorted(Comparator
                        .comparing(
                                (StudyGroup g) -> g.getCoordinates() == null ? null : g.getCoordinates().getX(),
                                Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(
                                g -> g.getCoordinates() == null ? null : g.getCoordinates().getY(),
                                Comparator.nullsLast(Double::compareTo)))
                .collect(Collectors.toList());

        String message = "Элементов в коллекции: " + sorted.size();

        return new CommandResponseDTO(ResponseStatus.SUCCESS, message, sorted);
    }
}