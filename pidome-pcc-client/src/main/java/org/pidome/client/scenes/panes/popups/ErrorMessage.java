/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.panes.popups;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.pidome.client.scenes.ScenesHandler;

/**
 *
 * @author John
 */
public class ErrorMessage extends PopUp {

    public ErrorMessage(String title) {
        super(FontAwesomeIcon.EXCLAMATION, title);
        this.getStyleClass().add("popup-error");
    }
    
    public void setMessage(String message){
        Text showMessage = new Text();
        showMessage.setWrappingWidth(ScenesHandler.getContentWidthProperty().multiply(0.9).doubleValue());
        HBox msgPane = new HBox();
        msgPane.getChildren().add(showMessage);
        this.setContent(msgPane);
    }

    @Override
    public void unload() {
        /// not used
    }
    
}