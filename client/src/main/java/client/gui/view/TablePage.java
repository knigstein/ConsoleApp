package client.gui.view;

import client.gui.AppSession;
import client.gui.I18n;
import client.gui.map.CoordinateToRoomService;
import client.gui.store.StudyGroupStore;
import client.gui.theme.AppStyles;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Color;
import model.Country;
import model.Semester;
import model.StudyGroup;

import java.util.List;
import java.util.function.Consumer;

/**
 * Standalone table page with 10-row pagination.
 */
public class TablePage extends BorderPane {

    private final StudyGroupStore store;
    private final AppSession session;
    private final CoordinateToRoomService projectionService;
    private final Consumer<StudyGroup> openOnMapCallback;
    private final Runnable onAdd;
    private final Consumer<StudyGroup> onEdit;
    private final Consumer<StudyGroup> onDelete;
    private final TableView<StudyGroup> table = new TableView<>();
    private final Label pageLabel = new Label();
    private final TitledPane filtersPane = new TitledPane();
    private final GridPane filtersGrid = new GridPane();
    private final Label fIdLabel = new Label();
    private final Label fCoordXLabel = new Label();
    private final Label fCoordYLabel = new Label();
    private final Label fCreationDateLabel = new Label();
    private final Label fStudentsLabel = new Label();
    private final Label fExpelledLabel = new Label();
    private final Label fTransferredLabel = new Label();
    private final Label fAdminNameLabel = new Label();
    private final Label fAdminBirthdayLabel = new Label();
    private final Label fEyeColorLabel = new Label();
    private final Label fNationalityLabel = new Label();
    private final Label fRoomLabel = new Label();

    public TablePage(
            StudyGroupStore store,
            AppSession session,
            CoordinateToRoomService projectionService,
            Consumer<StudyGroup> openOnMapCallback,
            Runnable onAdd,
            Consumer<StudyGroup> onEdit,
            Consumer<StudyGroup> onDelete
    ) {
        this.store = store;
        this.session = session;
        this.projectionService = projectionService;
        this.openOnMapCallback = openOnMapCallback;
        this.onAdd = onAdd;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.store.setProjectionService(projectionService);

        setStyle(AppStyles.TABLE_PANEL);
        setPadding(new Insets(10));
        setTop(buildTop());
        setCenter(buildTable());
        setBottom(buildPagination());
        subscribe();
        refreshTexts();
        refresh();
    }

    private void subscribe() {
        store.filters().idContainsProperty().addListener((obs, o, n) -> refresh());
        store.filters().nameContainsProperty().addListener((obs, o, n) -> refresh());
        store.filters().coordXContainsProperty().addListener((obs, o, n) -> refresh());
        store.filters().coordYContainsProperty().addListener((obs, o, n) -> refresh());
        store.filters().creationDateContainsProperty().addListener((obs, o, n) -> refresh());
        store.filters().studentsCountContainsProperty().addListener((obs, o, n) -> refresh());
        store.filters().expelledStudentsContainsProperty().addListener((obs, o, n) -> refresh());
        store.filters().transferredStudentsContainsProperty().addListener((obs, o, n) -> refresh());
        store.filters().ownerContainsProperty().addListener((obs, o, n) -> refresh());
        store.filters().adminNameContainsProperty().addListener((obs, o, n) -> refresh());
        store.filters().adminBirthdayContainsProperty().addListener((obs, o, n) -> refresh());
        store.filters().semesterProperty().addListener((obs, o, n) -> refresh());
        store.filters().eyeColorProperty().addListener((obs, o, n) -> refresh());
        store.filters().nationalityProperty().addListener((obs, o, n) -> refresh());
        store.filters().roomContainsProperty().addListener((obs, o, n) -> refresh());
        store.sortByProperty().addListener((obs, o, n) -> refresh());
        store.sortAscendingProperty().addListener((obs, o, n) -> refresh());
        store.currentPageProperty().addListener((obs, o, n) -> refresh());
    }

    private VBox buildTop() {
        HBox toolbar = buildToolbar();
        buildFiltersGrid();
        filtersPane.setContent(filtersGrid);
        filtersPane.setCollapsible(true);
        filtersPane.setExpanded(false);
        VBox box = new VBox(8, toolbar, filtersPane);
        return box;
    }

    private HBox buildToolbar() {
        TextField name = new TextField();
        name.textProperty().bindBidirectional(store.filters().nameContainsProperty());
        name.setPromptText(I18n.tr(session.getLocale(), "map.filter.name"));

        TextField owner = new TextField();
        owner.textProperty().bindBidirectional(store.filters().ownerContainsProperty());
        owner.setPromptText(I18n.tr(session.getLocale(), "map.filter.owner"));

        ComboBox<Semester> sem = new ComboBox<>(FXCollections.observableArrayList(Semester.values()));
        sem.valueProperty().bindBidirectional(store.filters().semesterProperty());
        sem.setPromptText(I18n.tr(session.getLocale(), "map.filter.semester"));

        Button reset = new Button(I18n.tr(session.getLocale(), "map.filter.reset"));
        reset.setStyle(AppStyles.SECONDARY_BUTTON);
        reset.setOnAction(e -> store.filters().reset());
        HBox toolbar = new HBox(8, name, owner, sem, reset);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        HBox.setHgrow(name, Priority.ALWAYS);
        HBox.setHgrow(owner, Priority.ALWAYS);
        return toolbar;
    }

    private void buildFiltersGrid() {
        filtersGrid.setHgap(8);
        filtersGrid.setVgap(8);
        filtersGrid.setPadding(new Insets(8));

        TextField fId = new TextField();
        fId.textProperty().bindBidirectional(store.filters().idContainsProperty());
        TextField fX = new TextField();
        fX.textProperty().bindBidirectional(store.filters().coordXContainsProperty());
        TextField fY = new TextField();
        fY.textProperty().bindBidirectional(store.filters().coordYContainsProperty());
        TextField fCreated = new TextField();
        fCreated.textProperty().bindBidirectional(store.filters().creationDateContainsProperty());
        TextField fStudents = new TextField();
        fStudents.textProperty().bindBidirectional(store.filters().studentsCountContainsProperty());
        TextField fExpelled = new TextField();
        fExpelled.textProperty().bindBidirectional(store.filters().expelledStudentsContainsProperty());
        TextField fTransferred = new TextField();
        fTransferred.textProperty().bindBidirectional(store.filters().transferredStudentsContainsProperty());
        TextField fAdminName = new TextField();
        fAdminName.textProperty().bindBidirectional(store.filters().adminNameContainsProperty());
        TextField fAdminBirthday = new TextField();
        fAdminBirthday.textProperty().bindBidirectional(store.filters().adminBirthdayContainsProperty());
        TextField fRoom = new TextField();
        fRoom.textProperty().bindBidirectional(store.filters().roomContainsProperty());

        ComboBox<Color> fEye = new ComboBox<>(FXCollections.observableArrayList(Color.values()));
        fEye.valueProperty().bindBidirectional(store.filters().eyeColorProperty());
        ComboBox<Country> fNat = new ComboBox<>(FXCollections.observableArrayList(Country.values()));
        fNat.valueProperty().bindBidirectional(store.filters().nationalityProperty());

        filtersGrid.addRow(0, fIdLabel, fId, fCoordXLabel, fX, fCoordYLabel, fY);
        filtersGrid.addRow(1, fCreationDateLabel, fCreated, fStudentsLabel, fStudents, fExpelledLabel, fExpelled);
        filtersGrid.addRow(2, fTransferredLabel, fTransferred, fAdminNameLabel, fAdminName, fAdminBirthdayLabel, fAdminBirthday);
        filtersGrid.addRow(3, fEyeColorLabel, fEye, fNationalityLabel, fNat, fRoomLabel, fRoom);
    }

    private TableView<StudyGroup> buildTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        TableColumn<StudyGroup, Integer> id = textColumn("table.col.id", "id", sg -> sg.getId());
        TableColumn<StudyGroup, String> name = textColumn("table.col.name", "name", StudyGroup::getName);
        TableColumn<StudyGroup, Integer> x = textColumn("table.col.coordX", "coordX", this::coordinatesX);
        TableColumn<StudyGroup, Double> y = textColumn("table.col.coordY", "coordY", this::coordinatesY);
        TableColumn<StudyGroup, java.time.LocalDate> creationDate = textColumn("table.col.creationDate", "creationDate", StudyGroup::getCreationDate);
        creationDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(java.time.LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : I18n.formatDate(session.getLocale(), item));
            }
        });
        TableColumn<StudyGroup, Integer> students = textColumn("table.col.students", "studentsCount", StudyGroup::getStudentsCount);
        TableColumn<StudyGroup, Long> expelled = textColumn("table.col.expelled", "expelledStudents", StudyGroup::getExpelledStudents);
        TableColumn<StudyGroup, Integer> transferred = textColumn("table.col.transferred", "transferredStudents", StudyGroup::getTransferredStudents);
        TableColumn<StudyGroup, Semester> semester = textColumn("table.col.semester", "semesterEnum", StudyGroup::getSemesterEnum);
        semester.setCellFactory(col -> semesterCell());
        TableColumn<StudyGroup, String> adminName = textColumn("table.col.adminName", "adminName", this::adminName);
        TableColumn<StudyGroup, java.util.Date> adminBirthday = textColumn("table.col.adminBirthday", "adminBirthday", this::adminBirthday);
        adminBirthday.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(java.util.Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : I18n.formatDate(session.getLocale(), item));
            }
        });
        TableColumn<StudyGroup, Color> eyeColor = textColumn("table.col.eyeColor", "eyeColor", this::adminEyeColor);
        eyeColor.setCellFactory(col -> colorCell());
        TableColumn<StudyGroup, Country> nationality = textColumn("table.col.nationality", "nationality", this::adminNationality);
        nationality.setCellFactory(col -> countryCell());
        TableColumn<StudyGroup, Integer> owner = textColumn("table.col.ownerId", "ownerId", StudyGroup::getOwnerId);
        TableColumn<StudyGroup, String> room = textColumn("table.col.room", "room",
                this::safeRoomName);

        table.getColumns().addAll(id, name, x, y, creationDate, students, expelled, transferred, semester, adminName, adminBirthday, eyeColor, nationality, owner, room);
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, selected) -> store.selectedGroupProperty().set(selected));
        table.setSortPolicy(tv -> {
            if (tv.getSortOrder().isEmpty()) {
                return true;
            }
            TableColumn<StudyGroup, ?> sorted = tv.getSortOrder().get(0);
            if (sorted.getId() != null) {
                store.sortByProperty().set(sorted.getId());
                store.sortAscendingProperty().set(sorted.getSortType() != SortType.DESCENDING);
                refresh();
            }
            return false;
        });
        return table;
    }

    private TableCell<StudyGroup, Semester> semesterCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Semester item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : I18n.semester(session.getLocale(), item));
            }
        };
    }

    private TableCell<StudyGroup, Color> colorCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Color item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : I18n.color(session.getLocale(), item));
            }
        };
    }

    private TableCell<StudyGroup, Country> countryCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Country item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : I18n.country(session.getLocale(), item));
            }
        };
    }

    private <T> TableColumn<StudyGroup, T> textColumn(String titleKey, String sortKey, java.util.function.Function<StudyGroup, T> extractor) {
        TableColumn<StudyGroup, T> c = new TableColumn<>(I18n.tr(session.getLocale(), titleKey));
        c.setId(sortKey);
        c.setCellValueFactory(v -> new javafx.beans.property.SimpleObjectProperty<>(extractor.apply(v.getValue())));
        c.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    return;
                }
                if (item instanceof Number n) {
                    setText(I18n.formatNumber(session.getLocale(), n));
                } else {
                    setText(item == null ? "" : String.valueOf(item));
                }
            }
        });
        return c;
    }

    private HBox buildPagination() {
        Button prev = new Button(I18n.tr(session.getLocale(), "table.prev"));
        prev.setStyle(AppStyles.SECONDARY_BUTTON);
        prev.setOnAction(e -> {
            store.prevPage();
            refresh();
        });
        Button next = new Button(I18n.tr(session.getLocale(), "table.next"));
        next.setStyle(AppStyles.SECONDARY_BUTTON);
        next.setOnAction(e -> {
            store.nextPage();
            refresh();
        });
        Button openMap = new Button(I18n.tr(session.getLocale(), "table.openMap"));
        openMap.setStyle(AppStyles.SECONDARY_BUTTON);
        openMap.setOnAction(e -> {
            StudyGroup selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openOnMapCallback.accept(selected);
            }
        });

        Button add = new Button(I18n.tr(session.getLocale(), "table.add"));
        add.setStyle(AppStyles.PRIMARY_BUTTON);
        add.setOnAction(e -> onAdd.run());

        Button edit = new Button(I18n.tr(session.getLocale(), "table.edit"));
        edit.setStyle(AppStyles.SECONDARY_BUTTON);
        edit.setOnAction(e -> {
            StudyGroup selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                onEdit.accept(selected);
            }
        });

        Button delete = new Button(I18n.tr(session.getLocale(), "table.delete"));
        delete.setStyle(AppStyles.DANGER_BUTTON);
        delete.setOnAction(e -> {
            StudyGroup selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                onDelete.accept(selected);
            }
        });

        HBox bottom = new HBox(10, prev, next, pageLabel, openMap, add, edit, delete);
        bottom.setPadding(new Insets(10, 0, 0, 0));
        return bottom;
    }

    public void refresh() {
        List<StudyGroup> page = store.getCurrentPageRows();
        table.setItems(FXCollections.observableArrayList(page));
        pageLabel.setText(
                I18n.tr(session.getLocale(), "table.page") + " "
                        + store.currentPageProperty().get() + "/" + store.pageCount()
                        + " (" + store.getFilteredSorted().size() + " " + I18n.tr(session.getLocale(), "table.rows") + ")"
        );
    }

    public void refreshTexts() {
        VBox top = (VBox) getTop();
        HBox toolbar = (HBox) top.getChildren().get(0);
        ((TextField) toolbar.getChildren().get(0)).setPromptText(I18n.tr(session.getLocale(), "map.filter.name"));
        ((TextField) toolbar.getChildren().get(1)).setPromptText(I18n.tr(session.getLocale(), "map.filter.owner"));
        ((ComboBox<?>) toolbar.getChildren().get(2)).setPromptText(I18n.tr(session.getLocale(), "map.filter.semester"));
        ((Button) toolbar.getChildren().get(3)).setText(I18n.tr(session.getLocale(), "map.filter.reset"));
        filtersPane.setText(I18n.tr(session.getLocale(), "table.filters"));
        fIdLabel.setText(I18n.tr(session.getLocale(), "table.col.id"));
        fCoordXLabel.setText(I18n.tr(session.getLocale(), "table.col.coordX"));
        fCoordYLabel.setText(I18n.tr(session.getLocale(), "table.col.coordY"));
        fCreationDateLabel.setText(I18n.tr(session.getLocale(), "table.col.creationDate"));
        fStudentsLabel.setText(I18n.tr(session.getLocale(), "table.col.students"));
        fExpelledLabel.setText(I18n.tr(session.getLocale(), "table.col.expelled"));
        fTransferredLabel.setText(I18n.tr(session.getLocale(), "table.col.transferred"));
        fAdminNameLabel.setText(I18n.tr(session.getLocale(), "table.col.adminName"));
        fAdminBirthdayLabel.setText(I18n.tr(session.getLocale(), "table.col.adminBirthday"));
        fEyeColorLabel.setText(I18n.tr(session.getLocale(), "table.col.eyeColor"));
        fNationalityLabel.setText(I18n.tr(session.getLocale(), "table.col.nationality"));
        fRoomLabel.setText(I18n.tr(session.getLocale(), "table.col.room"));
        List<TableColumn<StudyGroup, ?>> cols = table.getColumns();
        cols.get(0).setText(I18n.tr(session.getLocale(), "table.col.id"));
        cols.get(1).setText(I18n.tr(session.getLocale(), "table.col.name"));
        cols.get(2).setText(I18n.tr(session.getLocale(), "table.col.coordX"));
        cols.get(3).setText(I18n.tr(session.getLocale(), "table.col.coordY"));
        cols.get(4).setText(I18n.tr(session.getLocale(), "table.col.creationDate"));
        cols.get(5).setText(I18n.tr(session.getLocale(), "table.col.students"));
        cols.get(6).setText(I18n.tr(session.getLocale(), "table.col.expelled"));
        cols.get(7).setText(I18n.tr(session.getLocale(), "table.col.transferred"));
        cols.get(8).setText(I18n.tr(session.getLocale(), "table.col.semester"));
        cols.get(9).setText(I18n.tr(session.getLocale(), "table.col.adminName"));
        cols.get(10).setText(I18n.tr(session.getLocale(), "table.col.adminBirthday"));
        cols.get(11).setText(I18n.tr(session.getLocale(), "table.col.eyeColor"));
        cols.get(12).setText(I18n.tr(session.getLocale(), "table.col.nationality"));
        cols.get(13).setText(I18n.tr(session.getLocale(), "table.col.ownerId"));
        cols.get(14).setText(I18n.tr(session.getLocale(), "table.col.room"));
        HBox bottom = (HBox) getBottom();
        ((Button) bottom.getChildren().get(0)).setText(I18n.tr(session.getLocale(), "table.prev"));
        ((Button) bottom.getChildren().get(1)).setText(I18n.tr(session.getLocale(), "table.next"));
        ((Button) bottom.getChildren().get(3)).setText(I18n.tr(session.getLocale(), "table.openMap"));
        ((Button) bottom.getChildren().get(4)).setText(I18n.tr(session.getLocale(), "table.add"));
        ((Button) bottom.getChildren().get(5)).setText(I18n.tr(session.getLocale(), "table.edit"));
        ((Button) bottom.getChildren().get(6)).setText(I18n.tr(session.getLocale(), "table.delete"));
        refresh();
    }

    private Integer coordinatesX(StudyGroup sg) {
        return sg.getCoordinates() == null ? null : sg.getCoordinates().getX();
    }

    private Double coordinatesY(StudyGroup sg) {
        return sg.getCoordinates() == null ? null : sg.getCoordinates().getY();
    }

    private String adminName(StudyGroup sg) {
        return sg.getGroupAdmin() == null ? null : sg.getGroupAdmin().getName();
    }

    private java.util.Date adminBirthday(StudyGroup sg) {
        return sg.getGroupAdmin() == null ? null : sg.getGroupAdmin().getBirthday();
    }

    private Color adminEyeColor(StudyGroup sg) {
        return sg.getGroupAdmin() == null ? null : sg.getGroupAdmin().getEyeColor();
    }

    private Country adminNationality(StudyGroup sg) {
        return sg.getGroupAdmin() == null ? null : sg.getGroupAdmin().getNationality();
    }

    private String safeRoomName(StudyGroup sg) {
        try {
            return projectionService.project(sg, 800, 566, session.getLocale()).roomName();
        } catch (Exception e) {
            return "UNKNOWN_ROOM";
        }
    }
}
