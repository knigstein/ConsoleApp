package client.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Locale;
import java.util.Objects;

/**
 * Runtime session for GUI client.
 */
public class AppSession {

    private final StringProperty login = new SimpleStringProperty();
    private String password;
    private Integer userId;
    private Locale locale = Locale.forLanguageTag("ru");

    public String getLogin() {
        return login.get();
    }

    public StringProperty loginProperty() {
        return login;
    }

    public boolean isAuthorized() {
        return getLogin() != null && !getLogin().isBlank() && password != null && !password.isBlank();
    }

    public void authorize(String newLogin, String newPassword, Integer newUserId) {
        login.set(Objects.requireNonNullElse(newLogin, "").trim());
        password = newPassword;
        userId = newUserId;
    }

    public void logout() {
        login.set(null);
        password = null;
        userId = null;
    }

    public String getPassword() {
        return password;
    }

    public Integer getUserId() {
        return userId;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale == null ? Locale.forLanguageTag("ru") : locale;
    }
}
