package common.dto;

import model.StudyGroup;

/**
 * Команда добавления нового элемента в коллекцию.
 */
public class AddCommandDTO implements CommandDTO {

    private static final long serialVersionUID = 1L;

    private final StudyGroup studyGroup;

    public AddCommandDTO(StudyGroup studyGroup) {
        this.studyGroup = studyGroup;
    }

    public StudyGroup getStudyGroup() {
        return studyGroup;
    }
}

