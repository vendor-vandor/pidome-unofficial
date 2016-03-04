/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.panes.popups;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.stage.Screen;

/**
 *
 * @author John
 */
public class ColorPickerPopUp extends PopUp {

    public ColorPickerPopUp(String title) {
        super(FontAwesomeIcon.DASHBOARD, title);
        this.setMaxSize(Screen.getPrimary().getBounds().getWidth()*0.8, Screen.getPrimary().getBounds().getHeight()*0.8);
    }

    @Override
    public void unload() {
        
    }
    
}
