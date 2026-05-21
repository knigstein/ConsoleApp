package common.dto;

/**
 * Обертка над командой, добавляющая учетные данные пользователя.
 *
 * <p>Используется клиентом для передачи логина и пароля вместе с каждой
 * командой, требующей идентификации на сервере.</p>
 */
public class CommandWithUser implements CommandDTO {

    private static final long serialVersionUID = 1L;

    private final CommandDTO originalCommand;
    private final String login;
    private final String password;

    /**
     * Создает обертку над исходной командой с данными пользователя.
     *
     * @param originalCommand исходная бизнес-команда
     * @param login логин пользователя
     * @param password пароль пользователя в открытом виде
     */
    public CommandWithUser(CommandDTO originalCommand, String login, String password) {
        this.originalCommand = originalCommand;
        this.login = login;
        this.password = password;
    }

    /**
     * Фабричный метод для оборачивания команды учетными данными.
     */
    public static CommandDTO wrap(CommandDTO command, String login, String password) {
        return new CommandWithUser(command, login, password);
    }

    public CommandDTO getOriginalCommand() {
        return originalCommand;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}