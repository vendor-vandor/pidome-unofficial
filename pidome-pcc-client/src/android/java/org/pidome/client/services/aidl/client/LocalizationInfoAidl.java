/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services.aidl.client;

import org.pidome.client.services.aidl.service.SystemServiceAidlInterface;
import android.os.RemoteException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.settings.LocalizationInfoInterface;
import org.pidome.pcl.utilities.properties.ObjectPropertyBindingBean;

/**
 *
 * @author John
 */
public class LocalizationInfoAidl implements LocalizationInfoInterface {

    ObjectPropertyBindingBean<Double> currentDistance = new ObjectPropertyBindingBean(0.0);
    
    private SystemServiceAidlInterface proxy;
    
    protected LocalizationInfoAidl(SystemServiceAidlInterface proxy){
        this.proxy = proxy;
    }
    
    protected void updateGPSDistance(double newValue){
        currentDistance.setValue(newValue);
    }
    
    @Override
    public boolean GPSEnabled() {
        try {
            return proxy.GPSEnabled();
        } catch (RemoteException ex) {
            Logger.getLogger(LocalizationInfoAidl.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public long getGPSDelay() {
        try {
            return proxy.getGPSDelay();
        } catch (RemoteException ex) {
            Logger.getLogger(LocalizationInfoAidl.class.getName()).log(Level.SEVERE, null, ex);
            return 0l;
        }
    }

    @Override
    public void setLocalizationPreferences(boolean enabled, long timeToWait, boolean wifiHomeEnabled) {
        try {
            proxy.setLocalizationPreferences(enabled, timeToWait, wifiHomeEnabled);
        } catch (RemoteException ex) {
            Logger.getLogger(LocalizationInfoAidl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean getHomeNetworkHomePresenceEnabled() {
        try {
            return proxy.getHomeNetworkHomePresenceEnabled();
        } catch (RemoteException ex) {
            Logger.getLogger(LocalizationInfoAidl.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public String getHomeNetworkHomePresenceWifiNetworkName() throws IOException {
        try {
            return proxy.getHomeNetworkHomePresenceWifiNetworkName();
        } catch (RemoteException ex) {
            Logger.getLogger(LocalizationInfoAidl.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    @Override
    public ObjectPropertyBindingBean<Double> getCurrentDistanceProperty() {
        return currentDistance;
    }
    
}