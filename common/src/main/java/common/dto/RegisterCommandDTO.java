package common.dto;

import java.io.Serializable;

public class RegisterCommandDTO implements CommandDTO {

    private static final long serialVersionUID = 1L;

    private final String login;
    private final String password;

    public RegisterCommandDTO(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}