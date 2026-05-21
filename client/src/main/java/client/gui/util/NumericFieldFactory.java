package client.gui.util;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Поля только с неотрицательными целыми.
 */
public final class NumericFieldFactory {

    private static final Pattern NON_NEGATIVE_INT = Pattern.compile("\\d*");

    private NumericFieldFactory() {
    }

    public static TextFormatter<Integer> nonNegativeIntegerFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String next = change.getControlNewText();
            if (next.isEmpty() || NON_NEGATIVE_INT.matcher(next).matches()) {
                return change;
            }
            return null;
        };
        return new TextFormatter<>(new IntegerStringConverter(), 0, filter);
    }

    public static Spinner<Integer> spinner(int min, int max, int initial) {
        Spinner<Integer> spinner = new Spinner<>();
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initial));
        spinner.setEditable(true);
        spinner.getEditor().setTextFormatter(nonNegativeIntegerFormatter());
        return spinner;
    }
}
