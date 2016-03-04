/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.driver.nativeMySensorsDriver14;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.driver.device.pidomeNativeMySensorsDevice14.PidomeNativeMySensorsDevice14;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDataEvent;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralDriverDeviceMutationException;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriver;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentListNVP;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentSimpleNVP;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentationGroup;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentAddExistingDeviceRequest;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentCustomFunction;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentCustomFunctionInterface;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentCustomFunctionRequest;
import org.pidome.mysensors.PidomeNativeMySensorsDeviceResources14;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryInterface;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryServiceException;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDevice;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDeviceNotFoundException;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredItemsCollection;
import org.pidome.server.connector.drivers.peripherals.software.TimedDiscoveryException;

/**
 * MySensors driver.
 * Message structure: radioId;childId;messageType;ack;subType;payload\n
 * @author John
 */
public class NativeMySensorsDriver14 extends PeripheralSoftwareDriver implements PeripheralSoftwareDriverInterface,DeviceDiscoveryInterface,WebPresentCustomFunctionInterface {

    WebPresentationGroup present = new WebPresentationGroup("Gateway info", "Gateway information");
    WebPresentationGroup errors  = new WebPresentationGroup("Last known messages", "Below is a list of last known 64 messages");
    
    static Logger LOG = LogManager.getLogger(NativeMySensorsDriver14.class);
    
    private final String PRESENTATION = "0";
    private final String SET_VARIABLE = "1"; 
    private final String REQ_VARIABLE = "2"; 
    private final String INTERNAL     = "3";
    private final String STREAM       = "4";
    
    private final String I_BATTERY_LEVEL  = "0";
    private final String I_TIME           = "1";
    private final String I_VERSION        = "2";
    private final String I_REQUEST_ID     = "3";
    private final String I_RESPONSE_ID    = "4";
    private final String I_INCLUSION_MODE = "5";
    private final String I_CONFIG         = "6";
    private final String I_PING           = "7";
    private final String I_PING_ACK       = "8";
    private final String I_LOG_MESSAGE    = "9";
    private final String I_CHILDREN       = "10";
    private final String I_SKETCH_NAME    = "11";
    private final String I_SKETCH_VERSION = "12";
    private final String I_REBOOT         = "13";
    private final String I_GATEWAY_READY  = "14";
    
    PidomeNativeMySensorsDeviceResources14 resources = new PidomeNativeMySensorsDeviceResources14();
    
    boolean hardwareAvailable = true;
    
    Calendar cal = new GregorianCalendar(TimeZone.getDefault(), Locale.getDefault());
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    
    WebPresentSimpleNVP  version      = new WebPresentSimpleNVP("Version");
    WebPresentSimpleNVP  lastData     = new WebPresentSimpleNVP("Last receive time");
    WebPresentSimpleNVP  lastDataSend = new WebPresentSimpleNVP("Last send time");
    WebPresentListNVP    errorTable   = new WebPresentListNVP("Messages list");
    
    List<WebPresentSimpleNVP> errorList      = new ArrayList<>();
    
    private final ExecutorService writerExecutor = Executors.newFixedThreadPool(1);
    
    private StringBuilder workData = new StringBuilder();
    
    public NativeMySensorsDriver14(){
        dateFormat.setCalendar(cal);
        version.setValue("Waiting for gateway");
        
        present.add(version);
        present.add(lastData);
        present.add(lastDataSend);
        lastData.setValue("00-00-0000 00:00");
        lastDataSend.setValue("00-00-0000 00:00");
        
        errorTable.setValue(errorList);
        errors.add(errorTable);
        
        this.addWebPresentationGroup(present);
        this.addWebPresentationGroup(errors);
        
        
        WebPresentCustomFunction newDeviceRequestFunction = new WebPresentCustomFunction("Clear log list");
        newDeviceRequestFunction.setIdentifier("clearLogList");

        WebPresentSimpleNVP newDeviceFunctionNVP = new WebPresentSimpleNVP("custom_driver_function");
        newDeviceFunctionNVP.setValue(newDeviceRequestFunction.getPresentationValue());
        
        errorList.add(newDeviceFunctionNVP);
        
    }

    /**
     * Sends data
     * @param data
     * @return
     * @throws IOException 
     * @obsolete Use writeBytes
     */
    @Override 
    public boolean sendData(final String data, String prefix) throws IOException {
        return this.sendData(data);
    }

    /**
     * Sends data
     * @param data
     * @return
     * @throws IOException 
     * @obsolete Use writeBytes
     */
    @Override
    public boolean sendData(String data) throws IOException {
        writerExecutor.submit(() -> {
            try {
                boolean sendResult = this.writeBytes(data.getBytes());
                if(sendResult) {
                    cal.setTime(new Date());
                    lastDataSend.setValue(dateFormat.format(cal.getTime()));
                }
            } catch (IOException ex) {
                LOG.error("Could not pass data to hardware driver: {}", ex.getMessage());
            }
        });
        return true;
    }

    /**
     * Handles data being send by an device.
     * @param device
     * @param group
     * @param set
     * @param deviceData
     * @return
     * @throws IOException 
     */
    @Override
    public boolean handleDeviceData(Device device, String group, String set, String deviceData) throws IOException {
        String data;
        if(group.equals("255;3")){ /// We want to send an internal message from a device.
            data = device.getAddress()+";" + group + ";0;" + set + ";" + deviceData + "\n";
        } else {
            data = device.getAddress()+";" + group + ";" + SET_VARIABLE + ";0;" + set + ";" + deviceData + "\n";
        }
        LOG.debug("Passing to hardware driver for send: {}",data);
        if(hardwareAvailable){
            return sendData(data);
        } else {
            return false;
        }
    }
    
    /**
     * first entry in data receiving.
     * @param oEvent 
     */
    @Override
    public void driverBaseDataReceived(PeripheralHardwareDataEvent oEvent) {
        switch(oEvent.getEventType()){
            case PeripheralHardwareDataEvent.DATA_RECEIVED:
                cal.setTime(new Date());
                lastData.setValue(dateFormat.format(cal.getTime()));
                String localWork = oEvent.getByteArrayOutputStream().toString();
                workData.append(localWork);
                if(workData.toString().endsWith("\n")){
                    try {
                        String[] multiline = workData.toString().split("\n");
                        workData.setLength(0);
                        for(int i=0; i<multiline.length;i++){
                            try {
                                if(!multiline[i].trim().equals("")){
                                    String[] lineParts = multiline[i].split(";");
                                    if(lineParts[0].equals("0") || lineParts[0].equals("00")){
                                        /// Someone has debug turned on....
                                        LogError(multiline[i].trim());
                                    } else {
                                        switch(lineParts[2]){
                                            case INTERNAL:
                                                if(lineParts.length==5){
                                                    handleInternalMessage(lineParts[0], lineParts[1] ,lineParts[4], "",multiline[i].trim(), lineParts[3]); ///// Gateway message.                                                        
                                                } else {
                                                    handleInternalMessage(lineParts[0], lineParts[1] ,lineParts[4], lineParts[5],multiline[i].trim(), "");
                                                }
                                            break;
                                            case PRESENTATION:
                                                handlePresentation(lineParts[0], lineParts[1], resources.getSensorType(Integer.parseInt(lineParts[4])),lineParts[5]);
                                            break;
                                            case SET_VARIABLE:
                                            case REQ_VARIABLE:
                                                sendToDeviceByPassFilter(lineParts[0], lineParts[2], multiline[i].trim());
                                            break;
                                        }
                                    }
                                }
                            } catch(Exception ex){
                                LOG.error("Got some incorrect data. Full string: {} ({}, {})", multiline[i], ex.getMessage(), ex.getCause(), ex);
                            }
                        }
                    } catch (Exception ex){
                        LOG.error("Faulty data or method handling: {} ({})", ex.getMessage(), oEvent.getStringData(), ex);
                    }
                }
            break;
        }
    }
    
    /**
     * Handles presentation data.
     * @param nodeId
     * @param sensorId
     * @param sensorType 
     */
    private void handlePresentation(String nodeId, String sensorId, String sensorType, String payload){
        /// Presentation shows a newly added device not yet known in the server.
        boolean exists = false;
        if(!nodeId.equals("255")){
            for(Device device:getRunningDevices()){
                if(device.getAddress().equals(nodeId)){
                    exists = true; break;
                }
            }
        } 
        if(DiscoveredItemsCollection.discoveryEnabled(this) && (!exists || (nodeId.equals("255") && sensorId.equals("255")))){
            if(!sensorId.equals("255")){
                try {
                    DiscoveredItemsCollection.getDiscoveredDevice(this, nodeId).addVisualInformation(sensorId, sensorType);
                } catch (DiscoveredDeviceNotFoundException ex){
                    /// Not found.
                    DiscoveredDevice newDevice = new DiscoveredDevice(nodeId,"MySensors Device node presentation via Serial");
                    StringBuilder sensorBuilderType = new StringBuilder(sensorType);
                    if(!payload.equals("")){
                        sensorBuilderType.append(" (").append(payload).append(")");
                    }
                    newDevice.addVisualInformation(sensorId, sensorBuilderType.toString());
                    try {
                        DiscoveredItemsCollection.addDiscoveredDevice(this, newDevice);
                    } catch (DeviceDiscoveryServiceException ex1) {
                        LOG.warn("Could not add device to discovery table: {}", ex1.getMessage());
                    }
                }
            } else if(nodeId.equals("255")){
                if(!DiscoveredItemsCollection.hasDiscoveredDevice(this, "255")){
                    DiscoveredDevice newDevice = new DiscoveredDevice(nodeId,"MySensors Device node id (address request) via Serial");
                    try {
                        String nextAddress = getFreeAddress();
                        newDevice.setNewAddress(nextAddress);
                        newDevice.addVisualInformation("Possible new address", nextAddress);
                        newDevice.setFunctionType(DiscoveredDevice.FUNCTION_TYPE.FUNCTION_REQUEST_ADDRESS);
                        newDevice.setDescription("Make sure you have added all the fixed nodes first, otherwise it is possible the address assignments get's messed up. Below is the possible free address if not occupied before adding. If it is occupied the driver searches for the next first free address. After the request for address assignment has been done, the server will send a reboot command to the node. The node should re-appear with the new address within a couple of seconds. If it does not re-appear you must restart the node so it appears in the discovery again. Then, you can add it to the server.");
                    } catch (Exception ex){
                        newDevice.setNewAddress(nodeId);
                        newDevice.addVisualInformation("Possible new address", "No new free address found!");
                        newDevice.setFunctionType(DiscoveredDevice.FUNCTION_TYPE.FUNCTION_REQUEST_ADDRESS);
                        newDevice.setDescription("Adding this device will not work as the driver did not found a free address to assign to this node. If you are sure you have a free address, fill in the address below, set the address, and restart your node to add it to the server. Make sure you do NOT use address 255 as this is the broadcast address!");
                    }
                    try {
                        DiscoveredItemsCollection.addDiscoveredDevice(this, newDevice);
                    } catch (DeviceDiscoveryServiceException ex1) {
                        LOG.warn("Could not add device to discovery table: {}", ex1.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Returns a free address to be used.
     * @return
     * @throws Exception 
     */
    private synchronized String getFreeAddress() throws Exception {
        for(int i=1; i<254;i++){
            boolean hasAddress = false;
            for(Device device:getRunningDevices()){
                if(device.getAddress().equals(String.valueOf(i))){
                    hasAddress = true;
                    break;
                }
            }
            if(!hasAddress) return String.valueOf(i);
        }
        throw new Exception("There are no free addresses left");
    }
    
    /**
     * Logs an error.
     * @param message 
     */
    private void LogError(String message){
        cal.setTime(new Date());
        WebPresentSimpleNVP error = new WebPresentSimpleNVP(dateFormat.format(cal.getTime()));
        error.setValue(message);
        errorList.add(1,error);
        if(errorList.size()>63){
            errorList.removeAll(errorList.subList(63,errorList.size()));
        }
    }
    
    /**
     * Handles internal messages.
     * @param id
     * @param childId
     * @param subType
     * @param payload
     * @param fullMessage 
     */
    private void handleInternalMessage(String id, String childId, String subType, String payload, String fullMessage, String gatewayRequest){
        LOG.debug("Handling internal: {};{};INTERNAL;{};{}", id, childId, subType, payload);
        switch(gatewayRequest){
            case I_LOG_MESSAGE:
                LOG.debug("Log request: {}", fullMessage);
                LogError(fullMessage);
            break;
            default:
                switch(subType){
                    case I_CONFIG:
                        try {
                            sendInternalMessage("0","0",I_CONFIG,"M");
                        } catch (IOException ex) {
                            LOG.error("Could not send Metric configuration parameter");
                        }
                    break;
                    case I_PING:
                        try {
                            sendInternalMessage("0","0",I_PING_ACK,"");
                        } catch (IOException ex) {
                            LOG.error("Could not send Ping ack");
                        }
                    break;
                    case I_BATTERY_LEVEL:
                        sendToDeviceByPassFilter(id, "I_BATTERY_LEVEL", payload);
                    break;
                    case I_LOG_MESSAGE:
                        LOG.debug("Log request: {}", fullMessage);
                        LogError(fullMessage);
                    break;
                    case I_GATEWAY_READY:
                        hardwareAvailable = true;
                        try {
                            version.setValue("Gateway ready, version unknown, cross fingers!");
                            sendInternalMessage("0","0",I_VERSION,"GV");
                        } catch (IOException ex) {
                            LOG.error("Could not send version request to device: {}",ex.getMessage());
                        }
                    break;
                    case I_VERSION:
                        version.setValue(payload);
                    break;
                    case I_TIME:
                        cal.setTime(new Date());
                        String sendValue = String.valueOf(Math.round((cal.getTimeInMillis()+TimeZone.getDefault().getOffset(cal.getTimeInMillis()))/1000));
                        try {
                            sendInternalMessage(id,childId,I_TIME,sendValue);
                        } catch (IOException ex) {
                            LOG.error("Could not send to device: {}",ex.getMessage());
                        }
                    break;
                    case I_INCLUSION_MODE:
                        try {
                            switch(payload){
                                case "1":
                                    this.enableDiscovery(-1);
                                break;
                                default:
                                    this.disableDiscovery();
                                break;
                            }
                        } catch (TimedDiscoveryException ex) {
                            LOG.error("Could not enable inclusion mode: {}", ex.getMessage());
                        }
                    break;
                    case I_SKETCH_NAME:
                        handlePresentation(id, "SKETCH_NAME", payload,"");
                        sendToDeviceByPassFilter(id, "I_SKETCH_NAME", payload);
                    break;
                    case I_SKETCH_VERSION:
                        handlePresentation(id, "SKETCH_VERSION", payload,"");
                        sendToDeviceByPassFilter(id, "I_SKETCH_VERSION", payload);
                    break;
                    case I_REQUEST_ID:
                        handlePresentation("255", "255", "New node request","");
                    break;
                }
            break;
        }

    }
    
    /**
     * Sends an internal message.
     * @param to
     * @param childId
     * @param type
     * @param payload
     * @throws IOException 
     */
    private void sendInternalMessage(String to, String childId, String type, String payload) throws IOException {
        StringBuilder string = new StringBuilder(to);
        string.append(";").append(childId).append(";").append(INTERNAL).append(";0;").append(type).append(";").append(payload).append("\n");
        LOG.debug("Sending internal message to device: '{}'",string.toString().trim());
        sendData(string.toString());
    }
    
    /**
     * Delivers data to the correct device.
     * This function bypasses the filer function which is going to be fased out.
     * @param deviceAddress
     * @param messageType
     * @param data 
     */
    private void sendToDeviceByPassFilter(String deviceAddress, String messageType, String data){
        LOG.trace("Passing to device node: {}, message type: {}, data: {}", deviceAddress, messageType, data);
        for(Device device: getRunningDevices()){
            if(device.getAddress().equals(deviceAddress)){
                switch(messageType){
                    case "I_SKETCH_VERSION":
                        ((PidomeNativeMySensorsDevice14)device).handleSpecialData(I_SKETCH_VERSION, data);
                    break;
                    case "I_SKETCH_NAME":
                        ((PidomeNativeMySensorsDevice14)device).handleSpecialData(I_SKETCH_NAME, data);
                    break;
                    case "I_BATTERY_LEVEL":
                        ((PidomeNativeMySensorsDevice14)device).handleSpecialData(I_BATTERY_LEVEL, data);
                    break;
                    default:
                        ((Device)device).passToDevice(data, data);
                    break;
                }
            }
        }
    }

    /**
     * Handles a new device request.
     * @param request
     * @throws PeripheralDriverDeviceMutationException 
     */
    @Override
    public void handleNewDeviceRequest(WebPresentAddExistingDeviceRequest request) throws PeripheralDriverDeviceMutationException {
        try {
            Map<String,Object> customData = request.getCustomData();
            
            if(request.getRequestFunctionType() == DiscoveredDevice.FUNCTION_TYPE.FUNCTION_REQUEST_ADDRESS && DiscoveredItemsCollection.hasDiscoveredDevice(this, "255")){
                if(customData.containsKey("address") && !customData.get("address").equals("255")){
                    String newAddr = getFreeAddress();
                    try {
                        LogError(new StringBuilder("Trying assigning address '").append(newAddr).append("' to a new node").toString());
                        sendInternalMessage("255","255",I_RESPONSE_ID,newAddr);
                        DiscoveredItemsCollection.removeDiscoveredDevice(this, "255");
                        sendInternalMessage(newAddr,"255",I_REBOOT,"1");
                    } catch (IOException ex) {
                        LogError(new StringBuilder("Could not assign address '").append(newAddr).append("' to a new node: ").append(ex.getMessage()).toString());
                        throw new Exception(new StringBuilder("Could not assign address '").append(newAddr).append("' to a new node: ").append(ex.getMessage()).toString());
                    }
                } else {
                    if(customData.containsKey("address")){
                        LOG.error("Address 255 used which is invalid, node not added!");
                        LogError("Address 255 used which is invalid, node not added!");
                    } else {
                        LOG.error("Address missing in request, contact auhor/PiDome");
                        LogError("Address missing in request, contact auhor/PiDome");
                    }
                }
            } else {            
                if(request.getDeviceId()!=0 && DiscoveredItemsCollection.hasDiscoveredDevice(this, (String)customData.get("address"))){
                    String localName;
                    if(!request.getName().equals("")){
                        localName = request.getName();
                    } else {
                        DiscoveredDevice gotDevice = DiscoveredItemsCollection.getDiscoveredDevice(this, (String)customData.get("address"));
                        gotDevice.getParameterValues();
                        if(gotDevice.getParameterValues().containsKey("SKETCH_NAME")){
                            localName = (String)gotDevice.getParameterValues().get("SKETCH_NAME");
                        } else {
                            localName = "Nameless device";
                        }
                    }
                    this.createFromExistingDevice(request.getDeviceId(), localName, (String)customData.get("address"), request.getLocationId(), request.getCategoryId());
                    DiscoveredItemsCollection.removeDiscoveredDevice(this, (String)customData.get("address"));
                }
            }
        } catch (Exception ex){
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

    @Override
    public void discoveryEnabled() {
        ///Not used
    }

    @Override
    public void discoveryDisabled() {
        // not used
    }
    
}