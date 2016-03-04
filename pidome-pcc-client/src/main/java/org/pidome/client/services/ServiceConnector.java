/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services;

import org.pidome.client.settings.LocalizationInfoInterface;
import org.pidome.pcl.storage.preferences.LocalPreferenceStorage;
import org.pidome.pcl.storage.settings.LocalSettingsStorage;

/**
 *
 * @author John
 */
public interface ServiceConnector {
    
    public enum DisplayType {
        LARGE,SMALL,TINY,DUNNO
    }
    
    public enum PlatformBase {
        FIXED,MOBILE;
    }
    
    public PlatformBase getPlatformBase();
    
    public void startService();
    
    public void stopService();
    
    public void setServiceConnectionListener(ServiceConnectorListener listener);
    
    public LifeCycleHandlerInterface getLifeCycleHandler();
    
    public LocalizationInfoInterface getLocalizationService() throws UnsupportedOperationException;
    
    public void serviceLogin();
    
    public DisplayType userDisplayType();
    
    public void storeUserDisplayType(DisplayType type);
    
    public void addOrientationListener(PlatformOrientation listener);
    
    public void forceOrientation(PlatformOrientation.Orientation orientation);
    
    public LocalPreferenceStorage getPreferences();
    
    public LocalSettingsStorage getSettings();
    
    /**
     * Only available for tiny displays.
     * return Double.MAX_VALUE when large.
     */
    public double getMaxWorkWidth();
    
    /**
     * Used for pgysical display interfactions.
     * For example: brightness, on/off etc..
     * @return 
     */
    public PhysicalDisplayInterface getPhysicalDisplayInterface();
    
}
