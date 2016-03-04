/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.drivers.devices.devicestructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;

/**
 *
 * @author John
 */
public final class DeviceControlsGroup {
    
    private final String groupId;
    private final String groupLabel;
    private boolean hidden = false;
    private final Map<String,DeviceControl> controlsList = new HashMap<>();
    
    static Logger LOG = LogManager.getLogger(DeviceControlsGroup.class);
    
    private Device device;
    
    public DeviceControlsGroup(String groupId, String label) throws DeviceControlsGroupException {
        if(groupId==null || groupId.length()<1) throw new DeviceControlsGroupException("Invalid group id set (unavailable or length)");
        this.groupId = groupId;
        this.groupLabel = label;
    }
    
    protected final void setDevice(Device device){
        this.device = device;
    }
    
    protected final Device getDevice(){
        return this.device;
    }
    
    /**
     * Hides a group from visual output.
     */
    protected final void setHidden(){
        hidden = true;
    }
    
    /**
     * Returns true if the group is hidden.
     * @return 
     */
    public final boolean isHidden(){
        return hidden;
    }
    
    /**
     * Comnposes a groups content; 
     * @param controlsSet A list of controls details.
     * @throws org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException
     */
    public final void setGroupContents(List<Map<String,Object>> controlsSet) throws DeviceControlException {
        for(Map<String,Object> control:controlsSet){
            if(control.containsKey("type") && control.containsKey("id")){
                switch((String)control.get("type")){
                    case "#text": break; //// Just in case there are whitespaces
                    case "data":
                        createDataComponentSet(control);
                    break;
                    case "button":
                        createButtonComponentSet(control);
                    break;
                    case "toggle":
                        createToggleButtonComponentSet(control);
                    break;
                    case "select":    
                        createSelectComponentSet(control);
                    break;
                    case "slider":    
                        createSliderComponentSet(control);
                    break;
                    case "colorpicker":    
                        createColorPickerComponentSet(control);
                    break;
                    default:
                        throw new DeviceControlException("Unsupported control: " + (String)control.get("type"));
                }
            }
        }
    }

    /**
     * creates a data control.
     * @param dataMap 
     */
    private void createDataComponentSet(Map<String,Object> dataMap) throws DeviceControlException {
        DeviceDataControl control = new DeviceDataControl(this,(String)dataMap.get("id"));
        control.setDataControlData(dataMap);
        controlsList.put(control.getControlId(), control);
    }
    
    /**
     * creates a slider control.
     * @param dataMap 
     */
    private void createSliderComponentSet(Map<String,Object> dataMap) throws DeviceControlException {
        DeviceSliderControl control = new DeviceSliderControl(this,(String)dataMap.get("id"));
        control.setSliderControlData(dataMap);
        controlsList.put(control.getControlId(), control);
    }
    
    /**
     * creates a button control.
     * @param dataMap 
     */
    private void createButtonComponentSet(Map<String,Object> dataMap) throws DeviceControlException {
        DeviceButtonControl control = new DeviceButtonControl(this,(String)dataMap.get("id"));
        control.setButtonControlData(dataMap);
        controlsList.put(control.getControlId(), control);
    }
    
    /**
     * Creates a toggle button.
     * @param dataMap
     * @param childs 
     */
    private void createToggleButtonComponentSet(Map<String,Object> dataMap) throws DeviceControlException {
        DeviceToggleControl control = new DeviceToggleControl(this,(String)dataMap.get("id"));
        control.setToggleControlData(dataMap);
        controlsList.put(control.getControlId(), control);
    }
    
    /**
     * Creates a toggle button.
     * @param dataMap
     * @param childs 
     */
    private void createSelectComponentSet(Map<String,Object> dataMap) throws DeviceControlException {
        DeviceSelectControl control = new DeviceSelectControl(this,(String)dataMap.get("id"));
        control.setSelectControlData(dataMap);
        controlsList.put(control.getControlId(), control);
    }
    
    /**
     * Creates the color picker control.
     * @param dataMap
     * @param childs
     * @throws DeviceControlException 
     */
    private void createColorPickerComponentSet(Map<String,Object> dataMap) throws DeviceControlException {
        DeviceColorPickerControl control = new DeviceColorPickerControl(this,(String)dataMap.get("id"));
        control.setColorPickerControlData(dataMap);
        control.setDatatype(DeviceControlDataType.COLOR);
        controlsList.put(control.getControlId(), control);
    }
        
    /**
     * Returns the group id
     * @return 
     */
    public final String getGroupId(){
        return this.groupId;
    }
    
    /**
     * Returns the group label
     * @return 
     */
    public final String getGroupLabel(){
        return this.groupLabel;
    }
    
    /**
     * Returns the control by id.
     * @param controlId
     * @return 
     * @throws org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException 
     */
    public final DeviceControl getDeviceControl(String controlId) throws DeviceControlException {
        if(this.controlsList.containsKey(controlId)){
            return this.controlsList.get(controlId);
        } else {
            throw new DeviceControlException("Control id '"+controlId+"' does not exist in group id '" + this.getGroupId() + "' in device " + this.getDevice().getDeviceName());
        }
    }
    
    /**
     * Returns the created device controls list.
     * @return 
     */
    public final Map<String,DeviceControl> getGroupControls(){
        return this.controlsList;
    }
    
    /**
     * Returns all the device control groups.
     * @return 
     */
    public final DeviceControl[] getDeviceControlsAsList(){
        List<DeviceControl> list = new ArrayList<>(getGroupControls().values());
        return list.toArray(new DeviceControl[list.size()]);
    }
    
}
