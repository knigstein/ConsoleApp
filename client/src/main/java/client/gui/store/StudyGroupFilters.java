package client.gui.store;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ObjectProperty;
import model.Semester;
import model.Color;
import model.Country;

/**
 * Shared filters for map and table pages.
 */
public class StudyGroupFilters {

    private final StringProperty idContains = new SimpleStringProperty("");
    private final StringProperty nameContains = new SimpleStringProperty("");
    private final StringProperty coordXContains = new SimpleStringProperty("");
    private final StringProperty coordYContains = new SimpleStringProperty("");
    private final StringProperty creationDateContains = new SimpleStringProperty("");
    private final StringProperty studentsCountContains = new SimpleStringProperty("");
    private final StringProperty expelledStudentsContains = new SimpleStringProperty("");
    private final StringProperty transferredStudentsContains = new SimpleStringProperty("");
    private final StringProperty ownerContains = new SimpleStringProperty("");
    private final StringProperty adminNameContains = new SimpleStringProperty("");
    private final StringProperty adminBirthdayContains = new SimpleStringProperty("");
    private final ObjectProperty<Semester> semester = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Color> eyeColor = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Country> nationality = new SimpleObjectProperty<>(null);
    private final StringProperty roomContains = new SimpleStringProperty("");

    public StringProperty idContainsProperty() {
        return idContains;
    }

    public StringProperty nameContainsProperty() {
        return nameContains;
    }

    public StringProperty coordXContainsProperty() {
        return coordXContains;
    }

    public StringProperty coordYContainsProperty() {
        return coordYContains;
    }

    public StringProperty creationDateContainsProperty() {
        return creationDateContains;
    }

    public StringProperty studentsCountContainsProperty() {
        return studentsCountContains;
    }

    public StringProperty expelledStudentsContainsProperty() {
        return expelledStudentsContains;
    }

    public StringProperty transferredStudentsContainsProperty() {
        return transferredStudentsContains;
    }

    public StringProperty ownerContainsProperty() {
        return ownerContains;
    }

    public StringProperty adminNameContainsProperty() {
        return adminNameContains;
    }

    public StringProperty adminBirthdayContainsProperty() {
        return adminBirthdayContains;
    }

    public ObjectProperty<Semester> semesterProperty() {
        return semester;
    }

    public ObjectProperty<Color> eyeColorProperty() {
        return eyeColor;
    }

    public ObjectProperty<Country> nationalityProperty() {
        return nationality;
    }

    public StringProperty roomContainsProperty() {
        return roomContains;
    }

    public void reset() {
        idContains.set("");
        nameContains.set("");
        coordXContains.set("");
        coordYContains.set("");
        creationDateContains.set("");
        studentsCountContains.set("");
        expelledStudentsContains.set("");
        transferredStudentsContains.set("");
        ownerContains.set("");
        adminNameContains.set("");
        adminBirthdayContains.set("");
        semester.set(null);
        eyeColor.set(null);
        nationality.set(null);
        roomContains.set("");
    }
}
