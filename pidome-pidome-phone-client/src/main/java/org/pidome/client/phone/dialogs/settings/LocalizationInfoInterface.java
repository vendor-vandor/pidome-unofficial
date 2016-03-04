/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.dialogs.settings;

import java.io.IOException;
import org.pidome.pcl.utilities.properties.ObjectPropertyBindingBean;

/**
 *
 * @author John
 */
public interface LocalizationInfoInterface {
    
    public boolean GPSEnabled();
    public long getGPSDelay();
    public void setLocalizationPreferences(boolean enabled, long timeToWait, boolean wifiHomeEnabled);
    
    public boolean getHomeNetworkHomePresenceEnabled();
    public String getHomeNetworkHomePresenceWifiNetworkName() throws IOException;
    
    public ObjectPropertyBindingBean<Double> getCurrentDistanceProperty();
    
}