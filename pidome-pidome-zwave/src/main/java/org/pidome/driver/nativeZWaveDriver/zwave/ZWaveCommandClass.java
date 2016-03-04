/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.nativeZWaveDriver.zwave;

/**
 *
 * @author John
 */
public class ZWaveCommandClass {
 
    public enum ZWaveCommand {

        COMMAND_CLASS_NO_OPERATION((byte)0x00, "No action"),
        COMMAND_CLASS_BASIC((byte)0x20, "Basic"),
        COMMAND_CLASS_CONTROLLER_REPLICATION((byte)0x21, "Controller replication"),
        COMMAND_CLASS_APPLICATION_STATUS((byte)0x22, "Application status"),
        COMMAND_CLASS_ZIP_SERVICES((byte)0x23, "Zip service"),
        COMMAND_CLASS_ZIP_SERVER((byte)0x24, "Zip server"),
        COMMAND_CLASS_SWITCH_BINARY((byte)0x25, "Switch (bin)"),
        COMMAND_CLASS_SWITCH_MULTILEVEL((byte)0x26, "Switch (multi)"),
        COMMAND_CLASS_SWITCH_ALL((byte)0x27, "Switch all"),
        COMMAND_CLASS_SWITCH_TOGGLE_BINARY((byte)0x28, "Toggle (bin)"),
        COMMAND_CLASS_SWITCH_TOGGLE_MULTILEVEL((byte)0x29, "Toggle (multi)"),
        COMMAND_CLASS_CHIMNEY_FAN((byte)0x2A, "Chimney fan"),
        COMMAND_CLASS_SCENE_ACTIVATION((byte)0x2B, "Scene activation"),
        COMMAND_CLASS_SCENE_ACTUATOR_CONF((byte)0x2C, "Scene actuator config"),
        COMMAND_CLASS_SCENE_CONTROLLER_CONF((byte)0x2D, "Scene controller config"),
        COMMAND_CLASS_ZIP_CLIENT((byte)0x2E, "Zip client"),
        COMMAND_CLASS_ZIP_ADV_SERVICES((byte)0x2F, "Zip advert. service"),
        COMMAND_CLASS_SENSOR_BINARY((byte)0x30, "Sensor"),
        COMMAND_CLASS_SENSOR_MULTILEVEL((byte)0x31, "Multi sensor"),
        COMMAND_CLASS_METER((byte)0x32, "Meter"),
        COMMAND_CLASS_COLOR((byte)0x33, "Color control"),
        COMMAND_CLASS_ZIP_ADV_CLIENT((byte)0x34, "Zip advert. client"),
        COMMAND_CLASS_METER_PULSE((byte)0x35, "Pulse meter"),
        COMMAND_CLASS_THERMOSTAT_HEATING((byte)0x38, "Thermostat heating"),
        COMMAND_CLASS_THERMOSTAT_MODE((byte)0x40, "Thermostat mode"),
        COMMAND_CLASS_THERMOSTAT_OPERATING_STATE((byte)0x42, "Thermostat state"),
        COMMAND_CLASS_THERMOSTAT_SETPOINT((byte)0x43, "Thermostat setpoint"),
        COMMAND_CLASS_THERMOSTAT_FAN_MODE((byte)0x44, "Thermostat fan mode"),
        COMMAND_CLASS_THERMOSTAT_FAN_STATE((byte)0x45, "Thermostat fan state"),
        COMMAND_CLASS_CLIMATE_CONTROL_SCHEDULE((byte)0x46, "Climate control schedule"),
        COMMAND_CLASS_THERMOSTAT_SETBACK((byte)0x47, "Thermostat setback"),
        COMMAND_CLASS_BASIC_WINDOW_COVERING((byte)0x50, "Basic window covering"),
        COMMAND_CLASS_MTP_WINDOW_COVERING((byte)0x51, "MTP Window covering"),
        COMMAND_CLASS_CRC16_ENCAP((byte)0x56, "CRC16 Encap."),
        COMMAND_CLASS_DEVICE_RESET_LOCALLY((byte)0x5A, "Reset device"),
        COMMAND_CLASS_CENTRAL_SCENE((byte)0x5B, "Central scene"),
        COMMAND_CLASS_ZWAVE_PLUS_INFO((byte)0x5E, "ZWave+ info"),
        COMMAND_CLASS_MULTI_INSTANCE((byte)0x60, "Multi instance"),
        COMMAND_CLASS_DOOR_LOCK((byte)0x62, "Door lock"),
        COMMAND_CLASS_USER_CODE((byte)0x63, "User code"),
        COMMAND_CLASS_CONFIGURATION((byte)0x70, "Configuration"),
        COMMAND_CLASS_ALARM((byte)0x71, "Alarm"),
        COMMAND_CLASS_MANUFACTURER_SPECIFIC((byte)0x72, "Manufacturer specific"),
        COMMAND_CLASS_POWERLEVEL((byte)0x73, "Power level"),
        COMMAND_CLASS_PROTECTION((byte)0x75, "Protection"),
        COMMAND_CLASS_LOCK((byte)0x76, "Lock"),
        COMMAND_CLASS_NODE_NAMING((byte)0x77, "Node name"),
        COMMAND_CLASS_FIRMWARE_UPDATE_MD((byte)0x7A, "Firmware update (md)"),
        COMMAND_CLASS_GROUPING_NAME((byte)0x7B, "Group name"),
        COMMAND_CLASS_REMOTE_ASSOCIATION_ACTIVATE((byte)0x7C, "Activate remote association"),
        COMMAND_CLASS_REMOTE_ASSOCIATION((byte)0x7D, "Remote association"),
        COMMAND_CLASS_BATTERY((byte)0x80, "Battery"),
        COMMAND_CLASS_CLOCK((byte)0x81, "Clock"),
        COMMAND_CLASS_HAIL((byte)0x82, "Hail"),
        COMMAND_CLASS_WAKE_UP((byte)0x84, "Wake up"),
        COMMAND_CLASS_ASSOCIATION((byte)0x85, "Association"),
        COMMAND_CLASS_VERSION((byte)0x86, "Version"),
        COMMAND_CLASS_INDICATOR((byte)0x87, "Indicator"),
        COMMAND_CLASS_PROPRIETARY((byte)0x88, "Proprietary"),
        COMMAND_CLASS_LANGUAGE((byte)0x89, "Language"),
        COMMAND_CLASS_TIME((byte)0x8A, "Time"),
        COMMAND_CLASS_TIME_PARAMETERS((byte)0x8B, "Time parameters"),
        COMMAND_CLASS_GEOGRAPHIC_LOCATION((byte)0x8C, "Geo location"),
        COMMAND_CLASS_COMPOSITE((byte)0x8D, "Composite"),
        COMMAND_CLASS_MULTI_INSTANCE_ASSOCIATION((byte)0x8E, "Multi instance association"),
        COMMAND_CLASS_MULTI_CMD((byte)0x8F, "Multi command"),
        COMMAND_CLASS_ENERGY_PRODUCTION((byte)0x90, "Energy production"),
        COMMAND_CLASS_MANUFACTURER_PROPRIETARY((byte)0x91, "Manufacturer proprietary"),
        COMMAND_CLASS_SCREEN_MD((byte)0x92, "Screen (md)"),
        COMMAND_CLASS_SCREEN_ATTRIBUTES((byte)0x93, "Screen attributes"),
        COMMAND_CLASS_SIMPLE_AV_CONTROL((byte)0x94, "Simple AV control"),
        COMMAND_CLASS_AV_CONTENT_DIRECTORY_MD((byte)0x95, "AV content directory"),
        COMMAND_CLASS_AV_RENDERER_STATUS((byte)0x96, "AV renderer status"),
        COMMAND_CLASS_AV_CONTENT_SEARCH_MD((byte)0x97, "AV content search (md)"),
        COMMAND_CLASS_SECURITY((byte)0x98, "Security"),
        COMMAND_CLASS_AV_TAGGING_MD((byte)0x99, "AV tagging"),
        COMMAND_CLASS_IP_CONFIGURATION((byte)0x9A, "IP configuration"),
        COMMAND_CLASS_ASSOCIATION_CONFIGURATION((byte)0x9B, "Association configuration"),
        COMMAND_CLASS_SENSOR_ALARM((byte)0x9C, "Alarm sensor"),
        COMMAND_CLASS_SILENCE_ALARM((byte)0x9D, "Silence alarm"),
        COMMAND_CLASS_SENSOR_CONFIGURATION((byte)0x9E, "Sensor configuration"),
        COMMAND_CLASS_NON_INTEROPERABLE((byte)0xF0, "No interopration");
        
        private final byte value;
        private final String desc;
        
        private ZWaveCommand(byte value, String desc){
            this.value = value;
            this.desc = desc;
        }
        
        public final byte getValue(){
            return this.value;
        }
        
        public short getAsShort(){
            return (short)this.value;
        }
        
        public String getDescritpion(){
            return this.desc;
        }
        
    }
    
    public static ZWaveCommand getByString(String name){
        for(ZWaveCommand cmd:ZWaveCommand.values()){
            if(cmd.toString().equals(name)){
                return cmd;
            }
        }
        return ZWaveCommand.COMMAND_CLASS_NO_OPERATION;
    }
    
    public static ZWaveCommand getByString(short value){
        for(ZWaveCommand cmd:ZWaveCommand.values()){
            if(cmd.getAsShort() == value){
                return cmd;
            }
        }
        return ZWaveCommand.COMMAND_CLASS_NO_OPERATION;
    }
    
}