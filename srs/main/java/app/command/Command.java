package command;

/**
 * Интерфейс команды.
 */

public interface Command {

    /**
     * Выполнить команду.
     * @param args аргументы команды
     */

    void execute(String[] args);

    /**
     * Описание команды (для help).
     */

    String getDescription();

}
