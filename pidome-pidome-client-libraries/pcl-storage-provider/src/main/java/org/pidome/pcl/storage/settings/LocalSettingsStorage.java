/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.storage.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.backend.data.interfaces.storage.LocalSettingsStorageInterface;

/**
 * Main class responsible for the client's locally stored settings.
 * @author John
 */
public final class LocalSettingsStorage extends Properties implements LocalSettingsStorageInterface {
    
    static {
        Logger.getLogger(LocalSettingsStorage.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Path to settings file.
     */
    private final String settingsPath        = "../settings";
    
    /**
     * Application properties set by system.
     */
    private final String settingsFile        = "application.properties";
    
    /**
     * Default preferences
     */
    private final String settingsDefaultFile = "application.default.properties";
    
    /**
     * Properties file used.
     */
    File propsFile;
    
    /**
     * Constructor
     * @param appRoot application root path.
     */
    public LocalSettingsStorage(Path appRoot){
        super();
        File defaultProps = new File(new StringBuilder(appRoot.toString()).append(File.separator).append(settingsPath).append(File.separator).append(settingsDefaultFile).toString());
        boolean defaultExists = false;
        if(defaultProps.exists()) {
            defaultExists = true;
        } else {
            Logger.getLogger(LocalSettingsStorage.class.getName()).log(Level.WARNING, "No default settings file present ($1)", defaultProps);
        }
        this.propsFile = new File(new StringBuilder(appRoot.toString()).append(File.separator).append(settingsPath).append(File.separator).append(settingsFile).toString());
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
                        Logger.getLogger(LocalSettingsStorage.class.getName()).log(Level.WARNING, "Default settings file should exist but can not be read", ex);
                    }
                }
                load(input);
            } catch (IOException ex) {
                throw new RuntimeException("Problem loading default config file: " + ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * Stores the settings with a remark.
     * @param remark The save remark.
     * @throws IOException When saving fails.
     */
    @Override
    public void storeSettings(String remark) throws IOException {
        try (FileOutputStream out = new FileOutputStream(propsFile)) {
            store(out, remark);
        }
    }

    /**
     * Stores the settings.
     * @throws IOException When saving fails.
     */
    @Override
    public void storeSettings() throws IOException {
        storeSettings("No comment save.");
    }

        /**
     * Returns a settings.
     * @param name Settings name.
     * @return The settings value.
     */
    @Override
    public String getStringSetting(String name, String defaultValue) {
        String value = this.getProperty(name);
        if(value==null){
            return defaultValue;
        }
        return value;
    }
    
    /**
     * Returns a settings.
     * @param name Settings name.
     * @return The settings value.
     */
    @Override
    public boolean getBoolSetting(String name, boolean defaultValue) {
        String value = this.getProperty(name);
        if(value==null || value.isEmpty()){
            return defaultValue;
        }
        return Boolean.valueOf(value);
    }

    /**
     * Returns a settings.
     * @param name Settings name.
     * @return The settings value.
     */
    @Override
    public int getIntSetting(String name, int defaultValue) {
        String value = this.getProperty(name);
        if(value==null || value.isEmpty()){
            return defaultValue;
        }
        return Integer.valueOf(value);
    }

    /**
     * Returns a settings.
     * @param name Settings name.
     * @return The settings value.
     */
    @Override
    public long getLongSetting(String name, long defaultValue) {
        String value = this.getProperty(name);
        if(value==null || value.isEmpty()){
            return defaultValue;
        }
        return Long.valueOf(value);
    }

    /**
     * Returns a settings.
     * @param name Settings name.
     * @return The settings value.
     */
    @Override
    public float getFloatSetting(String name, float defaultValue) {
        String value = this.getProperty(name);
        if(value==null || value.isEmpty()){
            return defaultValue;
        }
        return Float.valueOf(value);
    }

    /**
     * Sets a setting
     * @param name Setting name.
     * @param value Setting value.
     */
    @Override
    public void setStringSetting(String name, String value) {
        this.setProperty(name, value);
    }
    
    /**
     * Sets a setting
     * @param name Setting name.
     * @param value Setting value.
     */
    @Override
    public void setBoolSetting(String name, boolean value) {
        this.setProperty(name, String.valueOf(value));
    }

    /**
     * Sets a setting
     * @param name Setting name.
     * @param value Setting value.
     */
    @Override
    public void setIntSetting(String name, int value) {
        this.setProperty(name, String.valueOf(value));
    }

    /**
     * Sets a setting
     * @param name Setting name.
     * @param value Setting value.
     */
    @Override
    public void setLongSetting(String name, long value) {
        this.setProperty(name, String.valueOf(value));
    }

    /**
     * Sets a setting
     * @param name Setting name.
     * @param value Setting value.
     */
    @Override
    public void setFloatSetting(String name, float value) {
        this.setProperty(name, String.valueOf(value));
    }
    
}