/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.visuals.panes;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import org.pidome.client.phone.visuals.interfaces.Destroyable;

/**
 *
 * @author John
 */
public abstract class ClosableTitledPane extends StackPane implements Destroyable {
 
    private final ScrollPane contentPane = new ScrollPane();
    
    private VBox parent = new VBox();
    
    public ClosableTitledPane(String title){
        getStyleClass().addAll("popup", "variable");
        Label visualTitle = new Label(title);
        visualTitle.setPrefWidth(Double.MAX_VALUE);
        visualTitle.getStyleClass().add("header");
        parent.getChildren().add(visualTitle);
        contentPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentPane.setFitToWidth(true);
        contentPane.getStyleClass().add("popup-content");
        VBox.setVgrow(contentPane, Priority.ALWAYS);
        getChildren().add(parent);
        parent.getChildren().add(contentPane);
        setMaxHeight((Screen.getPrimary().getBounds().getHeight()/100)*80);
        setMaxWidth((Screen.getPrimary().getBounds().getWidth()/100)*80);
    }
    
    public final void setContent(Node node){
        Platform.runLater(() -> {
            this.contentPane.setContent(node);
        });
    }
    
    @Override
    public abstract void destroy();
    
}