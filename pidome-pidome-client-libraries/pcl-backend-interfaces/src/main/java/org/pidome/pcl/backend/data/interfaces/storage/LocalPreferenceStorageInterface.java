/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.backend.data.interfaces.storage;

import java.io.IOException;

/**
 * Interface for storing local preferences.
 * @author John
 */
public interface LocalPreferenceStorageInterface {
    
    /**
     * Returns a preference setting.
     * @param name The name of the preference
     * @param defaultValue The default value if the preference is not known
     * @return The preference value.
     */
    public String getStringPreference(String name, String defaultValue);
    
    /**
     * Returns a preference setting.
     * @param name The name of the preference
     * @param defaultValue The default value if the preference is not known
     * @return The preference value.
     */
    public boolean getBoolPreference(String name, boolean defaultValue);
    
    /**
     * Returns a preference setting.
     * @param name The name of the preference
     * @param defaultValue The default value if the preference is not known
     * @return The preference value.
     */
    public int getIntPreference(String name, int defaultValue);
    
    /**
     * Returns a preference setting.
     * @param name The name of the preference
     * @param defaultValue The default value if the preference is not known
     * @return The preference value.
     */
    public long getLongPreference(String name, long defaultValue);
    
    /**
     * Returns a preference setting.
     * @param name The name of the preference
     * @param defaultValue The default value if the preference is not known
     * @return The preference value.
     */
    public float getFloatPreference(String name, float defaultValue);
    
    /**
     * Sets a preference.
     * @param name The preference name.
     * @param value The preference string value.
     */
    public void setStringPreference(String name, String value);
    
    /**
     * Sets a preference.
     * @param name The preference name.
     * @param value The preference boolean value.
     */
    public void setBoolPreference(String name, boolean value);
    
    /**
     * Sets a preference.
     * @param name The preference name.
     * @param value The preference int value.
     */
    public void setIntPreference(String name, int value);
    
    /**
     * Sets a preference.
     * @param name The preference name.
     * @param value The preference long value.
     */
    public void setLongPreference(String name, long value);
    
    /**
     * Sets a preference.
     * @param name The preference name.
     * @param value The preference float value.
     */
    public void setFloatPreference(String name, float value);
    
    /**
     * Stores the current preferences with a remark.
     * @param remark The remark to be stored with the settings.
     * @throws java.io.IOException Thrown when the preferences can not be stored.
     */
    public void storePreferences(String remark) throws IOException;
    
    /**
     * Stores the current preferences.
     * @throws java.io.IOException Throw when the preferences can not be stored.
     */
    public void storePreferences() throws IOException;
}
