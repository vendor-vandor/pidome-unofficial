/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.scenes.menus;

import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import org.pidome.client.phone.dialogs.settings.UserPresenceSettings;
import org.pidome.client.phone.scenes.PositionedPane;
import org.pidome.client.phone.scenes.visuals.DialogBox;
import org.pidome.client.phone.services.ServiceConnector;
import org.pidome.client.phone.visuals.interfaces.Destroyable;

/**
 *
 * @author John
 */
public class MainMenu extends MenuBase implements MenuAble,Destroyable {

    public enum MainMenuItem {
        
        PRESENCE_SETTINGS("Presence settings"),
        GLOBAL_SETTINGS("Settings");
        
        private final String menuText;
        
        private MainMenuItem(String value) {
            this.menuText = value;
        }

        public final String getItemName(){
            return this.menuText;
        }
        
    }
    
    MenuActionReceiver menuActionReceiver;
    PositionedPane baseScene;
    
    DialogBox presenceSettingsDialog;
    ServiceConnector service;
    
    public MainMenu(ServiceConnector service, PositionedPane baseScene, MenuActionReceiver menuActionReceiver){
        super();
        this.service = service;
        this.baseScene = baseScene;
        this.menuActionReceiver = menuActionReceiver;
        getStyleClass().add("main-menu");
    }
    
    @Override
    public void setItems() {
        Label visualMenuHeader = new Label("Menu");
        visualMenuHeader.setPrefWidth(Double.MAX_VALUE);
        visualMenuHeader.getStyleClass().addAll("menu-item", "menu-header");
        this.getChildren().add(visualMenuHeader);
        for (MainMenuItem menuItem : MainMenuItem.values()) {
            Label visualMenuItem = new Label(menuItem.getItemName());
            visualMenuItem.setPrefWidth(Double.MAX_VALUE);
            visualMenuItem.getStyleClass().add("menu-item");
            visualMenuItem.setUserData(menuItem);
            visualMenuItem.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
                this.menuActionReceiver.closeMenu(this);
                try {
                    if(presenceSettingsDialog==null){
                        presenceSettingsDialog = new DialogBox("Presence settings");
                        presenceSettingsDialog.setButtons(new DialogBox.PopUpButton[]{new DialogBox.PopUpButton("CANCEL", "Cancel"), new DialogBox.PopUpButton("OK", "Ok")});
                        final UserPresenceSettings settings = new UserPresenceSettings(service.getLocalizationService());
                        presenceSettingsDialog.setContent(settings);
                        presenceSettingsDialog.addListener((String buttonId) -> {
                            baseScene.closePopup(presenceSettingsDialog);
                            switch(buttonId){
                                case "CANCEL":
                                    /// Do nothing.
                                break;
                                case "OK":
                                    /// save stuff, optional show warning etc..
                                    service.getLocalizationService().setLocalizationPreferences(settings.getGPSEnabled(), Math.round(settings.getGPSTimeOut()*60000), settings.getWiFiEnabled());
                                break;
                            }
                            settings.unset();
                            presenceSettingsDialog = null;
                        });
                        presenceSettingsDialog.build();
                    }
                    if(!baseScene.hasPopup(presenceSettingsDialog)){
                        baseScene.showPopup(presenceSettingsDialog);
                    }
                } catch (UnsupportedOperationException ex){
                    /// not available on desktop.
                }
            });
            this.getChildren().add(visualMenuItem);
        }
    }

    @Override
    public void destroy() {
        /// not used yet.
    }
    
}