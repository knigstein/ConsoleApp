package common.dto;

public class CommandWithUser implements CommandDTO {

    private static final long serialVersionUID = 1L;

    private final CommandDTO originalCommand;
    private final String login;
    private final String password;

    public CommandWithUser(CommandDTO originalCommand, String login, String password) {
        this.originalCommand = originalCommand;
        this.login = login;
        this.password = password;
    }

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