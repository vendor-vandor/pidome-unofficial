/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.entities.devices;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A toggle control.
 * @author John
 */
public class DeviceToggleControl extends DeviceControl {

    /**
     * Map containing true and false state labels.
     */
    private final Map<Boolean,String> yesnoMap = new HashMap<>();
    
    static {
        Logger.getLogger(DeviceToggleControl.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Constructor.
     * @param controlId The id of this control.
     * @throws DeviceControlException When the control can not be constructed.
     */
    protected DeviceToggleControl(String controlId) throws DeviceControlException {
        super(DeviceControlType.TOGGLE, controlId);
        setLastKnownValueKnownDatatype(false);
    }
    
    /**
     * Constructs the toggle button.
     * @param data Basic toggle control data.
     * @throws DeviceControlException Thrown when dat is incomplete or faulty.
     */
    protected final void setToggleControlData(Map<String,Object> data) throws DeviceControlException {
        setInitialDataStructure((Map<String,Object>)data);
        Map<String,Map<String,String>> buttonData = (Map<String,Map<String,String>>)data.get("commandset");
        yesnoMap.put(true, (String)buttonData.get("on").get("label"));
        yesnoMap.put(false, (String)buttonData.get("off").get("label"));
    }
    
    /**
     * Returns the label for the on tag.
     * @return The label for th true position.
     */
    public final String getOnLabel(){
        return yesnoMap.get(true);
    }
    
    /**
     * Returns the label for the off tag.
     * @return The label for the false position.
     */
    public final String getOffLabel(){
        return yesnoMap.get(false);
    }
    
    /**
     * Returns the label for the off tag.
     * @param value true or false position.
     * @return label corresponding to the true or false position.
     */
    public final String getLabelByValue(boolean value){
        return yesnoMap.get(value);
    }
    
    /**
     * Returns the mapped real value.
     * Always returns an boolean.
     * @return current boolean value as it's primitive.
     */
    @Override
    public final Object getValueData(){
        switch(String.valueOf(this.getValue())){
            case "true":
            case "on":
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Sets the last known data.
     * A toggle control is ALWAYS true or false.
     * @param value boolean value as it's primitive.
     */
    @Override
    public void setLastKnownValue(Object value){
        if(String.valueOf(value).equals("true")){
            setLastKnownValueKnownDatatype(true);
        } else {
            setLastKnownValueKnownDatatype(false);
        }
    }

    /**
     * Returns a ready to send command.
     * @param command Command of type ToggleCommand.
     * @return Complete structure to be used with the JSONConnector.
     * @throws DeviceControlCommandException When the type is not of type ToggleCommand
     */
    public final DeviceCommandStructure createSendCommand(ToggleCommand command) throws DeviceControlCommandException {
        return new DeviceCommandStructure(this, command.getSendValue(), this.getCommandExtra());
    }
 
    /**
     * The toggle command.
     */
    public static class ToggleCommand implements DeviceControlCommand {
        
        /**
         * The value to be send.
         */
        private final boolean sendValue;
        
        /**
         * Constructs a toggle command with it's value.
         * @param value The primitive value to be send.
         */
        public ToggleCommand(boolean value){
            sendValue = value;
        }
     
        /**
         * Returns the value to be send.
         * @return 
         */
        private boolean getSendValue(){
            return sendValue;
        }
        
    }
    
    
}