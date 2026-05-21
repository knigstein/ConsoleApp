package client.gui.map;

import client.gui.I18n;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Аудитории корпуса (Ломоносова, 9): шаблоны зон на плане + номера по этажам.
 * Геометрия одна на все этажи; номер аудитории получают заменой второй цифры на номер этажа
 * (2119 на 1-м → 2219 на 2-м; 1003 на 1-м → 1203 на 2-м).
 */
public final class BuildingRoomCatalog {

    private static final List<RoomPick> TEMPLATES = buildRoomTemplates();

    private BuildingRoomCatalog() {
    }

    /** Шаблоны зон (номера как на плане 1-го этажа в каталоге). */
    public static List<RoomPick> roomTemplates() {
        return TEMPLATES;
    }

    /** @deprecated используйте {@link #roomTemplates()} */
    @Deprecated
    public static List<RoomPick> allRooms() {
        return roomTemplates();
    }

    public static List<RoomPick> roomsSorted() {
        return roomTemplates().stream()
                .sorted(Comparator.comparing(RoomPick::label))
                .toList();
    }

    /**
     * Все помещения на выбранном этаже (числовые — с подставленной второй цифрой этажа).
     */
    public static List<RoomPick> roomsForFloor(int floor) {
        return roomsForFloor(floor, Locale.forLanguageTag("ru"));
    }

    public static List<RoomPick> roomsForFloor(int floor, Locale locale) {
        if (floor < 1 || floor > 5) {
            return List.of();
        }
        return TEMPLATES.stream()
                .map(t -> forFloor(t, floor, locale))
                .sorted(Comparator.comparing(RoomPick::label))
                .toList();
    }

    /**
     * Этаж по номеру аудитории: вторая цифра (индекс 1). Блок 10xx (вторая цифра 0) — 1-й этаж.
     */
    public static int floorFromRoomId(String roomId) {
        if (roomId == null || roomId.length() < 2 || !isNumericRoomId(roomId)) {
            return -1;
        }
        char floorDigit = roomId.charAt(1);
        if (floorDigit == '0' && roomId.charAt(0) == '1') {
            return 1;
        }
        if (floorDigit >= '1' && floorDigit <= '5') {
            return floorDigit - '0';
        }
        return -1;
    }

    public static boolean matchesFloor(RoomPick room, int floor) {
        if (room == null || floor < 1 || floor > 5) {
            return false;
        }
        if (!isNumericRoomId(room.id())) {
            return true;
        }
        return floorFromRoomId(room.id()) == floor;
    }

    /**
     * Номер аудитории на целевом этаже (вторая цифра = этаж).
     */
    public static String roomIdForFloor(String templateId, int floor) {
        if (!isNumericRoomId(templateId)) {
            return templateId;
        }
        if (floor < 1 || floor > 5) {
            throw new IllegalArgumentException("floor must be 1..5");
        }
        if (floor == 1 && templateId.charAt(0) == '1' && templateId.charAt(1) == '0') {
            return templateId;
        }
        char[] chars = templateId.toCharArray();
        chars[1] = (char) ('0' + floor);
        return new String(chars);
    }

    public static RoomPick forFloor(RoomPick template, int floor) {
        return forFloor(template, floor, Locale.forLanguageTag("ru"));
    }

    public static RoomPick forFloor(RoomPick template, int floor, Locale locale) {
        if (template == null) {
            throw new IllegalArgumentException("template is null");
        }
        if (!isNumericRoomId(template.id())) {
            return new RoomPick(template.id(), template.localizedLabel(locale, floor),
                    template.normLeft(), template.normTop(), template.normRight(), template.normBottom(),
                    template.allowedFloors());
        }
        String id = roomIdForFloor(template.id(), floor);
        return new RoomPick(id, I18n.tr(locale, "room.auditorium", id),
                template.normLeft(), template.normTop(), template.normRight(), template.normBottom(),
                Set.of(floor));
    }

    public static Optional<RoomPick> findById(String roomId) {
        if (roomId == null) {
            return Optional.empty();
        }
        Optional<RoomPick> shared = TEMPLATES.stream()
                .filter(r -> r.id().equals(roomId) && !isNumericRoomId(roomId))
                .findFirst();
        if (shared.isPresent()) {
            return shared;
        }
        int floor = floorFromRoomId(roomId);
        if (floor < 1) {
            return Optional.empty();
        }
        for (RoomPick template : TEMPLATES) {
            if (!isNumericRoomId(template.id())) {
                continue;
            }
            if (roomIdForFloor(template.id(), floor).equals(roomId)) {
                return Optional.of(forFloor(template, floor, Locale.forLanguageTag("ru")));
            }
        }
        return Optional.empty();
    }

    public static List<FloorRoomMappingConfig.RoomZone> asRoomZones() {
        return TEMPLATES.stream()
                .map(r -> new FloorRoomMappingConfig.RoomZone(
                        r.id(), r.normLeft(), r.normTop(), r.normRight(), r.normBottom()))
                .toList();
    }

    private static boolean isNumericRoomId(String roomId) {
        for (int i = 0; i < roomId.length(); i++) {
            if (!Character.isDigit(roomId.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static List<RoomPick> buildRoomTemplates() {
        List<RoomPick> list = new ArrayList<>();
        list.add(room("4119", "Ауд. 4119", 0.03, 0.06, 0.10, 0.18));
        list.add(room("4101", "Ауд. 4101", 0.03, 0.18, 0.10, 0.30));
        list.add(room("4102", "Ауд. 4102", 0.03, 0.30, 0.10, 0.42));
        list.add(room("4103", "Ауд. 4103", 0.03, 0.42, 0.10, 0.54));
        list.add(room("4105", "Ауд. 4105", 0.03, 0.54, 0.10, 0.66));
        list.add(room("4106", "Ауд. 4106", 0.03, 0.66, 0.10, 0.78));
        list.add(room("4108", "Ауд. 4108", 0.10, 0.06, 0.16, 0.20));
        list.add(room("4110", "Ауд. 4110", 0.10, 0.20, 0.16, 0.34));
        list.add(room("4113", "Ауд. 4113", 0.10, 0.34, 0.16, 0.50));

        list.add(room("2152", "Ауд. 2152", 0.18, 0.06, 0.26, 0.16));
        list.add(room("2153", "Ауд. 2153", 0.26, 0.06, 0.34, 0.16));
        list.add(room("2154", "Ауд. 2154", 0.34, 0.06, 0.42, 0.16));
        list.add(room("2155", "Ауд. 2155", 0.42, 0.06, 0.50, 0.16));
        list.add(room("2156", "Ауд. 2156", 0.50, 0.06, 0.58, 0.16));
        list.add(room("2157", "Ауд. 2157", 0.58, 0.06, 0.66, 0.16));
        list.add(room("2158", "Ауд. 2158", 0.66, 0.06, 0.74, 0.16));

        list.add(room("2129", "Ауд. 2129", 0.18, 0.18, 0.26, 0.30));
        list.add(room("2127", "Ауд. 2127", 0.26, 0.18, 0.34, 0.30));
        list.add(room("2125", "Ауд. 2125", 0.34, 0.18, 0.42, 0.30));
        list.add(room("2120", "Ауд. 2120", 0.42, 0.18, 0.50, 0.30));
        list.add(room("2119", "Ауд. 2119", 0.50, 0.18, 0.58, 0.30));
        list.add(room("2117", "Ауд. 2117", 0.58, 0.18, 0.66, 0.30));
        list.add(room("2115", "Ауд. 2115", 0.66, 0.18, 0.74, 0.30));
        list.add(room("WC", "Санузел", 0.74, 0.22, 0.80, 0.28));

        list.add(room("2112", "Ауд. 2112", 0.18, 0.32, 0.28, 0.44));
        list.add(room("2113", "Ауд. 2113", 0.28, 0.32, 0.38, 0.44));
        list.add(room("2114", "Ауд. 2114", 0.38, 0.32, 0.48, 0.44));
        list.add(room("2116", "Ауд. 2116", 0.48, 0.32, 0.58, 0.44));

        list.add(room("1001", "Ауд. 1001", 0.76, 0.06, 0.84, 0.16));
        list.add(room("1002", "Ауд. 1002", 0.84, 0.06, 0.92, 0.16));
        list.add(room("1003", "Ауд. 1003", 0.76, 0.16, 0.84, 0.26));
        list.add(room("1004", "Ауд. 1004", 0.84, 0.16, 0.92, 0.26));
        list.add(room("1005", "Ауд. 1005", 0.76, 0.26, 0.84, 0.36));
        list.add(room("1006", "Ауд. 1006", 0.84, 0.26, 0.92, 0.36));
        list.add(room("1019", "Ауд. 1019", 0.76, 0.36, 0.84, 0.46));
        list.add(room("1123", "Ауд. 1123", 0.84, 0.36, 0.92, 0.46));
        list.add(room("1124", "Ауд. 1124", 0.76, 0.46, 0.84, 0.56));
        list.add(room("1132", "Ауд. 1132", 0.84, 0.46, 0.92, 0.56));

        list.add(room("3112", "Ауд. 3112", 0.18, 0.48, 0.30, 0.58));
        list.add(room("3110", "Ауд. 3110", 0.30, 0.48, 0.42, 0.58));
        list.add(room("3109", "Ауд. 3109", 0.42, 0.48, 0.54, 0.58));
        list.add(room("3107", "Ауд. 3107", 0.54, 0.48, 0.66, 0.58));
        list.add(room("3101", "Ауд. 3101", 0.66, 0.48, 0.76, 0.58));
        list.add(room("STOLOVAYA", "Столовая", 0.54, 0.60, 0.92, 0.88));
        list.add(room("CORRIDOR", "Коридор (нижний)", 0.18, 0.60, 0.54, 0.88));

        return List.copyOf(list);
    }

    private static RoomPick room(String id, String label, double left, double top, double right, double bottom) {
        return new RoomPick(id, label, left, top, right, bottom, Set.of(1, 2, 3, 4, 5));
    }

    public record RoomPick(
            String id,
            String label,
            double normLeft,
            double normTop,
            double normRight,
            double normBottom,
            Set<Integer> allowedFloors
    ) {
        public double centerX() {
            return (normLeft + normRight) / 2.0;
        }

        public double centerY() {
            return (normTop + normBottom) / 2.0;
        }

        public boolean containsBuilding(double localX, double localY) {
            return localX >= normLeft && localX <= normRight
                    && localY >= normTop && localY <= normBottom;
        }

        /** Подпись для UI с учётом этажа (вторая цифра номера). */
        public String localizedLabel(Locale locale, int floor) {
            if (!isNumericRoomId(id)) {
                return switch (id) {
                    case "STOLOVAYA" -> I18n.tr(locale, "room.cafeteria");
                    case "WC" -> I18n.tr(locale, "room.wc");
                    case "CORRIDOR" -> I18n.tr(locale, "room.corridor");
                    default -> id;
                };
            }
            String displayId = floorFromRoomId(id) == floor ? id : roomIdForFloor(id, floor);
            return I18n.tr(locale, "room.auditorium", displayId);
        }
    }
}
