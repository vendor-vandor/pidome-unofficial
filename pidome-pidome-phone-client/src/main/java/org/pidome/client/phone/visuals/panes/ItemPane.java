/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.visuals.panes;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.pidome.client.phone.visuals.interfaces.Destroyable;

/**
 *
 * @author John
 */
public abstract class ItemPane extends VBox implements Destroyable {

    public ItemPane(){
        super();
        getStyleClass().addAll("item-field");
    }
    
    public ItemPane(String title){
        this();
        Label textTitle = new Label(title);
        textTitle.getStyleClass().addAll("header");
        textTitle.setPrefWidth(Double.MAX_VALUE);
        getChildren().add(textTitle);
    }
    
    public final void setContent(Node contentNode){
        getChildren().add(contentNode);
    }

    @Override
    public abstract void destroy();
    
}