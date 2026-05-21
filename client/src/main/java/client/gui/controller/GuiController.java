package client.gui.controller;

import client.gui.AppSession;
import client.gui.I18n;
import client.gui.map.BuildingRoomCatalog;
import client.gui.map.RoomPlacementService;
import client.gui.net.ClientGateway;
import client.gui.store.StudyGroupStore;
import client.gui.util.FxTasks;
import client.gui.view.dialogs.CommandDialogs;
import common.dto.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import model.Semester;
import model.StudyGroup;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import model.Color;
import model.Country;
import model.Coordinates;
import model.Person;

public class GuiController {

    private static final Duration POLL_INTERVAL = Duration.seconds(5);

    private final AppSession session = new AppSession();
    private final StudyGroupStore store = new StudyGroupStore();
    private ClientGateway gateway;
    private boolean mockMode;
    private Timeline polling;
    private final AtomicBoolean fetchInProgress = new AtomicBoolean(false);
    private final AtomicBoolean commandInProgress = new AtomicBoolean(false);
    private Consumer<String> statusListener = msg -> {};
    /** Полная mock-коллекция */
    private List<StudyGroup> mockMasterList = List.of();

    public AppSession getSession() {
        return session;
    }

    public StudyGroupStore getStore() {
        return store;
    }

    public boolean isMockMode() {
        return mockMode;
    }

    public void setStatusListener(Consumer<String> listener) {
        this.statusListener = listener == null ? msg -> {} : listener;
    }

    public void setStatus(String message) {
        statusListener.accept(message == null ? "" : message);
    }

    public void connect(String host, int port) throws Exception {
        mockMode = false;
        gateway = new ClientGateway(host, port);
    }

    public void enableMockMode() {
        mockMode = true;
        gateway = null;
        setStatus(I18n.tr(session.getLocale(), "msg.mockMode"));
    }

    /** Called after successful login/register in mock mode. */
    public void completeMockLogin(String login, String password) {
        session.authorize(login, password, 1);
        loadMockCollection();
    }

    public CommandResponseDTO register(String login, String password) throws Exception {
        if (!commandInProgress.compareAndSet(false, true)) {
            throw new IllegalStateException(I18n.tr(session.getLocale(), "msg.commandInProgress"));
        }
        try {
            CommandResponseDTO r = gateway.register(login, password);
            if (r.getStatus() == ResponseStatus.SUCCESS) {
                session.authorize(login, password, r.getUserId());
            }
            return r;
        } finally {
            commandInProgress.set(false);
        }
    }

    public CommandResponseDTO login(String login, String password) throws Exception {
        if (!commandInProgress.compareAndSet(false, true)) {
            throw new IllegalStateException(I18n.tr(session.getLocale(), "msg.commandInProgress"));
        }
        try {
            CommandResponseDTO r = gateway.login(login, password);
            if (r.getStatus() == ResponseStatus.SUCCESS) {
                session.authorize(login, password, r.getUserId());
            }
            return r;
        } finally {
            commandInProgress.set(false);
        }
    }

    public void logout() {
        stopPolling();
        session.logout();
        mockMasterList = List.of();
        store.replaceAll(List.of());
        store.selectedGroupProperty().set(null);
        if (gateway != null) {
            gateway.setCredentials(null, null);
        }
    }

    public void refreshCollection(Runnable onDone) {
        if (!session.isAuthorized()) {
            return;
        }
        if (mockMode) {
            store.replaceAll(mockMasterList);
            if (onDone != null) {
                onDone.run();
            }
            return;
        }
        if (commandInProgress.get()) {
            return;
        }
        if (!fetchInProgress.compareAndSet(false, true)) {
            return;
        }
        FxTasks.runAsync(
                () -> gateway.loadCollection(),
                list -> {
                    try {
                        store.replaceAll(list);
                        setStatus(I18n.tr(session.getLocale(), "msg.collectionUpdated")
                                + ": " + list.size()
                                + " | " + I18n.formatDateTime(session.getLocale(), LocalDateTime.now()));
                        if (onDone != null) {
                            onDone.run();
                        }
                    } catch (RuntimeException e) {
                        setStatus(I18n.tr(session.getLocale(), "msg.loadFailed") + ": " + e.getMessage());
                    } finally {
                        fetchInProgress.set(false);
                    }
                },
                err -> {
                    setStatus(I18n.tr(session.getLocale(), "msg.loadFailed") + ": " + err.getMessage());
                    fetchInProgress.set(false);
                }
        );
    }

    public void startPolling(Runnable onRefresh) {
        stopPolling();
        if (mockMode || !session.isAuthorized()) {
            return;
        }
        polling = new Timeline(new KeyFrame(POLL_INTERVAL, e -> refreshCollection(onRefresh)));
        polling.setCycleCount(Timeline.INDEFINITE);
        polling.play();
    }

    public void stopPolling() {
        if (polling != null) {
            polling.stop();
            polling = null;
        }
    }

    public void executeCommand(String cmd, StudyGroup payload, Runnable onSuccess) {
        if (!session.isAuthorized()) {
            setStatus(I18n.tr(session.getLocale(), "msg.loginRequired"));
            return;
        }
        if (mockMode) {
            runMockCommand(cmd, payload);
            if (onSuccess != null) {
                onSuccess.run();
            }
            return;
        }
        if (!commandInProgress.compareAndSet(false, true)) {
            setStatus(I18n.tr(session.getLocale(), "msg.commandInProgress"));
            return;
        }
        FxTasks.runAsync(
                () -> {
                    CommandDTO dto = gateway.parseManualCommand(cmd, payload);
                    if (dto == null) {
                        throw new IllegalArgumentException(I18n.tr(session.getLocale(), "msg.unknownCommand"));
                    }
                    CommandResponseDTO r = gateway.sendAuthorized(dto);
                    if (r.getStatus() != ResponseStatus.SUCCESS) {
                        throw new IllegalStateException(
                                r.getMessage() == null ? I18n.tr(session.getLocale(), "msg.commandFailed") : r.getMessage());
                    }
                    List<StudyGroup> collection = r.getCollection();
                    if (collection == null && modifiesCollection(cmd)) {
                        collection = gateway.loadCollection();
                    }
                    return new CommandResult(r.getMessage(), collection);
                },
                result -> {
                    applyCommandResult(result);
                    commandInProgress.set(false);
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                    schedulePeerRefresh(onSuccess);
                },
                err -> {
                    commandInProgress.set(false);
                    String msg = err.getMessage() == null ? "" : err.getMessage();
                    setStatus(msg);
                    CommandDialogs.showError(session.getLocale(), I18n.tr(session.getLocale(), "msg.commandFailed"), msg);
                }
        );
    }

    public void executeDto(CommandDTO dto, Runnable onSuccess) {
        if (!session.isAuthorized()) {
            setStatus(I18n.tr(session.getLocale(), "msg.loginRequired"));
            return;
        }
        if (mockMode) {
            runMockDto(dto);
            if (onSuccess != null) {
                onSuccess.run();
            }
            return;
        }
        if (gateway == null) {
            return;
        }
        if (!commandInProgress.compareAndSet(false, true)) {
            setStatus(I18n.tr(session.getLocale(), "msg.commandInProgress"));
            return;
        }
        FxTasks.runAsync(
                () -> {
                    CommandResponseDTO r = gateway.sendAuthorized(dto);
                    if (r.getStatus() != ResponseStatus.SUCCESS) {
                        throw new IllegalStateException(
                                r.getMessage() == null ? I18n.tr(session.getLocale(), "msg.commandFailed") : r.getMessage());
                    }
                    List<StudyGroup> collection = r.getCollection();
                    if (collection == null && dto instanceof ShowCommandDTO) {
                        collection = gateway.loadCollection();
                    }
                    return new CommandResult(r.getMessage(), collection);
                },
                result -> {
                    applyCommandResult(result);
                    commandInProgress.set(false);
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                    schedulePeerRefresh(onSuccess);
                },
                err -> {
                    commandInProgress.set(false);
                    String msg = err.getMessage() == null ? "" : err.getMessage();
                    setStatus(msg);
                    CommandDialogs.showError(session.getLocale(), I18n.tr(session.getLocale(), "msg.commandFailed"), msg);
                }
        );
    }

    /** Дополнительный poll через 2 с — чтобы второй GUI быстрее увидел изменения. */
    private void schedulePeerRefresh(Runnable onRefresh) {
        if (mockMode || !session.isAuthorized() || onRefresh == null) {
            return;
        }
        Timeline once = new Timeline(new KeyFrame(Duration.seconds(2), e -> refreshCollection(onRefresh)));
        once.play();
    }

    private void applyCommandResult(CommandResult result) {
        if (result.collection() != null) {
            store.replaceAll(result.collection());
        }
        String full = result.message() == null ? "" : result.message();
        store.setStatusMessage(full);
        setStatus(summarizeForStatusBar(full));
    }

    /**
     * Полный текст — в диалогах ({@link client.gui.store.StudyGroupStore#statusMessageProperty()}),
     * в строке состояния — одна короткая строка (без «простыни» имён).
     */
    private String summarizeForStatusBar(String message) {
        if (message == null || message.isBlank()) {
            return "";
        }
        if (message.contains("\n")) {
            long lines = message.lines().filter(l -> !l.isBlank()).count();
            return I18n.tr(session.getLocale(), "msg.multilineResult", lines);
        }
        if (message.length() > 140) {
            return message.substring(0, 137) + "...";
        }
        return message;
    }

    public boolean canEdit(StudyGroup sg) {
        if (sg == null || sg.getOwnerId() == null) {
            return false;
        }
        Integer uid = session.getUserId();
        return uid != null && sg.getOwnerId().equals(uid);
    }

    public void close() {
        stopPolling();
        if (gateway != null) {
            try {
                gateway.close();
            } catch (Exception ignored) {
                // no-op
            }
        }
    }

    private static boolean modifiesCollection(String cmd) {
        if (cmd == null) {
            return false;
        }
        String op = cmd.trim().split("\\s+")[0];
        return switch (op) {
            case "add", "add_if_min", "update", "clear", "remove_first", "remove_by_id", "remove_lower" -> true;
            default -> false;
        };
    }

    private record CommandResult(String message, List<StudyGroup> collection) {
    }

    private void loadMockCollection() {
        int owner = session.getUserId() == null ? 1 : session.getUserId();
        RoomPlacementService placement = new RoomPlacementService(
                client.gui.map.FloorRoomMappingConfig.defaultConfig(
                        client.gui.DeployPathResolver.resolveDeployRoot()));
        List<StudyGroup> sample = new ArrayList<>();
        // templateRoomId — id из каталога; номер на этаже = roomIdForFloor(template, floor)
        sample.add(mockGroup(placement, 1, 1, "2125", "Alpha", 30, Semester.FIRST, 1, "Nika"));
        sample.add(mockGroup(placement, 2, 2, "1003", "Beta", 20, Semester.SECOND, 2, "Pavel"));
        sample.add(mockGroup(placement, 3, 3, "2125", "Gamma", 15, Semester.THIRD, owner, "Ivan"));
        sample.add(mockGroup(placement, 4, 4, "2112", "Delta", 45, Semester.FIFTH, 3, "Ilona"));
        sample.add(mockGroup(placement, 5, 5, "3107", "Epsilon", 12, Semester.FOURTH, owner, "Sergey"));
        sample.add(mockGroup(placement, 6, 1, "4101", "Zeta", 27, Semester.SIXTH, 4, "Anna"));
        sample.add(mockGroup(placement, 7, 2, "2158", "Eta", 9, Semester.SEVENTH, owner, "Kate"));
        sample.add(mockGroup(placement, 8, 3, "2129", "Theta", 33, Semester.EIGHTH, 5, "Oleg"));
        sample.add(mockGroup(placement, 9, 4, "CORRIDOR", "Iota", 18, Semester.SECOND, owner, "Roma"));
        sample.add(mockGroup(placement, 10, 5, "STOLOVAYA", "Kappa", 50, Semester.THIRD, 2, "Lina"));
        sample.add(mockGroup(placement, 11, 1, "4119", "Lambda", 21, Semester.FIRST, owner, "Masha"));
        sample.add(mockGroup(placement, 12, 3, "3110", "Mu", 16, Semester.FIFTH, 3, "Dima"));
        mockMasterList = new ArrayList<>(sample);
        store.replaceAll(mockMasterList);
    }

    private StudyGroup mockGroup(
            RoomPlacementService placement,
            int id,
            int floor,
            String templateRoomId,
            String name,
            int students,
            Semester semester,
            int owner,
            String adminName
    ) {
        String roomId = BuildingRoomCatalog.roomIdForFloor(templateRoomId, floor);
        Coordinates coords = placement.toWorldCoordinates(floor, roomId);
        StudyGroup sg = new StudyGroup(
                id,
                name,
                coords,
                LocalDate.now().minusDays(id),
                students,
                1L,
                1,
                semester,
                new Person(adminName, Date.from(LocalDate.now().minusYears(23).atStartOfDay(ZoneId.systemDefault()).toInstant()), Color.GREEN, Country.GERMANY)
        );
        sg.setOwnerId(owner);
        return sg;
    }

    private void runMockCommand(String cmd, StudyGroup payload) {
        String raw = cmd == null ? "" : cmd.trim();
        if (raw.isBlank()) {
            return;
        }
        List<StudyGroup> list = new ArrayList<>(mockMasterList);
        String[] parts = raw.split("\\s+");
        String op = parts[0];
        int owner = session.getUserId() == null ? 1 : session.getUserId();

        switch (op) {
            case "show", "info" -> {
                String msg = "Mock: " + op + " — " + list.size() + " " + I18n.tr(session.getLocale(), "table.rows");
                setStatus(msg);
                store.setStatusMessage(msg);
            }
            case "clear" -> {
                list.removeIf(sg -> owner == (sg.getOwnerId() == null ? -1 : sg.getOwnerId()));
                setStatus("Mock: cleared owned groups");
            }
            case "remove_first" -> list.stream().filter(sg -> owner == (sg.getOwnerId() == null ? -1 : sg.getOwnerId()))
                    .findFirst().ifPresent(list::remove);
            case "remove_by_id" -> {
                if (parts.length >= 2) {
                    int id = Integer.parseInt(parts[1]);
                    list.removeIf(sg -> sg.getId() != null && sg.getId() == id);
                }
            }
            case "add", "add_if_min" -> {
                if (payload != null) {
                    int nextId = list.stream().map(StudyGroup::getId).filter(i -> i != null).max(Integer::compareTo).orElse(0) + 1;
                    payload.setId(nextId);
                    payload.setOwnerId(owner);
                    list.add(payload);
                }
            }
            case "update" -> {
                if (parts.length >= 2 && payload != null) {
                    int id = Integer.parseInt(parts[1]);
                    list.removeIf(sg -> sg.getId() != null && sg.getId() == id);
                    payload.setId(id);
                    payload.setOwnerId(owner);
                    list.add(payload);
                }
            }
            case "remove_lower" -> {
                if (payload != null) {
                    list.removeIf(sg -> (sg.getOwnerId() == null ? -1 : sg.getOwnerId()) == owner
                            && sg.compareTo(payload) < 0);
                }
            }
            case "print_field_descending_group_admin" -> {
                String body = list.stream()
                        .map(g -> g.getGroupAdmin() == null ? "" : g.getGroupAdmin().getName())
                        .filter(n -> n != null && !n.isBlank())
                        .sorted(java.util.Comparator.reverseOrder())
                        .reduce((a, b) -> a + System.lineSeparator() + b)
                        .orElse(I18n.tr(session.getLocale(), "msg.noAdmins"));
                store.setStatusMessage(body);
                setStatus(summarizeForStatusBar(body));
            }
            default -> setStatus(I18n.tr(session.getLocale(), "msg.unknownCommand"));
        }
        mockMasterList = new ArrayList<>(list);
        store.replaceAll(mockMasterList);
    }

    private void runMockDto(CommandDTO dto) {
        List<StudyGroup> source = new ArrayList<>(mockMasterList);
        if (dto instanceof FilterContainsNameCommandDTO f) {
            String sub = f.getSubstring().toLowerCase();
            List<StudyGroup> filtered = source.stream()
                    .filter(g -> g.getName() != null && g.getName().toLowerCase().contains(sub))
                    .toList();
            store.replaceAll(filtered);
            String msg = I18n.tr(session.getLocale(), "msg.filterResult", filtered.size());
            store.setStatusMessage(msg);
            setStatus(msg);
        } else if (dto instanceof FilterGreaterThanSemesterCommandDTO f) {
            Semester sem = f.getSemester();
            List<StudyGroup> filtered = source.stream()
                    .filter(g -> g.getSemesterEnum() != null && g.getSemesterEnum().compareTo(sem) > 0)
                    .toList();
            store.replaceAll(filtered);
            String msg = I18n.tr(session.getLocale(), "msg.filterResult", filtered.size());
            store.setStatusMessage(msg);
            setStatus(msg);
        } else if (dto instanceof ExecuteScriptCommandDTO) {
            String msg = I18n.tr(session.getLocale(), "msg.mockScript");
            store.setStatusMessage(msg);
            setStatus(msg);
        }
    }
}
