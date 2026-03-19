package common.dto;

/**
 * Команда удаления элемента по его идентификатору.
 */
public class RemoveByIdCommandDTO implements CommandDTO {

    private final Integer id;

    public RemoveByIdCommandDTO(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}

