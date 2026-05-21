package client.gui.store;

import client.gui.map.CoordinateToRoomService;

import java.util.Locale;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import model.Semester;
import model.StudyGroup;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Shared UI store for map and table.
 */
public class StudyGroupStore {

    private final ObservableList<StudyGroup> source = FXCollections.observableArrayList();
    private final FilteredList<StudyGroup> filtered = new FilteredList<>(source, s -> true);

    private final StudyGroupFilters filters = new StudyGroupFilters();

    private final StringProperty sortBy = new SimpleStringProperty("id");
    private final BooleanProperty sortAscending = new SimpleBooleanProperty(true);
    private final IntegerProperty selectedFloor = new SimpleIntegerProperty(1);
    private final IntegerProperty pageSize = new SimpleIntegerProperty(10);
    private final IntegerProperty currentPage = new SimpleIntegerProperty(1);
    private final ObjectProperty<StudyGroup> selectedGroup = new SimpleObjectProperty<>(null);
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private CoordinateToRoomService projectionService;
    private Locale displayLocale = Locale.forLanguageTag("ru");

    public void setDisplayLocale(Locale locale) {
        if (locale != null) {
            displayLocale = locale;
        }
    }

    public StudyGroupStore() {
        filters.idContainsProperty().addListener((obs, o, n) -> applyFiltersAndResetPage());
        filters.nameContainsProperty().addListener((obs, o, n) -> applyFiltersAndResetPage());
        filters.coordXContainsProperty().addListener((obs, o, n) -> applyFiltersAndResetPage());
        filters.coordYContainsProperty().addListener((obs, o, n) -> applyFiltersAndResetPage());
        filters.creationDateContainsProperty().addListener((obs, o, n) -> applyFiltersAndResetPage());
        filters.studentsCountContainsProperty().addListener((obs, o, n) -> applyFiltersAndResetPage());
        filters.expelledStudentsContainsProperty().addListener((obs, o, n) -> applyFiltersAndResetPage());
        filters.transferredStudentsContainsProperty().addListener((obs, o, n) -> applyFiltersAndResetPage());
        filters.ownerContainsProperty().addListener((obs, o, n) -> applyFiltersAndResetPage());
        filters.adminNameContainsProperty().addListener((obs, o, n) -> applyFiltersAndResetPage());
        filters.adminBirthdayContainsProperty().addListener((obs, o, n) -> applyFiltersAndResetPage());
        filters.semesterProperty().addListener((obs, o, n) -> applyFiltersAndResetPage());
        filters.eyeColorProperty().addListener((obs, o, n) -> applyFiltersAndResetPage());
        filters.nationalityProperty().addListener((obs, o, n) -> applyFiltersAndResetPage());
        filters.roomContainsProperty().addListener((obs, o, n) -> applyFiltersAndResetPage());
        sortBy.addListener((obs, o, n) -> applyFiltersAndResetPage());
        sortAscending.addListener((obs, o, n) -> applyFiltersAndResetPage());
    }

    public ObservableList<StudyGroup> source() {
        return source;
    }

    public StudyGroupFilters filters() {
        return filters;
    }

    public StringProperty sortByProperty() {
        return sortBy;
    }

    public BooleanProperty sortAscendingProperty() {
        return sortAscending;
    }

    public IntegerProperty selectedFloorProperty() {
        return selectedFloor;
    }

    public IntegerProperty pageSizeProperty() {
        return pageSize;
    }

    public IntegerProperty currentPageProperty() {
        return currentPage;
    }

    public ObjectProperty<StudyGroup> selectedGroupProperty() {
        return selectedGroup;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public void setStatusMessage(String msg) {
        statusMessage.set(msg == null ? "" : msg);
    }

    public void setProjectionService(CoordinateToRoomService projectionService) {
        this.projectionService = projectionService;
    }

    public void replaceAll(List<StudyGroup> groups) {
        Integer selectedId = selectedGroup.get() == null ? null : selectedGroup.get().getId();
        int page = currentPage.get();
        List<StudyGroup> next = groups == null ? List.of() : groups;
        source.setAll(next);
        if (selectedId != null) {
            next.stream()
                    .filter(g -> g != null && selectedId.equals(g.getId()))
                    .findFirst()
                    .ifPresentOrElse(selectedGroup::set, () -> selectedGroup.set(null));
        }
        applyFiltersAndResetPage();
        if (page > 1 && page <= pageCount()) {
            currentPage.set(page);
        }
    }

    public List<StudyGroup> getFilteredSorted() {
        Predicate<StudyGroup> predicate = sg -> {
            if (sg.getCoordinates() == null || sg.getGroupAdmin() == null) {
                return false;
            }
            if (!containsValue(sg.getId(), filters.idContainsProperty().get())) {
                return false;
            }
            String name = filters.nameContainsProperty().get();
            if (name != null && !name.isBlank()) {
                if (sg.getName() == null || !sg.getName().toLowerCase().contains(name.trim().toLowerCase())) {
                    return false;
                }
            }
            if (!containsValue(sg.getCoordinates().getX(), filters.coordXContainsProperty().get())) {
                return false;
            }
            if (!containsValue(sg.getCoordinates().getY(), filters.coordYContainsProperty().get())) {
                return false;
            }
            if (!containsValue(sg.getCreationDate(), filters.creationDateContainsProperty().get())) {
                return false;
            }
            if (!containsValue(sg.getStudentsCount(), filters.studentsCountContainsProperty().get())) {
                return false;
            }
            if (!containsValue(sg.getExpelledStudents(), filters.expelledStudentsContainsProperty().get())) {
                return false;
            }
            if (!containsValue(sg.getTransferredStudents(), filters.transferredStudentsContainsProperty().get())) {
                return false;
            }
            String owner = filters.ownerContainsProperty().get();
            if (owner != null && !owner.isBlank()) {
                String ownerId = sg.getOwnerId() == null ? "" : String.valueOf(sg.getOwnerId());
                if (!ownerId.contains(owner.trim())) {
                    return false;
                }
            }
            if (!containsValue(adminName(sg), filters.adminNameContainsProperty().get())) {
                return false;
            }
            if (!containsValue(adminBirthday(sg), filters.adminBirthdayContainsProperty().get())) {
                return false;
            }
            Semester sem = filters.semesterProperty().get();
            if (!(sem == null || sem == sg.getSemesterEnum())) {
                return false;
            }
            if (filters.eyeColorProperty().get() != null && filters.eyeColorProperty().get() != adminEyeColor(sg)) {
                return false;
            }
            if (filters.nationalityProperty().get() != null && filters.nationalityProperty().get() != adminNationality(sg)) {
                return false;
            }
            if (!containsValue(roomName(sg), filters.roomContainsProperty().get())) {
                return false;
            }
            return true;
        };

        Comparator<StudyGroup> comparator = switch (sortBy.get()) {
            case "name" -> Comparator.comparing(StudyGroup::getName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "coordX" -> Comparator.comparing(s -> coordinatesX(s), Comparator.nullsLast(Integer::compareTo));
            case "coordY" -> Comparator.comparing(this::coordinatesY, Comparator.nullsLast(Double::compareTo));
            case "expelledStudents" -> Comparator.comparing(StudyGroup::getExpelledStudents, Comparator.nullsLast(Long::compareTo));
            case "transferredStudents" -> Comparator.comparingInt(StudyGroup::getTransferredStudents);
            case "studentsCount" -> Comparator.comparingInt(StudyGroup::getStudentsCount);
            case "ownerId" -> Comparator.comparing(StudyGroup::getOwnerId, Comparator.nullsLast(Integer::compareTo));
            case "semesterEnum" -> Comparator.comparing(StudyGroup::getSemesterEnum, Comparator.nullsLast(Enum::compareTo));
            case "creationDate" -> Comparator.comparing(StudyGroup::getCreationDate, Comparator.nullsLast(java.time.LocalDate::compareTo));
            case "adminName" -> Comparator.comparing(this::adminName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "adminBirthday" -> Comparator.comparing(this::adminBirthday, Comparator.nullsLast(java.util.Date::compareTo));
            case "eyeColor" -> Comparator.comparing(this::adminEyeColor, Comparator.nullsLast(Enum::compareTo));
            case "nationality" -> Comparator.comparing(this::adminNationality, Comparator.nullsLast(Enum::compareTo));
            case "room" -> Comparator.comparing(this::roomName, Comparator.nullsLast(String::compareToIgnoreCase));
            default -> Comparator.comparing(StudyGroup::getId, Comparator.nullsLast(Integer::compareTo));
        };

        if (!sortAscending.get()) {
            comparator = comparator.reversed();
        }

        return source.stream()
                .filter(Objects::nonNull)
                .filter(predicate)
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public List<StudyGroup> getCurrentPageRows() {
        List<StudyGroup> all = getFilteredSorted();
        int size = Math.max(1, pageSize.get());
        int from = Math.max(0, (currentPage.get() - 1) * size);
        int to = Math.min(all.size(), from + size);
        if (from >= all.size()) {
            return List.of();
        }
        return all.subList(from, to);
    }

    public int pageCount() {
        int total = getFilteredSorted().size();
        int size = Math.max(1, pageSize.get());
        return Math.max(1, (int) Math.ceil((double) total / size));
    }

    public void nextPage() {
        currentPage.set(Math.min(pageCount(), currentPage.get() + 1));
    }

    public void prevPage() {
        currentPage.set(Math.max(1, currentPage.get() - 1));
    }

    public void setPage(int page) {
        currentPage.set(Math.max(1, Math.min(page, pageCount())));
    }

    public void applyFiltersAndResetPage() {
        filtered.setPredicate(s -> true);
        currentPage.set(1);
    }

    public List<StudyGroup> getFilteredForMap() {
        return getFilteredSorted();
    }

    private boolean containsValue(Object value, String token) {
        if (token == null || token.isBlank()) {
            return true;
        }
        return String.valueOf(value == null ? "" : value).toLowerCase().contains(token.trim().toLowerCase());
    }

    private String roomName(StudyGroup sg) {
        if (projectionService == null) {
            return "UNKNOWN_ROOM";
        }
        try {
            return projectionService.project(sg, 800, 566, displayLocale).roomName();
        } catch (Exception e) {
            return "UNKNOWN_ROOM";
        }
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

    private model.Color adminEyeColor(StudyGroup sg) {
        return sg.getGroupAdmin() == null ? null : sg.getGroupAdmin().getEyeColor();
    }

    private model.Country adminNationality(StudyGroup sg) {
        return sg.getGroupAdmin() == null ? null : sg.getGroupAdmin().getNationality();
    }
}
