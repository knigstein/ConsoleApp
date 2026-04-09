package command;

/**
 * Команда {@code exit}.
 * Завершает работу приложения без сохранения коллекции.
 *
 * Реализует интерфейс {@link Command}.
 */
public class ExitCommand implements Command {

    /**
     * Выполняет команду завершения программы.
     * Выводит сообщение и вызывает {@link System#exit(int)}.
     *
     * @param args аргументы команды (не используются)
     */
    @Override
    public void execute(String[] args) {
        System.out.println("Завершение программы.");
        System.exit(0);
    }

    /**
     * Возвращает краткое описание команды.
     *
     * @return строка с описанием назначения команды {@code exit}
     */
    @Override
    public String getDescription() {
        return "Завершить программу";
    }
}
