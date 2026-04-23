package server.commands;

import collection.CollectionManager;
import common.dto.AddCommandDTO;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.ResponseStatus;
import model.Coordinates;
import model.Person;
import model.Semester;
import model.StudyGroup;
import server.CommandExecutionContext;
import server.ServerCommand;

import java.time.LocalDate;

public class AddServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, CommandExecutionContext context) {
        if (dto instanceof AddCommandDTO) {
            AddCommandDTO addDto = (AddCommandDTO) dto;
            StudyGroup fromClient = addDto.getStudyGroup();

            if (fromClient == null) {
                return new CommandResponseDTO(ResponseStatus.ERROR, "Объект группы не задан", null);
            }

            if (!context.isAuthorized()) {
                return new CommandResponseDTO(ResponseStatus.ERROR, "Требуется авторизация", null);
            }

            if (fromClient.getName() == null || fromClient.getName().trim().isEmpty()) {
                return new CommandResponseDTO(ResponseStatus.ERROR, "Название группы не может быть пустым", null);
            }

            if (fromClient.getStudentsCount() <= 0) {
                return new CommandResponseDTO(ResponseStatus.ERROR, "Количество студентов должно быть > 0", null);
            }

            if (fromClient.getTransferredStudents() <= 0) {
                return new CommandResponseDTO(ResponseStatus.ERROR, "Количество переведённых студентов должно быть > 0", null);
            }

            Coordinates coordinates = fromClient.getCoordinates();
            Person admin = fromClient.getGroupAdmin();

            if (coordinates == null) {
                return new CommandResponseDTO(ResponseStatus.ERROR, "Координаты не могут быть null", null);
            }

            if (admin == null) {
                return new CommandResponseDTO(ResponseStatus.ERROR, "Администратор группы не может быть null", null);
            }

            StudyGroup group = new StudyGroup(
                    null,
                    fromClient.getName(),
                    coordinates,
                    LocalDate.now(),
                    fromClient.getStudentsCount(),
                    fromClient.getExpelledStudents(),
                    fromClient.getTransferredStudents(),
                    fromClient.getSemesterEnum(),
                    admin
            );

            collectionManager.add(group, context.getUserId());
            return new CommandResponseDTO(ResponseStatus.SUCCESS, "Группа добавлена", null);
        }

        return new CommandResponseDTO(ResponseStatus.ERROR, "Неверный тип команды", null);
    }

    @Override
    public boolean modifiesCollection() {
        return true;
    }
}