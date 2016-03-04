/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.menus;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import org.pidome.client.PiDomeClient;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.users.User;
import org.pidome.client.entities.users.UserService;
import org.pidome.client.entities.users.UserServiceException;
import static org.pidome.client.menus.AppMainMenu.defaultIconHeight;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.scenes.macros.MacrosPopUpLargeScreen;
import org.pidome.client.scenes.panes.popups.PopUp;
import org.pidome.client.scenes.presences.PresencesPopUpLargeScreen;
import org.pidome.client.scenes.scenes.ScenesPopUpLargeScreen;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.services.ServiceConnector.DisplayType;
import org.pidome.client.settings.mobile.UserPresenceSettings;
import org.pidome.client.system.PCCSystem;
import org.pidome.client.tools.DisplayTools;

/**
 *
 * @author John
 */
public class AppMobileMenu extends AppMainMenu {

    private static User me;

    private boolean currentPresent = false;
    private final PropertyChangeListener presenceChanger = this::presenceChanged;
    private final PropertyChangeListener distanceChanger = this::distanceChanged;

    private static String presenceSize = "1em;";
    
    private Text personPresenceIcon = GlyphsDude.createIcon(FontAwesomeIcon.HOME, presenceSize);
    private Label presenceText = new Label("Presence");

    private UserService userService;

    private boolean presenceDone = false;
    
    public AppMobileMenu(PCCSystem system, ServiceConnector serviceConnector) {
        super(system, serviceConnector);
        presenceText.getStyleClass().add("my-presence");
    }

    @Override
    protected void appendBuild() {
        
        addMainBlockItem(new SceneBigMenuItem(GlyphsDude.createIcon(MaterialDesignIcon.REMOTE, String.valueOf(defaultIconHeight)), ScenesHandler.ScenePane.REMOTES, "Remotes"));
        
        BorderPane presenceContainer = new BorderPane();
        
        Text settingsIcon = GlyphsDude.createIcon(MaterialIcon.SETTINGS, String.valueOf(presenceSize));
        BorderPane.setMargin(settingsIcon, new Insets(0,10,0,0));
        
        BorderPane.setAlignment(personPresenceIcon, Pos.CENTER_LEFT);
        BorderPane.setAlignment(presenceText, Pos.CENTER_LEFT);
        
        BorderPane.setAlignment(settingsIcon, Pos.CENTER_RIGHT);
        
        presenceContainer.setLeft(personPresenceIcon);
        BorderPane.setMargin(presenceText, new Insets(0,0,0,7));
        presenceContainer.setCenter(presenceText);
        presenceContainer.setRight(settingsIcon);
        
        this.setPresenceContainer(presenceContainer);

        settingsIcon.setPickOnBounds(true);
        
        settingsIcon.setOnMouseClicked((MouseEvent me) -> {
            
            UserPresenceSettings prefPopUp = new UserPresenceSettings(serviceConnector.getLocalizationService());
            
            PopUp.PopUpButton applySettingsButton = new PopUp.PopUpButton("apply", "Save");
            applySettingsButton.setOnAction((ActionEvent e) -> {
                prefPopUp.close();
                serviceConnector.getLocalizationService().setLocalizationPreferences(prefPopUp.getGPSEnabled(), Math.round(prefPopUp.getGPSTimeOut()*60000), prefPopUp.getWiFiEnabled());
            });
            prefPopUp.setButtons(applySettingsButton, new PopUp.PopUpButton("cancel", "Cancel"));
            prefPopUp.build();
            prefPopUp.show(true);
            
        });

        addBottomContainerItem(GlyphsDude.createIcon(MaterialDesignIcon.PLAY, String.valueOf(uImageSize-10)), () -> {
            if(DisplayTools.getUserDisplayType()==DisplayType.LARGE){
                MacrosPopUpLargeScreen popup = new MacrosPopUpLargeScreen(this.system, this.serviceConnector);
                popup.setButtons();
                popup.build();
                popup.show(true);
            } else {
                PiDomeClient.switchScene(ScenesHandler.ScenePane.MACROS_EVENTS);
            }
        });
        
        addBottomContainerItem(GlyphsDude.createIcon(FontAwesomeIcon.LIGHTBULB_ALT, String.valueOf(uImageSize-10)), () -> {
            if(DisplayTools.getUserDisplayType()==DisplayType.LARGE){
                ScenesPopUpLargeScreen popup = new ScenesPopUpLargeScreen(this.system, this.serviceConnector);
                popup.setButtons();
                popup.build();
                popup.show(true);
            } else {
                PiDomeClient.switchScene(ScenesHandler.ScenePane.SCENES);
            }
        });
        
        addBottomContainerItem(GlyphsDude.createIcon(MaterialIcon.GROUP, String.valueOf(uImageSize-10)), () -> {
            if(DisplayTools.getUserDisplayType()==DisplayType.LARGE){
                PresencesPopUpLargeScreen popup = new PresencesPopUpLargeScreen(this.system, this.serviceConnector);
                popup.setButtons();
                popup.build();
                popup.show(true);
            } else {
                PiDomeClient.switchScene(ScenesHandler.ScenePane.PRESENCES);
            }
        });
        
    }
    
    private void distanceChanged(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            if (!currentPresent) {
                try {
                    presenceText.setText("Away: " + ((double) evt.getNewValue() / 1000) + " Km.");
                } catch (Exception ex) {
                    presenceText.setText("Away");
                }
            } else {
                presenceText.setText("At home");
            }
        });
    }
    
    @Override
    protected void bindPresence() {
        try {
            userService = this.system.getClient().getEntities().getUserService();
            startPersonalPresenceLoader();
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(AppMobileMenu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public final void opened(){
        if(!presenceDone){
            presenceDone = true;
            try {
                this.serviceConnector.getLocalizationService().getCurrentDistanceProperty().addPropertyChangeListener(distanceChanger);
            } catch (UnsupportedOperationException ex) {
                /// Not supported on all platforms.
            }
            bindPresence();
        }
    }
    
    private void startPersonalPresenceLoader() {
        presenceDone = true;
        new Thread() {
            @Override
            public final void run() {
                try {
                    me = userService.getMyData();
                    me.getPresent().addPropertyChangeListener(presenceChanger);
                    Platform.runLater(() -> { 
                        AppMobileMenu.this.setName(me.getFirstName().getValueSafe());
                        if (me.getPresent().getValue()) {
                            currentPresent = true;
                            personPresenceIcon.setText(FontAwesomeIcon.HOME.characterToString());
                            presenceText.setText("At home");
                        } else {
                            currentPresent = false;
                            personPresenceIcon.setText(FontAwesomeIcon.ROAD.characterToString());
                            if(AppMobileMenu.this.serviceConnector.getLocalizationService().GPSEnabled()){
                                presenceText.setText("Away");
                            } else {
                                presenceText.setText("Away/No GPS");
                            }
                        }
                    });
                } catch (UserServiceException ex) {
                    Logger.getLogger(AppMobileMenu.class.getName()).log(Level.SEVERE, "Personal not available", ex);
                }
            }
        }.start();
    }

    private void presenceChanged(PropertyChangeEvent evt) {
        currentPresent = (boolean) evt.getNewValue();
        Platform.runLater(() -> {
            if ((boolean) evt.getNewValue() == true) {
                presenceText.setText("At home");
                personPresenceIcon = GlyphsDude.createIcon(FontAwesomeIcon.HOME, presenceSize);
            } else {
                try {
                    if (this.serviceConnector.getLocalizationService().GPSEnabled()) {
                        presenceText.setText("Away: " + this.serviceConnector.getLocalizationService().getCurrentDistanceProperty().getValue() + " Km.");
                    } else {
                        presenceText.setText("Away/No GPS");
                    }
                } catch (UnsupportedOperationException ex) {
                    presenceText.setText("Away");
                }
                personPresenceIcon = GlyphsDude.createIcon(FontAwesomeIcon.ROAD, presenceSize);
                personPresenceIcon.getStyleClass().remove("home-personal");
                personPresenceIcon.getStyleClass().add("away-personal");
            }
        });
    }

}