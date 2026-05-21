package client.gui.view;

import client.gui.theme.AppStyles;
import client.gui.util.GreetingUtil;
import client.gui.util.UiScale;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Locale;

/**
 * Short animated greeting after successful login.
 */
public class WelcomeView extends StackPane {

    public WelcomeView(Locale locale, String login, Runnable onFinished) {
        setStyle(AppStyles.WELCOME_ROOT);
        setOpacity(0);

        Label greeting = new Label(GreetingUtil.formatGreeting(locale, login));
        greeting.setStyle(AppStyles.WELCOME_TEXT);
        greeting.setWrapText(true);
        greeting.setMaxWidth(UiScale.windowWidth() * 0.85);

        VBox card = new VBox(8, greeting);
        card.setAlignment(Pos.CENTER);
        card.setStyle(AppStyles.WELCOME_CARD);
        getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(650), card);
        scaleIn.setFromX(0.88);
        scaleIn.setFromY(0.88);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition hold = new PauseTransition(Duration.seconds(2.2));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(450), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });

        SequentialTransition seq = new SequentialTransition(
                new ParallelTransition(fadeIn, scaleIn),
                hold,
                fadeOut
        );
        seq.play();
    }
}
