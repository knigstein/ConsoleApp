package input;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Scanner;

public class InputHandler {

    private final Scanner scanner;
    private final boolean isScriptMode;

    public InputHandler(Scanner scanner, boolean isScriptMode) {
        this.scanner = scanner;
        this.isScriptMode = isScriptMode;
    }

    public String readString(String message, boolean nullable, String defaultValue) {

        if (!isScriptMode) System.out.println(message);

        if (!scanner.hasNextLine()) {
            if (isScriptMode && defaultValue != null) return defaultValue;
            throw new IllegalArgumentException("Отсутствует строка.");
        }

        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            if (nullable) return null;
            if (isScriptMode && defaultValue != null) return defaultValue;
            throw new IllegalArgumentException("Строка не может быть пустой.");
        }

        return input;
    }

    public int readInt(String message, int min, Integer defaultValue) {

        if (!isScriptMode) System.out.println(message);

        if (!scanner.hasNextLine()) {
            if (isScriptMode && defaultValue != null) return defaultValue;
            throw new IllegalArgumentException("Отсутствует число.");
        }

        String line = scanner.nextLine().trim();

        try {
            int value = Integer.parseInt(line);
            if (value <= min)
                throw new IllegalArgumentException("Число должно быть > " + min);
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректное число.");
        }
    }

    public <T extends Enum<T>> T readEnum(
            String message,
            Class<T> enumClass,
            boolean nullable,
            T defaultValue) {

        if (!isScriptMode) {
            System.out.println(message);
            for (T val : enumClass.getEnumConstants())
                System.out.println("- " + val.name());
        }

        if (!scanner.hasNextLine()) {
            if (isScriptMode && defaultValue != null) return defaultValue;
            throw new IllegalArgumentException("Отсутствует enum.");
        }

        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            if (nullable) return null;
            if (isScriptMode && defaultValue != null) return defaultValue;
            throw new IllegalArgumentException("Enum не может быть пустым.");
        }

        try {
            return Enum.valueOf(enumClass, input.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Некорректное значение enum.");
        }
    }

    public Date readDate(String message, boolean nullable, Date defaultValue) {

        if (!isScriptMode) System.out.println(message);

        if (!scanner.hasNextLine()) {
            if (isScriptMode && defaultValue != null) return defaultValue;
            throw new IllegalArgumentException("Дата отсутствует.");
        }

        String line = scanner.nextLine().trim();

        try {
            LocalDate localDate = LocalDate.parse(line);
            return java.sql.Date.valueOf(localDate);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Некорректная дата.");
        }
    }
}