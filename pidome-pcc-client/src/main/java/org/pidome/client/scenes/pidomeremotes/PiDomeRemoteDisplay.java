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

package org.pidome.client.scenes.pidomeremotes;

import eu.hansolo.enzo.canvasled.Led;
import eu.hansolo.enzo.canvasled.LedBuilder;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.pidome.client.entities.remotes.PiDomeRemote;
import org.pidome.client.entities.remotes.PiDomeRemoteButton;
import org.pidome.client.entities.remotes.PiDomeRemoteRow;
import org.pidome.client.entities.remotes.PiDomeRemoteSection;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.services.ServiceConnector.DisplayType;
import org.pidome.client.tools.DisplayTools;


/**
 *
 * @author John
 */
public class PiDomeRemoteDisplay extends StackPane {

    private final PiDomeRemote remote;
    
    private final Led led;
    
    private final double maxRemoteWidth;
    private final double maxRemoteContentWidth;
    
    public PiDomeRemoteDisplay(PiDomeRemote remote) {
        if(DisplayTools.getUserDisplayType().equals(DisplayType.SMALL)){
            this.setMinWidth(ScenesHandler.getContentWidthProperty().getValue());
        } else {
            this.setMinWidth(300);
            this.setMaxWidth(300);
        }
        maxRemoteWidth = this.getMinWidth() - 30;
        maxRemoteContentWidth = maxRemoteWidth-50;
        this.remote = remote;
        Color ledColor;
        led=LedBuilder.create()
                .minSize(20, 20)
                .maxSize(20, 20)
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

    protected void setupContent() {
        VBox remoteContent = new VBox();
        remoteContent.setId("pidomeremote");
        remoteContent.setMinWidth(USE_PREF_SIZE);
        remoteContent.setMaxWidth(USE_PREF_SIZE);
        remoteContent.setPrefWidth(maxRemoteWidth);
        
        remoteContent.setPadding(new Insets(12.5,0,12.5,0));
        
        this.setPadding(new Insets(20,0,20,0));
        StackPane.setAlignment(remoteContent, Pos.TOP_CENTER);
        
        HBox nameBox = getRemoteTop();
        nameBox.setMinWidth(USE_PREF_SIZE);
        nameBox.setMaxWidth(USE_PREF_SIZE);
        nameBox.setPrefWidth(maxRemoteContentWidth);
        VBox.setMargin(nameBox, new Insets(0,0,0,25));
        
        remoteContent.getChildren().add(nameBox);
        
        double maxButtonWidth = (maxRemoteContentWidth/4)-10;
        double maxButtonHeight = maxButtonWidth * 0.83;
        
        for(PiDomeRemoteSection section:this.remote.getConstruct()){
            VBox sectionSet = new VBox();
            sectionSet.setMinWidth(USE_PREF_SIZE);
            sectionSet.setMaxWidth(USE_PREF_SIZE);
            sectionSet.setPrefWidth(maxRemoteContentWidth);
            sectionSet.setPadding(new Insets(12.5,0,0,0));
            VBox.setMargin(sectionSet, new Insets(0,0,0,25));
            for (PiDomeRemoteRow row: section.getRows()){
                HBox rowBox = createRow(row, maxButtonWidth, maxButtonHeight);
                rowBox.setMinWidth(USE_PREF_SIZE);
                rowBox.setMaxWidth(USE_PREF_SIZE);
                rowBox.setPrefWidth(maxButtonWidth * row.getCellsAmount());
                if(row.getCellsAmount() == 3){
                    VBox.setMargin(rowBox, new Insets(5,0,0,maxButtonWidth/2));
                } else {
                    VBox.setMargin(rowBox, new Insets(5,0,0,0));
                }
                sectionSet.getChildren().add(rowBox);
            }
            remoteContent.getChildren().add(sectionSet);
        }
        this.getChildren().add(remoteContent);
    }

    private HBox createRow(PiDomeRemoteRow row, double maxButtonWidth, double maxButtonHeight){
        HBox rowBox = new HBox();
        rowBox.getStyleClass().add("remoterow");
        for (PiDomeRemoteButton button: row.getButtons()){
            Button butt = createButton(button, maxButtonWidth, maxButtonHeight);
            HBox.setMargin(butt, new Insets(0,5,0,5));
            HBox.setHgrow(butt, Priority.ALWAYS);
            rowBox.getChildren().add(butt);
        }
        return rowBox;
    }
    
    private Button createButton(PiDomeRemoteButton button, double maxButtonWidth, double maxButtonHeight){
        Button buttonBox = new Button();
        buttonBox.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        buttonBox.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        buttonBox.setPrefSize(maxButtonWidth, maxButtonHeight);
        buttonBox.getStyleClass().add("remotebutton");
        ImageView backgroundImage;
        try {
            backgroundImage = new ImageView(new Image(this.getClass().getResource("/org/pidome/client/appimages/remotes/" + button.getCategory() + "/" + button.getType() + ".png").toExternalForm()));
        } catch (Exception ex){
            backgroundImage = new ImageView(new Image(this.getClass().getResource("/org/pidome/client/appimages/remotes/" + button.getType() + ".png").toExternalForm()));
        }
        backgroundImage.setFitWidth(maxButtonWidth);
        ///backgroundImage.setFitHeight(46);
        backgroundImage.preserveRatioProperty().set(true);
        backgroundImage.setCacheHint(CacheHint.SPEED);
        switch(button.getButtonTypeCategory()){
            case PREDEF:
                buttonBox.setGraphic(backgroundImage);
                buttonBox.setContentDisplay(ContentDisplay.CENTER);
            break;
            case DEFAULT:
                Text buttonText = new Text(button.getLabel());
                buttonText.getStyleClass().add("text");
                buttonText.setStyle("-fx-font-size: " + maxButtonHeight * 0.35);
                Pane defButton = new StackPane();
                defButton.setMinSize(maxButtonWidth, maxButtonHeight);
                defButton.setMaxSize(maxButtonWidth, maxButtonHeight);
                defButton.getChildren().addAll(backgroundImage,buttonText);
                buttonBox.setGraphic(defButton);
                buttonBox.setContentDisplay(ContentDisplay.CENTER);
            break;
            case COLOR:
                Rectangle rect = new Rectangle();
                rect.setWidth(maxButtonWidth * 0.7);
                rect.setHeight(maxButtonHeight * 0.7);
                rect.setFill(Color.web(button.getColor()));
                rect.setTranslateX(-1);
                Pane pane = new StackPane();
                pane.setMinSize(maxButtonWidth, maxButtonHeight);
                pane.setMaxSize(maxButtonWidth, maxButtonHeight);
                pane.getChildren().addAll(backgroundImage,rect);
                buttonBox.setGraphic(pane);
            break;
        }
        buttonBox.setOnMouseClicked((MouseEvent event) -> {
            led.setOn(true);
            remote.sendRemoteSignal(button.getId());
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
        remoteName.setMinWidth(maxRemoteContentWidth - 20);
        remoteName.setMaxWidth(maxRemoteContentWidth - 20);
        remoteName.setPrefWidth(maxRemoteContentWidth - 20);
        
        nameBox.getChildren().addAll(remoteName, led);
        return nameBox;
    }
    
    protected void removeContent() {
        
    }
    
}
