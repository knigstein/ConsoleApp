package client.gui.view;

import client.gui.I18n;
import client.gui.controller.GuiController;
import client.gui.theme.AppStyles;
import client.gui.util.FxTasks;
import common.dto.CommandResponseDTO;
import common.dto.ResponseStatus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Locale;
import java.util.function.BooleanSupplier;

/**
 * Full-screen login and registration view (first screen on startup).
 */
public class LoginView extends BorderPane {

    private final GuiController controller;
    private final BooleanSupplier onSuccess;
    private final Label titleLabel = new Label();
    private final Label subtitleLabel = new Label();
    private final Label usernameCaption = new Label();
    private final Label passwordCaption = new Label();
    private final Label localeCaption = new Label();
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Label statusLabel = new Label();
    private final Button loginButton = new Button();
    private final Button registerButton = new Button();
    private final ComboBox<Locale> localeBox = new ComboBox<>();

    public LoginView(GuiController controller, BooleanSupplier onSuccess) {
        this.controller = controller;
        this.onSuccess = onSuccess;
        build();
        applyTexts();
    }

    private void build() {
        setStyle(AppStyles.LOGIN_ROOT);

        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_CENTER);
        card.setMaxWidth(480);
        card.setFillWidth(true);
        card.setStyle(AppStyles.LOGIN_CARD);

        titleLabel.setStyle(AppStyles.LOGIN_TITLE);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

        subtitleLabel.setStyle(AppStyles.LOGIN_SUBTITLE);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(Double.MAX_VALUE);
        subtitleLabel.setAlignment(Pos.CENTER);

        usernameField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setMaxWidth(Double.MAX_VALUE);
        localeBox.setMaxWidth(Double.MAX_VALUE);

        localeBox.getItems().addAll(
                Locale.forLanguageTag("ru"),
                Locale.forLanguageTag("be-BY"),
                Locale.forLanguageTag("hu"),
                Locale.forLanguageTag("en-IE")
        );
        localeBox.setCellFactory(c -> localeCell(controller));
        localeBox.setButtonCell(localeCell(controller));
        localeBox.setValue(controller.getSession().getLocale());
        localeBox.setOnAction(e -> {
            controller.getSession().setLocale(localeBox.getValue());
            applyTexts();
        });

        loginButton.setStyle(AppStyles.PRIMARY_BUTTON);
        loginButton.setDefaultButton(true);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> doLogin());

        registerButton.setStyle(AppStyles.SECONDARY_BUTTON);
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setOnAction(e -> doRegister());

        VBox form = new VBox(10,
                labeledField(usernameCaption, usernameField),
                labeledField(passwordCaption, passwordField),
                labeledField(localeCaption, localeBox)
        );
        form.setFillWidth(true);

        HBox buttons = new HBox(10, loginButton, registerButton);
        buttons.setAlignment(Pos.CENTER);
        HBox.setHgrow(loginButton, Priority.ALWAYS);
        HBox.setHgrow(registerButton, Priority.ALWAYS);

        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setStyle("-fx-text-fill: #c62828; -fx-font-size: 11px;");

        card.getChildren().addAll(titleLabel, subtitleLabel, form, buttons, statusLabel);
        BorderPane.setAlignment(card, Pos.CENTER);
        setCenter(card);
        BorderPane.setMargin(card, new Insets(24));
    }

    private VBox labeledField(Label caption, javafx.scene.control.Control field) {
        caption.setStyle(AppStyles.LOGIN_LABEL);
        caption.setWrapText(true);
        caption.setMaxWidth(Double.MAX_VALUE);
        VBox box = new VBox(4, caption, field);
        box.setFillWidth(true);
        return box;
    }

    private static ListCell<Locale> localeCell(GuiController controller) {
        return new ListCell<>() {
            @Override
            protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    return;
                }
                Locale display = controller.getSession().getLocale();
                setText(I18n.localeName(display, item));
            }
        };
    }

    private void doLogin() {
        String login = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String pass = passwordField.getText() == null ? "" : passwordField.getText();
        if (login.isBlank() || pass.isBlank()) {
            statusLabel.setText(I18n.tr(controller.getSession().getLocale(), "auth.error.empty"));
            return;
        }
        setBusy(true);
        statusLabel.setText(I18n.tr(controller.getSession().getLocale(), "auth.connecting"));
        if (controller.isMockMode()) {
            controller.completeMockLogin(login, pass);
            setBusy(false);
            onSuccess.getAsBoolean();
            return;
        }
        FxTasks.runAsync(
                () -> controller.login(login, pass),
                response -> handleAuth(response),
                err -> {
                    setBusy(false);
                    String msg = err.getMessage();
                    if (msg == null || msg.isBlank()) {
                        msg = I18n.tr(controller.getSession().getLocale(), "auth.error.network");
                    }
                    statusLabel.setText(msg);
                }
        );
    }

    private void doRegister() {
        String login = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String pass = passwordField.getText() == null ? "" : passwordField.getText();
        if (login.isBlank() || pass.isBlank()) {
            statusLabel.setText(I18n.tr(controller.getSession().getLocale(), "auth.error.empty"));
            return;
        }
        setBusy(true);
        statusLabel.setText(I18n.tr(controller.getSession().getLocale(), "auth.connecting"));
        if (controller.isMockMode()) {
            controller.completeMockLogin(login, pass);
            setBusy(false);
            onSuccess.getAsBoolean();
            return;
        }
        FxTasks.runAsync(
                () -> controller.register(login, pass),
                response -> handleAuth(response),
                err -> {
                    setBusy(false);
                    String msg = err.getMessage();
                    if (msg == null || msg.isBlank()) {
                        msg = I18n.tr(controller.getSession().getLocale(), "auth.error.network");
                    }
                    statusLabel.setText(msg);
                }
        );
    }

    private void handleAuth(CommandResponseDTO response) {
        setBusy(false);
        if (response.getStatus() == ResponseStatus.SUCCESS) {
            statusLabel.setText("");
            onSuccess.getAsBoolean();
        } else {
            statusLabel.setText(response.getMessage());
        }
    }

    private void setBusy(boolean busy) {
        loginButton.setDisable(busy);
        registerButton.setDisable(busy);
    }

    public void applyTexts() {
        var session = controller.getSession();
        titleLabel.setText(I18n.tr(session.getLocale(), "auth.title"));
        subtitleLabel.setText(I18n.tr(session.getLocale(), "auth.subtitle"));
        loginButton.setText(I18n.tr(session.getLocale(), "auth.login"));
        registerButton.setText(I18n.tr(session.getLocale(), "auth.register"));
        usernameCaption.setText(I18n.tr(session.getLocale(), "auth.username"));
        passwordCaption.setText(I18n.tr(session.getLocale(), "auth.password"));
        localeCaption.setText(I18n.tr(session.getLocale(), "auth.locale"));
        usernameField.setPromptText(I18n.tr(session.getLocale(), "auth.username"));
        passwordField.setPromptText(I18n.tr(session.getLocale(), "auth.password"));
    }
}
