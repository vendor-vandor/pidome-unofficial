/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.storage.preferences;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.backend.data.interfaces.storage.AbstractRemotePreferences;

/**
 * Not used yet.
 * @author John
 */
public class RemotePreferencesStorage extends AbstractRemotePreferences {

    static {
        Logger.getLogger(RemotePreferencesStorage.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Returns a preference.
     * @param name Name of the preference.
     * @return The preference value
     */
    @Override
    public String getStringPreference(String name, String defaultValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Returns a preference.
     * @param name Name of the preference.
     * @return The preference value
     */
    @Override
    public boolean getBoolPreference(String name, boolean defaultValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns a preference.
     * @param name Name of the preference.
     * @return The preference value
     */
    @Override
    public int getIntPreference(String name, int defaultValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns a preference.
     * @param name Name of the preference.
     * @return The preference value
     */
    @Override
    public long getLongPreference(String name, long defaultValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns a preference.
     * @param name Name of the preference.
     * @return The preference value
     */
    @Override
    public float getFloatPreference(String name, float defaultValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Sets a preference
     * @param name Name of the preference.
     * @param value Valueof the preference.
     */
    @Override
    public void setStringPreference(String name, String value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Sets a setting
     * @param name Setting name.
     * @param value Setting value.
     */
    @Override
    public void setBoolPreference(String name, boolean value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Sets a setting
     * @param name Setting name.
     * @param value Setting value.
     */
    @Override
    public void setIntPreference(String name, int value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Sets a setting
     * @param name Setting name.
     * @param value Setting value.
     */
    @Override
    public void setLongPreference(String name, long value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Sets a setting
     * @param name Setting name.
     * @param value Setting value.
     */
    @Override
    public void setFloatPreference(String name, float value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Stores the preferences on a remote resource.
     * @throws IOException When saving fails.
     */
    @Override
    public void storePreferences() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Stores the preferences on a remote resource.
     * @param remark Remark for saving.
     * @throws IOException When saving fails.
     */
    @Override
    public void storePreferences(String remark) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
