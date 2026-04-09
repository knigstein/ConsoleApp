package common.dto;

/**
 * Команда удаления элемента по его идентификатору.
 */
public class RemoveByIdCommandDTO implements CommandDTO {

    private static final long serialVersionUID = 1L;

    private final Integer id;

    public RemoveByIdCommandDTO(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}

