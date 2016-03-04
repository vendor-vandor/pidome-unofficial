/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.scenes;

import javafx.scene.Node;
import javafx.scene.Parent;
import org.pidome.client.phone.scenes.visuals.DialogBox;

/**
 *
 * @author John
 */
public interface PositionedPane {
    
    public void openAtPosition(double x, double y, Parent node);
    
    public boolean hasPositioned(Node node);
    
    public void closePositioned(Node node);
    
    public void showPopup(DialogBox node);
    
    public void closePopup(DialogBox node);
    
    public boolean hasPopup(DialogBox node);
    
}
