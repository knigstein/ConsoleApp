package client.gui.map;

import javafx.geometry.Point2D;

import java.nio.file.Path;
import java.util.*;

/**
 * Floor and room zones configuration.
 * Room zones are normalized within the building footprint on the plan image.
 * Assumes Corpus/834200..834204 corresponds to floors 1..5.
 */
public final class FloorRoomMappingConfig {

    private final Map<Integer, FloorConfig> floors;
    private final ImageLayout imageLayout;

    private FloorRoomMappingConfig(Map<Integer, FloorConfig> floors, ImageLayout imageLayout) {
        this.floors = floors;
        this.imageLayout = imageLayout;
    }

    public static FloorRoomMappingConfig defaultConfig(Path workspaceRoot) {
        String corpusDir = System.getProperty("lab5.corpus.dir");
        Path corpus = corpusDir == null || corpusDir.isBlank()
                ? workspaceRoot.resolve("Corpus")
                : Path.of(corpusDir);

        List<RoomZone> roomZones = BuildingRoomCatalog.asRoomZones();
        Map<Integer, FloorConfig> map = new HashMap<>();
        map.put(1, floor(1, corpus.resolve("834200.jpg"), 0, 199, roomZones));
        map.put(2, floor(2, corpus.resolve("834201.jpg"), 200, 399, roomZones));
        map.put(3, floor(3, corpus.resolve("834202.jpg"), 400, 599, roomZones));
        map.put(4, floor(4, corpus.resolve("834203.jpg"), 600, 799, roomZones));
        map.put(5, floor(5, corpus.resolve("834204.jpg"), 800, 1000, roomZones));

        // Title bar on floor plan image (~48px) + small side margin
        ImageLayout layout = new ImageLayout(0.04, 0.11, 0.96, 0.96);
        return new FloorRoomMappingConfig(map, layout);
    }

    private static FloorConfig floor(int number, Path image, double minX, double maxX, List<RoomZone> zones) {
        return new FloorConfig(number, image, new WorldBounds(minX, maxX, 0, 1000), zones);
    }

    public ImageLayout imageLayout() {
        return imageLayout;
    }

    public Collection<Integer> floorNumbers() {
        return floors.keySet().stream().sorted().toList();
    }

    public FloorConfig floor(int floorNumber) {
        return floors.get(floorNumber);
    }

    public FloorConfig floorForWorldCoordinates(double x, double y) {
        return floors.values().stream()
                .filter(f -> f.worldBounds().contains(x, y))
                .findFirst()
                .orElseGet(() -> nearestFloorByX(x));
    }

    private FloorConfig nearestFloorByX(double x) {
        FloorConfig best = floors.get(1);
        double bestDist = Double.MAX_VALUE;
        for (FloorConfig f : floors.values()) {
            WorldBounds b = f.worldBounds();
            double center = (b.minX() + b.maxX()) / 2.0;
            double dist = Math.abs(x - center);
            if (dist < bestDist) {
                bestDist = dist;
                best = f;
            }
        }
        return best;
    }

    public static int defaultWorldXForFloor(int floorNumber) {
        return switch (floorNumber) {
            case 1 -> 100;
            case 2 -> 300;
            case 3 -> 500;
            case 4 -> 700;
            case 5 -> 900;
            default -> 100;
        };
    }

    /**
     * Building footprint on the full floor-plan image (fractions 0..1).
     */
    public record ImageLayout(double buildingLeft, double buildingTop, double buildingRight, double buildingBottom) {
    }

    public record FloorConfig(
            int floorNumber,
            Path imagePath,
            WorldBounds worldBounds,
            List<RoomZone> roomZones
    ) {
    }

    /**
     * Room rectangle in normalized building coordinates (0..1).
     */
    public record RoomZone(String roomName, double normLeft, double normTop, double normRight, double normBottom) {

        public boolean containsBuilding(double localX, double localY) {
            return localX >= normLeft && localX <= normRight
                    && localY >= normTop && localY <= normBottom;
        }

        public Point2D toImagePoint(
                double localX,
                double localY,
                Integer groupId,
                double imageWidth,
                double imageHeight,
                ImageLayout layout
        ) {
            double margin = 0.015;
            double minX = normLeft + margin;
            double maxX = normRight - margin;
            double minY = normTop + margin;
            double maxY = normBottom - margin;

            double baseX = containsBuilding(localX, localY) ? localX : (normLeft + normRight) / 2.0;
            double baseY = containsBuilding(localX, localY) ? localY : (normTop + normBottom) / 2.0;

            double spreadX = Math.max(0.01, (normRight - normLeft) * 0.22);
            double spreadY = Math.max(0.01, (normBottom - normTop) * 0.22);
            int seed = groupId == null ? 0 : groupId;
            double jx = baseX + spreadX * (hash01(seed, 17) - 0.5);
            double jy = baseY + spreadY * (hash01(seed, 31) - 0.5);

            jx = clamp(jx, minX, maxX);
            jy = clamp(jy, minY, maxY);

            double bx = layout.buildingLeft() * imageWidth;
            double by = layout.buildingTop() * imageHeight;
            double bw = (layout.buildingRight() - layout.buildingLeft()) * imageWidth;
            double bh = (layout.buildingBottom() - layout.buildingTop()) * imageHeight;

            return new Point2D(bx + jx * bw, by + jy * bh);
        }

        private static double hash01(int seed, int salt) {
            return ((seed * salt) % 100) / 100.0;
        }

        private static double clamp(double v, double min, double max) {
            return Math.max(min, Math.min(max, v));
        }
    }

    public record WorldBounds(double minX, double maxX, double minY, double maxY) {
        public boolean contains(double x, double y) {
            return x >= minX && x <= maxX && y >= minY && y <= maxY;
        }
    }
}
