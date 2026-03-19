package server.commands;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.FilterGreaterThanSemesterCommandDTO;
import common.dto.ResponseStatus;
import model.Semester;
import model.StudyGroup;
import server.ServerCommand;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Серверная команда filter_greater_than_semester_enum.
 */
public class FilterGreaterThanSemesterServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager) {
        if (!(dto instanceof FilterGreaterThanSemesterCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для FilterGreaterThanSemesterServerCommand");
        }

        Semester semester = ((FilterGreaterThanSemesterCommandDTO) dto).getSemester();
        if (semester == null) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "Семестр не задан", null);
        }

        List<StudyGroup> result = collectionManager.getCollection()
                .stream()
                .filter(g -> g.getSemesterEnum() != null && g.getSemesterEnum().compareTo(semester) > 0)
                .sorted(Comparator
                        .comparing((StudyGroup g) -> g.getCoordinates().getX())
                        .thenComparing(g -> g.getCoordinates().getY()))
                .collect(Collectors.toList());

        String message = "Найдено элементов: " + result.size();
        return new CommandResponseDTO(ResponseStatus.SUCCESS, message, result);
    }
}

