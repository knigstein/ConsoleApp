package client.gui.view;

import client.gui.AppSession;
import client.gui.DeployPathResolver;
import client.gui.I18n;
import client.gui.controller.GuiController;
import client.gui.map.CoordinateToRoomService;
import client.gui.map.FloorRoomMappingConfig;
import client.gui.map.MapLayoutMetrics;
import client.gui.store.StudyGroupStore;
import client.gui.theme.AppStyles;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.StudyGroup;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Interactive floor map with letterboxed layout and zoom (scroll + wheel + toolbar).
 */
public class FloorMapView extends VBox {

    private static final double ZOOM_MIN = 0.6;
    private static final double ZOOM_MAX = 3.0;
    private static final double ZOOM_STEP = 1.15;

    private final GuiController controller;
    private final FloorRoomMappingConfig floorConfig;
    private final CoordinateToRoomService projection;
    private final Consumer<StudyGroup> onEdit;
    private final Consumer<StudyGroup> onDelete;
    private final Predicate<StudyGroup> canEdit;

    private final StudyGroupStore store;
    private final AppSession session;

    private final Label titleLabel = new Label();
    private final Label floorBadge = new Label();
    private final HBox floorTabs = new HBox(4);
    private final ToggleGroup floorGroup = new ToggleGroup();
    private final HBox zoomBar = new HBox(6);
    private final Label zoomLabel = new Label();
    private final ScrollPane mapScroll = new ScrollPane();
    private final Pane mapViewport = new Pane();
    private final Group mapGroup = new Group();
    private final ImageView floorImage = new ImageView();
    private final Canvas markerCanvas = new Canvas();
    private final Label hintLabel = new Label();

    private final Map<Integer, MarkerHit> hitAreas = new HashMap<>();
    private final HBox legendBox = new HBox(12);
    private double imageNaturalW = 1;
    private double imageNaturalH = 1;
    private double zoomLevel = 1.0;
    private double contentWidth = 1;
    private double contentHeight = 1;
    private MapLayoutMetrics lastLayout = MapLayoutMetrics.compute(0, 0, 1, 1);

    private boolean panning;
    private double panLastSceneX;
    private double panLastSceneY;
    private double panOffsetX;
    private double panOffsetY;

    private record MarkerHit(StudyGroup group, double x, double y, double radius) {
    }

    public FloorMapView(
            GuiController controller,
            FloorRoomMappingConfig floorConfig,
            CoordinateToRoomService projection,
            Consumer<StudyGroup> onEdit,
            Consumer<StudyGroup> onDelete,
            Predicate<StudyGroup> canEdit
    ) {
        this.controller = controller;
        this.store = controller.getStore();
        this.session = controller.getSession();
        this.floorConfig = floorConfig;
        this.projection = projection;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.canEdit = canEdit;
        store.setProjectionService(projection);

        setSpacing(6);
        setPadding(new Insets(4));
        VBox.setVgrow(mapScroll, Priority.ALWAYS);
        buildFloorTabs();
        buildZoomBar();
        buildMapArea();

        buildLegend();
        getChildren().addAll(buildHeader(), floorTabs, zoomBar, mapScroll, legendBox, hintLabel);

        store.selectedFloorProperty().addListener((o, ov, nv) -> {
            if (nv != null) {
                resetPan();
                loadFloorImage(nv.intValue());
                refresh();
            }
        });
        store.filters().nameContainsProperty().addListener((o, ov, nv) -> refresh());
        store.filters().ownerContainsProperty().addListener((o, ov, nv) -> refresh());
        store.filters().semesterProperty().addListener((o, ov, nv) -> refresh());
        store.source().addListener((javafx.collections.ListChangeListener<? super StudyGroup>) c -> refresh());

        mapScroll.widthProperty().addListener((o, ov, nv) -> layoutMap());
        mapScroll.heightProperty().addListener((o, ov, nv) -> layoutMap());

        loadFloorImage(store.selectedFloorProperty().get());
        refreshTexts();
    }

    private HBox buildHeader() {
        titleLabel.setStyle(AppStyles.MAP_TITLE);
        floorBadge.setStyle(AppStyles.MAP_FLOOR_BADGE);
        HBox box = new HBox(8, titleLabel, floorBadge);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void buildZoomBar() {
        zoomBar.setAlignment(Pos.CENTER_LEFT);
        zoomBar.setPadding(new Insets(0, 0, 4, 0));
        Button zoomOut = new Button("−");
        Button zoomIn = new Button("+");
        Button zoomReset = new Button();
        zoomOut.setStyle(AppStyles.SECONDARY_BUTTON + AppStyles.COMPACT_BUTTON);
        zoomIn.setStyle(AppStyles.SECONDARY_BUTTON + AppStyles.COMPACT_BUTTON);
        zoomReset.setStyle(AppStyles.SECONDARY_BUTTON + AppStyles.COMPACT_BUTTON);
        zoomOut.setOnAction(e -> applyZoom(zoomLevel / ZOOM_STEP));
        zoomIn.setOnAction(e -> applyZoom(zoomLevel * ZOOM_STEP));
        zoomReset.setOnAction(e -> {
            applyZoom(1.0);
            resetPan();
        });
        zoomLabel.setStyle(AppStyles.MAP_HINT);
        Label wheelHint = new Label();
        wheelHint.setStyle(AppStyles.MAP_HINT);
        wheelHint.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> I18n.tr(session.getLocale(), "map.zoom.hint")));
        zoomReset.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> I18n.tr(session.getLocale(), "map.zoom.reset")));
        zoomBar.getChildren().addAll(zoomOut, zoomLabel, zoomIn, zoomReset, wheelHint);
        updateZoomLabel();
    }

    private void buildLegend() {
        legendBox.setAlignment(Pos.CENTER_LEFT);
        legendBox.setStyle(AppStyles.LEGEND);
        refreshLegend();
    }

    private void refreshLegend() {
        legendBox.getChildren().clear();
        Integer currentUser = session.getUserId();
        legendBox.getChildren().add(legendItem(
                ownerColor(currentUser),
                I18n.tr(session.getLocale(), "map.legend.own")));
        legendBox.getChildren().add(legendItem(
                ownerColor(999),
                I18n.tr(session.getLocale(), "map.legend.others")));
        legendBox.getChildren().add(new Label(I18n.tr(session.getLocale(), "map.legend.hint")));
    }

    private HBox legendItem(javafx.scene.paint.Color color, String text) {
        javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(5);
        dot.setFill(color);
        dot.setStroke(Color.WHITE);
        dot.setStrokeWidth(1.5);
        Label label = new Label(text);
        HBox row = new HBox(4, dot, label);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void buildFloorTabs() {
        floorTabs.setAlignment(Pos.CENTER_LEFT);
        for (int floor : floorConfig.floorNumbers()) {
            int floorNum = floor;
            ToggleButton btn = new ToggleButton();
            btn.setUserData(floorNum);
            btn.setToggleGroup(floorGroup);
            btn.setStyle(AppStyles.FLOOR_TAB);
            btn.selectedProperty().addListener((o, was, selected) -> {
                btn.setStyle(selected ? AppStyles.FLOOR_TAB_SELECTED : AppStyles.FLOOR_TAB);
                if (selected) {
                    store.selectedFloorProperty().set(floorNum);
                }
            });
            if (floorNum == store.selectedFloorProperty().get()) {
                btn.setSelected(true);
            }
            floorTabs.getChildren().add(btn);
        }
    }

    private void buildMapArea() {
        mapScroll.setStyle(AppStyles.MAP_FRAME);
        mapScroll.setFitToWidth(false);
        mapScroll.setFitToHeight(false);
        mapScroll.setPannable(false);
        mapScroll.setMinHeight(180);
        mapScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mapScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        mapViewport.setStyle("-fx-background-color: #37474f;");
        mapGroup.getChildren().addAll(floorImage, markerCanvas);
        mapViewport.getChildren().add(mapGroup);
        mapScroll.setContent(mapViewport);

        floorImage.setPreserveRatio(true);
        floorImage.setSmooth(true);

        markerCanvas.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, this::onCanvasClick);

        mapScroll.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (e.getDeltaY() == 0) {
                return;
            }
            double factor = e.getDeltaY() > 0 ? ZOOM_STEP : 1.0 / ZOOM_STEP;
            applyZoom(zoomLevel * factor);
            e.consume();
        });

        installRightButtonPan();
    }

    private void installRightButtonPan() {
        javafx.event.EventHandler<MouseEvent> panPress = e -> {
            if (e.getButton() != MouseButton.SECONDARY || e.isPrimaryButtonDown()) {
                return;
            }
            panning = true;
            panLastSceneX = e.getSceneX();
            panLastSceneY = e.getSceneY();
            e.consume();
        };
        javafx.event.EventHandler<MouseEvent> panDrag = e -> {
            if (!panning) {
                return;
            }
            double dx = e.getSceneX() - panLastSceneX;
            double dy = e.getSceneY() - panLastSceneY;
            panLastSceneX = e.getSceneX();
            panLastSceneY = e.getSceneY();
            panOffsetX += dx;
            panOffsetY += dy;
            applyPan();
            e.consume();
        };
        javafx.event.EventHandler<MouseEvent> panRelease = e -> {
            if (!panning) {
                return;
            }
            panning = false;
            e.consume();
        };

        mapScroll.addEventFilter(MouseEvent.MOUSE_PRESSED, panPress);
        mapScroll.addEventFilter(MouseEvent.MOUSE_DRAGGED, panDrag);
        mapScroll.addEventFilter(MouseEvent.MOUSE_RELEASED, panRelease);
        mapViewport.addEventFilter(MouseEvent.MOUSE_PRESSED, panPress);
        mapViewport.addEventFilter(MouseEvent.MOUSE_DRAGGED, panDrag);
        mapViewport.addEventFilter(MouseEvent.MOUSE_RELEASED, panRelease);
        markerCanvas.addEventFilter(MouseEvent.MOUSE_PRESSED, panPress);
        markerCanvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, panDrag);
        markerCanvas.addEventFilter(MouseEvent.MOUSE_RELEASED, panRelease);

        mapScroll.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> e.consume());
        mapViewport.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> e.consume());
    }

    private void applyPan() {
        mapGroup.setTranslateX(panOffsetX);
        mapGroup.setTranslateY(panOffsetY);
    }

    private void resetPan() {
        panning = false;
        panOffsetX = 0;
        panOffsetY = 0;
        applyPan();
    }

    private void applyZoom(double requested) {
        zoomLevel = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, requested));
        mapGroup.setScaleX(zoomLevel);
        mapGroup.setScaleY(zoomLevel);
        mapViewport.setPrefSize(contentWidth * zoomLevel, contentHeight * zoomLevel);
        updateZoomLabel();
    }

    private void updateZoomLabel() {
        zoomLabel.setText(I18n.tr(session.getLocale(), "map.zoom.level", Math.round(zoomLevel * 100)));
    }

    private void layoutMap() {
        double vw = mapScroll.getViewportBounds().getWidth();
        double vh = mapScroll.getViewportBounds().getHeight();
        if (vw <= 0 || vh <= 0) {
            vw = mapScroll.getWidth();
            vh = mapScroll.getHeight();
        }
        if (vw <= 0 || vh <= 0 || imageNaturalW <= 0 || imageNaturalH <= 0) {
            return;
        }

        contentWidth = vw;
        contentHeight = vh;
        markerCanvas.setWidth(contentWidth);
        markerCanvas.setHeight(contentHeight);

        lastLayout = MapLayoutMetrics.compute(contentWidth, contentHeight, imageNaturalW, imageNaturalH);
        if (!lastLayout.isValid()) {
            return;
        }

        floorImage.setFitWidth(lastLayout.displayWidth());
        floorImage.setFitHeight(lastLayout.displayHeight());
        floorImage.setLayoutX(lastLayout.offsetX());
        floorImage.setLayoutY(lastLayout.offsetY());
        markerCanvas.setLayoutX(0);
        markerCanvas.setLayoutY(0);

        mapViewport.setPrefSize(contentWidth * zoomLevel, contentHeight * zoomLevel);
        mapGroup.setScaleX(zoomLevel);
        mapGroup.setScaleY(zoomLevel);

        refresh();
    }

    private void loadFloorImage(int floor) {
        FloorRoomMappingConfig.FloorConfig cfg = floorConfig.floor(floor);
        if (cfg == null) {
            floorImage.setImage(null);
            return;
        }
        String fileName = cfg.imagePath().getFileName().toString();
        try (FileInputStream fis = new FileInputStream(cfg.imagePath().toFile())) {
            Image img = new Image(fis);
            floorImage.setImage(img);
            bindImageReady(img);
        } catch (Exception e) {
            InputStream cp = FloorMapView.class.getResourceAsStream("/Corpus/" + fileName);
            if (cp != null) {
                Image img = new Image(cp);
                floorImage.setImage(img);
                bindImageReady(img);
            } else {
                try {
                    Path corpus = DeployPathResolver.resolveDeployRoot().resolve("Corpus").resolve(fileName);
                    try (FileInputStream fis2 = new FileInputStream(corpus.toFile())) {
                        Image img = new Image(fis2);
                        floorImage.setImage(img);
                        bindImageReady(img);
                    }
                } catch (Exception ex) {
                    floorImage.setImage(null);
                    hintLabel.setText(I18n.tr(session.getLocale(), "map.error.imageLoad"));
                }
            }
        }
        floorBadge.setText(I18n.tr(session.getLocale(), "map.floor") + " " + floor);
    }

    private void bindImageReady(Image image) {
        if (image == null) {
            return;
        }
        Runnable update = () -> {
            imageNaturalW = image.getWidth();
            imageNaturalH = image.getHeight();
            layoutMap();
        };
        if (image.getProgress() >= 1 && !image.isError()) {
            javafx.application.Platform.runLater(update);
        } else {
            image.progressProperty().addListener((o, ov, nv) -> {
                if (nv.doubleValue() >= 1) {
                    javafx.application.Platform.runLater(update);
                }
            });
        }
    }

    public void refresh() {
        if (floorImage.getImage() == null || imageNaturalW <= 0) {
            return;
        }
        try {
            refreshMarkers();
        } catch (RuntimeException e) {
            hintLabel.setStyle(AppStyles.MAP_HINT);
            hintLabel.setText(I18n.tr(session.getLocale(), "map.details.groupsOnFloor") + " —");
        }
    }

    private void refreshMarkers() {
        int floorNo = store.selectedFloorProperty().get();
        visibleOnFloor = store.getFilteredForMap().stream()
                .filter(sg -> {
                    try {
                        return projection.project(sg, imageNaturalW, imageNaturalH, session.getLocale()).floor() == floorNo;
                    } catch (RuntimeException ignored) {
                        return false;
                    }
                })
                .toList();

        double cw = markerCanvas.getWidth();
        double ch = markerCanvas.getHeight();
        if (cw <= 0 || ch <= 0) {
            return;
        }

        lastLayout = MapLayoutMetrics.compute(cw, ch, imageNaturalW, imageNaturalH);
        if (!lastLayout.isValid()) {
            return;
        }

        GraphicsContext gc = markerCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, cw, ch);

        hitAreas.clear();
        for (StudyGroup sg : visibleOnFloor) {
            drawMarker(gc, sg);
        }

        hintLabel.setStyle(AppStyles.MAP_HINT);
        hintLabel.setText(I18n.tr(session.getLocale(), "map.details.groupsOnFloor") + " " + floorNo
                + ": " + visibleOnFloor.size()
                + " · " + I18n.tr(session.getLocale(), "map.legend.click"));
    }

    private List<StudyGroup> visibleOnFloor = List.of();

    private void drawMarker(GraphicsContext gc, StudyGroup sg) {
        var proj = projection.project(sg, imageNaturalW, imageNaturalH, session.getLocale());
        Point2D pane = lastLayout.toPaneCoordinates(proj.point().getX(), proj.point().getY());
        double px = pane.getX();
        double py = pane.getY();
        double radius = Math.max(6, Math.min(14, 5 + sg.getStudentsCount() * 0.08));

        javafx.scene.paint.Color fill = ownerColor(sg.getOwnerId());
        boolean own = session.getUserId() != null && session.getUserId().equals(sg.getOwnerId());
        gc.setFill(fill);
        gc.setStroke(own ? Color.web("#1a237e") : Color.WHITE);
        gc.setLineWidth(own ? 2.5 : 1.5);
        gc.fillOval(px - radius, py - radius, radius * 2, radius * 2);
        gc.strokeOval(px - radius, py - radius, radius * 2, radius * 2);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 9));
        String idText = String.valueOf(sg.getId());
        gc.fillText(idText, px - idText.length() * 2.5, py + 3);

        if (sg.getId() != null) {
            hitAreas.put(sg.getId(), new MarkerHit(sg, px, py, radius + 5));
        }
    }

    private void onCanvasClick(javafx.scene.input.MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) {
            return;
        }
        double mx = e.getX();
        double my = e.getY();
        MarkerHit best = null;
        double bestDist = Double.MAX_VALUE;
        for (MarkerHit hit : hitAreas.values()) {
            double d = Math.hypot(mx - hit.x(), my - hit.y());
            if (d <= hit.radius() && d < bestDist) {
                best = hit;
                bestDist = d;
            }
        }
        if (best != null) {
            showObjectDialog(best.group(),
                    projection.project(best.group(), imageNaturalW, imageNaturalH, session.getLocale()).roomName());
        }
    }

    private void showObjectDialog(StudyGroup sg, String roomName) {
        store.selectedGroupProperty().set(sg);
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle(I18n.tr(session.getLocale(), "map.object.info"));
        d.getDialogPane().setStyle(AppStyles.DIALOG_PANE);
        ButtonType editBtn = new ButtonType(I18n.tr(session.getLocale(), "map.object.edit"), ButtonBar.ButtonData.LEFT);
        ButtonType deleteBtn = new ButtonType(I18n.tr(session.getLocale(), "map.object.delete"), ButtonBar.ButtonData.LEFT);
        d.getDialogPane().getButtonTypes().addAll(editBtn, deleteBtn, ButtonType.CLOSE);

        String content = I18n.tr(session.getLocale(), "map.object.field.id") + ": " + sg.getId() + "\n"
                + I18n.tr(session.getLocale(), "map.object.field.name") + ": " + sg.getName() + "\n"
                + I18n.tr(session.getLocale(), "map.object.field.coordinates") + ": ("
                + I18n.formatNumber(session.getLocale(), sg.getCoordinates().getX()) + ", "
                + I18n.formatNumber(session.getLocale(), sg.getCoordinates().getY()) + ")\n"
                + I18n.tr(session.getLocale(), "map.object.field.students") + ": "
                + I18n.formatNumber(session.getLocale(), sg.getStudentsCount()) + "\n"
                + I18n.tr(session.getLocale(), "map.object.room") + ": " + roomName + "\n"
                + I18n.tr(session.getLocale(), "map.object.owner") + ": " + sg.getOwnerId();

        Label body = new Label(content);
        body.setWrapText(true);
        d.getDialogPane().setContent(body);
        d.showAndWait().ifPresent(btn -> {
            if (btn == editBtn && canEdit.test(sg)) {
                onEdit.accept(sg);
            } else if (btn == deleteBtn && canEdit.test(sg)) {
                onDelete.accept(sg);
            }
        });
    }

    private javafx.scene.paint.Color ownerColor(Integer ownerId) {
        if (ownerId == null) {
            return javafx.scene.paint.Color.web("#90a4ae");
        }
        double hue = Math.floorMod(ownerId * 53, 360);
        return javafx.scene.paint.Color.hsb(hue, 0.7, 0.88);
    }

    public void refreshTexts() {
        titleLabel.setText(I18n.tr(session.getLocale(), "map.building"));
        floorBadge.setText(I18n.tr(session.getLocale(), "map.floor") + " " + store.selectedFloorProperty().get());
        for (var node : floorTabs.getChildren()) {
            if (node instanceof ToggleButton tb) {
                tb.setText(I18n.tr(session.getLocale(), "map.floor.short") + " " + tb.getUserData());
            }
        }
        updateZoomLabel();
        refreshLegend();
    }
}
