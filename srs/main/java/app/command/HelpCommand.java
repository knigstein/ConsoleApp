package command;

public class HelpCommand implements Command {

    private final CommandManager commandManager;

    public HelpCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public void execute(String[] args) {
        commandManager.printHelp();
    }

    @Override
    public String getDescription() {
        return "Вывести справку по доступным командам";
    }
}
