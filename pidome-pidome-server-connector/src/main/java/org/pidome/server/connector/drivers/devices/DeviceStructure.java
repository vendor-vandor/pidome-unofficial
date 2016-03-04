/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.drivers.devices;

import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceAddressException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceAddressing;

/**
 *
 * @author John
 */
public final class DeviceStructure {
    
    private DeviceControlsSet deviceControlsSet;
    private DeviceOptions     deviceOptions;
    private DeviceAddressing  deviceAddress;
    
    String canonicalBaseName;
    String canonical;
    String collectionName;
    
    String addressDescription = "";
    
    static Logger LOG = LogManager.getLogger(DeviceStructure.class);
    
    boolean created = false;
    
    /**
     * Constructor.
     * @param canonicalName
     */
    public DeviceStructure(String canonicalName) {
        canonical = canonicalName;
    }
    
    /**
     * Creates the device structure.
     * @param structSet The JSON object struct.
     * @throws IllegalDeviceActionException
     * @throws UnsupportedDeviceException 
     */
    public final void createStructure(Map<String,Object> structSet) throws IllegalDeviceActionException, UnsupportedDeviceException {
        LOG.trace("Struct set: {}", structSet);
        Map<String,Object> struct = (Map<String,Object>)structSet.get("device");
        if(created==false){
            
            canonicalBaseName = canonical +":"+ struct.get("name");
            LOG.debug("Set canonical device base name: {}", canonicalBaseName);
            
            if(!struct.containsKey("controlset") || !((Map<String, Object>)struct.get("controlset")).containsKey("groups")){
                LOG.debug("No control group sets available for : {}", canonicalBaseName);
                throw new IllegalDeviceActionException("No control group sets available for:" + canonicalBaseName);
            }
            deviceControlsSet = new DeviceControlsSet(canonicalBaseName, (List<Map<String,Object>>)((Map<String, Object>)struct.get("controlset")).get("groups"));
            try {
                if(struct.containsKey("address")){
                    deviceAddress = new DeviceAddressing((Map<String,Object>)struct.get("address"));
                } else {
                    deviceAddress = new DeviceAddressing(new HashMap<>());
                }
            } catch (DeviceAddressException ex) {
                throw new IllegalDeviceActionException(ex);
            }
            if(struct.containsKey("options")){
                deviceOptions = new DeviceOptions((List<Map<String,Object>>)struct.get("options"));
            } else {
                deviceOptions = new DeviceOptions(new ArrayList<>());
            }
        }
    }
    
    /**
     * Returns the device's control set.
     * @return 
     */
    public final DeviceControlsSet getControlsSet(){
        return deviceControlsSet;
    }
 
    /**
     * Returns the device options.
     * @return 
     */
    public final DeviceOptions getOptions(){
        return deviceOptions;
    }
    
    /**
     * Returns the device address.
     * @return 
     */
    public final DeviceAddressing getAddress(){
        return deviceAddress;
    }
    
    /**
     * Returns the barebone xml defined name.
     * @return 
     */
    public final String getCanonicalBaseName(){
        return canonicalBaseName;
    }
    
    /**
     * Convenience method to create device options.
     * @return 
     */
    public final OptionSettings createOptions(){
        return new OptionSettings();
    }
    
    public class OptionSettings {

        Map<String,Object> options = new HashMap<>();

        public OptionSettings(){}

        /**
         * Add an parameter with a vaalue.
         * This value can be any primitive type (Sring,float,int,boolean,long)
         * @param identification
         * @param value 
         */
        public final void addOptionValues(String identification, Object value){
            options.put(identification, value);
        }

        /**
         * Use when saving a device's settings.
         * This function creates the correct xml for the device settings.
         * @return
         * @throws Exception 
         */
        public Map<String,Object> createSaveSettings() throws Exception {
            return options;
        }
        
    }
    
    /**
     * The device options class.
     * @author John
     */
    public final static class DeviceOptions {

        Map<String, Map<String, Object>> optionSet = new HashMap<>();

        List<Map<String,Object>> originalSet;

        /**
         * Create a device options set.
         * @param options List of possible options.
         */
        public DeviceOptions(List<Map<String,Object>> options){
            this.originalSet = options;
        }
        
        /**
         * Sets the options.
         * This version will replace the obsolete set from string.
         * @param optionsArraySet an array list with options from the database
         */
        public final void set(Map<String,Object> optionsArraySet) {
            if(optionsArraySet!=null){
                for(Map.Entry<String,Object> setting:optionsArraySet.entrySet()){
                    setConfigItemDetails(setting.getKey(), setting.getValue());
                }
            }
            LOG.debug("Created device options: {}", optionSet);
        }
        
        /**
         * Sets options details.
         *
         * @param namedId
         * @param namedValue
         */
        final void setConfigItemDetails(String namedId, Object namedValue) {
            Map<String, Object> optionSettings = new HashMap<>();
            try {
                if(originalSet!=null && originalSet.size()>0){
                    for(Map<String,Object> option:originalSet){
                        optionSettings.put("order", option.get("order"));
                        optionSettings.put("datatype", option.get("datatype"));
                        optionSettings.put("value", namedValue);
                        optionSet.put(namedId, optionSettings);
                    }
                }
            } catch (NullPointerException ex) {
                LOG.error("Could not create options set for '{}': {}", namedId, ex.getMessage(), ex);
            }
        }

        /**
         * Get the options as an nvp map.
         *
         * @return
         */
        public final Map<String, Object> getSimpleSettingsMap() {
            Map<String, Object> returnSet = new HashMap<>();
            for (String id : optionSet.keySet()) {
                returnSet.put(id, optionSet.get(id).get("value"));
            }
            return returnSet;
        }

        /**
         * Retrieves a single option with value.
         *
         * @param optionName
         * @return
         */
        public final Map<String, Object> getOption(String optionName) {
            if (optionSet.containsKey(optionName)) {
                return optionSet.get(optionName);
            } else {
                return null;
            }
        }

        /**
         * Gets the option list in a sorted way ordered by order in the xml.
         *
         * @return
         */
        public final Map<Integer, Object> getSortedOptionMap() {
            Map<Integer, Object> temp = new TreeMap<>();
            for (String id : optionSet.keySet()) {
                if ((int)optionSet.get(id).get("order")!=0) {
                    temp.put((int)optionSet.get(id).get("order"), optionSet.get(id).get("value"));
                }
            }
            return temp;
        }

        /**
         * Returns the original struct set.
         * @return 
         */
        public final List<Map<String,Object>> getOriginalSet(){
            return originalSet;
        }
        
    }
    
}
