package command;

/**
 * Команда {@code help}.
 * Выводит справку по всем доступным командам, зарегистрированным
 * в {@link CommandManager}.
 *
 * Реализует интерфейс {@link Command}.
 */
public class HelpCommand implements Command {

    private final CommandManager commandManager;

    /**
     * Создаёт команду вывода справки.
     *
     * @param commandManager менеджер команд, из которого берётся список доступных команд
     */
    public HelpCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * Выполняет команду справки, выводя список команд и их описания.
     *
     * @param args аргументы команды (не используются)
     */
    @Override
    public void execute(String[] args) {
        commandManager.printHelp();
    }

    /**
     * Возвращает краткое описание команды.
     *
     * @return строка с описанием назначения команды {@code help}
     */
    @Override
    public String getDescription() {
        return "Вывести справку по доступным командам";
    }
}
