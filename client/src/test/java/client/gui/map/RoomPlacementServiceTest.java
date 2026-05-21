package client.gui.map;

import client.gui.DeployPathResolver;
import model.Coordinates;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomPlacementServiceTest {

    @Test
    void roundTripKeepsGroupInSelectedRoomOnEveryFloor() {
        FloorRoomMappingConfig config = FloorRoomMappingConfig.defaultConfig(DeployPathResolver.resolveDeployRoot());
        RoomPlacementService placement = new RoomPlacementService(config);

        for (int floor = 1; floor <= 5; floor++) {
            for (BuildingRoomCatalog.RoomPick room : BuildingRoomCatalog.roomsForFloor(floor)) {
                Coordinates world = placement.toWorldCoordinates(floor, room.id());
                assertEquals(floor, placement.floorFromWorldX(world.getX()), room.id());
                var resolved = placement.resolveRoom(floor, world);
                assertTrue(resolved.isPresent(), "no room for " + room.id() + " on floor " + floor);
                assertEquals(room.id(), resolved.get().id(), "room mismatch for " + room.id());
            }
        }
    }

    @Test
    void projectionUsesSameRoomAsPlacement() {
        FloorRoomMappingConfig config = FloorRoomMappingConfig.defaultConfig(DeployPathResolver.resolveDeployRoot());
        RoomPlacementService placement = new RoomPlacementService(config);
        CoordinateToRoomService projection = new CoordinateToRoomService(config);

        for (int floor = 1; floor <= 5; floor++) {
            BuildingRoomCatalog.RoomPick room = BuildingRoomCatalog.roomsForFloor(floor).get(0);
            Coordinates world = placement.toWorldCoordinates(floor, room.id());
            model.StudyGroup group = new model.StudyGroup(
                    1,
                    "Test",
                    world,
                    java.time.LocalDate.now(),
                    10,
                    null,
                    1,
                    model.Semester.FIRST,
                    new model.Person("Admin", new java.util.Date(), model.Color.GREEN, model.Country.GERMANY)
            );
            var proj = projection.project(group, 800, 566);
            assertEquals(floor, proj.floor());
            assertEquals(room.localizedLabel(java.util.Locale.forLanguageTag("ru"), floor), proj.roomName());
        }
    }
}
