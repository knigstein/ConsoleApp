package client.gui.util;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.function.Consumer;

/**
 * Runs background work off the JavaFX thread and applies results on the UI thread.
 */
public final class FxTasks {

    private FxTasks() {
    }

    public static <T> void runAsync(
            java.util.concurrent.Callable<T> background,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError
    ) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return background.call();
            }
        };
        task.setOnSucceeded(e -> {
            if (onSuccess != null) {
                onSuccess.accept(task.getValue());
            }
        });
        task.setOnFailed(e -> {
            if (onError != null) {
                onError.accept(task.getException());
            }
        });
        Thread t = new Thread(task, "fx-background-task");
        t.setDaemon(true);
        t.start();
    }

    public static void runOnUi(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
