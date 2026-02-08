package command;

public class ExitCommand implements Command {

    @Override
    public void execute(String[] args) {
        System.out.println("Завершение программы.");
        System.exit(0);
    }

    @Override
    public String getDescription() {
        return "завершить программу";
    }
}
