package client.gui.map;

import javafx.geometry.Point2D;
import model.Coordinates;
import model.StudyGroup;

import java.util.Locale;
import java.util.Optional;

/**
 * Отображение StudyGroup на плане: мировые координаты → аудитория → точка внутри зоны на изображении.
 */
public class CoordinateToRoomService {

    private final FloorRoomMappingConfig config;
    private final RoomPlacementService placement;

    public CoordinateToRoomService(FloorRoomMappingConfig config) {
        this.config = config;
        this.placement = new RoomPlacementService(config);
    }

    public FloorProjection project(StudyGroup group, double imageWidth, double imageHeight) {
        return project(group, imageWidth, imageHeight, Locale.forLanguageTag("ru"));
    }

    public FloorProjection project(StudyGroup group, double imageWidth, double imageHeight, Locale locale) {
        Coordinates c = group.getCoordinates();
        int floor = placement.floorFromWorldX(c.getX());
        FloorRoomMappingConfig.FloorConfig floorCfg = config.floor(floor);
        Optional<BuildingRoomCatalog.RoomPick> roomOpt = placement.resolveRoom(floor, c, locale);

        double localX;
        double localY;
        String roomName;

        if (roomOpt.isPresent()) {
            BuildingRoomCatalog.RoomPick room = roomOpt.get();
            roomName = room.localizedLabel(locale, floor);
            if (floorCfg != null) {
                double[] local = placement.toLocalNormalized(floorCfg, c);
                if (room.containsBuilding(local[0], local[1])) {
                    localX = local[0];
                    localY = local[1];
                } else {
                    localX = room.centerX();
                    localY = room.centerY();
                }
            } else {
                localX = room.centerX();
                localY = room.centerY();
            }

            FloorRoomMappingConfig.RoomZone zone = new FloorRoomMappingConfig.RoomZone(
                    room.id(), room.normLeft(), room.normTop(), room.normRight(), room.normBottom());
            Point2D imagePoint = zone.toImagePoint(
                    localX, localY, group.getId(), imageWidth, imageHeight, config.imageLayout());
            return new FloorProjection(floor, imagePoint, roomName);
        }

        roomName = "—";
        if (floorCfg != null) {
            double[] local = placement.toLocalNormalized(floorCfg, c);
            localX = clamp(local[0], 0.0, 1.0);
            localY = clamp(local[1], 0.0, 1.0);
            double bx = config.imageLayout().buildingLeft() * imageWidth;
            double by = config.imageLayout().buildingTop() * imageHeight;
            double bw = (config.imageLayout().buildingRight() - config.imageLayout().buildingLeft()) * imageWidth;
            double bh = (config.imageLayout().buildingBottom() - config.imageLayout().buildingTop()) * imageHeight;
            Point2D imagePoint = new Point2D(bx + localX * bw, by + localY * bh);
            return new FloorProjection(floor, imagePoint, roomName);
        }

        return new FloorProjection(floor, new Point2D(imageWidth / 2, imageHeight / 2), roomName);
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public RoomPlacementService placement() {
        return placement;
    }

    public record FloorProjection(
            int floor,
            Point2D point,
            String roomName
    ) {
    }
}
