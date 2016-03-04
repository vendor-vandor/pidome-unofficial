/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.login;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.pidome.client.PiDomeClient;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.scenes.panes.popups.PopUp;
import org.pidome.client.services.PlatformOrientation;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class OrientationChoice extends PopUp {

    PopUpButton setButton = new PopUp.PopUpButton("set", "Set");
    
    final ToggleGroup group = new ToggleGroup();

    RadioButton portrait = new RadioButton("Phone mode");

    RadioButton landscape = new RadioButton("Tablet mode");
    
    private String selectedOption = "portrait";
    
    private ServiceConnector connector;
    
    public OrientationChoice() {
        super(MaterialDesignIcon.CELLPHONE_LINK, "App mode");
        this.setMaxSize(250, 200);
        this.setMinSize(250, 200);
        
        portrait.setToggleGroup(group);
        portrait.setUserData("portrait");
        portrait.setSelected(true);
        
        landscape.setToggleGroup(group);
        landscape.setUserData("landscape");
        
        
        setButton.setOnAction((ActionEvent e) -> {
            if (group.getSelectedToggle() != null) {
                switch((String)group.getSelectedToggle().getUserData()){
                    case "portrait":
                        connector.storeUserDisplayType(ServiceConnector.DisplayType.SMALL);
                    break;
                    case "landscape":
                        connector.storeUserDisplayType(ServiceConnector.DisplayType.LARGE);
                    break;
                }
                PiDomeClient.switchScene(ScenesHandler.ScenePane.DASHBOARD);
            }
        });
        
        group.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) -> {
            if (group.getSelectedToggle() != null) {
                switch((String)group.getSelectedToggle().getUserData()){
                    case "portrait":
                        connector.forceOrientation(PlatformOrientation.Orientation.PORTRAIT);
                    break;
                    case "landscape":
                        connector.forceOrientation(PlatformOrientation.Orientation.LANDSCAPE);
                    break;
                }
            }
        });
        
        setContent(getSelectionContent());
        
    }

    protected final void setSystem(PCCSystem system, ServiceConnector connector) {
        this.connector = connector;
    }
    
    private Pane getSelectionContent(){
        VBox content = new VBox(5);
        HBox bottom = new HBox(5);
        content.getChildren().addAll(portrait,landscape,bottom);
        VBox.setMargin(portrait, new Insets(5));
        VBox.setMargin(landscape, new Insets(5));
        VBox.setMargin(bottom, new Insets(5));
        this.setButton.setPadding(new Insets(0,20,0,20));
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.getChildren().add(this.setButton);
        
        return content;
    }
    
    @Override
    public void unload() {
        this.connector = null;
    }
    
    
    
}