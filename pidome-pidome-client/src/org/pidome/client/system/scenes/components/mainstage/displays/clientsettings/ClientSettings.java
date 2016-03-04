/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays.clientsettings;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.AppProperties;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.scenes.components.controls.DefaultButton;
import org.pidome.client.system.scenes.components.mainstage.displays.components.TabbedContent;
import org.pidome.client.system.scenes.components.mainstage.displays.components.TabbedContentTabChangedListener;
import org.pidome.client.system.scenes.windows.TitledWindow;

/**
 *
 * @author John Sirach
 */
public class ClientSettings extends TitledWindow implements TabbedContentTabChangedListener {

    static Logger LOG = LogManager.getLogger(ClientSettings.class);
    
    TabbedContent tabs = new TabbedContent();
    
    ClientSettingsAppSettings appSettings = new ClientSettingsAppSettings();
    ClientSettingsDisplaySettings dispSettings = new ClientSettingsDisplaySettings();
    
    public ClientSettings() {
        super("Settings","System settings");
    }
    
    @Override
    protected void setupContent() {
        appSettings.build();
        dispSettings.build();
        tabs.addTabChangedListener(this);
        tabs.addTab("app","Application");
        tabs.addTab("disp","Display");
        tabs.addTab("bug","Bug report");
        tabs.setPrefWidth(631 * DisplayConfig.getWidthRatio());
        setContent(tabs);
    }

    @Override
    protected void removeContent() {
        appSettings.destroy();
        dispSettings.destroy();
        tabs.removeTabChangedListener(this);
        tabs.destroy();
    }
    
    final Node createIssueReporting(){
        HBox skeleton = new HBox(5*DisplayConfig.getHeightRatio());
        skeleton.getStyleClass().add("appsettings");
        skeleton.setTranslateY(10*DisplayConfig.getHeightRatio());
        
        VBox bugSection = new VBox(5*DisplayConfig.getHeightRatio());
        
        Text sectionBugTitle = new Text("Bug report");
        sectionBugTitle.getStyleClass().add("optiontitle");
        
        Text sectionBugDescription = new Text("Bug reports can be done online via our issue tracker via http://pidome.wordpress.com Go to the about menu and click the appropiate bug report link.");
        sectionBugDescription.getStyleClass().add("optiondescription");
        sectionBugDescription.setWrappingWidth(300 *DisplayConfig.getWidthRatio());
        
        bugSection.getChildren().addAll(sectionBugTitle, sectionBugDescription);
        
        Text sectionRequestTitle = new Text("Feature request");
        sectionRequestTitle.getStyleClass().add("optiontitle");
        
        Text sectionRequestDescription = new Text("Feature requests can be done online via our issue tracker via http://pidome.wordpress.com Go to the about menu and click the appropiate request link.");
        sectionRequestDescription.getStyleClass().add("optiondescription");
        sectionRequestDescription.setWrappingWidth(300 *DisplayConfig.getWidthRatio());
        
        VBox featureSection = new VBox(5*DisplayConfig.getHeightRatio());
        featureSection.getChildren().addAll(sectionRequestTitle,sectionRequestDescription);
        
        skeleton.getChildren().addAll(bugSection,featureSection);
        
        return skeleton;
    }
    
    @Override
    public void tabSwitched(String oldTab, String newTab) {
        switch(newTab){
            case "disp":
                tabs.setTabContent(newTab, new ClientSettingsDisplaySettings().build(), "Set display settings, this is for performance");
            break;
            case "bug":
                tabs.setTabContent(newTab, createIssueReporting(), "Bug report or view known issues");
            break;
            default:
                tabs.setTabContent(newTab, appSettings, "Set application settings");
            break;
        }
    }
    
}
