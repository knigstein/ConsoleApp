package common.dto;

/**
 * DTO для команды выполнения скрипта.
 * Содержит путь к файлу скрипта.
 * 
 * Примечание: Эта команда обрабатывается локально на клиенте,
 * а не отправляется на сервер.
 */
public class ExecuteScriptCommandDTO implements CommandDTO {

    private static final long serialVersionUID = 1L;

    private final String fileName;

    public ExecuteScriptCommandDTO(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
