/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.backend.data.interfaces.storage;

import java.io.IOException;

/**
 * Interface to be used for local settings storage.
 * @author John
 */
public interface LocalSettingsStorageInterface {
    
    /**
     * Returns a setting.
     * @param name The name of the preference
     * @param defaultValue The default value if the preference is not known
     * @return The preference value.
     */
    public String getStringSetting(String name, String defaultValue);
    
    /**
     * Returns a setting.
     * @param name The name of the preference
     * @param defaultValue The default value if the preference is not known
     * @return The preference value.
     */
    public boolean getBoolSetting(String name, boolean defaultValue);
    
    /**
     * Returns a setting.
     * @param name The name of the preference
     * @param defaultValue The default value if the preference is not known
     * @return The preference value.
     */
    public int getIntSetting(String name, int defaultValue);
    
    /**
     * Returns a setting.
     * @param name The name of the preference
     * @param defaultValue The default value if the preference is not known
     * @return The preference value.
     */
    public long getLongSetting(String name, long defaultValue);
    
    /**
     * Returns a setting.
     * @param name The name of the preference
     * @param defaultValue The default value if the preference is not known
     * @return The preference value.
     */
    public float getFloatSetting(String name, float defaultValue);
    
    /**
     * Sets a setting.
     * @param name The preference name.
     * @param value The preference string value.
     */
    public void setStringSetting(String name, String value);
    
    /**
     * Sets a setting.
     * @param name The preference name.
     * @param value The preference boolean value.
     */
    public void setBoolSetting(String name, boolean value);
    
    /**
     * Sets a setting.
     * @param name The preference name.
     * @param value The preference int value.
     */
    public void setIntSetting(String name, int value);
    
    /**
     * Sets a setting.
     * @param name The preference name.
     * @param value The preference long value.
     */
    public void setLongSetting(String name, long value);
    
    /**
     * Sets a setting.
     * @param name The preference name.
     * @param value The preference float value.
     */
    public void setFloatSetting(String name, float value);
    
    /**
     * Store settings with a remark.
     * @param remark The remark to be written down with the settings.
     * @throws IOException Thrown when the settings can not be saved.
     */
    public void storeSettings(String remark) throws IOException;
    
    /**
     * Stores the settings.
     * @throws IOException Thrown when the settings can not be saved.
     */
    public void storeSettings() throws IOException;
    
}
