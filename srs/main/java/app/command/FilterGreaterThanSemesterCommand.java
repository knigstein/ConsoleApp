package command;

import collection.CollectionManager;
import model.Semester;

public class FilterGreaterThanSemesterCommand implements Command {

    private final CollectionManager collectionManager;

    public FilterGreaterThanSemesterCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute(String[] args) {

        if (args.length < 2) {
            System.out.println("Не указан семестр.");
            return;
        }

        try {
            Semester semester = Semester.valueOf(args[1]);
            collectionManager.filterGreaterThanSemester(semester);
        } catch (Exception e) {
            System.out.println("Некорректный семестр.");
        }
    }

    @Override
    public String getDescription() {
        return "вывести элементы с semesterEnum больше заданного";
    }
}
