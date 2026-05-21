package client.gui.net;

import client.ClientNetwork;
import common.dto.*;
import model.Semester;
import model.StudyGroup;

import java.io.IOException;
import java.util.List;

/**
 * JavaFX-friendly adapter over existing UDP client.
 */
public class ClientGateway implements AutoCloseable {

    private final ClientNetwork network;
    private String login;
    private String password;

    public ClientGateway(String host, int port) throws IOException {
        this.network = ClientNetwork.forGui(host, port);
    }

    public void setCredentials(String login, String password) {
        this.login = login == null ? null : login.trim();
        this.password = password;
    }

    public CommandResponseDTO register(String login, String password) throws IOException, ClassNotFoundException {
        CommandResponseDTO response = network.sendAndReceive(new RegisterCommandDTO(login, password));
        if (response.getStatus() == ResponseStatus.SUCCESS) {
            setCredentials(login, password);
        }
        return response;
    }

    public CommandResponseDTO login(String login, String password) throws IOException, ClassNotFoundException {
        CommandResponseDTO response = network.sendAndReceive(new LoginCommandDTO(login, password));
        if (response.getStatus() == ResponseStatus.SUCCESS) {
            setCredentials(login, password);
        }
        return response;
    }

    public List<StudyGroup> loadCollection() throws IOException, ClassNotFoundException {
        CommandResponseDTO response = sendAuthorized(new ShowCommandDTO());
        return response.getCollection() == null ? List.of() : response.getCollection();
    }

    public CommandResponseDTO sendAuthorized(CommandDTO dto) throws IOException, ClassNotFoundException {
        if (login == null || password == null) {
            throw new IllegalStateException("No auth credentials");
        }
        return network.sendAndReceive(CommandWithUser.wrap(dto, login, password));
    }

    public CommandDTO parseManualCommand(String input, StudyGroup selectedOrCreated) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String[] parts = input.trim().split("\\s+");
        String cmd = parts[0];
        try {
            return switch (cmd) {
                case "info" -> new InfoCommandDTO();
                case "show" -> new ShowCommandDTO();
                case "clear" -> new ClearCommandDTO();
                case "remove_first" -> new RemoveFirstCommandDTO();
                case "print_field_descending_group_admin" -> new PrintFieldDescendingGroupAdminCommandDTO();
                case "filter_contains_name" -> parts.length < 2 ? null : new FilterContainsNameCommandDTO(parts[1]);
                case "filter_greater_than_semester_enum" -> {
                    if (parts.length < 2) {
                        yield null;
                    }
                    Semester sem;
                    try {
                        sem = Semester.valueOf(parts[1]);
                    } catch (Exception e) {
                        sem = null;
                    }
                    yield sem == null ? null : new FilterGreaterThanSemesterCommandDTO(sem);
                }
                case "remove_by_id" -> parts.length < 2 ? null : new RemoveByIdCommandDTO(Integer.parseInt(parts[1]));
                case "execute_script" -> parts.length < 2 ? null : new ExecuteScriptCommandDTO(parts[1]);
                case "add" -> selectedOrCreated == null ? null : new AddCommandDTO(selectedOrCreated);
                case "add_if_min" -> selectedOrCreated == null ? null : new AddIfMinCommandDTO(selectedOrCreated);
                case "remove_lower" -> selectedOrCreated == null ? null : new RemoveLowerCommandDTO(selectedOrCreated);
                case "update" -> {
                    if (parts.length < 2 || selectedOrCreated == null) {
                        yield null;
                    }
                    yield new UpdateCommandDTO(Integer.parseInt(parts[1]), selectedOrCreated);
                }
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        network.close();
    }
}
