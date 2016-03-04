/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.drivers.devices.devicestructure;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.Device.DeviceStatus;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceStatusException;
import static org.pidome.server.connector.drivers.devices.devicestructure.DeviceDataControl.LOG;
import org.pidome.server.connector.plugins.datamodifiers.DataModifierPlugin;
import org.pidome.server.connector.shareddata.SharedServerTimeService;
import org.pidome.server.connector.tools.properties.ObjectPropertyBindingBean;

/**
 *
 * @author John
 */
public abstract class DeviceControl {
    
    /**
     * The control's status.
     */
    public enum ControlStatus {
        /**
         * The control is ok.
         */
        OK,
        /**
         * The control has encountered a data timeout.
         */
        TIMEOUT;
    }
    
    /**
     * The control's id.
     */
    private String controlId;
    /**
     * If the control is an hidden control.
     * An hidden control is only used in code.
     */
    private boolean hidden;
    /**
     * The control's data type.
     */
    private DeviceControlDataType dataType = DeviceControlDataType.STRING;
    /**
     * The control type.
     */
    private DeviceControlType controlType  = DeviceControlType.DATA;
    
    /**
     * If retention is requested or not.
     */
    private boolean retention = false;
    
    /**
     * A read only control is a conntrol that can only be changed by data from devices.
     */
    private boolean readOnly = true;
    
    /**
     * The control's description.
     */
    private String description = "";
    
    /**
     * If an control has some extra information it is held here.
     */
    private String extra = "";
    
    /**
     * The shortcut order of the control.
     */
    private int shortcut = 0;
    
    /**
     * The visual type of the control.
     */
    private String visual;
    
    /**
     * The UTC data when the last data was received.
     */
    private Date lastDataChange = new Date();
    
    /**
     * Last dat received as formatted string.
     */
    private String lastDataChangeString = "00-00-0000 00:00:00";
    
    /**
     * The current value of the control.
     */
    private ObjectPropertyBindingBean lastValue = new ObjectPropertyBindingBean();
    
    /**
     * The device's custom meta data.
     */
    private DeviceControlCustomData customData;
    
    /**
     * Device logger.
     */
    static Logger LOG = LogManager.getLogger(DeviceControl.class);
    
    /**
     * The device control group member.
     */
    private final DeviceControlsGroup group;
    
    /**
     * The timeout of the control.
     */
    private int timeout = 0;
    
    /**
     * The current control status.s
     */
    private ControlStatus controlStatus = ControlStatus.OK;
    
    /**
     * Holds the timtout task when needed.
     */
    ScheduledExecutorService executorService;
    
    /**
     * Constructor.
     * @param group
     * @param type
     * @param controlId 
     * @throws org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException 
     */
    public DeviceControl(DeviceControlsGroup group, DeviceControlType type, String controlId) throws DeviceControlException {
        if(controlId==null || controlId.length()<1) throw new DeviceControlException("Control id is incorrect (length or unavailable)");
        this.controlId = controlId;
        this.controlType = type;
        this.group = group;
    }
    
    /**
     * Returns the group this control belongs to.
     * @return 
     */
    protected final DeviceControlsGroup getDeviceControlsGroup(){
        return this.group;
    }
    
    /**
     * Hides a group from visual output.
     */
    protected final void setHidden(){
        hidden = true;
    }
    
    /**
     * Returns the id of the attached modifier.
     * @return 
     */
    public final int getModifierId(){
        return this.attachedModifier;
    }
    
    /**
     * Returns true if the group is hidden.
     * @return 
     */
    public final boolean isHidden(){
        return hidden;
    }
    
    /**
     * Returns the fieldId.
     * @return 
     */
    public final String getControlId(){
        return this.controlId;
    }
    
    /**
     * Sets the initial minimal data.
     * @param data 
     * @throws org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException 
     */
    protected final void setInitialData(Map<String,Object> data) throws DeviceControlException {
        setDescription((String)data.get("description"));
        setDataTypeByString((String)data.get("datatype"));
        if(data.containsKey("modifier") && data.get("modifier") instanceof Boolean){
            this.modiferCompatible = (Boolean)data.get("modifier");
        } else {
            this.modiferCompatible = false;
        }
        if(data.containsKey("extra")){
            extra = (String)data.get("extra");
        }
        if(data.containsKey("hidden") && (boolean)data.get("hidden") == true){
            setHidden();
        }
        if(data.containsKey("readonly")){
            readOnly = (boolean)data.get("readonly") == true;
        }
        if(data.containsKey("shortcut")){
            try {
                shortcut = ((Number)data.get("shortcut")).intValue();
            } catch (NumberFormatException ex){
                throw new DeviceControlException("Invalid shortcut value: " + ex.getMessage());
            }
        }
        if(data.containsKey("timeout") && data.get("timeout") instanceof Number){
            this.timeout = ((Number)data.get("timeout")).intValue();
        }
        if(data.containsKey("retention")){
            retention = (boolean)data.get("retention") == true;
        }
        if(data.containsKey("visual")){
            switch((String)data.get("visual")){
                case "move":
                    if(data.get("datatype").equals("boolean")){
                        setVisualParam("move");
                    }
                break;
                case "temperature":
                case "temperatureF":
                case "luxlevel":
                case "lightpercentage":
                case "pcvalue":
                case "pressure":
                case "fluid":
                case "wind":
                case "battery":
                    if(data.get("datatype").equals("float") || data.get("datatype").equals("integer")){
                        setVisualParam((String)data.get("visual"));
                    }
                break;
            }
        }
        if(data.containsKey("customdata")){
            customData = new DeviceControlCustomData();
            for(Map.Entry<String,Object>entry:((Map<String,Object>)data.get("customdata")).entrySet()){
                if(entry.getValue() instanceof Number){
                    customData.putNumber(entry.getKey(), (Number)entry.getValue());
                } else if(entry.getValue() instanceof Boolean){
                    customData.putBoolean(entry.getKey(), (boolean)entry.getValue());
                } else if(entry.getValue() instanceof String){
                    customData.putString(entry.getKey(), (String)entry.getValue());
                }
            }
        }
    }
    
    /**
     * Returns custom data.
     * Implementor must check for null.
     * @return 
     */
    public final DeviceControlCustomData getControlCustomData(){
        return this.customData;
    }
    
    /**
     * Returns true if a control is read only.
     * This only applies to data controls.
     * @return 
     */
    public final boolean isReadOnly(){
        return readOnly;
    }
    
    /**
     * Returns true if this control has retention set.
     * @return 
     */
    public final boolean hasRetention(){
        return retention;
    }
    
    /**
     * Sets the visual parameter.
     * @param param 
     */
    private void setVisualParam(String param){
        visual = param;
    }
    
    /**
     * Returns if there is a visual parameter
     * @return 
     */
    public final boolean hasVisual(){
        return visual!=null;
    }
    
    /**
     * Returns the visual parameter content.
     * @return 
     */
    public final String getVisual(){
        return visual;
    }
    
    /**
     * Returns the extra data set if not set returns an empty string.
     * @return 
     */
    public final String getExtra(){
        return extra;
    }
    
    /**
     * Sets the field the fields datatype.
     * @param dataType
     */
    protected final void setDatatype(DeviceControlDataType dataType){
        this.dataType = dataType;
    }
    
    /**
     * Sets the datatype based on the datatype string representation.
     * @param type 
     * @throws org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException 
     */
    protected final void setDataTypeByString(String type) throws DeviceControlException{
        if(type==null || type.isEmpty()) throw new DeviceControlException("Need to supply a correct datatype");
        switch(type){
            case "string":
                setDatatype(DeviceControlDataType.STRING);
            break;
            case "integer":
                setDatatype(DeviceControlDataType.INTEGER);
            break;
            case "float":
                setDatatype(DeviceControlDataType.FLOAT);
            break;
            case "boolean":
                setDatatype(DeviceControlDataType.BOOLEAN);
            break;
            case "hex":
                setDatatype(DeviceControlDataType.HEX);
            break;
            case "color":
                setDatatype(DeviceControlDataType.COLOR);
            break;
            default:
                throw new DeviceControlException("Datatype " + type + " is unsupported");
        }
    }
    
    /**
     * Returns the field data type.
     * @return 
     */
    public final DeviceControlDataType getDataType(){
        return this.dataType;
    }
    
    /**
     * Returns the type of control it is.
     * @return 
     */
    public final DeviceControlType getControlType(){
        return this.controlType;
    }

    /**
     * Sets the description.
     * @param description 
     * @throws org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException 
     */
    protected final void setDescription(String description) throws DeviceControlException {
        if(description==null || description.length()<1 || description.length()>50) throw new DeviceControlException("Control description is incorrect (length or unavailable)");
        this.description = description;
    }
    
    /**
     * Returns the description.
     * @return 
     */
    public final String getDescription(){
        return this.description;
    }
    
    /**
     * Returns if a device has a shortcut or not.
     * @return 
     */
    public final boolean hasShortCut(){
        return shortcut!=0;
    }
    
    /**
     * Returns the shortcut position.
     * @return 
     */
    public final int getShortCutPosition(){
        return shortcut-1;
    }
    
    /**
     * Returns data by it's data type.
     * @param data
     * @return 
     */
    public final Object getDatatypeData(String data){
        switch(dataType){
            case INTEGER:
                return Integer.parseInt(data);
            case BOOLEAN:
                return Boolean.valueOf(data);
            case FLOAT:
                return Float.valueOf(data);
            default:
                return data;
        }
    }
    
    /**
     * Returns true or false depending on the same current known value.
     * @param toCheck
     * @return 
     * @todo: Move to hsb float epsilon calculation.
     */
    public final boolean valueIsSame(Object toCheck){
        try {
            switch(dataType){
                case INTEGER:
                    return (int)toCheck==(int)lastValue.getValue();
                case BOOLEAN:
                    return (boolean)toCheck==(boolean)lastValue.getValue();
                case FLOAT:
                    return (float)toCheck==(float)lastValue.getValue();
                case COLOR:
                    try {
                        if(((Map<String,Object>)toCheck).containsKey("hsb") && ((Map<String,Object>)lastValue.getValue()).containsKey("rgb")){
                            Map<String,Integer> got = (Map<String,Integer>)((Map<String,Object>)toCheck).get("rgb");
                            Map<String,Integer> have = (Map<String,Integer>)((Map<String,Object>)lastValue.getValue()).get("rgb");
                            if((have.containsKey("r") && have.containsKey("g") && have.containsKey("b")) && (got.containsKey("r") && got.containsKey("g") && got.containsKey("b"))){
                                return got.get("r").equals(have.get("r")) && 
                                       got.get("g").equals(have.get("g")) && 
                                       got.get("b").equals(have.get("b"));
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    } catch (Exception ex){
                        LOG.warn("Impossible to check equality between to check: {} and got: {}", toCheck, lastValue.getValue());
                        return false;
                    }
                default:
                    return ((String)lastValue.getValue()).equals((String)toCheck);
            }
        } catch (Exception ex){
            return false;
        }
    }
    
    /**
     * Sets the last known data.
     * Be sure to use primitives and not the objects!
     * @param value 
     */
    public void setLastKnownValue(Object value){
        LOG.trace("Converting '{}' to datatype: {}",value,dataType);
        lastDataChange = new Date();
        switch(dataType){
            case INTEGER:
                lastValue.setValue(Integer.parseInt(value.toString()));
            break;
            case BOOLEAN:
                if(value == null){
                    value = false;
                }
                lastValue.setValue(Boolean.parseBoolean(value.toString()));
            break;
            case FLOAT:
                lastValue.setValue(Float.parseFloat(value.toString()));
            break;
            case COLOR:
                lastValue.setValue(value);
            break;
            default:
                lastValue.setValue(value.toString());
            break;
        }
        lastDataChangeString = SharedServerTimeService.getDateTimeConverted(new GregorianCalendar());
    }
    
    /**
     * Returns the current status of the control.
     * @return 
     */
    public final ControlStatus getControlStatus(){
        return this.controlStatus;
    }
    
    /**
     * Returns true if this control has a timeout configured.
     * @return 
     */
    public final boolean hasTimeOutSet(){
        return this.timeout!=0;
    }
    
    /**
     * Returns the timeout in the amount of seconds.
     * @return 
     */
    public final int getTimeOut(){
        return this.timeout;
    }
    
    /**
     * Starts the timeout scheduler.
     */
    public final void startTimeOutScheduler(){
        stopTimeOutScheduler();
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(this.timeoutCheck(), timeout, timeout, TimeUnit.SECONDS);
    }
    
    /**
     * Stops the timeout scheduler.
     */
    public final void stopTimeOutScheduler(){
        if(executorService!=null){
            executorService.shutdownNow();
        }
    }
    
    /**
     * Returns the timout check runnable.
     * @return 
     */
    private Runnable timeoutCheck(){
        Runnable run = (Runnable)() -> {
            if(this.lastDataChange.getTime() < (new Date().getTime() - (this.timeout * 1000))){
                this.controlStatus = ControlStatus.TIMEOUT;
                try {
                    this.group.getDevice().setDeviceStatus(Device.DeviceStatus.CONTROL_TIMEOUT, this.getDescription() + " timed out.");
                } catch (UnsupportedDeviceStatusException ex) {
                    LOG.error("Could not modify the control's status");
                }
            } else {
                this.controlStatus = ControlStatus.OK;
                if(this.group.getDevice().getDeviceStatus()==DeviceStatus.CONTROL_TIMEOUT){
                    this.group.getDevice().setDeviceStatusOk();
                }
            }
        };
        return run;
    }
    
    /**
     * Returns the last data change date as string.
     * @return 
     */
    public final String getLastDataChangeAsString(){
        return this.lastDataChangeString;
    }
    
    /**
     * Sets the last known data where you MUST use the correct datatype yourself.
     * Use setLastKnownValue if you are not sure about your used datatype.
     * @param value 
     */
    public final void setLastKnownValueKnownDatatype(Object value){
        lastDataChange = new Date();
        lastValue.setValue(value);
    }
    
    /**
     * returns the button set value.
     * @return 
     */
    public final Object getValue(){
        return lastValue.getValue();
    }
    
    /**
     * Returns the value property to bind to.
     * @return 
     */
    public final ObjectPropertyBindingBean getValueProperty(){
        return this.lastValue;
    }
    
    public abstract Object getValueData();
    
    
    ////////////
    // Device modifier data
    ////////////
    /**
     * True if the control is modifier compatible
     */
    private boolean modiferCompatible = false;
    /**
     * Returns the attached modifier.
     */
    private int attachedModifier = 0;
    
    /**
     * Data direction hints.
     */
    public enum DataModifierDirection {
        /**
         * The plugin only supports output.
         * Output means that the plugin only delivers to the control where 
         * the control will broadcast data to both the web interface as the remote device.
         * 
         * A modifier output is handled the same way as if it is passed in via an
         * external resource.
         */
        OUTPUT,
        
        /**
         * The plugin bases it's output on it's input.
         * The values passed to devices are mostly calculated based on the input.
         * 
         * A modifiers output is handled the same way as it is passed in via an 
         * external resource
         */
        INPUT_OUTPUT;
    }
    
    /**
     * Sets a modifer listener.
     * @param modifier 
     */
    public final void setModifierListener(DataModifierPlugin modifier){
        modifier.addDeviceControl(this);
    }
    
    /**
     * Removes a modifier listener.
     * @param modifier 
     */
    public final void removeModifierListener(DataModifierPlugin modifier){
        modifier.removeDevicecontrol(this);
    }
    
    /**
     * Returns true if the control is modifier compatible.
     * @return 
     */
    public final boolean isModifierCompatible(){
        return modiferCompatible;
    }
    
    /**
     * Sets the modifier id.
     * @param modifierId 
     */
    public final void attachModifier(int modifierId){
        this.attachedModifier = modifierId;
    }
    
    /**
     * Returns the modifier id if attached.
     * @return 
     */
    public final boolean hasModifier(){
        return this.attachedModifier!=0;
    }
    
    /**
     * Contains the data direction.
     */
    private DataModifierDirection direction;
    
    /**
     * Returns the modifier meant for this control.
     * @return 
     */
    public final int getModifier(){
        return this.attachedModifier;
    }
    
    /**
     * Set's the data direction.
     * @param direction 
     */
    public final void setDataDirection(DataModifierDirection direction){
        this.direction = direction;
    }

    /**
     * Returns the data direction.
     * @return 
     */
    public final DataModifierDirection getDataDirection(){
        return this.direction;
    }
    
    /**
     * Handles data from a modifier.
     * @param data 
     */
    public final void handleModifierOutput(Object data){
        DeviceCommandRequest cmd = new DeviceCommandRequest(this);
        cmd.setCommandValue(data);
        cmd.setGroupId(this.getDeviceControlsGroup().getGroupId());
        cmd.setCommandValueData(data);
        try {
            this.getDeviceControlsGroup().getDevice().handleCommandRequestFromModifier(cmd);
        } catch (UnsupportedDeviceCommandException ex) {
            LOG.error("Data transfered from the modifier could not be handled: {}", ex.getMessage(), ex);
        }
    }    
}
