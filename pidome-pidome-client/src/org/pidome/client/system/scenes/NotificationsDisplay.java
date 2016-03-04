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

package org.pidome.client.system.scenes;

import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;

/**
 *
 * @author John
 */
public final class NotificationsDisplay {

    static ObservableList<NotificationMessage> realList = FXCollections.observableArrayList();
    static ObservableList<NotificationMessage> messageList = FXCollections.synchronizedObservableList(realList);

    final ListChangeListener viewHandler = this::viewHandler;
    
    private Pane pane;
    
    double messageHeight = 100;
    double messageSpacing= 10;
    
    static Logger LOG = LogManager.getLogger(NotificationsDisplay.class);
    
    public NotificationsDisplay(){
        messageList.addListener(viewHandler);
    }
    
    protected final void setDisplay(Pane pane){
        this.pane = pane;
    }
    
    public final void add(NotificationMessage message){
        message.setDisplay(this);
        messageList.add(message);
    }
    
    public final void close(NotificationMessage message){
        if(messageList.contains(message)){
            messageList.remove(message);
        }
    }
    
    private void viewHandler(ListChangeListener.Change change){
        while(change.next()){
            if(change.wasAdded()){
                addMessageToView(change.getAddedSubList());
            } else if(change.wasRemoved()){
                removeMessageFromView(change.getRemoved());
            }
        }
    }
    
    private void addMessageToView(List<NotificationMessage> list){
        if(pane!=null){
            int counter = 1;
            for(NotificationMessage message:list){
                int position = (messageList.size() - list.size()) + counter;
                double posx = DisplayConfig.getScreenWidth() - (400 + messageSpacing);
                double posy = DisplayConfig.getScreenHeight() - ((messageHeight + messageSpacing)*position);
                message.setTranslateX(posx);
                message.setTranslateY(posy);
                LOG.info("Displaying message at posx: {}, posy: {}", posx,posy);
                Platform.runLater(() -> {
                    pane.getChildren().add(message);
                    message.added();
                });
                counter++;
            }
        }
    }
    
    private void removeMessageFromView(List<NotificationMessage> list){
        for(NotificationMessage message:list){
            Platform.runLater(() -> {
                pane.getChildren().remove(message);
            });
        }
    }
    
}
