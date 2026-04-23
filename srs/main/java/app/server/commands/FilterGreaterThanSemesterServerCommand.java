package server.commands;
import server.CommandExecutionContext;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.FilterGreaterThanSemesterCommandDTO;
import common.dto.ResponseStatus;
import model.Semester;
import server.ServerCommand;

import java.util.List;

public class FilterGreaterThanSemesterServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, CommandExecutionContext context) {
        if (!(dto instanceof FilterGreaterThanSemesterCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для FilterGreaterThanSemesterServerCommand");
        }

        Semester semester = ((FilterGreaterThanSemesterCommandDTO) dto).getSemester();
        if (semester == null) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "Семестр не задан", null);
        }

        var result = collectionManager.filterGreaterThanSemester(semester);

        String message = "Найдено элементов: " + result.size();
        return new CommandResponseDTO(ResponseStatus.SUCCESS, message, (java.util.List<model.StudyGroup>)(java.util.List<?>)result);
    }
}