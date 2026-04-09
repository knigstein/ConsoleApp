package common.dto;

import model.StudyGroup;

/**
 * Команда удаления всех элементов, меньших указанного.
 */
public class RemoveLowerCommandDTO implements CommandDTO {

    private static final long serialVersionUID = 1L;

    private final StudyGroup studyGroup;

    public RemoveLowerCommandDTO(StudyGroup studyGroup) {
        this.studyGroup = studyGroup;
    }

    public StudyGroup getStudyGroup() {
        return studyGroup;
    }
}

