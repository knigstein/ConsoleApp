package common.dto;

import model.StudyGroup;

/**
 * Команда добавления элемента, если он меньше минимального.
 */
public class AddIfMinCommandDTO implements CommandDTO {

    private final StudyGroup studyGroup;

    public AddIfMinCommandDTO(StudyGroup studyGroup) {
        this.studyGroup = studyGroup;
    }

    public StudyGroup getStudyGroup() {
        return studyGroup;
    }
}

