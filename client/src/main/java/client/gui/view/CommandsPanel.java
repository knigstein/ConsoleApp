package client.gui.view;

import client.gui.I18n;
import client.gui.controller.GuiController;
import client.gui.map.RoomPlacementService;
import client.gui.theme.AppStyles;
import client.gui.util.CollectionPlacementReport;
import client.gui.view.dialogs.CommandDialogs;
import common.dto.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.StudyGroup;

import java.util.function.Consumer;

/**
 * Sidebar
 */
public class CommandsPanel extends ScrollPane {

    private final GuiController controller;
    private final RoomPlacementService placement;
    private final Runnable onDataChanged;
    private final Runnable onShowTable;
    private final Consumer<StudyGroup> onEdit;
    private final Consumer<StudyGroup> onDelete;
    private final Runnable onAdd;

    public CommandsPanel(
            GuiController controller,
            RoomPlacementService placement,
            Runnable onDataChanged,
            Runnable onShowTable,
            Runnable onAdd,
            Consumer<StudyGroup> onEdit,
            Consumer<StudyGroup> onDelete
    ) {
        this.controller = controller;
        this.placement = placement;
        this.onDataChanged = onDataChanged;
        this.onShowTable = onShowTable;
        this.onAdd = onAdd;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        setFitToWidth(true);
        setStyle(AppStyles.SIDEBAR);
        setContent(buildContent());
        setPrefWidth(200);
        setMinWidth(180);
        setMaxWidth(220);
    }

    private VBox buildContent() {
        VBox root = new VBox(6);
        root.setPadding(new Insets(2));
        var loc = controller.getSession().getLocale();

        root.getChildren().add(section(I18n.tr(loc, "cmd.section.collection")));
        root.getChildren().add(btn(I18n.tr(loc, "cmd.show"), () -> {
            controller.refreshCollection(() -> {
                onShowTable.run();
                onDataChanged.run();
            });
        }));
        root.getChildren().add(btn(I18n.tr(loc, "cmd.info"), () ->
                controller.executeCommand("info", null, () -> {
                    String serverMeta = controller.getStore().statusMessageProperty().get();
                    String placement = CollectionPlacementReport.build(
                            controller.getStore().source(), this.placement, loc);
                    String body = (serverMeta == null || serverMeta.isBlank() ? "" : serverMeta + "\n\n") + placement;
                    CommandDialogs.showResult(loc, I18n.tr(loc, "cmd.info"), body);
                })));

        root.getChildren().add(section(I18n.tr(loc, "cmd.section.crud")));
        root.getChildren().add(primary(I18n.tr(loc, "table.add"), onAdd));
        root.getChildren().add(btn(I18n.tr(loc, "table.edit"), () -> {
            StudyGroup sel = controller.getStore().selectedGroupProperty().get();
            if (sel != null) {
                onEdit.accept(sel);
            } else {
                controller.setStatus(I18n.tr(loc, "msg.selectGroupFirst"));
            }
        }));
        root.getChildren().add(danger(I18n.tr(loc, "table.delete"), () -> {
            StudyGroup sel = controller.getStore().selectedGroupProperty().get();
            if (sel != null) {
                onDelete.accept(sel);
            } else {
                controller.setStatus(I18n.tr(loc, "msg.selectGroupFirst"));
            }
        }));
        root.getChildren().add(btn(I18n.tr(loc, "cmd.addIfMin"), () -> addWithDialog("add_if_min")));
        root.getChildren().add(btn(I18n.tr(loc, "cmd.removeLower"), () -> addWithDialog("remove_lower")));

        root.getChildren().add(section(I18n.tr(loc, "cmd.section.modify")));
        root.getChildren().add(btn(I18n.tr(loc, "cmd.clear"), () -> {
            if (CommandDialogs.confirm(loc, "cmd.confirm.clear")) {
                run("clear", null);
            }
        }));
        root.getChildren().add(btn(I18n.tr(loc, "cmd.removeFirst"), () -> {
            if (CommandDialogs.confirm(loc, "cmd.confirm.removeFirst")) {
                run("remove_first", null);
            }
        }));
        root.getChildren().add(btn(I18n.tr(loc, "cmd.removeById"), () -> {
            StudyGroup sel = controller.getStore().selectedGroupProperty().get();
            if (sel != null && sel.getId() != null) {
                if (CommandDialogs.confirm(loc, "cmd.confirm.delete")) {
                    run("remove_by_id " + sel.getId(), null);
                }
            } else {
                controller.setStatus(I18n.tr(loc, "msg.selectGroupFirst"));
            }
        }));

        root.getChildren().add(section(I18n.tr(loc, "cmd.section.filters")));
        root.getChildren().add(btn(I18n.tr(loc, "cmd.filterName"), () ->
                CommandDialogs.filterByName(loc).ifPresent(dto ->
                        controller.executeDto(dto, () -> afterFilterResult(loc)))));
        root.getChildren().add(btn(I18n.tr(loc, "cmd.filterSemester"), () ->
                CommandDialogs.filterBySemester(loc).ifPresent(dto ->
                        controller.executeDto(dto, () -> afterFilterResult(loc)))));
        root.getChildren().add(btn(I18n.tr(loc, "cmd.restoreFilter"), () ->
                controller.refreshCollection(() -> {
                    onShowTable.run();
                    onDataChanged.run();
                    controller.setStatus(I18n.tr(loc, "msg.collectionUpdated"));
                })));
        root.getChildren().add(btn(I18n.tr(loc, "cmd.printAdmins"), () ->
                controller.executeCommand("print_field_descending_group_admin", null, () -> {
                    String msg = controller.getStore().statusMessageProperty().get();
                    CommandDialogs.showResult(loc, I18n.tr(loc, "cmd.printAdmins"), msg);
                })));

        root.getChildren().add(section(I18n.tr(loc, "cmd.section.other")));
        root.getChildren().add(btn(I18n.tr(loc, "cmd.executeScript"), () ->
                CommandDialogs.executeScript(loc).ifPresent(dto ->
                        controller.executeDto(dto, () -> {
                            String msg = controller.getStore().statusMessageProperty().get();
                            CommandDialogs.showResult(loc, I18n.tr(loc, "cmd.executeScript"), msg);
                            onDataChanged.run();
                        }))));
        root.getChildren().add(btn(I18n.tr(loc, "cmd.refresh"), () ->
                controller.refreshCollection(() -> {
                    onDataChanged.run();
                    controller.setStatus(I18n.tr(loc, "msg.collectionUpdated"));
                })));

        return root;
    }

    private void afterFilterResult(java.util.Locale loc) {
        onShowTable.run();
        onDataChanged.run();
        String msg = controller.getStore().statusMessageProperty().get();
        CommandDialogs.showResult(loc, I18n.tr(loc, "cmd.section.filters"), msg);
    }

    private void addWithDialog(String command) {
        StudyGroupEditorDialog.show(null, controller.getSession().getLocale(), placement)
                .ifPresent(g -> run(command, g));
    }

    private void run(String cmd, StudyGroup payload) {
        controller.executeCommand(cmd, payload, onDataChanged);
    }

    private Label section(String text) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setStyle(AppStyles.SIDEBAR_SECTION);
        return l;
    }

    private Button btn(String text, Runnable action) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setWrapText(true);
        b.setStyle(AppStyles.SECONDARY_BUTTON + AppStyles.COMPACT_BUTTON);
        b.setOnAction(e -> action.run());
        return b;
    }

    private Button primary(String text, Runnable action) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setWrapText(true);
        b.setStyle(AppStyles.PRIMARY_BUTTON + AppStyles.COMPACT_BUTTON);
        b.setOnAction(e -> action.run());
        return b;
    }

    private Button danger(String text, Runnable action) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setWrapText(true);
        b.setStyle(AppStyles.DANGER_BUTTON + AppStyles.COMPACT_BUTTON);
        b.setOnAction(e -> action.run());
        return b;
    }

    public void refreshTexts() {
        setContent(buildContent());
    }
}
