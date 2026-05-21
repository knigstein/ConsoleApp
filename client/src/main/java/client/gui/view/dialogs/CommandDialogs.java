package client.gui.view.dialogs;

import client.gui.I18n;
import client.gui.theme.AppStyles;
import common.dto.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import model.Semester;

import java.io.File;
import java.util.Locale;
import java.util.Optional;

/**
 * окна команд
 */
public final class CommandDialogs {

    private CommandDialogs() {
    }

    public static Optional<FilterContainsNameCommandDTO> filterByName(Locale locale) {
        Dialog<String> d = new Dialog<>();
        d.setTitle(I18n.tr(locale, "cmd.filterName"));
        d.getDialogPane().setStyle(AppStyles.DIALOG_PANE);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField field = new TextField();
        field.setPromptText(I18n.tr(locale, "cmd.filterName.prompt"));
        Label hint = hintLabel(locale, "cmd.filterName.prompt");
        GridPane grid = formGrid(hint, field);
        d.getDialogPane().setContent(grid);
        d.setResultConverter(btn -> btn == ButtonType.OK ? field.getText() : null);
        return d.showAndWait()
                .filter(s -> s != null && !s.isBlank())
                .map(FilterContainsNameCommandDTO::new);
    }

    public static Optional<FilterGreaterThanSemesterCommandDTO> filterBySemester(Locale locale) {
        Dialog<Semester> d = new Dialog<>();
        d.setTitle(I18n.tr(locale, "cmd.filterSemester"));
        d.getDialogPane().setStyle(AppStyles.DIALOG_PANE);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<Semester> box = new ComboBox<>();
        box.getItems().setAll(Semester.values());
        box.setCellFactory(cb -> semesterCell(locale));
        box.setButtonCell(semesterCell(locale));
        box.getSelectionModel().selectFirst();
        Label hint = hintLabel(locale, "cmd.filterSemester.hint");
        GridPane grid = formGrid(hint, box);
        d.getDialogPane().setContent(grid);
        d.setResultConverter(btn -> btn == ButtonType.OK ? box.getValue() : null);
        return d.showAndWait().map(FilterGreaterThanSemesterCommandDTO::new);
    }

    public static Optional<ExecuteScriptCommandDTO> executeScript(Locale locale) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.tr(locale, "cmd.executeScript"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Script (*.txt)", "*.txt"));
        File file = chooser.showOpenDialog(null);
        if (file == null) {
            return Optional.empty();
        }
        return Optional.of(new ExecuteScriptCommandDTO(file.getAbsolutePath()));
    }

    public static void showInfoAlert(Locale locale, String message) {
        showResult(locale, I18n.tr(locale, "cmd.info"), message);
    }

    public static void showResult(Locale locale, String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.getDialogPane().setStyle(AppStyles.DIALOG_PANE);
        if (message != null && message.length() > 200) {
            TextArea area = new TextArea(message);
            area.setEditable(false);
            area.setWrapText(true);
            area.setPrefRowCount(12);
            area.setMaxWidth(Double.MAX_VALUE);
            a.getDialogPane().setContent(area);
        } else {
            a.setContentText(message == null ? "" : message);
        }
        a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        a.showAndWait();
    }

    public static void showError(Locale locale, String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title == null || title.isBlank() ? I18n.tr(locale, "dialog.error") : title);
        a.setHeaderText(null);
        a.setContentText(message == null ? "" : message);
        a.getDialogPane().setStyle(AppStyles.DIALOG_PANE);
        a.showAndWait();
    }

    public static boolean confirm(Locale locale, String key) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(I18n.tr(locale, "dialog.confirm"));
        a.setHeaderText(null);
        a.setContentText(I18n.tr(locale, key));
        a.getDialogPane().setStyle(AppStyles.DIALOG_PANE);
        return a.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }

    private static Label hintLabel(Locale locale, String key) {
        Label l = new Label(I18n.tr(locale, key));
        l.setStyle(AppStyles.DIALOG_HINT);
        l.setWrapText(true);
        return l;
    }

    private static ListCell<Semester> semesterCell(Locale locale) {
        return new ListCell<>() {
            @Override
            protected void updateItem(Semester item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : I18n.semester(locale, item));
            }
        };
    }

    private static GridPane formGrid(Label hint, Control field) {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));
        grid.add(hint, 0, 0, 2, 1);
        grid.add(field, 0, 1, 2, 1);
        GridPane.setHgrow(field, Priority.ALWAYS);
        return grid;
    }
}
