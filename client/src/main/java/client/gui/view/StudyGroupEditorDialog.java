package client.gui.view;

import client.gui.I18n;
import client.gui.map.BuildingRoomCatalog;
import client.gui.map.RoomPlacementService;
import client.gui.theme.AppStyles;
import client.gui.util.NumericFieldFactory;
import client.gui.util.StudyGroupFormValidator;
import client.gui.view.dialogs.CommandDialogs;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import model.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Редактор этаж + аудитория координаты; числовые поля без отрицательных значений.
 */
public final class StudyGroupEditorDialog {

    private StudyGroupEditorDialog() {
    }

    public static Optional<StudyGroup> show(StudyGroup base, Locale locale, RoomPlacementService placement) {
        Dialog<StudyGroup> d = new Dialog<>();
        d.setTitle(I18n.tr(locale, "editor.title"));
        d.getDialogPane().setStyle(AppStyles.DIALOG_PANE);

        ButtonType save = new ButtonType(I18n.tr(locale, "editor.save"), ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        TextField name = new TextField(base == null ? "" : base.getName());

        ComboBox<Integer> floorPick = new ComboBox<>();
        floorPick.getItems().setAll(1, 2, 3, 4, 5);

        ComboBox<BuildingRoomCatalog.RoomPick> roomPick = new ComboBox<>();
        roomPick.setDisable(true);
        roomPick.setPromptText(I18n.tr(locale, "editor.room.selectFloorFirst"));
        roomPick.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(BuildingRoomCatalog.RoomPick item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    return;
                }
                Integer f = floorPick.getValue();
                setText(item.localizedLabel(locale, f == null ? 1 : f));
            }
        });
        roomPick.setButtonCell(roomPick.getCellFactory().call(null));

        Label coordsPreview = new Label();
        coordsPreview.setStyle(AppStyles.DIALOG_HINT);
        coordsPreview.setWrapText(true);

        Runnable updatePreview = () -> {
            if (floorPick.getValue() == null || roomPick.getValue() == null) {
                coordsPreview.setText("");
                return;
            }
            var coords = placement.toWorldCoordinates(floorPick.getValue(), roomPick.getValue().id());
            coordsPreview.setText(I18n.tr(locale, "editor.coordsPreview",
                    roomPick.getValue().label(),
                    floorPick.getValue(),
                    coords.getX(),
                    coords.getY()));
        };

        Runnable refreshRoomsForFloor = () -> {
            Integer floor = floorPick.getValue();
            if (floor == null) {
                roomPick.getItems().clear();
                roomPick.setValue(null);
                roomPick.setDisable(true);
                coordsPreview.setText("");
                return;
            }
            List<BuildingRoomCatalog.RoomPick> rooms = BuildingRoomCatalog.roomsForFloor(floor, locale);
            BuildingRoomCatalog.RoomPick previous = roomPick.getValue();
            roomPick.getItems().setAll(rooms);
            roomPick.setDisable(rooms.isEmpty());
            if (previous != null && rooms.stream().anyMatch(r -> r.id().equals(previous.id()))) {
                roomPick.setValue(rooms.stream().filter(r -> r.id().equals(previous.id())).findFirst().orElse(null));
            } else if (!rooms.isEmpty()) {
                roomPick.setValue(rooms.get(0));
            } else {
                roomPick.setValue(null);
            }
            updatePreview.run();
        };

        floorPick.valueProperty().addListener((o, ov, nv) -> refreshRoomsForFloor.run());
        roomPick.valueProperty().addListener((o, ov, nv) -> updatePreview.run());

        int initialFloor = base == null ? 1 : placement.floorFromWorldX(base.getCoordinates().getX());
        final BuildingRoomCatalog.RoomPick initialRoom = base == null
                ? null
                : placement.resolveRoom(initialFloor, base.getCoordinates()).orElse(null);
        floorPick.setValue(initialFloor);
        refreshRoomsForFloor.run();
        if (initialRoom != null) {
            roomPick.getItems().stream()
                    .filter(r -> r.id().equals(initialRoom.id()))
                    .findFirst()
                    .ifPresent(roomPick::setValue);
            if (roomPick.getValue() == null && !roomPick.getItems().isEmpty()) {
                roomPick.setValue(roomPick.getItems().get(0));
            }
            updatePreview.run();
        }

        Spinner<Integer> students = NumericFieldFactory.spinner(1, 10_000,
                base == null ? 10 : Math.max(1, base.getStudentsCount()));
        Spinner<Integer> expelled = NumericFieldFactory.spinner(0, 1_000_000,
                base == null ? 0 : (int) Math.max(0, base.getExpelledStudents()));
        Spinner<Integer> transferred = NumericFieldFactory.spinner(1, 10_000,
                base == null ? 1 : Math.max(1, base.getTransferredStudents()));

        ComboBox<Semester> semester = new ComboBox<>();
        semester.getItems().setAll(Semester.values());
        semester.setCellFactory(cb -> enumCell(locale, Semester.class));
        semester.setButtonCell(enumCell(locale, Semester.class));
        semester.setValue(base == null ? Semester.FIRST : base.getSemesterEnum());
        TextField admin = new TextField(base == null ? "Admin" : base.getGroupAdmin().getName());
        DatePicker birthday = new DatePicker(LocalDate.now().minusYears(25));
        ComboBox<Color> eyeColor = new ComboBox<>();
        eyeColor.getItems().setAll(Color.values());
        eyeColor.setCellFactory(cb -> enumCell(locale, Color.class));
        eyeColor.setButtonCell(enumCell(locale, Color.class));
        eyeColor.setValue(base == null ? Color.GREEN : base.getGroupAdmin().getEyeColor());
        ComboBox<Country> nationality = new ComboBox<>();
        nationality.getItems().setAll(Country.values());
        nationality.setCellFactory(cb -> enumCell(locale, Country.class));
        nationality.setButtonCell(enumCell(locale, Country.class));
        nationality.setValue(base == null ? Country.GERMANY : base.getGroupAdmin().getNationality());
        if (base != null && base.getGroupAdmin().getBirthday() != null) {
            birthday.setValue(base.getGroupAdmin().getBirthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));
        int row = 0;
        grid.addRow(row++, label(locale, "editor.field.name"), name);
        grid.addRow(row++, label(locale, "editor.field.floor"), floorPick);
        grid.addRow(row++, label(locale, "editor.field.room"), roomPick);
        grid.add(coordsPreview, 1, row++);
        GridPane.setHgrow(coordsPreview, Priority.ALWAYS);
        grid.addRow(row++, label(locale, "editor.field.students"), students);
        grid.addRow(row++, label(locale, "editor.field.expelled"), expelled);
        grid.addRow(row++, label(locale, "editor.field.transferred"), transferred);
        grid.addRow(row++, label(locale, "editor.field.semester"), semester);
        grid.addRow(row++, label(locale, "editor.field.adminName"), admin);
        grid.addRow(row++, label(locale, "editor.field.adminBirthday"), birthday);
        grid.addRow(row++, label(locale, "editor.field.eyeColor"), eyeColor);
        grid.addRow(row++, label(locale, "editor.field.nationality"), nationality);

        Label errorLabel = new Label();
        errorLabel.setStyle(AppStyles.DIALOG_ERROR);
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        grid.add(errorLabel, 0, row++, 2, 1);

        d.getDialogPane().setContent(grid);
        d.getDialogPane().setPrefWidth(480);

        Button saveButton = (Button) d.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            ValidationOutcome outcome = validateForm(
                    locale, base, name, floorPick, roomPick, students, expelled, transferred,
                    semester, admin, birthday, eyeColor, nationality, placement);
            if (!outcome.valid()) {
                errorLabel.setText(outcome.message());
                CommandDialogs.showError(locale, I18n.tr(locale, "editor.invalid"), outcome.message());
                event.consume();
            } else {
                errorLabel.setText("");
            }
        });

        d.setResultConverter(btn -> {
            if (btn != save) {
                return null;
            }
            ValidationOutcome outcome = validateForm(
                    locale, base, name, floorPick, roomPick, students, expelled, transferred,
                    semester, admin, birthday, eyeColor, nationality, placement);
            if (!outcome.valid()) {
                return null;
            }
            return outcome.group();
        });
        return d.showAndWait();
    }

    private record ValidationOutcome(boolean valid, String message, StudyGroup group) {
    }

    private static ValidationOutcome validateForm(
            Locale locale,
            StudyGroup base,
            TextField name,
            ComboBox<Integer> floorPick,
            ComboBox<BuildingRoomCatalog.RoomPick> roomPick,
            Spinner<Integer> students,
            Spinner<Integer> expelled,
            Spinner<Integer> transferred,
            ComboBox<Semester> semester,
            TextField admin,
            DatePicker birthday,
            ComboBox<Color> eyeColor,
            ComboBox<Country> nationality,
            RoomPlacementService placement
    ) {
        int studentsCount = StudyGroupFormValidator.readSpinnerValue(students, -1);
        int expelledInput = StudyGroupFormValidator.readSpinnerValue(expelled, -1);
        int transferredCount = StudyGroupFormValidator.readSpinnerValue(transferred, -1);

        if (studentsCount < 0 || expelledInput < 0 || transferredCount < 0) {
            String msg = I18n.tr(locale, "editor.error.spinnerInvalid");
            return new ValidationOutcome(false, msg, null);
        }

        StudyGroupFormValidator.ValidationResult check = StudyGroupFormValidator.validate(
                name.getText(),
                floorPick.getValue(),
                roomPick.getValue(),
                studentsCount,
                expelledInput,
                transferredCount,
                semester.getValue(),
                admin.getText(),
                birthday.getValue());

        if (!check.valid()) {
            String msg = formatValidationMessage(locale, check);
            return new ValidationOutcome(false, msg, null);
        }

        try {
            StudyGroup group = StudyGroupFormValidator.build(
                    base,
                    name.getText(),
                    floorPick.getValue(),
                    roomPick.getValue(),
                    studentsCount,
                    expelledInput,
                    transferredCount,
                    semester.getValue(),
                    admin.getText(),
                    birthday.getValue(),
                    eyeColor.getValue(),
                    nationality.getValue(),
                    placement);
            return new ValidationOutcome(true, "", group);
        } catch (IllegalArgumentException ex) {
            StudyGroupFormValidator.ValidationResult mapped = StudyGroupFormValidator.mapModelException(ex);
            return new ValidationOutcome(false, formatValidationMessage(locale, mapped), null);
        } catch (Exception ex) {
            String msg = I18n.tr(locale, "editor.error.generic",
                    ex.getMessage() == null ? "" : ex.getMessage());
            return new ValidationOutcome(false, msg, null);
        }
    }

    private static String formatValidationMessage(Locale locale, StudyGroupFormValidator.ValidationResult check) {
        if (check.args() == null || check.args().length == 0) {
            return I18n.tr(locale, check.messageKey());
        }
        return I18n.tr(locale, check.messageKey(), check.args());
    }

    private static <E extends Enum<E>> ListCell<E> enumCell(Locale locale, Class<E> type) {
        return new ListCell<>() {
            @Override
            protected void updateItem(E item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    return;
                }
                if (item instanceof Semester s) {
                    setText(I18n.semester(locale, s));
                } else if (item instanceof Color c) {
                    setText(I18n.color(locale, c));
                } else if (item instanceof Country c) {
                    setText(I18n.country(locale, c));
                } else {
                    setText(item.name());
                }
            }
        };
    }

    private static Label label(Locale locale, String key) {
        Label l = new Label(I18n.tr(locale, key));
        l.setStyle(AppStyles.DIALOG_LABEL);
        l.setMinWidth(120);
        l.setWrapText(true);
        return l;
    }
}
