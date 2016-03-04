/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.dialogs;

import javafx.event.EventHandler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import org.pidome.client.config.AppResources;
import org.pidome.client.system.scenes.MainScene;
import org.pidome.client.system.scenes.windows.ComponentUtils;

/**
 *
 * @author John Sirach
 */
public class BaseDialog {

    Pane parentPane;
    public final static String ERRORMESSAGE   = "ERRORMESSAGE";
    public final static String WARNINGMESSAGE = "WARNINGMESSAGE";
    public final static String INFOMESSAGE    = "INFOMESSAGE";
    String TYPE = "";
    String title = "";
    String message = "";
    
    double baseWidth = 500;
    double baseHeight = 100;
    
    protected Popup popMessage;
    
    public BaseDialog(String type){
        TYPE = type;
    }
    
    protected final void setTitle(String dialogTitle){
        title = dialogTitle;
    }
    
    protected final void setMessage(String dialogMessage){
        message = dialogMessage;
    }
    
    protected void show(){
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
        skeleton.setPrefWidth(baseWidth);
        skeleton.setMinHeight( baseHeight+70);
        skeleton.setId("BaseDialog");
        skeleton.setAlignment(Pos.TOP_LEFT);
        skeleton.getChildren().add(createTitle());
        skeleton.getChildren().add(messageContents());
        skeleton.getChildren().add(buttons());
        return skeleton;
    }
    
    HBox buttons(){
        HBox hbox = new HBox();
        DropShadow ds = new DropShadow();
        ds.setColor(Color.BLACK);
        hbox.setPrefSize(baseWidth, 40);
        hbox.setAlignment(Pos.CENTER);
        Button okButton = new Button("Ok");
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
        hbox.setPrefSize(baseWidth, 30);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getStyleClass().add("titlebar");
        Label titleLabel = new Label(title);
        HBox.setMargin(titleLabel, new Insets(0, 0, 0, 10));
        hbox.getChildren().add(titleLabel);
        ComponentUtils.addDraggableNode(hbox);
        return hbox;
    }
    
    HBox messageContents(){
        HBox bg = new HBox();
        bg.setPrefWidth(baseWidth);
        bg.setMinHeight(baseHeight);
        ImageView iv;
        switch(TYPE){
            case BaseDialog.ERRORMESSAGE:
                iv = new ImageView(new Image(AppResources.getImage("dialogs/messageError.png")));
            break;
            case BaseDialog.WARNINGMESSAGE:
                iv = new ImageView(new Image(AppResources.getImage("dialogs/messageWarning.png")));
            break;
            default:
                iv = new ImageView(new Image(AppResources.getImage("dialogs/messageInformation.png")));
            break;
        }
        HBox.setMargin(iv, new Insets(14, 0, 0, 14));
        bg.getChildren().add(iv);
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        HBox.setMargin(messageLabel, new Insets(14, 0, 0, 18));
        
        messageLabel.getStyleClass().add("title");
        bg.getChildren().add(messageLabel);
        bg.setAlignment(Pos.TOP_LEFT);
        return bg;
    }
    
    
    
}
