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

package org.pidome.client.system.scenes.components.mainstage.applicationsbar;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;
import org.pidome.client.system.scenes.components.mainstage.displays.clientsettings.ClientSettings;
import org.pidome.client.system.scenes.windows.WindowManager;

/**
 *
 * @author John
 */
public class TaskBar extends HBox implements ClientDataConnectionListener {
    
    TaskBarStatusIcon netIcon = new TaskBarStatusIcon("network-status");
    TaskBarStatusIcon loggedInIcon = new TaskBarStatusIcon("loggedin-status");
    
    HBox iconsContainer = new HBox();
    
    public TaskBar(){
        super(6*DisplayConfig.getWidthRatio());
        setMaxHeight(44*DisplayConfig.getHeightRatio());
        setTranslateX(2*DisplayConfig.getHeightRatio());
        setPrefWidth(579*DisplayConfig.getWidthRatio());
        setMinWidth(579*DisplayConfig.getWidthRatio());
        setMaxWidth(579*DisplayConfig.getWidthRatio());
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("taskbar");
        ClientData.addClientLoggedInConnectionListener(this);
        
        Image rightOff = new ImageLoader("notificationbar/scrollcontent.png",13,14).getImage();
        Image rightOn = new ImageLoader("notificationbar/scrollcontentactive.png",13,14).getImage();
        ImageView rightArrow = new ImageView(rightOff);
        
        Image leftOff = new ImageLoader("notificationbar/scrollcontent.png",13,14).getImage();
        Image leftOn = new ImageLoader("notificationbar/scrollcontentactive.png",13,14).getImage();
        ImageView leftArrow = new ImageView(leftOff);
        leftArrow.setScaleX(-1);
        
        getChildren().addAll(leftArrow, iconsContainer, rightArrow);
        
        iconsContainer.setPrefWidth(526*DisplayConfig.getWidthRatio());
        iconsContainer.setMinWidth(526*DisplayConfig.getWidthRatio());
        iconsContainer.setMaxWidth(526*DisplayConfig.getWidthRatio());
        
        HBox.setMargin(leftArrow, new Insets(1,0,0,5*DisplayConfig.getWidthRatio()));
        
    }
    
    public final void build(){
        ImageView settingsIcon = new ImageView(new ImageLoader("notificationbar/settings.png", 33,33).getImage());
        
        settingsIcon.setOnMouseClicked((MouseEvent t) -> {
            WindowManager.openWindow(new ClientSettings());
        });
        
        VBox serverInfoField = new VBox();
        serverInfoField.setAlignment(Pos.CENTER_LEFT);
        serverInfoField.getChildren().addAll(netIcon.getImage(),loggedInIcon.getImage());
        
        HBox.setMargin(serverInfoField, new Insets(1,0,0,10*DisplayConfig.getWidthRatio()));
        HBox.setMargin(settingsIcon, new Insets(3,0,0,3*DisplayConfig.getWidthRatio()));
        
        iconsContainer.getChildren().addAll(serverInfoField,settingsIcon);
    }

    @Override
    public void handleClientDataConnectionEvent(ClientDataConnectionEvent event) {
        switch (event.getEventType()) {
            case ClientDataConnectionEvent.LOGGEDIN:
                String[] data = event.getClientData();
                Platform.runLater(() -> {
                    loggedInIcon.swapStatus("on");
                });
            break;
            case ClientDataConnectionEvent.CONNECTED:
                Platform.runLater(() -> {
                    netIcon.swapStatus("on");
                });
                break;
            case ClientDataConnectionEvent.DISCONNECTED:
                Platform.runLater(() -> {
                    loggedInIcon.swapStatus("off");
                    netIcon.swapStatus("off");
                });
                break;
        }
    }
    
}
