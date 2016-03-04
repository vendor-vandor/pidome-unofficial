/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.windows;


import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author John Sirach
 */
public class ComponentUtils {

    static double dragInitialX;
    static double dragInitialY;
    
    public static void addDraggableNode(final Node node) {

        node.setOnMousePressed((MouseEvent me) -> {
            dragInitialX = me.getSceneX();
            dragInitialY = me.getSceneY();
        });

        node.setOnMouseDragged((MouseEvent me) -> {
            node.getScene().getWindow().setX(me.getScreenX() - dragInitialX);
            node.getScene().getWindow().setY(me.getScreenY() - dragInitialY);
        });
    }
    
}
