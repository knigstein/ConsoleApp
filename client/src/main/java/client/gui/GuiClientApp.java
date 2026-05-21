package client.gui;

import client.gui.controller.GuiController;
import client.gui.theme.AppStyles;
import client.gui.util.UiScale;
import client.gui.view.LoginView;
import client.gui.view.MainView;
import client.gui.view.WelcomeView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.List;

/**
 * JavaFX GUI entry point.
 * Flow: Login/Register → Welcome → Main screen (map + commands + table).
 *
 * Args: [--mock] OR {@code <host> <port>}
 * Local UI test without server: {@code ./run-gui-client.sh mock}
 */
public class GuiClientApp extends Application {

    private final GuiController controller = new GuiController();
    private StackPane root;
    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        root = new StackPane();
        root.setStyle(AppStyles.ROOT);

        double w = UiScale.windowWidth();
        double h = UiScale.windowHeight();
        Scene scene = new Scene(root, w, h);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(560);
        primaryStage.setTitle(I18n.tr(controller.getSession().getLocale(), "app.title"));

        if (!initConnection()) {
            Platform.exit();
            return;
        }

        showLogin();
        primaryStage.show();
    }

    private boolean initConnection() {
        List<String> params = getParameters().getRaw();
        boolean mock = params.stream().anyMatch("--mock"::equalsIgnoreCase);
        List<String> positional = params.stream().filter(p -> !p.startsWith("--")).toList();

        if (mock) {
            controller.enableMockMode();
            return true;
        }

        String host = positional.isEmpty() ? "127.0.0.1" : positional.get(0);
        int port = 5555;
        if (positional.size() >= 2) {
            try {
                port = Integer.parseInt(positional.get(1));
            } catch (NumberFormatException e) {
                showError("Invalid port: " + positional.get(1));
                return false;
            }
        }
        try {
            controller.connect(host, port);
            return true;
        } catch (Exception e) {
            showError("Cannot connect to server " + host + ":" + port + "\n" + e.getMessage()
                    + "\n\nUse --mock for local UI test without server.");
            return false;
        }
    }

    private void showLogin() {
        LoginView login = new LoginView(controller, () -> {
            showWelcome();
            return true;
        });
        root.getChildren().setAll(login);
        stage.setTitle(I18n.tr(controller.getSession().getLocale(), "auth.title"));
    }

    private void showWelcome() {
        String login = controller.getSession().getLogin();
        WelcomeView welcome = new WelcomeView(
                controller.getSession().getLocale(),
                login,
                this::showMain
        );
        root.getChildren().setAll(welcome);
    }

    private void showMain() {
        MainView main = new MainView(controller, this::showLogin);
        root.getChildren().setAll(main);
        stage.setTitle(I18n.tr(controller.getSession().getLocale(), "app.title"));
        main.startLiveSync();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        controller.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
