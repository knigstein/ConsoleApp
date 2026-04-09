package common.dto;

import java.io.Serializable;

/**
 * Статусы результата выполнения команды на сервере.
 * Используются в объекте {@link CommandResponseDTO}.
 */
public enum ResponseStatus implements Serializable {
    SUCCESS,
    ERROR;

    private static final long serialVersionUID = 1L;
}

