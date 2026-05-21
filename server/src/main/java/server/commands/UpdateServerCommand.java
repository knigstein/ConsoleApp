package server.commands;
import server.CommandExecutionContext;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.ResponseStatus;
import common.dto.UpdateCommandDTO;
import model.StudyGroup;
import server.ServerCommand;

import java.time.LocalDate;

public class UpdateServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, CommandExecutionContext context) {
        if (!context.isAuthorized()) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "Не авторизован", null);
        }

        if (!(dto instanceof UpdateCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для UpdateServerCommand");
        }

        UpdateCommandDTO updateDto = (UpdateCommandDTO) dto;
        Integer id = updateDto.getId();
        StudyGroup updatedGroup = updateDto.getUpdatedGroup();

        if (id == null || updatedGroup == null) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "id и объект группы не должны быть null", null);
        }

        StudyGroup existing = collectionManager.getById(id);
        if (existing == null) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "Элемент с таким id не найден.", null);
        }

        if (!context.getUserId().equals(existing.getOwnerId())) {
            return new CommandResponseDTO(ResponseStatus.ERROR, "Нельзя изменять чужие группы", null);
        }

        StudyGroup serverGroup = new StudyGroup(
                id,
                updatedGroup.getName(),
                updatedGroup.getCoordinates(),
                existing.getCreationDate() != null ? existing.getCreationDate() : LocalDate.now(),
                updatedGroup.getStudentsCount(),
                updatedGroup.getExpelledStudents(),
                updatedGroup.getTransferredStudents(),
                updatedGroup.getSemesterEnum(),
                updatedGroup.getGroupAdmin()
        );

        boolean ok = collectionManager.update(id, serverGroup, context.getUserId());
        String message = ok ? "Элемент обновлён." : "Элемент с таким id не найден.";
        return new CommandResponseDTO(ok ? ResponseStatus.SUCCESS : ResponseStatus.ERROR, message, null);
    }

    @Override
    public boolean modifiesCollection() {
        return true;
    }
}