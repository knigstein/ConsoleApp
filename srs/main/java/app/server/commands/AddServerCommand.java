package server.commands;
import io.FileManager;

import collection.CollectionManager;
import common.dto.AddCommandDTO;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.ResponseStatus;
import model.Coordinates;
import model.Person;
import model.Semester;
import model.StudyGroup;
import server.ServerCommand;
import util.IdGenerator;

import java.time.LocalDate;

/**
 * Серверная реализация команды add.
 * Сервер назначает собственный id и дату создания,
 * игнорируя возможные client-side значения.
 */
public class AddServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, FileManager fileManager) {
        if (!(dto instanceof AddCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для AddServerCommand");
        }

        StudyGroup fromClient = ((AddCommandDTO) dto).getStudyGroup();
        if (fromClient == null) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "Объект группы не задан", null);
        }

        Coordinates coordinates = fromClient.getCoordinates();
        Person admin = fromClient.getGroupAdmin();
        String name = fromClient.getName();
        int studentsCount = fromClient.getStudentsCount();
        Long expelled = fromClient.getExpelledStudents();
        int transferred = fromClient.getTransferredStudents();
        Semester semester = fromClient.getSemesterEnum();

        StudyGroup serverGroup = new StudyGroup(
                IdGenerator.generateId(),
                name,
                coordinates,
                LocalDate.now(),
                studentsCount,
                expelled,
                transferred,
                semester,
                admin
        );

        collectionManager.add(serverGroup);
        return new CommandResponseDTO(ResponseStatus.SUCCESS, "Группа добавлена на сервере.", null);
    }

    @Override
    public boolean modifiesCollection() {
        return true;
    }
}

