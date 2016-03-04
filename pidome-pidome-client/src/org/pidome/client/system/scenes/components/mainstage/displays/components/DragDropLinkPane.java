/*
 * Copyright 2014 John.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.client.system.scenes.components.mainstage.displays.components;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.scenes.MainScene;

/**
 *
 * @author John
 */
public abstract class DragDropLinkPane extends StackPane {
    
    static Logger LOG = LogManager.getLogger(DragDropLinkPane.class);
    
    EventHandler<DragEvent> desktopDrag = this::desktopDragHelper;
    ImageView moveItem;
    
    boolean inDrag = false;
    
    public DragDropLinkPane(){
        if(!DisplayConfig.getRunMode().equals(DisplayConfig.RUNMODE_WIDGET)){
            setDragHandler();
        }
    }
    
    final void setDragHandler(){
        setOnDragDetected((MouseEvent event) -> {
            SnapshotParameters snapParams = new SnapshotParameters();
            snapParams.setFill(Color.TRANSPARENT);
            final WritableImage snapshot = snapshot(snapParams, null);
            moveItem = new ImageView(snapshot);
            moveItem.setOpacity(0.7);
            moveItem.toFront();
            moveItem.setMouseTransparent(true);
            MainScene.getPane().getChildren().add(moveItem);
            MainScene.getPane().addEventHandler(DragEvent.DRAG_OVER,desktopDrag);
            moveItem.setVisible(true);
            Dragboard db = startDragAndDrop(TransferMode.LINK);
            /* Just put something in it, otherwise it won't work? */
            ClipboardContent content = new ClipboardContent();
            content.putString(this.getClass().getName());
            db.setContent(content);
            inDrag = true;

            event.consume();
        });
        setOnDragDone((DragEvent event) -> {
            moveItem.setVisible(false);
            MainScene.getPane().getChildren().remove(moveItem);
            MainScene.getPane().removeEventHandler(DragEvent.DRAG_OVER,desktopDrag);
            moveItem = null;
            if (event.getTransferMode() == TransferMode.LINK) {
                //LOG.info("Drag stuff: {}", event);
                //dragDropDone(event.getGestureTarget());
            }
            event.consume();
        });
    }
    
    public abstract void dragDropDone(Object source);
    
    final void desktopDragHelper(DragEvent eventInternal){
        Point2D localPoint = MainScene.getPane().sceneToLocal(new Point2D(eventInternal.getSceneX(), eventInternal.getSceneY()));
        moveItem.relocate(
                (int) (localPoint.getX() - moveItem.getBoundsInLocal().getWidth() / 2),
                (int) (localPoint.getY() - moveItem.getBoundsInLocal().getHeight() / 2));
        eventInternal.consume();
    }
    
    public final boolean dragging(){
        return this.inDrag;
    }
    
}
