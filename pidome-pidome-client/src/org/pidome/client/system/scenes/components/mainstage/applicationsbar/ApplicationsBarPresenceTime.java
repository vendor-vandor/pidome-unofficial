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
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.domotics.components.dayparts.DayParts;
import org.pidome.client.system.domotics.components.userpresence.UserPresenceEvent;
import org.pidome.client.system.domotics.components.userpresence.UserPresences;
import org.pidome.client.system.domotics.components.userpresence.UserPresencesEventListener;
import org.pidome.client.system.scenes.windows.WindowManager;
import org.pidome.client.system.time.ClientTime;

/**
 *
 * @author John
 */
public class ApplicationsBarPresenceTime extends VBox implements UserPresencesEventListener {
    
    Label date = new Label();
    Label time = new Label();
    Label currentPresence = new Label();
    
    public ApplicationsBarPresenceTime(){
        setAlignment(Pos.CENTER);
        
        date.setAlignment(Pos.CENTER);
        
        date.setText(ClientTime.getLongDateString().getValueSafe());
        time.setText(ClientTime.get24HourNameProperty().getValueSafe());
        
        HBox PresenceTime = new HBox();
        PresenceTime.setAlignment(Pos.CENTER);
        PresenceTime.getStyleClass().add("presencetime");
        PresenceTime.getChildren().addAll(currentPresence, new Label(" - "), time);
        
        setMinSize(302*DisplayConfig.getWidthRatio(), 44*DisplayConfig.getHeightRatio());
        setMaxSize(302*DisplayConfig.getWidthRatio(), 44*DisplayConfig.getHeightRatio());
        setPrefSize(302*DisplayConfig.getWidthRatio(), 44*DisplayConfig.getHeightRatio());
        
        getChildren().addAll(PresenceTime, date);
        
        currentPresence.setOnMouseClicked(this::openPresenceWindow);
        time.setOnMouseClicked(this::openTimeDate);
        date.setOnMouseClicked(this::openTimeDate);
        
        ClientTime.get24HourNameProperty().addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            Platform.runLater(() -> { time.setText(t1); });
        });
        
        ClientTime.getLongDateString().addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            Platform.runLater(() -> { date.setText(t1); });
        });
        
        UserPresences.addPresencesEventListener(this);
        
    }

    final void openPresenceWindow(MouseEvent t){
        WindowManager.openWindow(new ApplicationsBarSettingsUserPresence(), t.getSceneX(), t.getSceneY());
    }
    
    final void openTimeDate(MouseEvent t){
        WindowManager.openWindow(new ApplicationsBarTimeDisplay(), t.getSceneX(), t.getSceneY());
    }
    
    @Override
    public void handleUserPresencesEvent(UserPresenceEvent event) {
        switch(event.getEventType()){
            case UserPresenceEvent.PRESENCECHANGED:
                Platform.runLater(() -> { currentPresence.setText(event.getPresenceName()); });
            break;
            case UserPresenceEvent.PRESENCEUPDATED:
                if(DayParts.getCurrent()==event.getPresenceId()){
                    Platform.runLater(() -> { currentPresence.setText(event.getPresenceName()); });
                }
            break;
            case UserPresenceEvent.PRESENCEREMOVED:
                if(DayParts.getCurrent()==event.getPresenceId()){
                    Platform.runLater(() -> { currentPresence.setText("Unknown"); });
                }
            break;
        }
    }
    
}
