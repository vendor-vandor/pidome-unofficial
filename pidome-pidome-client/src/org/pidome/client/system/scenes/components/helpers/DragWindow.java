/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.helpers;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author John Sirach
 */
public final class DragWindow {

    double clickedObjectX;
    double clickedObjectY;
    
    EventHandler<MouseEvent> dragStartEventHandler;
    EventHandler<MouseEvent> dragProgressEventHandler;
    
    public DragWindow(final Node node) {
        node.setOnMousePressed(createDragStartEventHandler(node));
        node.setOnMouseDragged(createDragEventEventHandler(node));
    }
    
    public DragWindow(final Node node, final Node parent) {
        node.setOnMousePressed(createDragStartEventHandler(node));
        node.setOnMouseDragged(createDragEventEventHandler(parent));
    }
    
    EventHandler<MouseEvent> createDragStartEventHandler(final Node node){
        node.setPickOnBounds(false);
        dragStartEventHandler = (MouseEvent me) -> {
            node.toFront();
            clickedObjectX = (double)me.getX() + node.getTranslateX();
            clickedObjectY = (double)me.getY() + node.getTranslateY();
        };
        return dragStartEventHandler;
    }
    
    EventHandler<MouseEvent> createDragEventEventHandler(final Node node){
        dragProgressEventHandler = (MouseEvent me) -> {
            node.relocate((double) (me.getSceneX() - clickedObjectX),
                    (double) (me.getSceneY() - clickedObjectY));
        };
        return dragProgressEventHandler;
    }
    
    public void release(Node node){
        node.removeEventHandler(MouseEvent.MOUSE_CLICKED, dragStartEventHandler);
        node.removeEventHandler(MouseEvent.MOUSE_DRAGGED, dragProgressEventHandler);
    }
 
    public void release(Node node, Node parent){
        node.removeEventHandler(MouseEvent.MOUSE_CLICKED, dragStartEventHandler);
        parent.removeEventHandler(MouseEvent.MOUSE_DRAGGED, dragProgressEventHandler);
    }
    
}
