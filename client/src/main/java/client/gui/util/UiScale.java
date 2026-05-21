package client.gui.util;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

/**
 * Screen-aware window and layout sizing.
 */
public final class UiScale {

    private UiScale() {
    }

    public static Rectangle2D primaryBounds() {
        return Screen.getPrimary().getVisualBounds();
    }

    public static double windowWidth() {
        return Math.max(960, Math.min(1280, primaryBounds().getWidth() * 0.92));
    }

    public static double windowHeight() {
        return Math.max(600, Math.min(820, primaryBounds().getHeight() * 0.88));
    }
}
