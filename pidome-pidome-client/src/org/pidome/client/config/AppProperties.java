/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author John Sirach
 */
public class AppProperties {

    static Map<String,Props> propList = new HashMap<>();
    
    public static void initialize() throws IOException {
        System.setProperty("prism.dirtyopts", "false");
        propList.put("system", new Props("system", new Props("system.default")));
        propList.put("desktop", new Props("desktop", new Props("desktop.default")));
        propList.put("applicationswidgetbar", new Props("applicationswidgetbar", new Props("applicationswidgetbar.default")));
    }
    
    public static String getProperty(String type, String propName) throws AppPropertiesException {
        if(propList.containsKey(type)){
            String propValue = propList.get(type).getProperty(propName);
            if(propValue==null){
                throw new AppPropertiesException("Property '"+propName+"' in '"+type+"' does not exist");
            } else {
                return propValue;
            }
        } else {
            throw new AppPropertiesException("Property type '"+type+"' does not exist");
        }
    }
    
    public static Set<Map.Entry<Object,Object>>getPropertiesNVP(String type) throws AppPropertiesException {
        if(propList.containsKey(type)){
            ConcurrentHashMap propertiesSet = new ConcurrentHashMap<>();
            for (Enumeration<?> e = propList.get(type).propertyNames(); e.hasMoreElements();) {
                String key = (String)e.nextElement();
                propertiesSet.put(key, propList.get(type).getProperty(key));
            }
            return propertiesSet.entrySet();
        } else {
            throw new AppPropertiesException("Property type '"+type+"' does not exist");
        }
    }

    public static void deleteProperty(String type, String propName){
        if(propList.containsKey(type)) propList.get(type).remove(propName);
    }
    
    public static void setProperty(String type, String propName, String propValue){
        if(propList.containsKey(type)) propList.get(type).setProperty(propName, propValue);
    }
    
    public static void store(String type, String comment) throws IOException {
        if(propList.containsKey(type)) propList.get(type).store(comment);
    }
    
}
class Props extends Properties {

    String propertiesFile;
    
    static Logger LOG = LogManager.getLogger(Props.class);
    
    public Props(String fileName, Props defaultProps) throws IOException {
        super(defaultProps);
        propertiesFile = "resources/properties/" + fileName + ".properties";
        File propsFile = new File(propertiesFile);
        if(!propsFile.exists()) {
            propsFile.createNewFile();
        }
        load(new FileInputStream(propsFile));
    }

    public Props(String fileName) throws IOException {
        super();
        propertiesFile = "resources/properties/" + fileName + ".properties";
        load(new FileInputStream(propertiesFile));
    }
    
    public void store(String comment) throws IOException {
        store(new FileOutputStream(propertiesFile), comment);
    }
    
}