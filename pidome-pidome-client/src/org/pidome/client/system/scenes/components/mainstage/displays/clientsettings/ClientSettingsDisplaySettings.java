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

package org.pidome.client.system.scenes.components.mainstage.displays.clientsettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.pidome.client.config.AppProperties;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.scenes.MainScene;
import org.pidome.client.system.scenes.components.controls.DefaultButton;
import static org.pidome.client.system.scenes.components.mainstage.displays.clientsettings.ClientSettings.LOG;
import org.pidome.client.system.scenes.windows.SimpleErrorMessage;
import org.pidome.client.system.scenes.windows.TitledWindow;
import org.pidome.client.system.scenes.windows.WindowManager;

/**
 *
 * @author John
 */
public class ClientSettingsDisplaySettings extends VBox implements ClientSettingsItemInterface {
    
    final ToggleGroup qualityMode = new ToggleGroup();
    File newBgFile;
    
    Button setBackground = new DefaultButton("Choose image");
    Button clearBackground = new DefaultButton("Default");
    
    Button setSettings = new DefaultButton("Save display settings");
    
    public ClientSettingsDisplaySettings(){
        super(10*DisplayConfig.getHeightRatio());
        
    }
    
    final Node build(){
        TitledWindow.setMargin(this, new Insets(10*DisplayConfig.getHeightRatio(),10*DisplayConfig.getWidthRatio(),10*DisplayConfig.getHeightRatio(),10*DisplayConfig.getWidthRatio()));
        getStyleClass().add("appsettings");
        
        Text sectionTitle = new Text("Display settings");
        sectionTitle.getStyleClass().add("sectiontitle");
        Text sectionDescription = new Text("Here you can change the appearance and display settings. Some settings may require an application restart.");
        sectionDescription.getStyleClass().add("optiondescription");
        
        setSettings.addEventFilter(ActionEvent.ACTION, this::saveButtonHelper);
        
        getChildren().addAll(sectionTitle, sectionDescription, createQuality(), createBackground(),setSettings);
        return this;
    }
    
    final Node createQuality(){
        
        VBox qualitySection = new VBox(5*DisplayConfig.getHeightRatio());
        
        qualitySection.getStyleClass().add("section");
        
        Text sectionTitle = new Text("Quality settings");
        sectionTitle.getStyleClass().add("sectionheader");
        
        Text sectionDescription = new Text("Changing the quality requires you to restart the application.");
        sectionDescription.getStyleClass().add("optiondescription");
        
        RadioButton high = new RadioButton("High graphics quality, animations etc enabled");
        high.setStyle("-fx-text-fill: lightgrey;");
        high.setToggleGroup(qualityMode);
        if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)) high.setSelected(true);
        high.setUserData(DisplayConfig.QUALITY_HIGH);
        VBox.setMargin(high, new Insets(5 * DisplayConfig.getHeightRatio(), 5 * DisplayConfig.getWidthRatio(), 0, 5 * DisplayConfig.getWidthRatio()));
        
        RadioButton low = new RadioButton("Low graphics quality, animations etc disabled");
        low.setStyle("-fx-text-fill: lightgrey;");
        low.setToggleGroup(qualityMode);
        low.setUserData(DisplayConfig.QUALITY_LOW);
        if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_LOW)) low.setSelected(true);
        VBox.setMargin(low, new Insets(5 * DisplayConfig.getHeightRatio(), 5 * DisplayConfig.getWidthRatio(), 0, 5 * DisplayConfig.getWidthRatio()));
        
        qualitySection.getChildren().addAll(sectionTitle,sectionDescription,high,low);
        
        return qualitySection;
    }
    
    final void setBackgroundImageHelper(ActionEvent t){
        newBgFile = selectBgFile();
        if(newBgFile!=null){
            setSelectedFile(newBgFile);
        }
    }
    
    final void clearBackgroundHelper(ActionEvent t){
        MainScene.getRootPane().setStyle("-fx-font-size: " + DisplayConfig.getFontDpiScaler() +"px; -fx-background-image: null;");
        AppProperties.setProperty("system", "display.background", "default");
    }
    
    final Node createBackground(){
        
        VBox backgroundSection = new VBox(5*DisplayConfig.getHeightRatio());
        backgroundSection.getStyleClass().add("section");
        
        Text sectionTitle = new Text("Background settings");
        sectionTitle.getStyleClass().add("sectionheader");
        
        Text sectionDescription = new Text("Here you can change the background to an image or to the default background color.");
        sectionDescription.getStyleClass().add("optiondescription");
        
        HBox buttonBox = new HBox(10*DisplayConfig.getHeightRatio());
        
        setBackground.addEventFilter(ActionEvent.ACTION, this::setBackgroundImageHelper);
        
        clearBackground.addEventFilter(ActionEvent.ACTION,this::clearBackgroundHelper);
        
        buttonBox.getChildren().addAll(setBackground, clearBackground);

        backgroundSection.getChildren().addAll(sectionTitle,sectionDescription,buttonBox);
        return backgroundSection;
    }
    
    
    final File selectBgFile(){
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select background picture");
        fileChooser.setInitialDirectory(
                new File("./resources/images/backgrounds/")
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg")
        );
        return fileChooser.showOpenDialog(MainScene.getWindow());
    }
    
    final void setSelectedFile(File file){
        try {
            Files.copy( file.toPath(), new File("./resources/images/backgrounds/"+file.getName()).toPath() );
            if(DisplayConfig.getRunMode().equals(DisplayConfig.RUNMODE_DEFAULT)){
                MainScene.getRootPane().setStyle("-fx-font-size: " + DisplayConfig.getFontDpiScaler() +"px; -fx-background-image:url(\"file:resources/images/backgrounds/"+file.getName()+"\")");
            }
        } catch (IOException ex) {
            LOG.error("Could not copy image: {}", ex.getMessage(),ex);
            SimpleErrorMessage error = new SimpleErrorMessage("background image");
            error.setMessage("Could not copy file");
            WindowManager.openWindow(error);
        }
    }
    
    final void saveButtonHelper(ActionEvent t){
        if(newBgFile!=null){
            AppProperties.setProperty("system", "display.background", newBgFile.getName());
        }
        /*
        try {
            if(AppProperties.getProperty("system", "display.quality").equals("low") && ((String)qualityMode.getSelectedToggle().getUserData()).equals("high")){
                MainScene.getRootPane().getStylesheets().add(AppResources.getCss("high.css"));
            } else {
                MainScene.getRootPane().getStylesheets().remove(AppResources.getCss("high.css"));
            }
        } catch (AppPropertiesException ex) {
            /// Could not change, restart app
        }
        */
        AppProperties.setProperty("system", "display.quality", (String)qualityMode.getSelectedToggle().getUserData());
        try {
            AppProperties.store("system", null);
        } catch (IOException ex) {
            /// could not save settings
        }
    }
    
    @Override
    public final void destroy(){
        clearBackground.removeEventFilter(ActionEvent.ACTION,this::clearBackgroundHelper);
        setBackground.removeEventFilter(ActionEvent.ACTION, this::setBackgroundImageHelper);
        setSettings.removeEventFilter(ActionEvent.ACTION, this::saveButtonHelper);
    }
    
}
