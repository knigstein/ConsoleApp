package server.commands;
import server.CommandExecutionContext;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;
import common.dto.PrintFieldDescendingGroupAdminCommandDTO;
import common.dto.ResponseStatus;
import server.ServerCommand;

import java.util.List;

public class PrintFieldDescendingGroupAdminServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, CommandExecutionContext context) {
        if (!(dto instanceof PrintFieldDescendingGroupAdminCommandDTO)) {
            throw new IllegalArgumentException("Некорректный тип DTO для PrintFieldDescendingGroupAdminServerCommand");
        }

        List<String> admins = collectionManager.printAdminsDescending();

        String body = admins.isEmpty() ? "Администраторы не найдены." : String.join(System.lineSeparator(), admins);

        return new CommandResponseDTO(ResponseStatus.SUCCESS, body, null);
    }
}