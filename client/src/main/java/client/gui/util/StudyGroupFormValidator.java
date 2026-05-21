package client.gui.util;

import client.gui.map.BuildingRoomCatalog;
import client.gui.map.RoomPlacementService;
import javafx.scene.control.Spinner;
import model.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

/**
 * Валидация полей формы add/update по ограничениям
 */
public final class StudyGroupFormValidator {

    public record ValidationResult(boolean valid, String messageKey, Object[] args) {
        public static ValidationResult ok() {
            return new ValidationResult(true, null, null);
        }

        public static ValidationResult error(String messageKey, Object... args) {
            return new ValidationResult(false, messageKey, args);
        }
    }

    private StudyGroupFormValidator() {
    }

    public static ValidationResult validate(
            String name,
            Integer floor,
            BuildingRoomCatalog.RoomPick room,
            Integer studentsCount,
            Integer expelledInput,
            Integer transferredCount,
            Semester semester,
            String adminName,
            LocalDate birthday
    ) {
        if (name == null || name.isBlank()) {
            return ValidationResult.error("editor.error.name");
        }
        if (floor == null || floor < 1 || floor > 5) {
            return ValidationResult.error("editor.error.floor");
        }
        if (room == null) {
            return ValidationResult.error("editor.error.room");
        }
        if (studentsCount == null || studentsCount <= 0) {
            return ValidationResult.error("editor.error.students");
        }
        if (expelledInput != null && expelledInput < 0) {
            return ValidationResult.error("editor.error.expelledNegative");
        }
        // expelled == 0 → null при сборке; expelled > 0 проверяется в модели
        if (transferredCount == null || transferredCount <= 0) {
            return ValidationResult.error("editor.error.transferred");
        }
        if (adminName == null || adminName.isBlank()) {
            return ValidationResult.error("editor.error.adminName");
        }
        if (birthday == null) {
            return ValidationResult.error("editor.error.birthday");
        }
        return ValidationResult.ok();
    }

    public static int readSpinnerValue(Spinner<Integer> spinner, int fallback) {
        if (spinner == null || spinner.getValueFactory() == null) {
            return fallback;
        }
        try {
            Integer value = spinner.getValue();
            return value == null ? fallback : value;
        } catch (Exception e) {
            try {
                String text = spinner.getEditor().getText();
                if (text == null || text.isBlank()) {
                    return fallback;
                }
                return Integer.parseInt(text.trim());
            } catch (Exception ex) {
                return -1;
            }
        }
    }

    public static StudyGroup build(
            StudyGroup base,
            String name,
            int floor,
            BuildingRoomCatalog.RoomPick room,
            int studentsCount,
            int expelledInput,
            int transferredCount,
            Semester semester,
            String adminName,
            LocalDate birthday,
            Color eyeColor,
            Country nationality,
            RoomPlacementService placement
    ) {
        Long expelled = expelledInput <= 0 ? null : (long) expelledInput;
        Coordinates coords = placement.toWorldCoordinates(floor, room.id());
        StudyGroup created = new StudyGroup(
                base == null ? null : base.getId(),
                name.trim(),
                coords,
                base == null || base.getCreationDate() == null ? LocalDate.now() : base.getCreationDate(),
                studentsCount,
                expelled,
                transferredCount,
                semester,
                new Person(
                        adminName.trim(),
                        Date.from(birthday.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                        eyeColor,
                        nationality
                )
        );
        if (base != null) {
            created.setOwnerId(base.getOwnerId());
        }
        return created;
    }

    public static ValidationResult mapModelException(IllegalArgumentException ex) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase(Locale.ROOT);
        if (msg.contains("studentscount")) {
            return ValidationResult.error("editor.error.students");
        }
        if (msg.contains("expelled")) {
            return ValidationResult.error("editor.error.expelledPositive");
        }
        if (msg.contains("transferred")) {
            return ValidationResult.error("editor.error.transferred");
        }
        if (msg.contains("name")) {
            return ValidationResult.error("editor.error.name");
        }
        if (msg.contains("birthday")) {
            return ValidationResult.error("editor.error.birthday");
        }
        if (msg.contains("admin")) {
            return ValidationResult.error("editor.error.adminName");
        }
        if (msg.contains("coordinates") || msg.contains("room") || msg.contains("floor")) {
            return ValidationResult.error("editor.error.room");
        }
        return ValidationResult.error("editor.error.generic", ex.getMessage());
    }
}
