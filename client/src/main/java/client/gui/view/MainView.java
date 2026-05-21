package client.gui.view;

import client.gui.DeployPathResolver;
import client.gui.I18n;
import client.gui.controller.GuiController;
import client.gui.map.CoordinateToRoomService;
import client.gui.map.FloorRoomMappingConfig;
import client.gui.map.RoomPlacementService;
import client.gui.theme.AppStyles;
import client.gui.view.dialogs.CommandDialogs;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ListCell;
import javafx.scene.layout.*;

import java.util.Locale;

/**
 * Main application shell after login
 */
public class MainView extends BorderPane {

    private final GuiController controller;
    private final Runnable onLogout;
    private RoomPlacementService placement;
    private final StackPane contentStack = new StackPane();
    private FloorMapView floorMapView;
    private TablePage tablePage;
    private CommandsPanel commandsPanel;
    private Label statusLabel = new Label();
    private Label userLabel = new Label();
    private Button mapNavBtn;
    private Button tableNavBtn;

    public MainView(GuiController controller, Runnable onLogout) {
        this.controller = controller;
        this.onLogout = onLogout;
        controller.setStatusListener(msg -> statusLabel.setText(msg == null ? "" : msg));
        build();
        controller.getStore().setDisplayLocale(controller.getSession().getLocale());
        showMap();
        refreshAll();
    }

    private void build() {
        setStyle(AppStyles.ROOT);
        setTop(buildHeader());
        setCenter(buildCenter());
        setBottom(buildStatusBar());
    }

    private HBox buildHeader() {
        Label title = new Label(I18n.tr(controller.getSession().getLocale(), "app.title"));
        title.setStyle(AppStyles.HEADER_TITLE);

        userLabel.setStyle(AppStyles.HEADER_USER);
        userLabel.textProperty().bind(
                controller.getSession().loginProperty().map(l ->
                        I18n.tr(controller.getSession().getLocale(), "user") + ": " + (l == null ? "—" : l))
        );

        mapNavBtn = navButton(I18n.tr(controller.getSession().getLocale(), "nav.map"), true);
        tableNavBtn = navButton(I18n.tr(controller.getSession().getLocale(), "nav.table"), false);
        mapNavBtn.setOnAction(e -> showMap());
        tableNavBtn.setOnAction(e -> showTable());

        ComboBox<Locale> locales = localeCombo();

        Button logout = new Button(I18n.tr(controller.getSession().getLocale(), "nav.logout"));
        logout.setStyle(AppStyles.SECONDARY_BUTTON);
        logout.setOnAction(e -> {
            controller.stopPolling();
            controller.logout();
            onLogout.run();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, title, spacer, mapNavBtn, tableNavBtn, userLabel, locales, logout);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(AppStyles.HEADER);
        header.setPadding(new Insets(6, 12, 6, 12));
        return header;
    }

    private ListCell<Locale> localeCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    return;
                }
                setText(I18n.localeName(controller.getSession().getLocale(), item));
            }
        };
    }

    private Button navButton(String text, boolean active) {
        Button b = new Button(text);
        b.setStyle(active ? AppStyles.NAV_BUTTON_ACTIVE : AppStyles.NAV_BUTTON);
        return b;
    }

    private ComboBox<Locale> localeCombo() {
        ComboBox<Locale> box = new ComboBox<>();
        box.getItems().addAll(
                Locale.forLanguageTag("ru"),
                Locale.forLanguageTag("be-BY"),
                Locale.forLanguageTag("hu"),
                Locale.forLanguageTag("en-IE")
        );
        box.setValue(controller.getSession().getLocale());
        box.setCellFactory(c -> localeCell());
        box.setButtonCell(localeCell());
        box.setOnAction(e -> {
            controller.getSession().setLocale(box.getValue());
            refreshTexts();
            javafx.application.Platform.runLater(() -> {
                if (getScene() != null && getScene().getWindow() instanceof javafx.stage.Stage stage) {
                    stage.setTitle(I18n.tr(controller.getSession().getLocale(), "app.title"));
                }
            });
        });
        return box;
    }

    private BorderPane buildCenter() {
        FloorRoomMappingConfig floorConfig = FloorRoomMappingConfig.defaultConfig(DeployPathResolver.resolveDeployRoot());
        this.placement = new RoomPlacementService(floorConfig);
        CoordinateToRoomService projection = new CoordinateToRoomService(floorConfig);

        Runnable onDataChanged = this::refreshAll;
        Runnable afterAdd = () -> {
            // focus вызывается из callback executeCommand после reload коллекции
            refreshAll();
        };

        floorMapView = new FloorMapView(
                controller,
                floorConfig,
                projection,
                this::editGroup,
                this::deleteGroup,
                controller::canEdit
        );

        tablePage = new TablePage(
                controller.getStore(),
                controller.getSession(),
                projection,
                group -> {
                    if (group != null) {
                        controller.getStore().selectedGroupProperty().set(group);
                        controller.getStore().selectedFloorProperty().set(
                                projection.project(group, 800, 566, controller.getSession().getLocale()).floor());
                        showMap();
                    }
                },
                () -> StudyGroupEditorDialog.show(null, controller.getSession().getLocale(), placement)
                        .ifPresent(g -> controller.executeCommand("add", g, () -> {
                            focusGroupOnMap(g);
                            afterAdd.run();
                        })),
                this::editGroup,
                this::deleteGroup
        );

        commandsPanel = new CommandsPanel(
                controller,
                placement,
                onDataChanged,
                this::showTable,
                () -> StudyGroupEditorDialog.show(null, controller.getSession().getLocale(), placement)
                        .ifPresent(g -> controller.executeCommand("add", g, () -> {
                            focusGroupOnMap(g);
                            afterAdd.run();
                        })),
                this::editGroup,
                this::deleteGroup
        );

        contentStack.getChildren().addAll(floorMapView, tablePage);
        tablePage.setVisible(false);
        tablePage.setManaged(false);
        contentStack.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        floorMapView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tablePage.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        BorderPane center = new BorderPane();
        center.setCenter(contentStack);
        center.setLeft(commandsPanel);
        BorderPane.setMargin(commandsPanel, new Insets(4, 0, 4, 4));
        return center;
    }

    private Label buildStatusBar() {
        statusLabel.setStyle(AppStyles.STATUS_BAR);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setWrapText(false);
        statusLabel.setMaxHeight(28);
        BorderPane.setAlignment(statusLabel, Pos.CENTER_LEFT);
        return statusLabel;
    }

    private void showMap() {
        floorMapView.setVisible(true);
        floorMapView.setManaged(true);
        tablePage.setVisible(false);
        tablePage.setManaged(false);
        mapNavBtn.setStyle(AppStyles.NAV_BUTTON_ACTIVE);
        tableNavBtn.setStyle(AppStyles.NAV_BUTTON);
        floorMapView.refresh();
    }

    private void showTable() {
        tablePage.setVisible(true);
        tablePage.setManaged(true);
        floorMapView.setVisible(false);
        floorMapView.setManaged(false);
        tableNavBtn.setStyle(AppStyles.NAV_BUTTON_ACTIVE);
        mapNavBtn.setStyle(AppStyles.NAV_BUTTON);
        tablePage.refresh();
    }

    private void editGroup(model.StudyGroup selected) {
        if (selected == null) {
            controller.setStatus(I18n.tr(controller.getSession().getLocale(), "msg.selectGroupFirst"));
            return;
        }
        if (!controller.canEdit(selected)) {
            controller.setStatus(I18n.tr(controller.getSession().getLocale(), "msg.editDenied"));
            return;
        }
        StudyGroupEditorDialog.show(selected, controller.getSession().getLocale(), placement)
                .ifPresent(edited -> controller.executeCommand(
                        "update " + selected.getId(), edited, () -> {
                            focusGroupOnMap(edited);
                            refreshAll();
                        }));
    }

    private void deleteGroup(model.StudyGroup selected) {
        if (selected == null) {
            controller.setStatus(I18n.tr(controller.getSession().getLocale(), "msg.selectGroupFirst"));
            return;
        }
        if (!controller.canEdit(selected)) {
            controller.setStatus(I18n.tr(controller.getSession().getLocale(), "msg.deleteDenied"));
            return;
        }
        if (CommandDialogs.confirm(controller.getSession().getLocale(), "cmd.confirm.delete")) {
            controller.executeCommand("remove_by_id " + selected.getId(), null, this::refreshAll);
        }
    }

    public void refreshAll() {
        floorMapView.refresh();
        tablePage.refresh();
    }

    public void refreshTexts() {
        controller.getStore().setDisplayLocale(controller.getSession().getLocale());
        floorMapView.refreshTexts();
        tablePage.refreshTexts();
        commandsPanel.refreshTexts();
    }

    public void startLiveSync() {
        controller.refreshCollection(() -> {
            refreshAll();
            controller.startPolling(this::refreshAll);
        });
    }

    /** После add/update переключает этаж и выделяет группу на карте. */
    private void focusGroupOnMap(model.StudyGroup template) {
        if (template == null || template.getCoordinates() == null) {
            return;
        }
        int floor = placement.floorFromWorldX(template.getCoordinates().getX());
        controller.getStore().selectedFloorProperty().set(floor);
        controller.getStore().source().stream()
                .filter(g -> template.getName() != null && template.getName().equals(g.getName()))
                .filter(g -> g.getCoordinates() != null
                        && g.getCoordinates().getX() == template.getCoordinates().getX()
                        && Double.compare(g.getCoordinates().getY(), template.getCoordinates().getY()) == 0)
                .findFirst()
                .ifPresent(g -> controller.getStore().selectedGroupProperty().set(g));
        showMap();
    }
}
