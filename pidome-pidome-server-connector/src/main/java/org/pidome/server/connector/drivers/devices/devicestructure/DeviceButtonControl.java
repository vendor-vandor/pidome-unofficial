/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.drivers.devices.devicestructure;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John
 */
public class DeviceButtonControl extends DeviceControl {
    
    String label          = "";
    Object command        = "";
    
    static Logger LOG = LogManager.getLogger(DeviceButtonControl.class);
    
    protected DeviceButtonControl(DeviceControlsGroup group, String fieldId) throws DeviceControlException {
        super(group,DeviceControlType.BUTTON, fieldId);
    }
    
    /**
     * Sets the button data.
     * @param data
     * @throws DeviceControlException 
     */
    protected final void setButtonControlData(Map<String,Object> data) throws DeviceControlException {
        if(!data.containsKey("label") || !data.containsKey("value")){
            throw new DeviceControlException("Not all mimimal required attributes are present for button control type");
        }
        if(((String)data.get("label")).isEmpty() || ((String)data.get("label")).length()>20) throw new DeviceControlException("Incorrect label in button control. Length too large (max 20): " + ((String)data.get("label")).length());
        if(data.get("value").toString().isEmpty()) throw new DeviceControlException("Incorrect value used in button");
        setInitialData(data);
        label     = (String)data.get("label");
        setLastKnownValue(data.get("value"));
    }
    
    /**
     * Sets the controls label.
     * @return
     */
    public final String getLabel(){
        return label;
    }
    
    /**
     * Returns the mapped real value
     * @return 
     */
    public final Object getValueData(){
        return this.getValue();
    }
    
}
