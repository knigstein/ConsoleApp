package common.dto;

import model.StudyGroup;

/**
 * Команда полного обновления элемента по идентификатору.
 * Клиент формирует новый объект {@link StudyGroup} и отправляет его на сервер.
 */
public class UpdateCommandDTO implements CommandDTO {

    private static final long serialVersionUID = 1L;

    private final Integer id;
    private final StudyGroup updatedGroup;

    public UpdateCommandDTO(Integer id, StudyGroup updatedGroup) {
        this.id = id;
        this.updatedGroup = updatedGroup;
    }

    public Integer getId() {
        return id;
    }

    public StudyGroup getUpdatedGroup() {
        return updatedGroup;
    }
}

