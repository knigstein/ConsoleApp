package input;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Scanner;

/**
 * Универсальный обработчик пользовательского ввода для интерактивного режима и режима скрипта.
 * Инкапсулирует чтение строк, чисел, перечислений и дат из {@link Scanner} с единообразной
 * валидацией и сообщениями об ошибках.
 *
 * Режим работы определяется флагом {@link #isScriptMode}:
 * <ul>
 *     <li>в интерактивном режиме запросы пользователю выводятся в консоль,</li>
 *     <li>в режиме скрипта запросы не печатаются, а при отсутствии входных данных
 *     или пустом вводе могут использоваться значения по умолчанию.</li>
 * </ul>
 *
 * Методы класса выбрасывают {@link IllegalArgumentException} при некорректном вводе,
 * что позволяет командам верхнего уровня единообразно обрабатывать ошибки.
 */
public class InputHandler {

    /**
     * Источник данных (консоль или файл-скрипт).
     */
    private final Scanner scanner;

    /**
     * Признак режима скрипта. Если {@code true}, запросы не выводятся в консоль,
     * а методы при необходимости используют значения по умолчанию.
     */
    private final boolean isScriptMode;

    /**
     * Создаёт новый обработчик ввода.
     *
     * @param scanner      {@link Scanner}, из которого считываются данные
     * @param isScriptMode признак режима скрипта: {@code true}, если ввод идёт из файла-скрипта,
     *                     {@code false}, если используется интерактивный ввод из консоли
     */
    public InputHandler(Scanner scanner, boolean isScriptMode) {
        this.scanner = scanner;
        this.isScriptMode = isScriptMode;
    }

    /**
     * Считывает непустую строку.
     *
     * @param message      текст сообщения-запроса (используется только в интерактивном режиме)
     * @param nullable     признак допустимости {@code null} при пустом вводе
     * @param defaultValue значение по умолчанию, используемое в режиме скрипта при отсутствии ввода
     * @return считанная строка (может быть {@code null}, если {@code nullable == true})
     * @throws IllegalArgumentException если строка отсутствует или пуста и при этом не допускается
     */
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

    /**
     * Считывает целое число с нижней границей.
     *
     * @param message      текст сообщения-запроса (используется только в интерактивном режиме)
     * @param min          минимально допустимое значение (ввод должен быть строго больше этого числа)
     * @param defaultValue значение по умолчанию, используемое в режиме скрипта при отсутствии ввода
     * @return считанное целое число
     * @throws IllegalArgumentException если значение отсутствует, не является числом
     *                                  или не удовлетворяет ограничению {@code value > min}
     */
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

    /**
     * Считывает значение перечисления.
     *
     * @param message     текст сообщения-запроса (в интерактивном режиме дополнительно
     *                    выводится список всех возможных значений)
     * @param enumClass   класс перечисления, из которого выбирается значение
     * @param nullable    признак допустимости {@code null} при пустом вводе
     * @param defaultValue значение по умолчанию, используемое в режиме скрипта при отсутствии ввода
     * @param <T>         тип перечисления
     * @return выбранное значение перечисления или {@code null}, если это разрешено параметрами
     * @throws IllegalArgumentException если значение отсутствует или не соответствует
     *                                  ни одному элементу перечисления
     */
    public <T extends Enum<T>> T readEnum(String message, Class<T> enumClass, boolean nullable, T defaultValue) {

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

    /**
     * Считывает дату в формате {@code yyyy-MM-dd} и возвращает её в виде {@link Date}.
     *
     * @param message      текст сообщения-запроса (используется только в интерактивном режиме)
     * @param nullable     признак допустимости {@code null}; в текущей реализации
     *                     пустой ввод не допускается и приводит к исключению
     * @param defaultValue значение по умолчанию, используемое в режиме скрипта при отсутствии ввода
     * @return считанная дата
     * @throws IllegalArgumentException если дата отсутствует или имеет некорректный формат
     */
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