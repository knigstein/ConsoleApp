package common.dto;

/**
 * Команда фильтрации элементов по подстроке имени.
 */
public class FilterContainsNameCommandDTO implements CommandDTO {

    private final String substring;

    public FilterContainsNameCommandDTO(String substring) {
        this.substring = substring;
    }

    public String getSubstring() {
        return substring;
    }
}

