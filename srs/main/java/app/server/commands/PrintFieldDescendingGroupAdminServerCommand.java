package server.commands;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.PrintFieldDescendingGroupAdminCommandDTO;
import common.dto.ResponseStatus;
import model.StudyGroup;
import server.ServerCommand;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Серверная команда print_field_descending_group_admin.
 * Возвращает имена администраторов в порядке убывания в тексте сообщения.
 */
public class PrintFieldDescendingGroupAdminServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager) {
        if (!(dto instanceof PrintFieldDescendingGroupAdminCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для PrintFieldDescendingGroupAdminServerCommand");
        }

        String body = collectionManager.getCollection()
                .stream()
                .map(StudyGroup::getGroupAdmin)
                .filter(admin -> admin != null && admin.getName() != null)
                .map(admin -> admin.getName())
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.joining(System.lineSeparator()));

        if (body.isEmpty()) {
            body = "Администраторы не найдены.";
        }

        return new CommandResponseDTO(ResponseStatus.SUCCESS, body, null);
    }
}

