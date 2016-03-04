/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.system.hardware.devices;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author John
 */
public final class RetentionHandler extends Properties {
    
    File retentionFile;    
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(RetentionHandler.class);
    
    /**
     * Creates the retention file and loads the last known data.
     * @param retentionLocation 
     * @param retentionId 
     */
    public RetentionHandler(String retentionLocation, int retentionId){
        new File(retentionLocation).mkdirs();
        retentionFile = new File(new StringBuilder(retentionLocation).append(retentionId).append(".retention").toString());
        try {   
            if(!retentionFile.exists()) {
                retentionFile.createNewFile();
            }
            try (FileInputStream stream = new FileInputStream(retentionFile)){
                load(stream);
            }
        } catch (FileNotFoundException ex) {
            LOG.error("Could not open retention file {}: {}", retentionFile.getAbsoluteFile(), ex.getMessage());
        } catch (IOException ex) {
            LOG.error("Could not open read retention file {}: {}", retentionFile.getAbsoluteFile(), ex.getMessage());
        }
    }
    
    /**
     * Returns the last known data.
     * @param group
     * @param control
     * @return 
     */
    public final String getRetentionData(String group, String control){
        return this.getProperty(new StringBuilder(group).append("_").append(control).toString(), null);
    }
    
    /**
     * Sets the last known data.
     * @param group
     * @param control
     * @param value 
     */
    public final void setRetentionData(String group, String control, String value){
        this.setProperty(new StringBuilder(group).append("_").append(control).toString(), value);
    }
    
    /**
     * Stores data by an external process.
     */
    public final void storeData(){
        storeData("Auto save by device update/shutdown");
    }
    
    /**
     * Stores the data with custom store message.
     * @param comment 
     */
    public final void storeData(String comment){
        try (FileOutputStream stream = new FileOutputStream(retentionFile)){
            store(stream, comment);
        } catch (FileNotFoundException ex) {
            LOG.error("Retention file {} does not exist: {}", retentionFile.getAbsoluteFile(), ex.getMessage());
        } catch (IOException ex) {
            LOG.error("Could not write to retention file {}: {}",retentionFile.getAbsoluteFile(), ex.getMessage());
        }
    }
    
}
