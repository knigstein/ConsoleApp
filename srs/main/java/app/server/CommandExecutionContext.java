package server;

public class CommandExecutionContext {

    private final Integer userId;

    public CommandExecutionContext(Integer userId) {
        this.userId = userId;
    }

    public Integer getUserId() {
        return userId;
    }

    public boolean isAuthorized() {
        return userId != null;
    }
}