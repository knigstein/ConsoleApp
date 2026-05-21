package client.gui.util;

import client.gui.I18n;

import java.time.LocalTime;
import java.util.Locale;

/**
 * Time-of-day greeting using {@link java.time.LocalTime}.
 */
public final class GreetingUtil {

    private GreetingUtil() {
    }

    public static String greetingKey(LocalTime time) {
        int hour = time.getHour();
        if (hour >= 5 && hour < 12) {
            return "welcome.morning";
        }
        if (hour >= 12 && hour < 17) {
            return "welcome.afternoon";
        }
        if (hour >= 17 && hour < 23) {
            return "welcome.evening";
        }
        return "welcome.night";
    }

    public static String formatGreeting(Locale locale, String login) {
        String key = greetingKey(LocalTime.now());
        String name = login == null || login.isBlank() ? "—" : login.trim();
        return I18n.tr(locale, key, name);
    }
}
