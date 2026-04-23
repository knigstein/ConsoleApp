package server.commands;
import server.CommandExecutionContext;

import collection.CollectionManager;
import common.dto.AddIfMinCommandDTO;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.ResponseStatus;
import model.StudyGroup;
import server.ServerCommand;

import java.time.LocalDate;

public class AddIfMinServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, CommandExecutionContext context) {
        if (!context.isAuthorized()) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "Не авторизован", null);
        }

        if (!(dto instanceof AddIfMinCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для AddIfMinServerCommand");
        }

        StudyGroup fromClient = ((AddIfMinCommandDTO) dto).getStudyGroup();
        if (fromClient == null) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "Объект группы не задан", null);
        }

        StudyGroup candidate = new StudyGroup(
                null,
                fromClient.getName(),
                fromClient.getCoordinates(),
                LocalDate.now(),
                fromClient.getStudentsCount(),
                fromClient.getExpelledStudents(),
                fromClient.getTransferredStudents(),
                fromClient.getSemesterEnum(),
                fromClient.getGroupAdmin()
        );

        boolean added = collectionManager.addIfMin(candidate, context.getUserId());
        String message = added ? "Элемент добавлен как минимальный." : "Элемент не является минимальным, не добавлен.";
        return new CommandResponseDTO(ResponseStatus.SUCCESS, message, null);
    }

    @Override
    public boolean modifiesCollection() {
        return true;
    }
}