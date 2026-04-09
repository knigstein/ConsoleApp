package server.commands;
import io.FileManager;

import collection.CollectionManager;
import common.dto.AddIfMinCommandDTO;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.ResponseStatus;
import model.StudyGroup;
import server.ServerCommand;
import util.IdGenerator;

import java.time.LocalDate;

/**
 * Серверная команда add_if_min.
 */
public class AddIfMinServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, FileManager fileManager) {
        if (!(dto instanceof AddIfMinCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для AddIfMinServerCommand");
        }

        StudyGroup fromClient = ((AddIfMinCommandDTO) dto).getStudyGroup();
        if (fromClient == null) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "Объект группы не задан", null);
        }

        StudyGroup candidate = new StudyGroup(
                IdGenerator.generateId(),
                fromClient.getName(),
                fromClient.getCoordinates(),
                LocalDate.now(),
                fromClient.getStudentsCount(),
                fromClient.getExpelledStudents(),
                fromClient.getTransferredStudents(),
                fromClient.getSemesterEnum(),
                fromClient.getGroupAdmin()
        );

        boolean added = collectionManager.addIfMin(candidate);
        String message = added ? "Элемент добавлен как минимальный." : "Элемент не является минимальным, не добавлен.";
        return new CommandResponseDTO(ResponseStatus.SUCCESS, message, null);
    }
}

