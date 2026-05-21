package client.gui.theme;

/**
 * Centralized visual theme for the JavaFX client.
 */
public final class AppStyles {

    public static final String ROOT = """
            -fx-font-family: "Segoe UI", "Roboto", "Noto Sans", sans-serif;
            -fx-font-size: 11px;
            -fx-background-color: #f0f2f5;
            """;

    public static final String LOGIN_ROOT = """
            -fx-background-color: linear-gradient(to bottom right, #1a237e 0%, #283593 40%, #3949ab 100%);
            """;

    public static final String LOGIN_CARD = """
            -fx-background-color: white;
            -fx-background-radius: 14;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.22), 18, 0.18, 0, 6);
            -fx-padding: 24;
            """;

    public static final String LOGIN_TITLE = """
            -fx-font-size: 18px;
            -fx-font-weight: bold;
            -fx-text-fill: #1a237e;
            """;

    public static final String LOGIN_SUBTITLE = """
            -fx-font-size: 11px;
            -fx-text-fill: #5c6bc0;
            """;

    public static final String LOGIN_LABEL = """
            -fx-font-size: 11px;
            -fx-text-fill: #37474f;
            -fx-wrap-text: true;
            """;

    public static final String HEADER = """
            -fx-background-color: #1a237e;
            -fx-padding: 6 12 6 12;
            """;

    public static final String HEADER_TITLE = """
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            """;

    public static final String HEADER_USER = """
            -fx-font-size: 11px;
            -fx-text-fill: #e8eaf6;
            """;

    public static final String NAV_BUTTON = """
            -fx-background-color: transparent;
            -fx-text-fill: #c5cae9;
            -fx-font-size: 11px;
            -fx-padding: 4 10 4 10;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            """;

    public static final String NAV_BUTTON_ACTIVE = """
            -fx-background-color: rgba(255,255,255,0.2);
            -fx-text-fill: white;
            -fx-font-size: 11px;
            -fx-font-weight: bold;
            -fx-padding: 4 10 4 10;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            """;

    public static final String SIDEBAR = """
            -fx-background-color: #ffffff;
            -fx-border-color: #e0e0e0;
            -fx-border-width: 0 1 0 0;
            -fx-padding: 8;
            """;

    public static final String SIDEBAR_SECTION = """
            -fx-font-size: 11px;
            -fx-font-weight: bold;
            -fx-text-fill: #757575;
            -fx-padding: 8 0 4 0;
            """;

    public static final String PRIMARY_BUTTON = """
            -fx-background-color: #3949ab;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-font-size: 11px;
            -fx-background-radius: 6;
            -fx-padding: 6 10 6 10;
            -fx-cursor: hand;
            """;

    public static final String SECONDARY_BUTTON = """
            -fx-background-color: #eceff1;
            -fx-text-fill: #37474f;
            -fx-font-size: 11px;
            -fx-background-radius: 6;
            -fx-padding: 5 8 5 8;
            -fx-cursor: hand;
            """;

    public static final String DANGER_BUTTON = """
            -fx-background-color: #ffebee;
            -fx-text-fill: #c62828;
            -fx-font-size: 11px;
            -fx-background-radius: 6;
            -fx-padding: 5 8 5 8;
            -fx-cursor: hand;
            """;

    public static final String COMPACT_BUTTON = """
            -fx-font-size: 10px;
            -fx-padding: 4 6 4 6;
            """;

    public static final String FLOOR_TAB = """
            -fx-background-color: #eceff1;
            -fx-text-fill: #455a64;
            -fx-background-radius: 8;
            -fx-padding: 6 14 6 14;
            -fx-cursor: hand;
            """;

    public static final String FLOOR_TAB_SELECTED = """
            -fx-background-color: #3949ab;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
            -fx-padding: 6 14 6 14;
            -fx-cursor: hand;
            """;

    public static final String MAP_FRAME = """
            -fx-background-color: #263238;
            -fx-background-radius: 8;
            -fx-padding: 6;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0.12, 0, 2);
            """;

    public static final String STATUS_BAR = """
            -fx-background-color: #eceff1;
            -fx-padding: 8 16 8 16;
            -fx-text-fill: #455a64;
            -fx-font-size: 12px;
            """;

    public static final String CARD = """
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0.1, 0, 2);
            -fx-padding: 12;
            """;

    public static final String TABLE_PANEL = """
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-padding: 8;
            """;

    public static final String MAP_HINT = """
            -fx-font-size: 12px;
            -fx-text-fill: #546e7a;
            """;

    public static final String MAP_TITLE = """
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-text-fill: #263238;
            """;

    public static final String WELCOME_ROOT = """
            -fx-background-color: linear-gradient(to bottom right, #1a237e 0%, #283593 50%, #3949ab 100%);
            """;

    public static final String WELCOME_CARD = """
            -fx-padding: 24;
            """;

    public static final String WELCOME_TEXT = """
            -fx-font-size: 26px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            -fx-text-alignment: center;
            """;

    public static final String MAP_FLOOR_BADGE = """
            -fx-background-color: #e8eaf6;
            -fx-text-fill: #3949ab;
            -fx-padding: 4 10;
            -fx-background-radius: 12;
            -fx-font-weight: bold;
            """;

    public static final String LEGEND = """
            -fx-font-size: 11px;
            -fx-text-fill: #607d8b;
            -fx-padding: 4 0 0 0;
            """;

    public static final String DIALOG_PANE = """
            -fx-font-size: 11px;
            """;

    public static final String DIALOG_LABEL = """
            -fx-font-weight: bold;
            -fx-text-fill: #37474f;
            """;

    public static final String DIALOG_HINT = """
            -fx-font-size: 10px;
            -fx-text-fill: #607d8b;
            -fx-wrap-text: true;
            """;

    public static final String DIALOG_ERROR = """
            -fx-font-size: 11px;
            -fx-text-fill: #c62828;
            -fx-wrap-text: true;
            """;

    private AppStyles() {
    }
}
