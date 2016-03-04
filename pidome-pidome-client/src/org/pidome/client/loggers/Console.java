/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.loggers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import org.pidome.client.system.scenes.ComponentDimensions;
import org.pidome.client.system.scenes.MainScene;
import org.pidome.client.system.scenes.windows.ComponentUtils;

/**
 *
 * @author John Sirach
 */
public final class Console extends ComponentDimensions {
    
    final static TextArea area = new TextArea();
    
    Popup popMessage;
    
    public Console (){
        setDimensions(1152,324);
    }

    public final void show(){
        Platform.runLater(() -> {
            popMessage = new Popup();
            popMessage.getContent().add(createDialog());
            popMessage.setAutoFix(true);
            popMessage.centerOnScreen();
            popMessage.show(MainScene.getWindow());
        });
    }
    
    VBox createDialog(){
        DropShadow ds = new DropShadow();
        VBox skeleton = new VBox();
        ds.setColor(Color.BLACK);
        skeleton.setEffect(ds);
        skeleton.setPrefWidth(getWidth());
        skeleton.setMinHeight(getHeight()+70);
        skeleton.setId("BaseDialog");
        skeleton.setAlignment(Pos.TOP_LEFT);
        skeleton.getChildren().add(createTitle());
        TextArea aConsole = createConsole();
        VBox.setMargin(area, new Insets(10,0,0,10));
        skeleton.getChildren().add(aConsole);
        skeleton.getChildren().add(buttons());
        return skeleton;
    }
    
    TextArea createConsole(){
        area.setId("Console");
        area.setPrefSize(getWidth() - (20 * widthRatio), getHeight() - (20*heightRatio));
        area.setMaxSize(getWidth() - (20 * widthRatio), getHeight() - (20*heightRatio));
        area.setEditable(false);
        return area;
    }
    
    HBox buttons(){
        HBox hbox = new HBox();
        DropShadow ds = new DropShadow();
        ds.setColor(Color.BLACK);
        hbox.setPrefSize(getWidth(), 40);
        hbox.setAlignment(Pos.CENTER);
        Button okButton = new Button("Close");
        okButton.setEffect(ds);
        HBox.setMargin(okButton, new Insets(20, 40, 20, 40));
        okButton.setOnAction((ActionEvent t) -> {
            popMessage.hide();
        });
        hbox.getChildren().add(okButton);
        return hbox;
    }
    
    HBox createTitle(){
        HBox hbox = new HBox();
        hbox.setPrefSize(getWidth(), 30);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getStyleClass().add("titlebar");
        Label titleLabel = new Label("System console");
        HBox.setMargin(titleLabel, new Insets(0, 0, 0, 10));
        hbox.getChildren().add(titleLabel);
        ComponentUtils.addDraggableNode(hbox);
        return hbox;
    }
    
    public static void addLog(final String message){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                /// Temporary comment out, eats memory and buggers the application thread quite hard. need to restrict length
                //area.appendText(message);
            }
        });
    }
 
}
