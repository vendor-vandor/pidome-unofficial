/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system.scenes.components.mainstage.desktop;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.rpc.PidomeJSONRPC;
import org.pidome.client.system.rpc.PidomeJSONRPCException;
import org.pidome.client.system.scenes.MainScene;

/**
 *
 * @author John Sirach <john.sirach@gmail.com>
 */
public class NewDesktopShortcut {
    
    EventHandler<MouseEvent> pressedEvent;
    EventHandler<MouseEvent> releasedEvent;
    long pressStart;
    boolean inPress = false;
    boolean inDrag  = false;
    boolean created = false;
    
    Node nodeHandle;
    
    String title = "";
    String type = DesktopIcon.ITEM;
    
    String serverAction;
    Map<String,Object> serverParams;
    
    static Logger LOG = LogManager.getLogger(NewDesktopShortcut.class);
    
    public NewDesktopShortcut(Node node){
        if(!DisplayConfig.getRunMode().equals(DisplayConfig.RUNMODE_WIDGET)){
            nodeHandle = node;
            pressedEvent = (MouseEvent t) -> {
                inPress = true;
                handleLongPressEvent(t);
            };
            releasedEvent = (MouseEvent t) -> {
                inPress = false;
            };
            nodeHandle.setOnMousePressed(pressedEvent);
            nodeHandle.setOnMouseReleased(releasedEvent);
        }
    }
    
    public final void setIconType(String type){
        this.type = type;
    }
    
    public final void setServerCall(String serverAction, Map<String,Object> serverParams){
        this.serverAction = serverAction;
        this.serverParams = serverParams;
    }
    
    final void handleLongPressEvent(final MouseEvent t){
        Timer pressLength = new Timer(true);
        pressLength.schedule(
            new TimerTask() {
                @Override
                public void run() { 
                    if(inPress==true && created==false){
                        inPress = false;
                        t.consume();
                        Platform.runLater(() -> {
                            SnapshotParameters snapParams = new SnapshotParameters();
                            snapParams.setFill(Color.TRANSPARENT);
                            final WritableImage snapshot = nodeHandle.snapshot(snapParams, null);
                            final ImageView moveItem = new ImageView(snapshot);
                            moveItem.setOpacity(0.5);
                            moveItem.toFront();
                            moveItem.setMouseTransparent(true);
                            moveItem.relocate(
                                    (int) (t.getSceneX() - moveItem.getBoundsInLocal().getWidth() / 2),
                                    (int) (t.getSceneY() - moveItem.getBoundsInLocal().getHeight() / 2));
                            MainScene.getPane().getChildren().add(moveItem);
                            moveItem.setVisible(true);
                            MoveNodeCopy(moveItem);
                        });
                    }
                }
            }, 2000);
    }
    
    final void MoveNodeCopy(final ImageView CopyedNode){
        nodeHandle.setOnMouseDragged((MouseEvent t) -> {
            CopyedNode.relocate(
                    (int) (t.getSceneX() - CopyedNode.getBoundsInLocal().getWidth() / 2),
                    (int) (t.getSceneY() - CopyedNode.getBoundsInLocal().getHeight() / 2));
            t.consume();
        });
        nodeHandle.setOnDragDetected((MouseEvent me) -> {
            inDrag = true;
            me.consume();
        });
        nodeHandle.setOnMouseReleased((MouseEvent e) -> {
            CopyedNode.setVisible(false);
            if(inDrag==true){
                inDrag = false;
                setDragDone();
            }
            MainScene.getPane().getChildren().remove(CopyedNode);
            e.consume();
        });
    }
    
    final void setDragDone(){
        try {
            ClientData.sendData(PidomeJSONRPC.createExecMethod(serverAction, serverAction, serverParams));
            created = true;
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not send data for new shortcut creation: {}, ", serverAction, serverParams);
        }
    }
    
}
