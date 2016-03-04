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

import java.util.Timer;
import java.util.TimerTask;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.AppResources;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.domotics.components.notifications.Notifications.NotificationType;

/**
 *
 * @author John
 */
public class NotificationMessage extends VBox {
    
    Timer timer;
    
    ImageView typeIcon;

    HBox header = new HBox(DisplayConfig.getWidthRatio() * 2);
    
    NotificationsDisplay display;
    
    static Logger LOG = LogManager.getLogger(NotificationMessage.class);
    
    public NotificationMessage(NotificationType type, String subject, String message){
        this.getStyleClass().add("notificationMessage");
        this.setPrefSize(400,100);
        this.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        String iconName = "messageInformation.png";
        switch(type){
            case WARNING:
                iconName = "messageWarning.png";
            break;
            case ERROR:
                iconName = "messageError.png";
            break;
        }
        typeIcon = new ImageView(new Image(AppResources.getImage("dialogs/"+iconName)));
        typeIcon.setFitHeight(25);
        typeIcon.setPreserveRatio(true);
        Label headerText = new Label(subject);
        headerText.setAlignment(Pos.CENTER_LEFT);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(typeIcon,headerText);
        header.getStyleClass().add("header");
        
        Label messageText = new Label(message);
        messageText.setWrapText(true);
        
        StackPane messageholder = new StackPane();
        messageholder.setMinSize(398, 73);
        messageholder.setMaxSize(398, 73);
        messageholder.getStyleClass().add("body");
        messageholder.setPadding(new Insets(5,5,5,5));
        messageholder.getChildren().add(messageText);
        StackPane.setAlignment(messageText, Pos.TOP_LEFT);
        
        this.getChildren().addAll(header,messageholder);
        LOG.info("Adding message: {} to view", subject);
    }
    
    protected final void setDisplay(NotificationsDisplay display){
        this.display = display;
    }
    
    protected final void added(){
        timer = new Timer();
        timer.schedule(new TimerCloseTask(), 3000);
    }
    
    protected final void close(){
        this.display.close(this);
        this.display = null;
    }
    
    private class TimerCloseTask extends TimerTask {
        @Override
        public void run() {
            close();
        }
        
    }
    
    
    
}
