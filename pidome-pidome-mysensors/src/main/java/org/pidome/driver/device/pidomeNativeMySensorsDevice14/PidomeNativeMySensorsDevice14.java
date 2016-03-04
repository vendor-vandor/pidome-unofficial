/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.device.pidomeNativeMySensorsDevice14;

import java.util.HashMap;
import java.util.Map;
import org.pidome.mysensors.PidomeNativeMySensorsDeviceResources14;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceNotification;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceColorPickerControlColorData;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroupException;

/**
 *
 * @author John
 */
public class PidomeNativeMySensorsDevice14 extends Device {

    private final PidomeNativeMySensorsDeviceResources14 resources = new PidomeNativeMySensorsDeviceResources14();
    static Logger LOG = LogManager.getLogger(PidomeNativeMySensorsDevice14.class);
    
    public PidomeNativeMySensorsDevice14(){}
    
    @Override
    public void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        switch(command.getDataType()){
            case BOOLEAN:
                dispatchToDriver(command.getGroupId(), String.valueOf(resources.getIntByVar(command.getControlId())), ((boolean)command.getCommandValue()==true)?"1":"0");
            break;
            case COLOR:
                DeviceColorPickerControlColorData colorData = new DeviceColorPickerControlColorData(command.getCommandValue());
                dispatchToDriver(command.getGroupId(), String.valueOf(resources.getIntByVar(command.getControlId())), colorData.getHex().replace("#", ""));
            break;
            default:
                dispatchToDriver(command.getGroupId(), String.valueOf(resources.getIntByVar(command.getControlId())), command.getCommandValueData().toString());
            break;
        }
    }

    /**
     * Handles special data from driver.
     * @param messageType
     * @param data 
     */
    public void handleSpecialData(String messageType, String data){
        try {
            switch(messageType){
                case "0": ///I_BATTERY_LEVEL
                    DeviceNotification notification = new DeviceNotification();
                    switch(this.getFullCommandSet().getControlsGroup("INTERNAL").getDeviceControl("I_BATTERY_LEVEL").getDataType()){
                        case INTEGER:
                            notification.addData("INTERNAL", "I_BATTERY_LEVEL", Integer.parseInt(data));
                        break;
                        case FLOAT:
                            notification.addData("INTERNAL", "I_BATTERY_LEVEL", Float.parseFloat(data));
                        break;
                        default:
                            notification.addData("INTERNAL", "I_BATTERY_LEVEL", data);
                        break;
                    }
                    dispatchToHost(notification);
                break;
            }
        } catch (DeviceControlsGroupException | DeviceControlException ex) {
            LOG.error("Trying to address non existing group and/or control: {}", ex.getMessage());
        }
    }
    
    @Override
    public void handleData(String data, Object object) {
        String[] lineParts = data.split(";");
        try {
            String subType = resources.getVarType(Integer.parseInt(lineParts[4]));
            DeviceControl command;
            if(lineParts[4].equals("2")){
                try {
                    command = getFullCommandSet().getControlsGroup(lineParts[1]).getDeviceControl("V_STATUS");
                } catch (DeviceControlException ex){
                    command = getFullCommandSet().getControlsGroup(lineParts[1]).getDeviceControl("V_LIGHT");
                    subType = "V_LIGHT";
                    LOG.warn("V_LIGHT is deprecated, plause change this to V_STATUS");
                }
            } else if (lineParts[4].equals("3")){
                try {
                    command = getFullCommandSet().getControlsGroup(lineParts[1]).getDeviceControl("V_PERCENTAGE");
                } catch (DeviceControlException ex){
                    command = getFullCommandSet().getControlsGroup(lineParts[1]).getDeviceControl("V_DIMMER");
                    subType = "V_DIMMER";
                    LOG.warn("V_DIMMER is deprecated, plause change this to V_PERCENTAGE");
                }
            } else if (lineParts[4].equals("21")){
                try {
                    command = getFullCommandSet().getControlsGroup(lineParts[1]).getDeviceControl("V_HVAC_FLOW_STATE");
                } catch (DeviceControlException ex){
                    command = getFullCommandSet().getControlsGroup(lineParts[1]).getDeviceControl("V_HEATER");
                    subType = "V_HEATER";
                    LOG.warn("V_HEATER is deprecated, this variable has been renamed to V_HVAC_FLOW_STATE");
                }
            } else if (lineParts[4].equals("21")){
                try {
                    command = getFullCommandSet().getControlsGroup(lineParts[1]).getDeviceControl("V_HVAC_SPEED");
                } catch (DeviceControlException ex){
                    command = getFullCommandSet().getControlsGroup(lineParts[1]).getDeviceControl("V_HEATER_SW");
                    subType = "V_HEATER_SW";
                    LOG.warn("V_HEATER_SW is deprecated, this variable has been renamed to V_HVAC_SPEED");
                }
            }else {
                command = getFullCommandSet().getControlsGroup(lineParts[1]).getDeviceControl(subType);
            }
            switch(lineParts[2]){
                case "2":
                    LOG.trace("Got a value request from node {} for {}, {}.", this.getAddress(), lineParts[1], lineParts[4]);
                    Object lastKnownData = this.getFullCommandSet().getControlsGroup(lineParts[1]).getDeviceControl(subType).getValueData();
                    if(lastKnownData==null){
                        switch(this.getFullCommandSet().getControlsGroup(lineParts[1]).getDeviceControl(subType).getDataType()){
                            case STRING:
                            case HEX:
                                this.dispatchToDriver(lineParts[1], lineParts[4], "");
                            break;
                            case INTEGER:
                                this.dispatchToDriver(lineParts[1], lineParts[4], "0");
                            break;
                            case FLOAT:
                                this.dispatchToDriver(lineParts[1], lineParts[4], "0.0");
                            break;
                            case BOOLEAN:
                                this.dispatchToDriver(lineParts[1], lineParts[4], "false");
                            break;
                            case COLOR:
                                this.dispatchToDriver(lineParts[1], lineParts[4], "000000");
                            break;
                        }
                    } else {
                        if(command.getValue().toString()!=null) this.dispatchToDriver(lineParts[1], lineParts[4],lastKnownData.toString());                        
                    }
                break;
                default:
                    DeviceNotification notification = new DeviceNotification();
                    switch(command.getDataType()){
                        case STRING:
                        case HEX:
                            command.setLastKnownValueKnownDatatype(lineParts[5]);
                            notification.addData(lineParts[1], subType, lineParts[5]);
                        break;
                        case INTEGER:
                            command.setLastKnownValueKnownDatatype(Integer.valueOf(lineParts[5]));
                            notification.addData(lineParts[1], subType, Integer.valueOf(lineParts[5]));
                        break;
                        case FLOAT:
                            command.setLastKnownValueKnownDatatype(Float.valueOf(lineParts[5]));
                            notification.addData(lineParts[1], subType, Float.valueOf(lineParts[5]));
                        break;
                        case BOOLEAN:
                            command.setLastKnownValueKnownDatatype(lineParts[5].equals("1"));
                            notification.addData(lineParts[1], subType, lineParts[5].equals("1"));
                        break;
                        case COLOR:
                            Map<String,String> colorMap = new HashMap<>();
                            colorMap.put("hex", "#"+lineParts[5]);
                            command.setLastKnownValue(colorMap);
                            notification.addData(lineParts[1], subType, command.getValue(), false);
                        break;
                    }
                    dispatchToHost(notification);
                break;
            }
        } catch (Exception ex){
            LOG.error("Error handling: {} in device {}, error: {}", data, getDeviceName(), ex.getMessage());
        }
    }

    /**
     * For usages when extending this class.
     * @return 
     */
    public final PidomeNativeMySensorsDeviceResources14 getResources(){
        return this.resources;
    }
    
    @Override
    public void shutdownDevice() {
        throw new UnsupportedOperationException("Not used"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void startupDevice() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}