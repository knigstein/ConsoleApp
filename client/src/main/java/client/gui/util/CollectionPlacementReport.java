package client.gui.util;

import client.gui.I18n;
import client.gui.map.BuildingRoomCatalog;
import client.gui.map.RoomPlacementService;
import model.StudyGroup;

import java.util.*;

/**
 * Отчёт «кто в какой аудитории» для команды info
 */
public final class CollectionPlacementReport {

    private CollectionPlacementReport() {
    }

    public static String build(List<StudyGroup> groups, RoomPlacementService placement, Locale locale) {
        if (groups == null || groups.isEmpty()) {
            return I18n.tr(locale, "info.placement.empty");
        }

        record Entry(int floor, String roomLabel, StudyGroup group) {
        }

        List<Entry> entries = new ArrayList<>();
        for (StudyGroup g : groups) {
            if (g == null || g.getCoordinates() == null) {
                continue;
            }
            int floor = placement.floorFromWorldX(g.getCoordinates().getX());
            String room = placement.resolveRoom(floor, g.getCoordinates(), locale)
                    .map(r -> r.localizedLabel(locale, floor))
                    .orElse("—");
            entries.add(new Entry(floor, room, g));
        }

        entries.sort(Comparator
                .comparingInt(Entry::floor)
                .thenComparing(Entry::roomLabel)
                .thenComparing(e -> e.group().getName() == null ? "" : e.group().getName()));

        StringBuilder sb = new StringBuilder();
        sb.append(I18n.tr(locale, "info.placement.header")).append('\n');
        sb.append(I18n.tr(locale, "info.placement.count", entries.size())).append("\n\n");

        String currentKey = null;
        for (Entry e : entries) {
            String key = e.floor() + "|" + e.roomLabel();
            if (!key.equals(currentKey)) {
                if (currentKey != null) {
                    sb.append('\n');
                }
                sb.append(I18n.tr(locale, "info.placement.roomLine", e.floor(), e.roomLabel())).append('\n');
                currentKey = key;
            }
            StudyGroup g = e.group();
            String owner = g.getOwnerId() == null ? "—" : String.valueOf(g.getOwnerId());
            sb.append("  • ")
                    .append(g.getName() == null ? "—" : g.getName())
                    .append(" (id=").append(g.getId())
                    .append(", ").append(I18n.tr(locale, "info.placement.owner")).append(": ").append(owner)
                    .append(", ").append(I18n.tr(locale, "info.placement.students"))
                    .append(": ").append(g.getStudentsCount())
                    .append(")\n");
        }
        return sb.toString();
    }
}
