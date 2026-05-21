package common.dto;

import model.StudyGroup;

import java.io.Serializable;
import java.util.List;

/**
 * Универсальный ответ сервера на выполнение команды.
 * Содержит статус, человеко-читаемое сообщение и,
 * при необходимости, коллекцию учебных групп.
 */
public class CommandResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ResponseStatus status;
    private final String message;
    private final List<StudyGroup> collection;
    private final Integer userId;

    public CommandResponseDTO(ResponseStatus status, String message, List<StudyGroup> collection) {
        this(status, message, collection, null);
    }

    public CommandResponseDTO(ResponseStatus status, String message, List<StudyGroup> collection, Integer userId) {
        this.status = status;
        this.message = message;
        this.collection = collection;
        this.userId = userId;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Коллекция учебных групп, возвращаемая сервером.
     * Гарантируется, что при наличии она уже отсортирована на стороне сервера
     * по «местоположению» (координатам).
     */
    public List<StudyGroup> getCollection() {
        return collection;
    }

    public Integer getUserId() {
        return userId;
    }
}

