/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.helpers;

import java.util.Locale;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.TextInputControl;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.comtel.javafx.control.KeyBoardPopup;
import org.comtel.javafx.control.KeyBoardPopupBuilder;
import org.comtel.javafx.robot.RobotFactory;
import org.pidome.client.config.DisplayConfig;

/**
 *
 * @author John Sirach
 */
public class Osdk {

    static KeyBoardPopup osdk;
    static Stage stage;
    
    public static void load(Stage rootStage){
        if(osdk==null){
            stage = rootStage;
            osdk = KeyBoardPopupBuilder.create().initLocale("pidome", Locale.ENGLISH).addIRobot(RobotFactory.createFXRobot()).build();
            osdk.getKeyBoard().setOnKeyboardCloseButton((Event event) -> {
                show(false, null);
            });
        }
    }
    
    public static KeyBoardPopup getKeyBoard(){
        return osdk;
    }
    
    public static void setStage(Stage stage){
        Osdk.stage = stage;
    }
    
    public static void show(final boolean b, final TextInputControl textNode) {

        Platform.runLater(new Runnable() {
            private Animation fadeAnimation;

            @Override
            public void run() {
                if (b) {
                    if (textNode != null) {
                        Rectangle2D textNodeBounds = new Rectangle2D(textNode.getScene().getWindow().getX()
                                + textNode.getLocalToSceneTransform().getTx(), textNode.getScene().getWindow().getY()
                                + textNode.getLocalToSceneTransform().getTy(), textNode.getWidth(), textNode
                                .getHeight());

                        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                        if (textNodeBounds.getMinX() + osdk.getWidth() > screenBounds.getMaxX()) {
                            osdk.setX(screenBounds.getMaxX() - osdk.getWidth());
                        } else {
                            osdk.setX(textNodeBounds.getMinX());
                        }
                        if (textNodeBounds.getMaxY() + osdk.getHeight() > screenBounds.getMaxY()) {
                            osdk.setY(textNodeBounds.getMinY() - osdk.getHeight() + 20);
                        } else {
                            osdk.setY(textNodeBounds.getMaxY() + 40);
                        }
                    }

                }
                if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){
                    if (fadeAnimation != null) {
                        fadeAnimation.stop();
                    }
                    if (!b) {
                        osdk.hide();
                        return;
                    }
                    if (osdk.isShowing()) {
                        return;
                    }
                    osdk.getKeyBoard().setOpacity(0.0);

                    FadeTransition fade = new FadeTransition(Duration.seconds(.5), osdk.getKeyBoard());
                    fade.setToValue(b ? 1.0 : 0.0);
                    fade.setOnFinished((ActionEvent event) -> {
                        fadeAnimation = null;
                    });

                    ScaleTransition scale = new ScaleTransition(Duration.seconds(.5), osdk.getKeyBoard());
                    scale.setToX(b ? 1 : 0.8);
                    scale.setToY(b ? 1 : 0.8);

                    ParallelTransition tx = new ParallelTransition(fade, scale);
                    fadeAnimation = tx;
                    tx.play();
                    if (b) {
                        if (!osdk.isShowing()) {
                            osdk.show(stage);
                        }
                    }
                } else {
                    if (b) {
                        if (!osdk.isShowing()) {
                            osdk.show(stage);
                        }
                    } else {
                        osdk.hide();
                    }
                }
            }
        });
    }
    
}
