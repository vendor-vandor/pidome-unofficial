/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.plugins.mqtt.pidomeMQTTBroker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.PluginDeviceMutationException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroup;
import org.pidome.server.connector.interfaces.web.configuration.WebConfiguration;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationOptionSet;
import org.pidome.server.connector.interfaces.web.configuration.WebOption;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentComplexNVP;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentListNVP;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentSimpleNVP;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentationGroup;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.emulators.DevicePlugin;

/**
 * This will be the plugin frontend for the adjusted Moquette MQTT broker.
 * @author John
 */
public final class PidomeMQTTBroker extends DevicePlugin {

    static Logger LOG = LogManager.getLogger(PidomeMQTTBroker.class);
    
    private boolean running = false;
    
    private String PUB_ROOT = "/Home/";
    private String PUB_DEVICE_ROOT = "/hooks/devices/";
    private String PUB_RPC_ROOT = "/hooks/jsonrpc/";

    private enum RunType {
        BROKER,CLIENT;
    }
    
    PluginRunner plugin;
    
    private RunType runType = RunType.BROKER;
    
    Map<String, String> config = new HashMap<>();
    
    WebPresentationGroup deviceSection = new WebPresentationGroup("Active broker devices", "Below is a list of active broker devices. Click on a device to view the details, MQTT path and datatype to send. To add a device to the server create a custom device and add it. It will appear below.");
    WebPresentComplexNVP deviceSectionTable   = new WebPresentComplexNVP("Active devices");
    Map<String,List<Map<String,Object>>> deviceTable = new HashMap<>(); ///Map<device,List<Map<subject,value>>>
    
    WebPresentationGroup      errors     = new WebPresentationGroup("Last known messages", "Below is a list of the last 5 messages");
    WebPresentListNVP         errorTable = new WebPresentListNVP("Messages list");
    List<WebPresentSimpleNVP> errorList  = new ArrayList<>();
    
    Calendar cal = new GregorianCalendar(TimeZone.getDefault(), Locale.getDefault());
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");   
    
    public PidomeMQTTBroker(){
        WebConfiguration conf = new WebConfiguration();
        WebConfigurationOptionSet optionSet = new WebConfigurationOptionSet("MQTT options");
        optionSet.setConfigurationSetDescription("Below are the default broker options, as this project grows possible extra options can be added in the future. "
                                               + "This broker can be used to connect to with an MQTT client and to publish device data to.<br/><br/>"
                                               + "Leave the paths as default unless you know what you are doing. The 'Publication root' is used to publicize data with names you have gave devices<br/>"
                                               + "The 'Device raw publication root' is used for raw data interchange. Data publushed to this path is seen as data to update devices.<br/><br/>"
                                               + "Both the customizable paths MUST start and end with a slash (\"/\").");
        WebOption port = new WebOption("PORT", "MQTT port", "The port of the MQTT server/client", WebOption.WebOptionConfigurationFieldType.INT);
        port.setDefaultValue("1883");
        WebOption pubRoot = new WebOption("PUBROOT", "Publication root", "Used to publicize statuses a semantic way, must start and end with a slash (\"/\").", WebOption.WebOptionConfigurationFieldType.STRING);
        pubRoot.setDefaultValue("/Home/");
        WebOption pubDeviceRoot = new WebOption("PUBDEVICEROOT", "Device raw publication root", "This path is used for raw devices data, must start and end with a slash (\"/\").", WebOption.WebOptionConfigurationFieldType.STRING);
        pubDeviceRoot.setDefaultValue("/hooks/devices/");

        WebOption pubRPCRoot = new WebOption("PUBRPCROOT", "JSON-RPC publications root", "This path is used to publish/receive JSON-RPC data, must start and end with a slash (\"/\"). (\"/\").", WebOption.WebOptionConfigurationFieldType.STRING);
        pubRPCRoot.setDefaultValue("/hooks/jsonrpc/");
        
        WebOption pubRemoteBroker = new WebOption("IPADDRESS", "Remote broker ip", "Remote broker ip address, only when running in client mode.", WebOption.WebOptionConfigurationFieldType.IP_ADDRESS);
        optionSet.addOption(pubRemoteBroker);
        
        optionSet.addOption(port);
        optionSet.addOption(pubRoot);
        optionSet.addOption(pubDeviceRoot);
        optionSet.addOption(pubRPCRoot);
        
        conf.addOptionSet(optionSet);
        
        WebConfigurationOptionSet optionRunTypeSet = new WebConfigurationOptionSet("Plugin run type options");
        optionRunTypeSet.setConfigurationSetDescription("Here you can choose how this pugin should behave. When running as broker all the above options are applicable. When it runs as a client "
                                                      + "The port is used to connect to and the 'Device raw publication root' is used to determine device publications on the remote MQTT broker.<br/><br/>"
                                                      + "NOTICES:<br/>- When the plugin runs in client mode it will currently NOT be publishing device statusses over the 'Device raw publication root' path. The remote "
                                                      + "broker is responsible to supply these, this feature will be added but need some server changes.<br/>- In client mode there will be no semantic broadcasts.");
        
        Map<String,String> runChoices = new HashMap<>();
        runChoices.put("BROKER", "Run as broker (default)");
        runChoices.put("CLIENT", "Run as client");
        
        WebOption runOption = new WebOption("RUNTYPE", "Select plugin mode", "Select the mode the MQTT plugin should run as, read the above remark!", WebOption.WebOptionConfigurationFieldType.SELECT, runChoices);
        runOption.setDefaultValue("BROKER");
        optionRunTypeSet.addOption(runOption);
        
        conf.addOptionSet(optionRunTypeSet);
        
        setConfiguration(conf);
        
        errorTable.setValue(errorList);
        errors.add(errorTable);
        
        deviceSectionTable.setValue(deviceTable);
        deviceSection.add(deviceSectionTable);
        
        this.addWebPresentationGroup(errors);
        this.addWebPresentationGroup(deviceSection);
    }
    
    /**
     * Logs an error.
     * @param message 
     */
    protected void LogError(String message){
        WebPresentSimpleNVP error = new WebPresentSimpleNVP(dateFormat.format(cal.getTime()));
        error.setValue(message);
        errorList.add(0,error);
        if(errorList.size()>5){
            errorList.remove(5);
        }
    }
    
    @Override
    public void setConfigurationValues(Map<String, String> conf) throws WebConfigurationException {
        config = conf;
        if(!config.containsKey("PORT") || config.get("PORT").equals("")){
            throw new WebConfigurationException("Check your port.");
        }
        if(!config.containsKey("PUBROOT") || config.get("PUBROOT").equals("")){
            throw new WebConfigurationException("Check your publication root.");
        } else {
            PUB_ROOT = config.get("PUBROOT");
        }
        if(!config.containsKey("PUBDEVICEROOT") || config.get("PUBDEVICEROOT").equals("")){
            throw new WebConfigurationException("Check your device root.");
        } else {
            PUB_DEVICE_ROOT = config.get("PUBDEVICEROOT");
        }
        if(config.containsKey("PUBRPCROOT") && !config.get("PUBRPCROOT").equals("")){
            PUB_RPC_ROOT = config.get("PUBRPCROOT");
        }
        if(config.containsKey("RUNTYPE")){
            switch(config.get("RUNTYPE")){
                case "CLIENT":
                    runType = RunType.CLIENT;
                break;
                default:
                    runType = RunType.BROKER;
                break;
            }
        }
    }

    @Override
    public void startPlugin() throws PluginException {
        try {
            switch(runType){
                case CLIENT:
                    plugin = new PluginClientRunner(config);
                break;
                default:
                    plugin = new PluginBrokerRunner(config);
                break;
            }
            plugin.setParent(this);
            plugin.start();
        } catch (WebConfigurationException | PluginException ex){
            throw new PluginException(ex.getMessage());
        }
        running = true;
        this.setRunning(running);
    }

    @Override
    public void stopPlugin() {
        if(plugin!=null){
            plugin.stop();
        }
        running = false;
        this.setRunning(running);
    }

    @Override
    public String getExpectedDriverId() {
        return "NATIVE_PIDOMEMQTTBROKER_DRIVER";
    }

    @Override
    public String getExpectedDriverVersion() {
        return "0.0.1";
    }

    @Override
    public void handleDeviceData(Device device, String group, String control, byte[] data, boolean userIntent) {
        plugin.handleDeviceData(device, group, control, data, userIntent);
    }

    @Override
    public void handleDeviceData(Device device, DeviceCommandRequest dcr) {
        plugin.handleDeviceData(device, dcr.getGroupId(), dcr.getControlId(), dcr.getCommandValueData().toString().getBytes(), true);
    }
    
    @Override
    public void prepareDelete() {
        List<Device> devices = getHardwareDevice().getSoftwareDriver().getRunningDevices();
        List<Integer> deviceIds= new ArrayList<>();
        for(Device device: devices){
            deviceIds.add(device.getId());
        }
        for(Integer id:deviceIds){
            try {
                this.deleteDevice(id);
            } catch (PluginDeviceMutationException ex) {
                LOG.error("Could not delete device {}: {}", id,ex.getMessage());
            }
        }
    }

    @Override
    public void deviceRemoved(Device device) {
        /// not used
    }

    @Override
    public void deviceAdded(Device device) {
        /// not used
    }

    /**
     * Prepares any presentation needed to be displayed on the web.
     */
    @Override
    public void prepareWebPresentation() {
        deviceTable.clear();
        for(Device device:getHardwareDevice().getSoftwareDriver().getRunningDevices()){
            ArrayList ListHolder = new ArrayList<>();
            for(DeviceControlsGroup controlGroup:device.getFullCommandSet().getControlsGroups().values()){
                for(DeviceControl deviceControl:controlGroup.getGroupControls().values()){
                    Map<String,Object> deviceDetail = new HashMap<>();
                    deviceDetail.put("Control", deviceControl.getDescription());
                    deviceDetail.put("Topic", new StringBuilder(PUB_DEVICE_ROOT)
                                                .append(device.getId())
                                                .append("/")
                                                .append(controlGroup.getGroupId())
                                                .append("/")
                                                .append(deviceControl.getControlId()).toString());
                    deviceDetail.put("Data type", deviceControl.getDataType());
                    ListHolder.add(deviceDetail);
                }
            }
            deviceTable.put(new StringBuilder("(").append(device.getId()).append(") ").append(device.getDeviceName()).toString(), ListHolder);
        }
    }

    @Override
    public boolean hasGraphData() {
        return false;
    }
    
}