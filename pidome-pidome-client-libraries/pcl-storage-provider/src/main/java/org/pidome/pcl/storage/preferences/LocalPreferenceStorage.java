/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.storage.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.backend.data.interfaces.storage.LocalPreferenceStorageInterface;

/**
 * Main class responsible for the client's locally stored settings.
 * @author John
 */
public final class LocalPreferenceStorage extends Properties implements LocalPreferenceStorageInterface {
    
    static {
        Logger.getLogger(LocalPreferenceStorage.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Path to preferences file.
     */
    private final String settingsPath         = "../settings";
    
    /**
     * Application preferences set by user.
     */
    private final String preferenceFile       = "preferences.properties";
    
    /**
     * Default preferences
     */
    private final String preferenceDefaultFile = "preferences.default.properties";
    
    /**
     * Properties file used.
     */
    File propsFile;
    
    /**
     * Constructor
     * @param appRoot application root path.
     */
    public LocalPreferenceStorage(Path appRoot){
        super();
        File defaultProps = new File(new StringBuilder(appRoot.toString()).append(File.separator).append(settingsPath).append(File.separator).append(preferenceDefaultFile).toString());
        boolean defaultExists = false;
        if(defaultProps.exists()) {
            defaultExists = true;
        } else {
            Logger.getLogger(LocalPreferenceStorage.class.getName()).log(Level.WARNING, "No default preferences file present ($1)", defaultProps);
        }
        this.propsFile = new File(new StringBuilder(appRoot.toString()).append(File.separator).append(settingsPath).append(File.separator).append(preferenceFile).toString());
        if(!propsFile.exists()) {
            try {
                propsFile.getParentFile().mkdirs();
                propsFile.createNewFile();
            } catch (IOException ex) {
                throw new RuntimeException("Can not create a settings file: " + ex.getMessage(), ex);
            }
        } else {
            try (FileInputStream input = new FileInputStream(propsFile)) {
                if(defaultExists){
                    try (FileInputStream defaultInput = new FileInputStream(defaultProps)) {
                        Properties props = new Properties();
                        props.load(defaultInput);
                        this.defaults = props;
                    } catch (IOException ex) {
                        Logger.getLogger(LocalPreferenceStorage.class.getName()).log(Level.WARNING, "Default preferences file should exist but can not be read", ex);
                    }
                }
                load(input);
            } catch (IOException ex) {
                throw new RuntimeException("Problem loading default config file: " + ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * Returns a preference.
     * @param name Name of the preference.
     * @return The preference value
     */
    @Override
    public String getStringPreference(String name, String defaultValue) {
        String value = this.getProperty(name);
        if(value==null){
            return defaultValue;
        }
        return value;
    }
    
    /**
     * Returns a preference.
     * @param name Name of the preference.
     * @return The preference value
     */
    @Override
    public boolean getBoolPreference(String name, boolean defaultValue) {
        String value = this.getProperty(name);
        if(value==null || value.isEmpty()){
            return defaultValue;
        }
        return Boolean.valueOf(value);
    }

    /**
     * Returns a preference.
     * @param name Name of the preference.
     * @return The preference value
     */
    @Override
    public int getIntPreference(String name, int defaultValue) {
        String value = this.getProperty(name);
        if(value==null || value.isEmpty()){
            return defaultValue;
        }
        return Integer.valueOf(value);
    }

    /**
     * Returns a preference.
     * @param name Name of the preference.
     * @return The preference value
     */
    @Override
    public long getLongPreference(String name, long defaultValue) {
        String value = this.getProperty(name);
        if(value==null || value.isEmpty()){
            return defaultValue;
        }
        return Long.valueOf(value);
    }

    /**
     * Returns a preference.
     * @param name Name of the preference.
     * @return The preference value
     */
    @Override
    public float getFloatPreference(String name, float defaultValue) {
        String value = this.getProperty(name);
        if(value==null || value.isEmpty()){
            return defaultValue;
        }
        return Float.valueOf(value);
    }

    /**
     * Sets a preference
     * @param name Name of the preference.
     * @param value Valueof the preference.
     */
    @Override
    public void setStringPreference(String name, String value) {
        this.setProperty(name, value);
    }
    
    /**
     * Sets a setting
     * @param name Setting name.
     * @param value Setting value.
     */
    @Override
    public void setBoolPreference(String name, boolean value) {
        this.setProperty(name, String.valueOf(value));
    }

    /**
     * Sets a setting
     * @param name Setting name.
     * @param value Setting value.
     */
    @Override
    public void setIntPreference(String name, int value) {
        this.setProperty(name, String.valueOf(value));
    }

    /**
     * Sets a setting
     * @param name Setting name.
     * @param value Setting value.
     */
    @Override
    public void setLongPreference(String name, long value) {
        this.setProperty(name, String.valueOf(value));
    }

    /**
     * Sets a setting
     * @param name Setting name.
     * @param value Setting value.
     */
    @Override
    public void setFloatPreference(String name, float value) {
        this.setProperty(name, String.valueOf(value));
    }

    /**
     * Stores the settings with a remark.
     * @param remark Remark for saving.
     * @throws IOException When saving fails.
     */
    @Override
    public void storePreferences(String remark) throws IOException {
        try (FileOutputStream out = new FileOutputStream(propsFile)) {
            store(out, remark);
        }
    }

    /**
     * Stores the settings.
     * @throws IOException When saving fails.
     */
    @Override
    public void storePreferences() throws IOException {
        storePreferences("No comment save.");
    }
    
}
