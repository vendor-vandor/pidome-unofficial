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

package org.pidome.client.system.scenes.components.mainstage.displays;

import eu.hansolo.enzo.canvasled.Led;
import eu.hansolo.enzo.canvasled.LedBuilder;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.domotics.components.remotes.PiDomeRemote;
import org.pidome.client.system.domotics.components.remotes.PiDomeRemoteButton;
import org.pidome.client.system.domotics.components.remotes.PiDomeRemoteRow;
import org.pidome.client.system.domotics.components.remotes.PiDomeRemoteSection;
import org.pidome.client.system.domotics.components.remotes.PiDomeRemotes;
import org.pidome.client.system.domotics.components.remotes.PiDomeRemotesException;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;
import org.pidome.client.system.scenes.windows.TitledWindow;

/**
 *
 * @author John
 */
public class RemoteDisplay extends TitledWindow {

    PiDomeRemote remote;
    
    Led led;
    
    double maxRemoteWidth             = 258 * DisplayConfig.getWidthRatio();
    double maxRemoteContainerWidth    = (maxRemoteWidth-10) * DisplayConfig.getWidthRatio();
    double maxRemoteContentWidth      = (maxRemoteWidth-50) * DisplayConfig.getWidthRatio();
    double maxRemoteContentLeftMargin = 20 * DisplayConfig.getWidthRatio();
    
    static Logger LOG = LogManager.getLogger(RemoteDisplay.class);
    
    public RemoteDisplay(Object... remoteIds) throws Exception {
        this((PiDomeRemote)PiDomeRemotes.getRemote(Integer.valueOf((String)remoteIds[0])));
    }
    
    public RemoteDisplay(PiDomeRemote remote) {
        super("remote_" + remote.getId(), remote.getName());
        this.remote = remote;
        Color ledColor;
        led=LedBuilder.create()
                .minSize(maxRemoteContentLeftMargin, maxRemoteContentLeftMargin)
                .maxSize(maxRemoteContentLeftMargin, maxRemoteContentLeftMargin)
                .frameVisible(false)
                .build();
        if(remote.hasSendDevice()){
            ledColor = Color.GREEN;
            led.onProperty().addListener((ChangeListener)(ObservableValue observable, Object oldValue, Object newValue) -> {
                if((boolean)newValue==true){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                    }
                    Platform.runLater(() -> {
                        led.setOn(false);
                    });
                }
            });
        } else {
            ledColor = Color.RED;
            led.setBlinking(true);
        }
        led.setLedColor(ledColor);
    }

    @Override
    protected void setupContent() {
        VBox remoteBox = new VBox();
        remoteBox.setId("pidomeremote");
        remoteBox.setMinWidth(maxRemoteContainerWidth);
        remoteBox.setMaxWidth(maxRemoteContainerWidth);
        remoteBox.setPrefWidth(maxRemoteContainerWidth);
        
        HBox nameBox = getRemoteTop();
        VBox.setMargin(nameBox, new Insets(5,
                                           0,
                                           5,
                                           maxRemoteContentLeftMargin));
        
        remoteBox.getChildren().add(nameBox);
        
        for(PiDomeRemoteSection section:this.remote.getConstruct()){
            VBox sectionBox = createSection(section);
            VBox.setMargin(sectionBox, new Insets(5,
                                                  0,
                                                  5,
                                                  maxRemoteContentLeftMargin));
            remoteBox.getChildren().add(sectionBox);
        }
        setContent(remoteBox);
    }

    private VBox createSection(PiDomeRemoteSection section){
        VBox sectionBox = new VBox();
        sectionBox.setMinWidth(maxRemoteContentWidth);
        sectionBox.setMaxWidth(maxRemoteContentWidth);
        sectionBox.setPrefWidth(maxRemoteContentWidth);
        sectionBox.getStyleClass().add("remotesection");
        for (PiDomeRemoteRow row: section.getRows()){
            sectionBox.getChildren().add(createRow(row));
        }
        return sectionBox;
    }
    
    private HBox createRow(PiDomeRemoteRow row){
        HBox rowBox = new HBox();
        rowBox.setMinWidth(maxRemoteContentWidth);
        rowBox.setMaxWidth(maxRemoteContentWidth);
        rowBox.setPrefWidth(maxRemoteContentWidth);
        rowBox.getStyleClass().add("remoterow");
        rowBox.setAlignment(Pos.CENTER);
        for (PiDomeRemoteButton button: row.getButtons()){
            rowBox.getChildren().add(createButton(button));
        }
        return rowBox;
    }
    
    private Button createButton(PiDomeRemoteButton button){
        Button buttonBox = new Button();
        buttonBox.setPadding(new Insets(2,2,2,2));
        buttonBox.getStyleClass().add("remotebutton");
        Image backgroundImage = new ImageLoader("remotes/" + button.getCategory() + "/" + button.getType() + ".png", 46,38).getImage();
        switch(button.getButtonTypeCategory()){
            case PREDEF:
                buttonBox.setGraphic(new ImageView(backgroundImage));
            break;
            case DEFAULT:
                buttonBox.setGraphic(new ImageView(backgroundImage));
                buttonBox.setText(button.getLabel());
                buttonBox.setContentDisplay(ContentDisplay.CENTER);
            break;
            case COLOR:
                Rectangle rect = new Rectangle();
                rect.setWidth(37 * DisplayConfig.getWidthRatio());
                rect.setHeight(30 * DisplayConfig.getHeightRatio());
                rect.setFill(button.getColor());
                Pane pane = new StackPane();
                pane.setMinSize(46 * DisplayConfig.getWidthRatio(), 38 * DisplayConfig.getHeightRatio());
                pane.setMaxSize(46 * DisplayConfig.getWidthRatio(), 38 * DisplayConfig.getHeightRatio());
                rect.setTranslateX(-1);
                pane.getChildren().addAll(new ImageView(backgroundImage),rect);
                buttonBox.setGraphic(pane);
            break;
        }
        buttonBox.setOnMouseClicked((MouseEvent event) -> {
            led.setOn(true);
            try {
                PiDomeRemotes.getRemote(remote.getId()).sendRemoteSignal(button.getId());
            } catch (PiDomeRemotesException ex) {
                //// No specific message. If it can not be send it is already handled prior.
            }
        });
        
        return buttonBox;
    }
    
    private HBox getRemoteTop(){
        HBox nameBox = new HBox();
        nameBox.setMinWidth(maxRemoteContentWidth);
        nameBox.setMaxWidth(maxRemoteContentWidth);
        nameBox.setPrefWidth(maxRemoteContentWidth);
        nameBox.getStyleClass().add("remotetoptitle");
                
        Label remoteName = new Label(this.remote.getName());
        remoteName.setMinWidth(maxRemoteContentWidth - (20* DisplayConfig.getHeightRatio()));
        remoteName.setMaxWidth(maxRemoteContentWidth - (20* DisplayConfig.getHeightRatio()));
        remoteName.setPrefWidth(maxRemoteContentWidth - (20* DisplayConfig.getHeightRatio()));
        
        nameBox.getChildren().addAll(remoteName, led);
        return nameBox;
    }
    
    @Override
    protected void removeContent() {
        
    }
    
}
