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

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.pidome.client.config.AppProperties;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.scenes.components.controls.DefaultButton;
import org.pidome.client.system.scenes.windows.TitledWindow;

/**
 *
 * @author John
 */
public class ClientSettingsAppSettings extends VBox implements ClientSettingsItemInterface {
    
    final ToggleGroup runMode = new ToggleGroup();
    
    Button setSettings = new DefaultButton("Save application settings");
    
    public ClientSettingsAppSettings(){
        super(10*DisplayConfig.getHeightRatio());
    }
    
    final void build(){
        TitledWindow.setMargin(this, new Insets(10*DisplayConfig.getHeightRatio(),10*DisplayConfig.getWidthRatio(),10*DisplayConfig.getHeightRatio(),10*DisplayConfig.getWidthRatio()));
        getStyleClass().add("appsettings");
        
        Text sectionTitle = new Text("Application settings");
        sectionTitle.getStyleClass().add("sectiontitle");
        
        Text sectionDescription = new Text("Here you can change the application settings");
        sectionDescription.getStyleClass().add("optiondescription");
        
        setSettings.addEventFilter(ActionEvent.ACTION, this::saveButtonHelper);
        
        getChildren().addAll(sectionTitle, sectionDescription, appType(), setSettings);
        
    }

    final Node appType(){
        
        VBox appSection = new VBox(5*DisplayConfig.getHeightRatio());
        
        appSection.getStyleClass().add("section");
        
        Text sectionTitle = new Text("Application run mode");
        sectionTitle.getStyleClass().add("sectionheader");
        
        Text sectionDescription = new Text("You can set the run mode here. Changing the run mode requires you to restart the application.");
        sectionDescription.getStyleClass().add("optiondescription");
        
        RadioButton full = new RadioButton("Full mode, full blown fullscreen original mode");
        full.setStyle("-fx-text-fill: lightgrey;");
        full.setToggleGroup(runMode);
        if(DisplayConfig.getRunMode().equals(DisplayConfig.RUNMODE_DEFAULT)) full.setSelected(true);
        full.setUserData(DisplayConfig.RUNMODE_DEFAULT);
        VBox.setMargin(full, new Insets(5 * DisplayConfig.getHeightRatio(), 5 * DisplayConfig.getWidthRatio(), 0, 5 * DisplayConfig.getWidthRatio()));
        
        RadioButton widget = new RadioButton("Widget mode, small application, all functionalities");
        widget.setStyle("-fx-text-fill: lightgrey;");
        widget.setToggleGroup(runMode);
        widget.setUserData(DisplayConfig.RUNMODE_WIDGET);
        if(DisplayConfig.getRunMode().equals(DisplayConfig.RUNMODE_WIDGET)) widget.setSelected(true);
        VBox.setMargin(widget, new Insets(5 * DisplayConfig.getHeightRatio(), 5 * DisplayConfig.getWidthRatio(), 0, 5 * DisplayConfig.getWidthRatio()));
        widget.setToggleGroup(runMode);
        
        appSection.getChildren().addAll(sectionTitle,sectionDescription,full,widget);
        
        return appSection;
    }
    
    final void saveButtonHelper(ActionEvent t){
        AppProperties.setProperty("system", "client.mode", (String)runMode.getSelectedToggle().getUserData());
        try {
            AppProperties.store("system", null);
        } catch (IOException ex) {
            /// could not save settings
        }
    }
    
    @Override
    public void destroy() {
        setSettings.removeEventFilter(ActionEvent.ACTION, this::saveButtonHelper);
    }
    
}
