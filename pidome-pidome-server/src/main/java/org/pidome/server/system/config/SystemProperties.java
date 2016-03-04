/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * Extended Properties class to load the requested properties file but also 
 * the default properties file for loading the default configuration settings.
 * 
 * By using this class as new Props(String filename, new Props(String default_filename)));
 * it will load the default_filename as the default properties and filename to overwrite the defaults.
 * 
 * When saving is called the default_filename will be left alone and filename will be saved.
 * 
 * @author John
 */
public class SystemProperties extends Properties {

    String propertiesFile;
    
    /**
     * Loads the default properties with the overwriting properties.
     * @param prefixDir The directory prefix.
     * @param fileName The file name of the file to load.
     * @param defaultProps If default SystemProperties are set load them here.
     * @throws IOException 
     */
    public SystemProperties(String prefixDir, String fileName, SystemProperties defaultProps) throws IOException {
        super(defaultProps);
        propertiesFile = prefixDir + fileName + ".properties";
        File propsFile = new File(propertiesFile);
        if(!propsFile.exists()) {
            propsFile.createNewFile();
        }
        load(new FileInputStream(propsFile));
    }

    /**
     * Loads a properties file.
     * @param prefixDir Prefix the directory choice
     * @param fileName the filename to load.
     * @throws IOException 
     */
    public SystemProperties(String prefixDir, String fileName) throws IOException {
        super();
        propertiesFile = prefixDir + fileName + ".properties";
        load(new FileInputStream(propertiesFile));
    }
    
    /**
     * Stores a properties file. comment may be null;
     * @param comment
     * @throws IOException 
     */
    public void store(String comment) throws IOException {
        store(new FileOutputStream(propertiesFile), comment);
    }
    
    /**
     * Overwriting load as it is private.
     * @param inputStream 
     */
    private void load(FileInputStream inputStream) throws IOException {
        super.load(inputStream);
    }
    
}
