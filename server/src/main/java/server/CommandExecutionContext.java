package server;

/**
 * Контекст выполнения, передаваемый в каждую серверную команду.
 *
 * <p>Содержит идентификатор текущего пользователя и признак авторизации.</p>
 */
public class CommandExecutionContext {

    private final Integer userId;

    /**
     * Создает контекст выполнения команды.
     *
     * @param userId идентификатор авторизованного пользователя или {@code null}
     */
    public CommandExecutionContext(Integer userId) {
        this.userId = userId;
    }

    /**
     * Возвращает идентификатор авторизованного пользователя.
     *
     * @return user id или {@code null}
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * Показывает, выполняется ли команда в авторизованном контексте.
     *
     * @return {@code true}, если userId присутствует
     */
    public boolean isAuthorized() {
        return userId != null;
    }
}