package common.dto;

/**
 * Команда фильтрации элементов по подстроке имени.
 */
public class FilterContainsNameCommandDTO implements CommandDTO {

    private static final long serialVersionUID = 1L;

    private final String substring;

    public FilterContainsNameCommandDTO(String substring) {
        this.substring = substring;
    }

    public String getSubstring() {
        return substring;
    }
}

