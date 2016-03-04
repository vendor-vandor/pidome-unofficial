/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.entities.devices;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A button is a single button and an action control.
 * @author John
 */
public class DeviceButtonControl extends DeviceControl {
    
    static {
        Logger.getLogger(DeviceButtonControl.class.getName()).setLevel(Level.ALL);
    }
    
    String label   = "";
    Object command = "";
    
    /**
     * Constructs the button.
     * @param fieldId the control id
     * @throws DeviceControlException When an incorrect control type is used.
     */
    protected DeviceButtonControl(String fieldId) throws DeviceControlException {
        super(DeviceControlType.BUTTON, fieldId);
    }
    
    /**
     * Sets the button data.
     * @param data Map containing device data.
     * @throws DeviceControlException Thrown when data is incomplete.
     */
    protected final void setButtonControlData(Map<String,Object> data) throws DeviceControlException {
        this.setInitialDataStructure(data);
        if(!data.containsKey("label")){
            throw new DeviceControlException("Not all mimimal required attributes are present for button control type");
        }
        label     = (String)data.get("label");
    }
    
    /**
     * Sets the controls label.
     * @return The label to be put on a button.
     */
    public final String getLabel(){
        return label;
    }
    
    /**
     * Returns the mapped real value.
     * This returns the value set below the button.
     * Do not use this unless explicit requested.
     * @return The value this button executes.
     */
    @Override
    public final Object getValueData(){
        return this.getValue();
    }
 
    /**
     * Returns a ready to send command.
     * @param command The ButtonCommand
     * @return Complete structure to be used with the JSONConnector.
     * @throws DeviceControlCommandException When the type is not of type ButtonCommand
     */
    public DeviceCommandStructure createSendCommand(ButtonCommand command) throws DeviceControlCommandException {
        return new DeviceCommandStructure(this, this.getValueData(), this.getCommandExtra());
    }
    
    /**
     * The Button command.
     * This command does not have extra options because the createSendCommand already knows the value.
     */
    public static class ButtonCommand implements DeviceControlCommand {
        /**
         * Constructs a button command.
         */
        public ButtonCommand(){}
    }
    
}