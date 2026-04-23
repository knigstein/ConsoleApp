import database.DatabaseManager;
import database.StudyGroupRepository;
import model.StudyGroup;

public class Main {

    public static void main(String[] args) {
        System.out.println("CLI mode not supported with database.");
        System.out.println("Use: java -jar server.jar <db_login> <db_password> [port]");
    }
}