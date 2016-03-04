/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.devices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Group containing device controls.
 * @author John
 */
public class DeviceControlGroup {
    
    static {
        Logger.getLogger(DeviceControlGroup.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * The group id.
     */
    private final String groupId;
    /**
     * The group name.
     */
    private final String groupName;
    
    /**
     * List of controls.
     */
    private final List<DeviceControl> controls = new ArrayList<>();
    
    /**
     * The device this group belongs to.
     */
    private final Device device;
    
    /**
     * Constructor setting id and name.
     * @param device The device of this group.
     * @param groupId The id of the group.
     * @param groupName The name of the group.
     */
    protected DeviceControlGroup(Device device, String groupId, String groupName){
        this.groupId   = groupId;
        this.groupName = groupName;
        this.device    = device;
    }
    
    /**
     * Returns the device this group belongs to.
     * @return The device of the id group.
     */
    public final Device getDevice(){
        return this.device;
    }
    
    /**
     * Returns the group id.
     * @return The id of the group.
     */
    public final String getGroupId(){
        return this.groupId;
    }
    
    /**
     * Returns the group name.
     * @return The name of the group.
     */
    public final String getGroupName(){
        return this.groupName;
    }
 
    /**
     * Updates a set of controls with new data.
     * @param controlSet Control data.
     */
    protected final void updateControlValues(Map<String,Object> controlSet){
        for(Map.Entry<String,Object> controlData: controlSet.entrySet()){
            try {
                getControl(controlData.getKey()).setLastKnownValueKnownDatatype(controlData.getValue());
            } catch (DeviceControlNotFoundException ex) {
                Logger.getLogger(DeviceControlGroup.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Returns a control by it's id.
     * @param controlId The control id.
     * @return The control.
     * @throws org.pidome.client.entities.devices.DeviceControlNotFoundException When the control is not found
     */
    public final DeviceControl getControl(String controlId) throws DeviceControlNotFoundException{
        for(DeviceControl control:this.controls){
            if(control.getControlId().equals(controlId)){
                return control;
            }
        }
        throw new DeviceControlNotFoundException("Control with id " + controlId + " does not exist");
    }
    
    /**
     * Creates the controls.
     * @param controls An list of control mapped data.
     */
    protected final void createControlsGroup(List<Map<String,Object>> controls) {
        for(Map<String,Object> control: controls){
            Map<String,Object> typeDetails = (Map<String,Object>)control.get("typedetails");
            String controlId = (String)((Map<String,Object>)control.get("typedetails")).get("id");
            try {
                switch((String)control.get("commandtype")){
                    case "data":
                        DeviceDataControl dataControl = new DeviceDataControl(controlId);
                        dataControl.setGroup(this);
                        dataControl.setDataControlData(typeDetails);
                        dataControl.setLastKnownValueKnownDatatype(control.get("currentvalue"));
                        this.controls.add(dataControl);
                    break;
                    case "toggle":
                        DeviceToggleControl toggleControl = new DeviceToggleControl(controlId);
                        toggleControl.setGroup(this);
                        toggleControl.setToggleControlData(typeDetails);
                        toggleControl.setLastKnownValueKnownDatatype(control.get("currentvalue"));
                        this.controls.add(toggleControl);
                    break;
                    case "slider":
                        DeviceSliderControl sliderControl = new DeviceSliderControl(controlId);
                        sliderControl.setGroup(this);
                        sliderControl.setSliderControlData(typeDetails);
                        sliderControl.setLastKnownValueKnownDatatype(control.get("currentvalue"));
                        this.controls.add(sliderControl);
                    break;
                    case "button":
                        DeviceButtonControl buttonControl = new DeviceButtonControl(controlId);
                        buttonControl.setGroup(this);
                        buttonControl.setButtonControlData(typeDetails);
                        this.controls.add(buttonControl);
                    break;
                    case "colorpicker":
                        DeviceColorPickerControl colorpickerControl = new DeviceColorPickerControl(controlId);
                        colorpickerControl.setGroup(this);
                        colorpickerControl.setColorPickerControlData(typeDetails);
                        colorpickerControl.setLastKnownValueKnownDatatype(control.get("currentvalue"));
                        this.controls.add(colorpickerControl);
                    break;
                }
            } catch (DeviceControlException ex){
                Logger.getLogger(DeviceControlGroup.class.getName()).log(Level.SEVERE, "Problem creating control " + controlId + " in group " + this.groupId + " in device " + this.device.getDeviceName(), ex);
            }
        }
    }
    
    /**
     * Returns the list of controls in this controls group.
     * @return The list of the controls in this group.
     */
    public final List<DeviceControl> getGroupControls(){
        return this.controls;
    }
    
}
