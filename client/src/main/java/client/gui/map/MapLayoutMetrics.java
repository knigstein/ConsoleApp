package client.gui.map;

import javafx.geometry.Point2D;

/**
 * Letterboxed image layout inside the map container: maps image pixel coords to pane coords.
 */
public record MapLayoutMetrics(
        double offsetX,
        double offsetY,
        double displayWidth,
        double displayHeight,
        double naturalWidth,
        double naturalHeight
) {
    public boolean isValid() {
        return naturalWidth > 0 && naturalHeight > 0 && displayWidth > 0 && displayHeight > 0;
    }

    public Point2D toPaneCoordinates(double imageX, double imageY) {
        double px = offsetX + imageX * displayWidth / naturalWidth;
        double py = offsetY + imageY * displayHeight / naturalHeight;
        return new Point2D(px, py);
    }

    public static MapLayoutMetrics compute(double containerWidth, double containerHeight,
                                           double naturalWidth, double naturalHeight) {
        if (containerWidth <= 0 || containerHeight <= 0 || naturalWidth <= 0 || naturalHeight <= 0) {
            return new MapLayoutMetrics(0, 0, 0, 0, naturalWidth, naturalHeight);
        }
        double scale = Math.min(containerWidth / naturalWidth, containerHeight / naturalHeight);
        double displayW = naturalWidth * scale;
        double displayH = naturalHeight * scale;
        double offsetX = (containerWidth - displayW) / 2.0;
        double offsetY = (containerHeight - displayH) / 2.0;
        return new MapLayoutMetrics(offsetX, offsetY, displayW, displayH, naturalWidth, naturalHeight);
    }
}
