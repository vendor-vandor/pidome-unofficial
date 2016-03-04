/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.settings.mobile;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import java.io.IOException;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.pidome.client.scenes.panes.popups.PopUp;
import org.pidome.client.settings.LocalizationInfoInterface;

/**
 *
 * @author John
 */
public class UserPresenceSettings extends PopUp {
    
    private final VBox popContent = new VBox(5);
    
    private LocalizationInfoInterface presenceSettings;
    
    private final CheckBox enableGPS  = new CheckBox();
    private final TextField timedelay = new TextField();
    
    private final CheckBox enableWifi = new CheckBox();
    private final TextField wifiName  = new TextField();
    
    public UserPresenceSettings(){
        super(MaterialIcon.GPS_OFF, "Presence");
        presenceSettings = null;
        popContent.getChildren().addAll(new Label("Not supported on this platform"));
        this.setContent(popContent);
    }
    
    public UserPresenceSettings(LocalizationInfoInterface presenceSettings){
        super(MaterialIcon.GPS_FIXED, "Presence");
        this.presenceSettings = presenceSettings;
        
        enableGPS.setSelected(this.presenceSettings.GPSEnabled());
        enableGPS.setText("Enable localization");
        
        timedelay.setText(String.valueOf(((this.presenceSettings.getGPSDelay()/1000)/60)));
        
        enableWifi.setText("Enable WiFi home");
        enableWifi.setSelected(this.presenceSettings.getHomeNetworkHomePresenceEnabled());
        
        popContent.getChildren().addAll(enableGPS,
                                     new Label("Update delay (minutes)"),timedelay,
                                     enableWifi,
                                     wifiName);
        try {
            wifiName.setText(this.presenceSettings.getHomeNetworkHomePresenceWifiNetworkName());
        } catch (IOException ex) {
            wifiName.setText("Not connected to wifi?");
        }
        wifiName.setEditable(false);
        popContent.setMinWidth(300);
        popContent.setPadding(new Insets(5));
        this.setContent(popContent);
    }
    
    public boolean getGPSEnabled(){
        return enableGPS.isSelected();
    }
    
    public float getGPSTimeOut(){
        try {
            return Float.parseFloat(timedelay.getText().replace(",", ""));
        } catch (Exception ex){
            return 5f;
        }
    }
    
    public boolean getWiFiEnabled(){
        return enableWifi.isSelected();
    }
    
    public final void unset(){
        presenceSettings = null;
    }

    @Override
    public void unload() {
        unset();
    }
    
}