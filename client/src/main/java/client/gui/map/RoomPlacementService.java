package client.gui.map;

import model.Coordinates;

import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

/**
 * Двунаправленное сопоставление: аудитория ↔ мировые координаты (для сервера).
 */
public class RoomPlacementService {

    private static final double NEAREST_ROOM_MAX_DIST_SQ = 0.04 * 0.04;

    private final FloorRoomMappingConfig config;

    public RoomPlacementService(FloorRoomMappingConfig config) {
        this.config = config;
    }

    public Coordinates toWorldCoordinates(int floor, String roomId) {
        BuildingRoomCatalog.RoomPick room = BuildingRoomCatalog.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown room: " + roomId));
        if (!BuildingRoomCatalog.matchesFloor(room, floor)) {
            throw new IllegalArgumentException("Room " + roomId + " is not on floor " + floor);
        }
        FloorRoomMappingConfig.FloorConfig floorCfg = config.floor(floor);
        if (floorCfg == null) {
            throw new IllegalArgumentException("Unknown floor: " + floor);
        }
        FloorRoomMappingConfig.WorldBounds b = floorCfg.worldBounds();
        double localX = room.centerX();
        double localY = room.centerY();
        int worldX = (int) Math.round(b.minX() + localX * (b.maxX() - b.minX()));
        double worldY = b.minY() + localY * (b.maxY() - b.minY());
        worldX = Math.max((int) b.minX(), Math.min((int) b.maxX(), worldX));
        worldY = Math.max(b.minY(), Math.min(b.maxY(), worldY));
        return new Coordinates(worldX, worldY);
    }

    public int floorFromWorldX(int worldX) {
        if (worldX < 200) {
            return 1;
        }
        if (worldX < 400) {
            return 2;
        }
        if (worldX < 600) {
            return 3;
        }
        if (worldX < 800) {
            return 4;
        }
        return 5;
    }

    public Optional<BuildingRoomCatalog.RoomPick> resolveRoom(int floor, Coordinates coordinates) {
        return resolveRoom(floor, coordinates, Locale.forLanguageTag("ru"));
    }

    public Optional<BuildingRoomCatalog.RoomPick> resolveRoom(int floor, Coordinates coordinates, Locale locale) {
        if (coordinates == null) {
            return Optional.empty();
        }
        FloorRoomMappingConfig.FloorConfig floorCfg = config.floor(floor);
        if (floorCfg == null) {
            return Optional.empty();
        }
        double[] local = toLocalNormalized(floorCfg, coordinates);
        double localX = local[0];
        double localY = local[1];

        Optional<BuildingRoomCatalog.RoomPick> hit = BuildingRoomCatalog.roomTemplates().stream()
                .filter(r -> r.containsBuilding(localX, localY))
                .min(Comparator
                        .comparingDouble(RoomPlacementService::zoneArea)
                        .thenComparingInt(RoomPlacementService::roomPriority));
        if (hit.isPresent()) {
            return Optional.of(BuildingRoomCatalog.forFloor(hit.get(), floor, locale));
        }

        return BuildingRoomCatalog.roomTemplates().stream()
                .min(Comparator.comparingDouble(r -> distanceSq(r, localX, localY)))
                .filter(r -> distanceSq(r, localX, localY) <= NEAREST_ROOM_MAX_DIST_SQ)
                .map(t -> BuildingRoomCatalog.forFloor(t, floor, locale));
    }

    public double[] toLocalNormalized(FloorRoomMappingConfig.FloorConfig floorCfg, Coordinates coordinates) {
        FloorRoomMappingConfig.WorldBounds b = floorCfg.worldBounds();
        double spanX = Math.max(1.0, b.maxX() - b.minX());
        double spanY = Math.max(1.0, b.maxY() - b.minY());
        double localX = (coordinates.getX() - b.minX()) / spanX;
        double localY = (coordinates.getY() - b.minY()) / spanY;
        return new double[] {localX, localY};
    }

    public String roomLabel(int floor, Coordinates coordinates) {
        return resolveRoom(floor, coordinates)
                .map(BuildingRoomCatalog.RoomPick::label)
                .orElse("—");
    }

    private static int roomPriority(BuildingRoomCatalog.RoomPick room) {
        return switch (room.id()) {
            case "CORRIDOR", "STOLOVAYA" -> 2;
            case "WC" -> 1;
            default -> 0;
        };
    }

    private static double zoneArea(BuildingRoomCatalog.RoomPick room) {
        return Math.max(0.0, room.normRight() - room.normLeft())
                * Math.max(0.0, room.normBottom() - room.normTop());
    }

    private static double distanceSq(BuildingRoomCatalog.RoomPick r, double lx, double ly) {
        double dx = r.centerX() - lx;
        double dy = r.centerY() - ly;
        return dx * dx + dy * dy;
    }
}
