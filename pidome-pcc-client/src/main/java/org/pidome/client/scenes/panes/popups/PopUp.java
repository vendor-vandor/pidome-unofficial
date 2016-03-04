/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.panes.popups;

import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.GlyphsDude;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.services.ServiceConnector.DisplayType;
import org.pidome.client.tools.DisplayTools;

/**
 *
 * @author John
 */
public abstract class PopUp extends BorderPane {
    
    private HBox header = new HBox();
    private HBox buttonBar = new HBox(20);
    
    List<PopUpButton> buttons = new ArrayList<>();
    
    private Pane content;
    
    private PopUpActionListener listener;
    
    ChangeListener<Number> heightChangeListener = this::heightChangeListener;
    ChangeListener<Number> widthChangeListener  = this::widthChangeListener;
    
    public PopUp(GlyphIcons icon, String title){
        if(DisplayTools.getUserDisplayType()==DisplayType.TINY){
            StackPane.setAlignment(this, Pos.TOP_CENTER);
        } else {
            StackPane.setAlignment(this, Pos.CENTER);
        }
        getStyleClass().add("popup-base");
        createHeader(icon, title);
    }
    
    private void createHeader(GlyphIcons icon, String title){
        header.getStyleClass().add("header");
        header.setSpacing(12);
        header.setPadding(new Insets(6,8,8,6));
        header.getChildren().addAll(GlyphsDude.createIcon(icon, "2em;"),new Label(title));
        header.setAlignment(Pos.CENTER_LEFT);
    }
    
    public final void setContent(Pane content){
        this.content = content;
        this.content.widthProperty().addListener(widthChangeListener);
        this.content.heightProperty().addListener(heightChangeListener);
        setPopWidth(this.content.getWidth());
        setPopHeight(this.content.getHeight());
    }

    private void widthChangeListener(ObservableValue<? extends Number> observable, Number oldValue, Number newValue){
        setPopWidth(newValue.doubleValue());
    }
    
    private void heightChangeListener(ObservableValue<? extends Number> observable, Number oldValue, Number newValue){
        setPopHeight(newValue.doubleValue());
    }
    
    private void setPopHeight(double height){
        double newHeight = height + 
                           header.getHeight() +
                           buttonBar.getHeight();
        this.setPrefHeight(newHeight);
        this.setMaxHeight(newHeight);
    }
    
    private void setPopWidth(double width){
        this.setPrefWidth(width);
        this.setMaxWidth(width);
    }
    
    public final void addListener(PopUpActionListener listener){
        if(this.listener==null){
            this.listener = listener;
        }
    }
    
    public final void removeListener(PopUpActionListener listener){
        this.listener = null;
    }
    
    public final void build(){
        setTop(header);
        setBottom(buttonBar);
        setCenter(content);
    }

    public final void show(boolean unique){
        ScenesHandler.openPopUp(this, unique);
    }
    
    public final void show(){
        show(false);
    }
    
    public final void setButtons(PopUpButton... popupButtons){
        buttonBar.setPadding(new Insets(6,8,8,6));
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        if(popupButtons.length>0){
            this.buttons = Arrays.asList(popupButtons);
            
            Iterator<PopUpButton> buttons = this.buttons.iterator();
            
            while(buttons.hasNext()){
                PopUpButton button = buttons.next();
                if(button.onActionProperty().isNull().get()){
                    button.setOnAction((ActionEvent e) -> {
                        provideListener((String)button.getUserData());
                        this.close();
                    });
                }
                buttonBar.getChildren().add(button);
            }
        } else {
            Button button = new Button("Close");
            button.setOnAction((ActionEvent e) -> {
                this.close();
            });
            buttonBar.getChildren().add(button);
        }
    }
    
    private void provideListener(String id){
        if(listener!=null) listener.handleDialogAction(id);
    }
    
    public static class PopUpButton extends Button {
        
        public PopUpButton(String id, String caption){
            super(caption);
            setUserData(id);
        }
        
    }
    
    public final void close(){
        this.content.widthProperty().removeListener(widthChangeListener);
        this.content.heightProperty().removeListener(heightChangeListener);
        removeListener(listener);
        ScenesHandler.closePopUp(this);
        unload();
    }
    
    public abstract void unload();
    
}