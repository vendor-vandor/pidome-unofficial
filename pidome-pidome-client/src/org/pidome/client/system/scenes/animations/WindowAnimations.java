/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.animations;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.scenes.windows.WindowComponent;

/**
 *
 * @author John Sirach
 */
public class WindowAnimations {

    final static public String OPEN = "OPEN";
    
    final static public String CLOSE_TO_LEFT    = "CLOSE_TO_LEFT";
    final static public String CLOSE_IN_PLACE   = "CLOSE_IN_PLACE";
    
    static String windowingStyle = DisplayConfig.getRunMode();
    
    static Logger LOG = LogManager.getLogger(WindowAnimations.class);
    
    public static void prepareWindowAnimation(final WindowComponent node){
        node.setOpacity(0.0);
    }
    
    public static void openFromCenter(final WindowComponent node, double posX, double posY){
        if(windowingStyle.equals(DisplayConfig.RUNMODE_DEFAULT)){
            
                double fromX = (DisplayConfig.getScreenWidth() / 2) - (node.widthProperty().get()/2);
                double fromY = (DisplayConfig.getScreenHeight() / 2) - (node.heightProperty().get()/2);

                if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){

                    node.setScaleX(0.0);
                    node.setScaleY(0.0);

                    /* position the thing */
                    node.setTranslateX(fromX);
                    node.setTranslateY(fromY);

                    /* Fade in */
                    FadeTransition fadeInTransition = new FadeTransition(Duration.millis(250), node);
                    fadeInTransition.setFromValue(0.0);
                    fadeInTransition.setToValue(1.0);
                    fadeInTransition.setCycleCount(1);
                    fadeInTransition.setAutoReverse(false);

                    /* set size */
                    ScaleTransition scaleSizeTransition = new ScaleTransition(Duration.millis(250), node);
                    scaleSizeTransition.setToY(1.0);
                    scaleSizeTransition.setToX(1.0);
                    scaleSizeTransition.setCycleCount(1);
                    scaleSizeTransition.setAutoReverse(false);

                    /* set position movement */
                    TranslateTransition translatePositionTransition = new TranslateTransition(Duration.millis(250), node);
                    translatePositionTransition.setToX(posX);
                    translatePositionTransition.setToY(posY);
                    translatePositionTransition.setCycleCount(1);
                    translatePositionTransition.setAutoReverse(false);

                    final ParallelTransition parallelWidenAndMovement = new ParallelTransition();
                    parallelWidenAndMovement.getChildren().addAll(fadeInTransition, translatePositionTransition, scaleSizeTransition);
                    parallelWidenAndMovement.setCycleCount(1);
                    parallelWidenAndMovement.setAutoReverse(false);

                    parallelWidenAndMovement.setOnFinished((ActionEvent t) -> {
                        handleOpenDone(node);
                        LOG.debug("pref sizes: {}, {}, set sizes {}, {}", node.getPrefWidth(),node.getPrefHeight(), node.getWidth(),node.getHeight());
                    });
                    parallelWidenAndMovement.play();

                } else {
                    /* position the thing */
                    node.setTranslateX(posX);
                    node.setTranslateY(posY);
                    node.setOpacity(1.0);
                    handleOpenDone(node);
                }
        } else {
            openStage(node);
        }
    }
    
    public static void openCentered(final WindowComponent node){
        if(windowingStyle.equals(DisplayConfig.RUNMODE_DEFAULT)){
            
                /* center position */
                double fromX = (DisplayConfig.getScreenWidth() / 2) - (node.widthProperty().get()/2);
                double fromY = (DisplayConfig.getScreenHeight() / 2) - (node.heightProperty().get()/2);

                /* position the thing */
                node.setTranslateX(fromX);
                node.setTranslateY(fromY);

                if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){

                    node.setScaleX(0.0);
                    node.setScaleY(0.0);

                    /* Fade in */
                    FadeTransition fadeInTransition = new FadeTransition(Duration.millis(250), node);
                    fadeInTransition.setFromValue(0.0);
                    fadeInTransition.setToValue(1.0);
                    fadeInTransition.setCycleCount(1);
                    fadeInTransition.setAutoReverse(false);

                    /* set size */
                    ScaleTransition scaleSizeTransition = new ScaleTransition(Duration.millis(250), node);
                    scaleSizeTransition.setToY(1.0);
                    scaleSizeTransition.setToX(1.0);
                    scaleSizeTransition.setCycleCount(1);
                    scaleSizeTransition.setAutoReverse(false);

                    final ParallelTransition parallelWidenAndMovement = new ParallelTransition();
                    parallelWidenAndMovement.getChildren().addAll(fadeInTransition, scaleSizeTransition);
                    parallelWidenAndMovement.setCycleCount(1);
                    parallelWidenAndMovement.setAutoReverse(false);

                    parallelWidenAndMovement.setOnFinished((ActionEvent t) -> {
                        handleOpenDone(node);
                    });
                    parallelWidenAndMovement.play();
                } else {
                    node.setTranslateX(fromX);
                    node.setTranslateY(fromY);
                    node.setOpacity(1.0);
                    handleOpenDone(node);
                }
        } else {
            openStage(node);
        }
    }
    
    public static void openFromToCenter(final WindowComponent node, double fromX, double fromY){
        if(windowingStyle.equals(DisplayConfig.RUNMODE_DEFAULT)){
            /* center the thing */
            openFromLocation(node,
                             fromX, 
                             fromY);
        } else {
            openStage(node);
        }
        
    }
    
    public static void openFromToLocation(final WindowComponent node, double fromX, double fromY, double toX, double toY){
        if(windowingStyle.equals(DisplayConfig.RUNMODE_DEFAULT)){

                if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){

                    node.setScaleX(0.0);
                    node.setScaleY(0.0);

                    /* position the thing */
                    node.setTranslateX(fromX - (node.widthProperty().get()/2));
                    node.setTranslateY(fromY - (node.heightProperty().get()/2));

                    /* Fade in */
                    FadeTransition fadeInTransition = new FadeTransition(Duration.millis(250), node);
                    fadeInTransition.setFromValue(0.0);
                    fadeInTransition.setToValue(1.0);
                    fadeInTransition.setCycleCount(1);
                    fadeInTransition.setAutoReverse(false);

                    /* set size */
                    ScaleTransition scaleSizeTransition = new ScaleTransition(Duration.millis(250), node);
                    scaleSizeTransition.setToY(1.0);
                    scaleSizeTransition.setToX(1.0);
                    scaleSizeTransition.setCycleCount(1);
                    scaleSizeTransition.setAutoReverse(false);

                    /* set position movement */
                    TranslateTransition translatePositionTransition = new TranslateTransition(Duration.millis(250), node);
                    translatePositionTransition.setToX(toX);
                    translatePositionTransition.setToY(toY);
                    translatePositionTransition.setCycleCount(1);
                    translatePositionTransition.setAutoReverse(false);

                    final ParallelTransition parallelWidenAndMovement = new ParallelTransition();
                    parallelWidenAndMovement.getChildren().addAll(fadeInTransition, translatePositionTransition, scaleSizeTransition);
                    parallelWidenAndMovement.setCycleCount(1);
                    parallelWidenAndMovement.setAutoReverse(false);

                    parallelWidenAndMovement.setOnFinished((ActionEvent t) -> {
                        handleOpenDone(node);
                    });
                    parallelWidenAndMovement.play();
                } else {
                    /* position the thing */
                    node.setTranslateX(toX);
                    node.setTranslateY(toY);
                    node.setOpacity(1.0);
                    handleOpenDone(node);
                }

        } else {
            openStage(node);
        }
    }
    
    public static void openFromLocation(final WindowComponent node, double fromX, double fromY){
        if(windowingStyle.equals(DisplayConfig.RUNMODE_DEFAULT)){

                /* center position */
                double posX = (DisplayConfig.getScreenWidth() / 2) - (node.widthProperty().get()/2);
                double posY = (DisplayConfig.getScreenHeight() / 2) - (node.heightProperty().get()/2);

                if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){

                    node.setScaleX(0.0);
                    node.setScaleY(0.0);

                    /* position the thing */
                    node.setTranslateX(fromX - (node.widthProperty().get()/2));
                    node.setTranslateY(fromY - (node.heightProperty().get()/2));

                    /* Fade in */
                    FadeTransition fadeInTransition = new FadeTransition(Duration.millis(250), node);
                    fadeInTransition.setFromValue(0.0);
                    fadeInTransition.setToValue(1.0);
                    fadeInTransition.setCycleCount(1);
                    fadeInTransition.setAutoReverse(false);

                    /* set size */
                    ScaleTransition scaleSizeTransition = new ScaleTransition(Duration.millis(250), node);
                    scaleSizeTransition.setToY(1.0);
                    scaleSizeTransition.setToX(1.0);
                    scaleSizeTransition.setCycleCount(1);
                    scaleSizeTransition.setAutoReverse(false);

                    /* set position movement */
                    TranslateTransition translatePositionTransition = new TranslateTransition(Duration.millis(250), node);
                    translatePositionTransition.setToX(posX);
                    translatePositionTransition.setToY(posY);
                    translatePositionTransition.setCycleCount(1);
                    translatePositionTransition.setAutoReverse(false);

                    final ParallelTransition parallelWidenAndMovement = new ParallelTransition();
                    parallelWidenAndMovement.getChildren().addAll(fadeInTransition, translatePositionTransition, scaleSizeTransition);
                    parallelWidenAndMovement.setCycleCount(1);
                    parallelWidenAndMovement.setAutoReverse(false);

                    parallelWidenAndMovement.setOnFinished((ActionEvent t) -> {
                        handleOpenDone(node);
                        LOG.debug("pref sizes: {}, {}, set sizes {}, {}", node.getPrefWidth(),node.getPrefHeight(), node.getWidth(),node.getHeight());
                    });
                    parallelWidenAndMovement.play();

                } else {
                    /* position the thing */
                    node.setTranslateX(posX);
                    node.setTranslateY(posY);
                    node.setOpacity(1.0);
                    handleOpenDone(node);
                }

        } else {
            openStage(node);
        }
    }
    
    public static void closeInPlace(final WindowComponent node, double pause){
        if(windowingStyle.equals(DisplayConfig.RUNMODE_DEFAULT)){
            if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(250), node);
                scaleTransition.setToX(0.0);
                scaleTransition.setToY(0.0);
                scaleTransition.setCycleCount(1);
                scaleTransition.setAutoReverse(false);

                FadeTransition fadeOuTransition = new FadeTransition(Duration.millis(250), node);
                fadeOuTransition.setToValue(0.0);
                fadeOuTransition.setCycleCount(1);
                fadeOuTransition.setAutoReverse(false);

                final ParallelTransition parallelRemovement = new ParallelTransition();
                parallelRemovement.getChildren().addAll(scaleTransition, fadeOuTransition);
                parallelRemovement.setCycleCount(1);
                parallelRemovement.setAutoReverse(false);

                if(pause!=0){
                    parallelRemovement.setDelay(Duration.millis(pause));
                }

                parallelRemovement.setOnFinished((ActionEvent t) -> {
                    handleCloseDone(node,CLOSE_IN_PLACE);
                });
                parallelRemovement.play();
            } else {
                handleCloseDone(node,CLOSE_IN_PLACE);
            }
        } else {
            closeStage(node);
        }
    }
    
    public static void closeToLeft(final WindowComponent node){
        if(windowingStyle.equals(DisplayConfig.RUNMODE_DEFAULT)){
            if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){
                /* Fade in */
                FadeTransition fadeOutTransition = new FadeTransition(Duration.millis(250), node);
                fadeOutTransition.setToValue(0.0);
                fadeOutTransition.setCycleCount(1);
                fadeOutTransition.setAutoReverse(false);
                /* horizontal movement */
                TranslateTransition translatePositionTransition = new TranslateTransition(Duration.millis(250), node);
                translatePositionTransition.setToX(0 - node.getWidth());
                translatePositionTransition.setCycleCount(1);
                translatePositionTransition.setAutoReverse(false);

                final ParallelTransition parallelGoAwayLeftMovement = new ParallelTransition();
                parallelGoAwayLeftMovement.getChildren().addAll(fadeOutTransition, translatePositionTransition);
                parallelGoAwayLeftMovement.setCycleCount(1);
                parallelGoAwayLeftMovement.setAutoReverse(false);
                parallelGoAwayLeftMovement.setOnFinished((ActionEvent t) -> {
                    handleCloseDone(node,CLOSE_TO_LEFT);
                });
                parallelGoAwayLeftMovement.play();
            } else {
                handleCloseDone(node,CLOSE_TO_LEFT);
            }      
        } else {
            closeStage(node);
        }
    }
    
    static void openStage(final WindowComponent node){
        node.getStagedWindow().show();
        node.getStagedWindow().centerOnScreen();
        handleOpenDone(node);
    }
    
    static void closeStage(final WindowComponent node){
        handleCloseDone(node, WindowAnimations.CLOSE_IN_PLACE);
        if(!Platform.isFxApplicationThread()){
            Platform.runLater(() -> {
                node.getStagedWindow().close();
            });
        } else {
            node.getStagedWindow().close();
        }
    }
    
    static void handleOpenDone(final WindowComponent node){
        if (node instanceof WindowAnimationDoneListener) {
            ((WindowAnimationDoneListener) node).handleAnimationDone(OPEN);
        }
    }
    
    static void handleCloseDone(final WindowComponent node, final String method){
        if (node instanceof WindowAnimationDoneListener) {
            if(!Platform.isFxApplicationThread()){
                Platform.runLater(() -> {
                    ((WindowAnimationDoneListener) node).handleAnimationDone(method);
                });
            } else {
                ((WindowAnimationDoneListener) node).handleAnimationDone(method);
            }
        }
    }
    
}
