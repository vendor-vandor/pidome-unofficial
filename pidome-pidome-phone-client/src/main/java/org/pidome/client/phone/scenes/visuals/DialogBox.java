/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.scenes.visuals;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

/**
 *
 * @author John
 */
public class DialogBox extends BorderPane {

    private final Label header = new Label();
    private List<PopUpButton> buttons;
    private final HBox buttonBox = new HBox();
    private final ScrollPane contentPane = new ScrollPane();
    private DialogBoxActionListener listener;
    
    public DialogBox(){
        StackPane.setAlignment(this, Pos.CENTER);
        setPrefSize(Control.USE_COMPUTED_SIZE,Control.USE_COMPUTED_SIZE);
        setMaxSize(Control.USE_PREF_SIZE,Control.USE_PREF_SIZE);
        getStyleClass().add("popup");
        contentPane.getStyleClass().add("popup-content");
    }
    
    public DialogBox(String title){
        this();
        header.setText(title);
        header.getStyleClass().add("header");
    }
    
    public final void setContent(Pane content){
        contentPane.setContent(content);
        contentPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentPane.setFitToWidth(true);
    }
    
    public final void addListener(DialogBoxActionListener listener){
        if(this.listener==null){
            this.listener = listener;
        }
    }
    
    public final void removeListener(DialogBoxActionListener listener){
        this.listener = null;
    }
    
    public final void setButtons(PopUpButton... buttons){
        if(buttons.length>0){
            this.buttons = Arrays.asList(buttons);
        }
    }
    
    private void provideListener(String id){
        listener.handleDialogAction(id);
    }
    
    public final void build(){
        if(header!=null){
            StackPane.setMargin(header, new Insets(0));
            setTop(header);
            widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                header.setPrefWidth(newValue.doubleValue());
            });
        }
        setCenter(contentPane);
        if(buttons!=null && buttons.size()>0){
            widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                buttonBox.setPrefWidth(newValue.doubleValue()-2.0);
            });
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setMinWidth(Control.USE_PREF_SIZE);
            buttonBox.setMaxWidth(Control.USE_PREF_SIZE);
            buttonBox.getStyleClass().add("bottom-button-bar");
            Iterator<PopUpButton> buttons = this.buttons.iterator();
            while(buttons.hasNext()){
                PopUpButton button = buttons.next();
                button.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
                    provideListener((String)button.getUserData());
                });
                buttonBox.getChildren().add(button);
            }
            setBottom(buttonBox);
        }
    }
    
    public static class PopUpButton extends StackPane {
        
        public PopUpButton(String id, String caption){
            
            HBox.setHgrow(this, Priority.ALWAYS);
            getStyleClass().add("button-field");
            setAlignment(Pos.CENTER);
            setUserData(id);
            
            Label buttonLabel = new Label(caption);
            buttonLabel.getStyleClass().add("button");
            buttonLabel.setAlignment(Pos.CENTER);
            getChildren().add(buttonLabel);
            
        }
        
    }
    
}