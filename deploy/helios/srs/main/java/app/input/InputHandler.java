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
     * Считывает строку с учётом ограничений.
     * При некорректном вводе (пустая строка при {@code nullable == false} и отсутствии
     * значения по умолчанию) пользователю выводится сообщение об ошибке и ввод поля
     * повторяется до тех пор, пока не будет получено корректное значение.
     *
     * @param message      текст сообщения-запроса (используется только в интерактивном режиме)
     * @param nullable     признак допустимости {@code null} при пустом вводе
     * @param defaultValue значение по умолчанию, используемое в режиме скрипта или как
     *                     «старое значение» при обновлении
     * @return считанная строка (может быть {@code null}, если {@code nullable == true})
     * @throws IllegalArgumentException если при чтении ввода достигнут конец потока и
     *                                  отсутствует значение по умолчанию
     */
    public String readString(String message, boolean nullable, String defaultValue) {

        while (true) {
            if (!isScriptMode) {
                System.out.println(message);
            }

            if (!scanner.hasNextLine()) {
                if (defaultValue != null) {
                    return defaultValue;
                }
                throw new IllegalArgumentException("Отсутствует строка.");
            }

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                if (nullable) {
                    return null;
                }
                if (defaultValue != null) {
                    return defaultValue;
                }
                System.out.println("Строка не может быть пустой. Повторите ввод.");
                continue;
            }

            return input;
        }
    }

    /**
     * Считывает целое число с нижней границей.
     * При некорректном вводе (не число, значение меньше либо равно {@code min})
     * пользователю выводится сообщение об ошибке и предложено повторить ввод.
     *
     * @param message      текст сообщения-запроса (используется только в интерактивном режиме)
     * @param min          минимально допустимое значение (ввод должен быть строго больше этого числа)
     * @param defaultValue значение по умолчанию, используемое в режиме скрипта или как
     *                     «старое значение» при обновлении
     * @return считанное целое число
     * @throws IllegalArgumentException если данные отсутствуют и нет значения по умолчанию
     */
    public int readInt(String message, int min, Integer defaultValue) {

        while (true) {
            if (!isScriptMode) {
                System.out.println(message);
            }

            if (!scanner.hasNextLine()) {
                if (defaultValue != null) {
                    return defaultValue;
                }
                throw new IllegalArgumentException("Отсутствует число.");
            }

            String line = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(line);
                if (value <= min) {
                    System.out.println("Число должно быть > " + min + ". Повторите ввод.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Некорректное число. Повторите ввод.");
            }
        }
    }

    /**
     * Считывает значение перечисления.
     * При некорректном вводе (значение не является константой перечисления)
     * пользователю выводится сообщение об ошибке и запрашивается повторный ввод.
     *
     * @param message      текст сообщения-запроса (в интерактивном режиме дополнительно
     *                     выводится список всех возможных значений)
     * @param enumClass    класс перечисления, из которого выбирается значение
     * @param nullable     признак допустимости {@code null} при пустом вводе
     * @param defaultValue значение по умолчанию, используемое в режиме скрипта или как
     *                     «старое значение» при обновлении
     * @param <T>          тип перечисления
     * @return выбранное значение перечисления или {@code null}, если это разрешено параметрами
     * @throws IllegalArgumentException если данные отсутствуют и нет значения по умолчанию
     */
    public <T extends Enum<T>> T readEnum(String message, Class<T> enumClass, boolean nullable, T defaultValue) {

        while (true) {

            if (!isScriptMode) {
                System.out.println(message);
                for (T val : enumClass.getEnumConstants()) {
                    System.out.println("- " + val.name());
                }
            }

            if (!scanner.hasNextLine()) {
                if (defaultValue != null) {
                    return defaultValue;
                }
                throw new IllegalArgumentException("Отсутствует enum.");
            }

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                if (nullable) {
                    return null;
                }
                if (defaultValue != null) {
                    return defaultValue;
                }
                System.out.println("Enum не может быть пустым. Повторите ввод.");
                continue;
            }

            try {
                return Enum.valueOf(enumClass, input.toUpperCase());
            } catch (Exception e) {
                System.out.println("Некорректное значение enum. Повторите ввод.");
            }
        }
    }

    /**
     * Считывает дату в формате {@code yyyy-MM-dd} и возвращает её в виде {@link Date}.
     * При некорректном формате даты пользователю выводится сообщение об ошибке
     * и запрашивается повторный ввод.
     *
     * @param message      текст сообщения-запроса (используется только в интерактивном режиме)
     * @param nullable     признак допустимости {@code null}; если установлен и введена пустая строка,
     *                     метод вернёт {@code null} либо значение по умолчанию
     * @param defaultValue значение по умолчанию, используемое в режиме скрипта или как
     *                     «старое значение» при обновлении
     * @return считанная дата или {@code null}, если это разрешено параметрами
     * @throws IllegalArgumentException если данные отсутствуют и нет значения по умолчанию
     */
    public Date readDate(String message, boolean nullable, Date defaultValue) {

        while (true) {

            if (!isScriptMode) {
                System.out.println(message);
            }

            if (!scanner.hasNextLine()) {
                if (defaultValue != null) {
                    return defaultValue;
                }
                throw new IllegalArgumentException("Дата отсутствует.");
            }

            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                if (nullable) {
                    return defaultValue;
                }
                if (defaultValue != null) {
                    return defaultValue;
                }
                System.out.println("Дата не может быть пустой. Повторите ввод.");
                continue;
            }

            try {
                LocalDate localDate = LocalDate.parse(line);
                return java.sql.Date.valueOf(localDate);
            } catch (DateTimeParseException e) {
                System.out.println("Некорректная дата. Ожидаемый формат: yyyy-MM-dd. Повторите ввод.");
            }
        }
    }
}