/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.scenes.menus;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author John
 */
public class MenuBase extends VBox {
    
    protected MenuBase(){
        getStyleClass().add("popup-menu");
        StackPane.setAlignment(this, Pos.TOP_LEFT);
    }
    
}
