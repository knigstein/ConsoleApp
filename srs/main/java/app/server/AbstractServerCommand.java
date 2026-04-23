package server;

import collection.CollectionManager;
import common.dto.CommandDTO;
import common.dto.CommandResponseDTO;

import java.lang.reflect.Method;

public abstract class AbstractServerCommand implements ServerCommand {

    @Override
    public CommandResponseDTO execute(CommandDTO dto, CollectionManager collectionManager, CommandExecutionContext context) {
        try {
            Method m = findMethod(collectionManager.getClass());
            if (m != null) {
                return executeReflect(dto, collectionManager, context, m);
            }
        } catch (Exception e) {
            return new CommandResponseDTO(common.dto.ResponseStatus.ERROR, "Error: " + e.getMessage(), null);
        }
        return new CommandResponseDTO(common.dto.ResponseStatus.ERROR, "Not implemented", null);
    }

    private Method findMethod(Class<?> clazz) {
        return null;
    }

    private CommandResponseDTO executeReflect(CommandDTO dto, CollectionManager cm, CommandExecutionContext ctx, Method m) {
        throw new UnsupportedOperationException("Not implemented");
    }
}