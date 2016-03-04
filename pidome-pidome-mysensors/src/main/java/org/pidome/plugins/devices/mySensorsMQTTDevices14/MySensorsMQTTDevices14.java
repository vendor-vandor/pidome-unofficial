/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.plugins.devices.mySensorsMQTTDevices14;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.fusesource.mqtt.client.Tracer;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceNotification;
import org.pidome.server.connector.drivers.devices.PluginDeviceMutationException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceToggleControl;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryInterface;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryServiceException;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDevice;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDeviceNotFoundException;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredItemsCollection;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralDriverDeviceMutationException;
import org.pidome.server.connector.drivers.peripherals.software.TimedDiscoveryException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfiguration;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationOptionSet;
import org.pidome.server.connector.interfaces.web.configuration.WebOption;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentComplexNVP;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentListNVP;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentSimpleNVP;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentationGroup;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentAddExistingDeviceInterface;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentAddExistingDeviceRequest;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentCustomFunction;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentCustomFunctionInterface;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentCustomFunctionRequest;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.emulators.DevicePlugin;

/**
 *
 * @author John
 */
public class MySensorsMQTTDevices14 extends DevicePlugin implements DeviceDiscoveryInterface,WebPresentAddExistingDeviceInterface,WebPresentCustomFunctionInterface {

    private MQTT mqtt;
    private CallbackConnection connection;
    
    WebPresentationGroup sensors = new WebPresentationGroup("New nodes presentation", "When a new node is added it will be displayed below. You will then have the option to add the node to the server. It is required for the nodes to use presentation.");
    WebPresentComplexNVP nodesTable   = new WebPresentComplexNVP("Presentation");
    List<WebPresentSimpleNVP> newNodePresent = new ArrayList<>();
    Map<String,List<Map<String,Object>>> deviceTable = new HashMap<>(); ///Map<deviceAddr,Map<sensorgroup,sensor>>
    
    List<WebPresentSimpleNVP> errorList = new ArrayList<>();
    WebPresentListNVP    errorTable     = new WebPresentListNVP("Messages list");
    WebPresentationGroup errors         = new WebPresentationGroup("Last known messages", "Below is a list of last known 20 messages");
    
    WebPresentationGroup present      = new WebPresentationGroup("Gateway info", "Gateway information");
    WebPresentSimpleNVP  ifcon        = new WebPresentSimpleNVP("Connected");
    WebPresentSimpleNVP  lastData     = new WebPresentSimpleNVP("Last receive time");
    WebPresentSimpleNVP  lastDataSend = new WebPresentSimpleNVP("Last send time");
    
    Map<String, String> config = new HashMap<>();
    
    private String prefix = "MyMQTT";
    
    static Logger LOG = LogManager.getLogger(MySensorsMQTTDevices14.class);
    
    Calendar cal = new GregorianCalendar(TimeZone.getDefault(), Locale.getDefault());
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");  
    
    Tracer tracer = new MQTTTracer();

    @Override
    public boolean hasGraphData() {
        return false;
    }

    @Override
    public void handleDeviceData(Device device, DeviceCommandRequest dcr) {
        /// not used yet
    }

    @Override
    public void discoveryEnabled() {
        //not used
    }

    @Override
    public void discoveryDisabled() {
        //not used
    }

    private class MQTTTracer extends Tracer {
        @Override
        public void debug(String message, Object[] args) {
            LOG.debug("{}: {}", message, args);
        }
    }
    
    /**
     * Constructor.
     * Sets the configuration.
     */
    public MySensorsMQTTDevices14() {
        WebConfiguration conf = new WebConfiguration();
        WebConfigurationOptionSet optionSet = new WebConfigurationOptionSet("MySensors MQTT gateway options");
        optionSet.setConfigurationSetDescription("Please refer to the MQTT gateway information on mysensors.org about how to install the MQTT gateway");
        WebOption ipAddress = new WebOption("IPADDRESS", "Ip address", "The Ip address of the MySensors MQTT broker", WebOption.WebOptionConfigurationFieldType.STRING);
        WebOption port = new WebOption("PORT", "Port used", "The port of the MySensors MQTT broker", WebOption.WebOptionConfigurationFieldType.INT);
        WebOption prefix = new WebOption("PREF", "Prefix used", "This is the broker MQTT_BROKER_PREFIX option", WebOption.WebOptionConfigurationFieldType.STRING);
        
        port.setDefaultValue("1883");
        
        prefix.setDefaultValue("MyMQTT");
        
        optionSet.addOption(ipAddress);
        optionSet.addOption(port);
        optionSet.addOption(prefix);
        conf.addOptionSet(optionSet);
        setConfiguration(conf);
        
        dateFormat.setCalendar(cal);
        ifcon.setValue("Not connected");
        
        present.add(ifcon);
        present.add(lastData);
        present.add(lastDataSend);        
        lastData.setValue("00-00-0000 00:00");
        lastDataSend.setValue("00-00-0000 00:00");
        
        this.addWebPresentationGroup(present);
        
        nodesTable.setValue(deviceTable);
        sensors.add(nodesTable);
        
        this.addWebPresentationGroup(sensors);
        
        WebPresentCustomFunction newDeviceRequestFunction = new WebPresentCustomFunction("Clear log list");
        newDeviceRequestFunction.setIdentifier("clearLogList");

        WebPresentSimpleNVP newDeviceFunctionNVP = new WebPresentSimpleNVP("custom_driver_function");
        newDeviceFunctionNVP.setValue(newDeviceRequestFunction.getPresentationValue());
        
        errors.add(errorTable);
        errorTable.setValue(errorList);
        
        errorList.add(newDeviceFunctionNVP);

        this.addWebPresentationGroup(errors);
    }
    
    @Override
    public void setConfigurationValues(Map<String, String> conf) throws WebConfigurationException {
        config = conf;
        if((!config.containsKey("IPADDRESS") || config.get("IPADDRESS").equals("")) || (!config.containsKey("PORT") || config.get("PORT").equals(""))){
            StringBuilder string = new StringBuilder("Check your ip address and/or port. Current config: ");
            for(Map.Entry<String,String> item:conf.entrySet()){
                string.append(item.getKey()).append(": ").append(item.getValue());
            }
            LogError(string.toString());
            LOG.error("Check your ip address and/or port. Current config: {}", config);
            throw new WebConfigurationException("Check your ip address and/or port.");
        }
        if(config.containsKey("PREF") && config.get("PREF").length()>0){
            this.prefix = config.get("PREF");
        }
    }

    @Override
    public void startPlugin() throws PluginException {
        if(mqtt==null){
            mqtt = new MQTT();
            mqtt.setTracer(tracer);
            try {
                mqtt.setHost(config.get("IPADDRESS"), Integer.parseInt(config.get("PORT")));
                mqtt.setClientId("PiDome");
                mqtt.setCleanSession(true);
                mqtt.setReconnectDelay(2000);
                mqtt.setReconnectDelayMax(30000);
                mqtt.setVersion("3.1");
                connection = mqtt.callbackConnection();
                connection.listener(new MQTTListener());
                connection.connect(new MQTTConnectListener());
                setRunning(true);
            } catch (Exception ex) {
                LogError(new StringBuilder("Could not start plugin (ip: ")
                             .append(config.get("IPADDRESS"))
                             .append(", port:")
                             .append(config.get("PORT"))
                             .append("): ")
                             .append(ex.getMessage()).toString());
            }
            try {
                this.enableDiscovery(0);
            } catch (TimedDiscoveryException ex) {
                LOG.error("Could not start discovery: {}", ex.getMessage());
            }
        }
    }

    @Override
    public void stopPlugin() {
        if(connection!=null)connection.disconnect(new MQTTDisConnectListener());
        mqtt = null;
        setRunning(false);
    }
    
    @Override
    public String getExpectedDriverId() {
        return "NATIVE_MQTTMYSENSORS_DRIVER";
    }

    @Override
    public String getExpectedDriverVersion() {
        return "0.0.1";
    }

    /**
     * Logs an error.
     * @param message 
     */
    private void LogError(String message){
        WebPresentSimpleNVP error = new WebPresentSimpleNVP(dateFormat.format(cal.getTime()));
        error.setValue(message);
        errorList.add(1,error);
        if(errorList.size()>10){
            errorList.remove(10);
        }
    }
    
    /**
     * Handles data to be send.
     * The MySensors broker does not deliver guarantees. Errors regarding this are still logged though.
     * @param device
     * @param group
     * @param control
     * @param data 
     */
    @Override
    public void handleDeviceData(final Device device, final String group, final String control, final byte[] data, boolean userIntent) {
        if(getRunning()){
            final String path = new StringBuilder(prefix).append("/").append(device.getAddress()).append("/").append(group).append("/").append(control).toString();
            connection.getDispatchQueue().execute(() -> {
                connection.publish(path, data, QoS.AT_MOST_ONCE, false, new Callback<Void>() {
                    @Override
                    public void onSuccess(Object v) {
                      updateSendData(device);
                      LOG.trace("Succesfully published: {} to {}", data,path);
                    }
                    @Override
                    public void onFailure(Throwable ex) {
                        updateSendData(device);
                        LOG.error("Problem publishing to {}: ", path,ex.getMessage(), ex);
                    }
                });
            });
        } else {
            LOG.error("Plugin not running, no published {} from device {} to broker", data, device.getDeviceName());
        }      
    }
    
    /**
     * Updates the send data.
     * @param device 
     */
    private void updateSendData(Device device){
        cal.setTime(new Date());
        lastDataSend.setValue(dateFormat.format(cal.getTime()));
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

    /**
     * Notification when a device is removed.
     * Not used.
     * @param device 
     */
    @Override
    public void deviceRemoved(Device device) {
        /// Notif
    }

    /**
     * Notification when a device is added.
     * Not used.
     * @param device 
     */
    @Override
    public void deviceAdded(Device device) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Handle a request to add a new device.
     * Used in combination with web presentations.
     * @param request 
     * @throws org.pidome.server.connector.drivers.peripherals.software.PeripheralDriverDeviceMutationException 
     */
    @Override
    public void handleNewDeviceRequest(WebPresentAddExistingDeviceRequest request) throws PeripheralDriverDeviceMutationException {
        try {
            Map<String,Object> customData = request.getCustomData();
            LOG.info("Creating a new MySensors MQTT device: {}", customData);
            if(request.getDeviceId()!=0 && DiscoveredItemsCollection.hasDiscoveredDevice((WebPresentAddExistingDeviceInterface)this.getSoftwareDriverLink(), (String)customData.get("address"))){
                String localName = "";
                if(!request.getName().equals("")){
                    localName = request.getName();
                } else {
                    DiscoveredDevice gotDevice = DiscoveredItemsCollection.getDiscoveredDevice((WebPresentAddExistingDeviceInterface)this.getSoftwareDriverLink(), (String)customData.get("address"));
                    gotDevice.getParameterValues();
                    if(gotDevice.getParameterValues().containsKey("V_SKETCH_NAME")){
                        localName = (String)gotDevice.getParameterValues().get("V_SKETCH_NAME");
                    } else {
                        localName = "Nameless device";
                    }
                }
                this.createFromExistingDevice(request.getDeviceId(), localName, (String)customData.get("address"), request.getLocationId(), request.getCategoryId());
                DiscoveredItemsCollection.removeDiscoveredDevice((WebPresentAddExistingDeviceInterface)this.getSoftwareDriverLink(), (String)customData.get("address"));
            } else {
                throw new PeripheralDriverDeviceMutationException("No valid device found to add: " + request.getDeviceId());
            }
        } catch (DiscoveredDeviceNotFoundException | PluginDeviceMutationException ex){
            LOG.error("Problem creating device: {}",ex.getMessage(), ex);
            throw new PeripheralDriverDeviceMutationException("Problem creating device: "+ex.getMessage());
        }
    }

    /**
     * Handles a custom function request.
     * @param request
     * @throws Exception 
     */
    @Override
    public void handleCustomFunctionRequest(WebPresentCustomFunctionRequest request) throws Exception {
        switch(request.getIdentifier()){
            case "clearLogList":
                if(errorList.size()>1){
                    errorList.removeAll(errorList.subList(1,errorList.size()));
                }
            break;
        }
    }
    
    /**
     * Creates a discovered device.
     * @param address
     * @param type
     * @param value 
     */
    private void createDiscoveredDevice(String address, String type, String value){
        DiscoveredDevice newDevice = new DiscoveredDevice(address,"MySensors Device via MQTT");
        newDevice.addVisualInformation(type, value);
        try {
            DiscoveredItemsCollection.addDiscoveredDevice((WebPresentAddExistingDeviceInterface)this.getSoftwareDriverLink(), newDevice);
        } catch (DeviceDiscoveryServiceException ex) {
            LOG.warn("Could not add device to discovery table: {}", ex.getMessage());
        }
    }
    
    /**
     * Adds data to a discovered device.
     * @param address
     * @param type
     * @param value
     * @throws DiscoveredDeviceNotFoundException 
     */
    private void appendToDiscovereDevice(String address, String type, String value) throws DiscoveredDeviceNotFoundException {
        DiscoveredItemsCollection.getDiscoveredDevice((WebPresentAddExistingDeviceInterface)this.getSoftwareDriverLink(), address).addVisualInformation(type, value);
    }
    
    @Override
    public void prepareWebPresentation() {
        /// not used.
    }
    
    class MQTTListener implements Listener {

        @Override
        public void onConnected() {
            //// Not used.
        }

        @Override
        public void onDisconnected() {
            //// Not used.
        }

        @Override
        public void onPublish(final UTF8Buffer utfb, final Buffer buffer, final Runnable r) {
            byte[] data = utfb.getData();
            LOG.trace("Got data from broker: {} - {}", utfb.toString(), buffer.toString());
            Runnable run = () -> {
                cal.setTime(new Date());
                lastData.setValue(dateFormat.format(cal.getTime()));
                //// "MyMQTT/[0-255]/[0-255]/V_[STRING]" (Expected)
                String[] path = utfb.toString().split("/");
                try {
                    boolean hasDevice = false;
                    String address = path[1];
                    String sensorId = path[2];
                    String v_type = path[3];
                    for(Device device:getHardwareDevice().getSoftwareDriver().getRunningDevices()){
                        if(device.getAddress().equals(address)){
                            hasDevice = true;
                            if(!sensorId.equals("255")){
                                DeviceControl control = device.getFullCommandSet().getControlsGroup(sensorId).getDeviceControl(path[3]);
                                DeviceNotification notification = new DeviceNotification();
                                switch(control.getControlType()){
                                    case TOGGLE:
                                        if(buffer.ascii().toString().equals(((DeviceToggleControl)control).getOnValue().toString())){
                                            notification.addData(sensorId, v_type, true);
                                        } else {
                                            notification.addData(sensorId, v_type, false);
                                        }
                                    break;
                                    default:
                                        switch(control.getDataType()){
                                            case STRING:
                                            case HEX:
                                                notification.addData(sensorId, v_type, buffer.ascii().toString());
                                            break;
                                            case INTEGER:
                                                notification.addData(sensorId, v_type, Integer.valueOf(buffer.ascii().toString()));
                                            break;
                                            case FLOAT:
                                                notification.addData(sensorId, v_type, Float.valueOf(buffer.ascii().toString()));
                                            break;
                                            case BOOLEAN:
                                                notification.addData(sensorId, v_type, buffer.ascii().toString().equals("1"));
                                            break;
                                        }
                                    break;
                                }
                                device.dispatchToHost(notification);
                            } else if (sensorId.equals("255") && path[3].equals("V_")){
                                /// lets assume this is battery level?????? Implementation seems broken
                                DeviceControl control = device.getFullCommandSet().getControlsGroup(sensorId).getDeviceControl(path[3]);
                                DeviceNotification notification = new DeviceNotification();
                                switch(control.getDataType()){
                                    case INTEGER:
                                        notification.addData(sensorId, v_type, Integer.valueOf(buffer.ascii().toString()));
                                    break;
                                    case FLOAT:
                                        notification.addData(sensorId, v_type, Float.valueOf(buffer.ascii().toString()));
                                    break;
                                    default:
                                        notification.addData(sensorId, v_type, buffer.ascii().toString());
                                    break;
                                }
                                device.dispatchToHost(notification);
                            }
                        }
                    }
                    if(hasDevice==false && !sensorId.equals("255")){
                        try {
                            appendToDiscovereDevice(address, v_type, buffer.ascii().toString());
                        } catch (DiscoveredDeviceNotFoundException ex){
                            /// Not found, so make a new one.
                            createDiscoveredDevice(address, v_type, buffer.ascii().toString());
                            LOG.warn("Node id {} not found in server, Please check the discovered devices page to add this node.", address);
                            LogError("Node id "+address+" not found in server, check in discovery list list to add, if not present restart node for presentation.");
                        }
                    } else if(hasDevice==false) {
                        LOG.warn("Node id {}  not found in server, also not able to add it to the discovered devices list, invalid address.", address);
                        LogError("Node id "+address+" not found in server, also not able to add it to the discovered devices list, invalid address.");
                    }
                } catch (Exception ex){
                    LOG.error("Can not work with/handle the following data '{}', '{}'. Reason: {}", path, buffer, ex.getMessage(), ex);
                }
            };
            run.run();
        }

        @Override
        public void onFailure(Throwable thrwbl) {
            LOG.error("An MQTT publish message failed: {} ({}:{})", thrwbl.getMessage(), config.get("IPADDRESS"), config.get("PORT"));
        }
        
    }
    
    /**
     * Actions to be taken for the connection listener.
     */
    class MQTTConnectListener implements Callback<Void> {

        @Override
        public void onSuccess(Object t) {

            String stringOk = new StringBuilder("Yes, with ")
                                        .append(config.get("IPADDRESS"))
                                        .append(":")
                                        .append(config.get("PORT"))
                                        .append(" at topic: ")
                                        .append(prefix)
                                        .append("/# (without subscribe promise)")
                                        .toString();
            
            ifcon.setValue(stringOk);
            LogError(stringOk);
            LOG.debug(stringOk);
            
            Topic[] topics = {new Topic(prefix+"/#", QoS.AT_MOST_ONCE)};
            connection.subscribe(topics, new Callback<byte[]>() {
                public void onSuccess(byte[] qoses) {
                    
                    ifcon.setValue("Yes, with " + config.get("IPADDRESS") +":"+ (config.get("PORT") +" at topic: " + prefix +"/#"));
                    
                    LogError("Connected to " + config.get("IPADDRESS") +":"+ (config.get("PORT") +" at topic: " + prefix +"/#"));
                    
                    cal.setTime(new Date());
                    lastDataSend.setValue(dateFormat.format(cal.getTime()));
                    
                    LOG.trace("Subscribed: {}", new String(qoses));
                }
                @Override
                public void onFailure(Throwable value) {
                    ifcon.setValue("Not connected");
                    LogError("Not connected: " + value.getMessage());
                    LOG.trace("Not subscribed: {}", value.getMessage());
                }

                @Override
                public void onSuccess(Object o) {
                    LOG.trace("Subscribed: {}", o.toString());
                }
            });

        }

        @Override
        public void onFailure(Throwable thrwbl) {
            LOG.debug("Disconnected from or connect failure with: {}:{} - ", config.get("IPADDRESS"), config.get("PORT"), thrwbl.getMessage());
            LogError("Disconnected from or connect failure with: "+config.get("IPADDRESS")+":"+config.get("PORT")+" - " + thrwbl.getMessage());
        }
        
    }
    
    /**
     * Actions to be taken for the connection listener.
     */
    class MQTTDisConnectListener implements Callback<Void> {

        @Override
        public void onSuccess(Object t) {
            LOG.debug("Disconnected with: {}:{}", config.get("IPADDRESS"), config.get("PORT"));
            LogError("Disconnected with: " + config.get("IPADDRESS") +":"+ config.get("PORT"));
        }

        @Override
        public void onFailure(Throwable thrwbl) {
            /// disconnect should not fail
            LOG.error("Failed to disconnect from: {}:{}", config.get("IPADDRESS"), config.get("PORT"));
        }
        
    }
    
}
