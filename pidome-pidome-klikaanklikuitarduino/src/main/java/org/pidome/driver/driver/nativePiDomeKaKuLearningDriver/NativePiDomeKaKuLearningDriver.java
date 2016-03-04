/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.driver.nativePiDomeKaKuLearningDriver;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceStructure;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDataEvent;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryServiceException;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDevice;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDeviceNotFoundException;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredItemsCollection;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralDriverDeviceMutationException;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriver;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentSimpleNVP;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentationGroup;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentAddExistingDeviceInterface;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentAddExistingDeviceRequest;

/**
 * KaKu driver.
 * @author John
 */
public class NativePiDomeKaKuLearningDriver extends PeripheralSoftwareDriver implements PeripheralSoftwareDriverInterface,WebPresentAddExistingDeviceInterface {

    WebPresentationGroup present = new WebPresentationGroup("Gateway info", "Gateway information");
    WebPresentationGroup sensors = new WebPresentationGroup("New nodes presentation", "When a new KaKu device is active, it will automatically shown below. When it is a device controlled by a remote, just press a button and it will show up.");
    
    static Logger LOG = LogManager.getLogger(NativePiDomeKaKuLearningDriver.class);
    
    boolean hardwareAvailable = false;
    
    Calendar cal = new GregorianCalendar(TimeZone.getDefault(), Locale.getDefault());
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    
    WebPresentSimpleNVP lastData     = new WebPresentSimpleNVP("Last receive time");
    WebPresentSimpleNVP lastDataSend = new WebPresentSimpleNVP("Last send time");
    
    SendQueue queue = new SendQueue();
    
    public NativePiDomeKaKuLearningDriver(){
        dateFormat.setCalendar(cal);
        lastData.setValue("00-00-0000 00:00");
        lastDataSend.setValue("00-00-0000 00:00");
        present.add(lastData);
        present.add(lastDataSend);
        this.addWebPresentationGroup(present);
        this.addWebPresentationGroup(sensors);
    }

    /**
     * Sends data
     * @param data
     * @return
     * @throws IOException 
     * @obsolete Use writeBytes
     */
    @Override 
    public boolean sendData(String data, String prefix) throws IOException {
        return this.writeBytes(data.getBytes());
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
        queue.put(data);
        queue.initialHandle();
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
        LOG.debug("Passing to hardware driver for send: {}",device.getAddress()+":" + deviceData);
        if(hardwareAvailable){
            return sendData(new StringBuilder(device.getAddress()).append(":").append(deviceData).append("\n").toString());
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
                LOG.trace("Received from hardware driver: {}", oEvent.getByteArrayOutputStream().toString().trim());
                try {
                    String[] multiline = oEvent.getStringData().trim().split("\n");
                    for(int i=0; i<multiline.length;i++){
                        String[] lineParts = multiline[i].split(":");
                        switch(lineParts[0]){
                            case "stat":
                                switch(lineParts[1]){
                                    case "begin":
                                        hardwareAvailable = true;
                                    break;
                                    case "send":
                                        queue.done();
                                    break;
                                }
                            break;
                            default:
                                handleIncoming(lineParts[0], lineParts[1], lineParts[2], lineParts[3], lineParts[4]);
                            break;
                        }
                    }
                } catch (Exception ex){
                    LOG.error("Faulty data: {} ({})", ex.getMessage(), ex.getCause());
                }
            break;
        }
    }
    
    private void handleIncoming(String address, String deviceId, String switchType, String value, String delay){
        /// Presentation shows a newly added device not yet known in the server.
        boolean exists = false;
        String addressCombined = new StringBuilder(address).append(":").append(deviceId).toString();
        for(Device device:getRunningDevices()){
            if(device.getAddress().equals(addressCombined)){
                exists = true; break;
            }
        }
        if(!exists){
            try {
                DiscoveredItemsCollection.getDiscoveredDevice(this, addressCombined);
            } catch (DiscoveredDeviceNotFoundException ex){
                DiscoveredDevice newDevice = new DiscoveredDevice(addressCombined,"KlikAanKlikUit " + (switchType.equals("2") || switchType.equals("3")?"Dimmer":"Switch/Detector"));
                newDevice.addVisualInformation("address", addressCombined);
                newDevice.addVisualInformation("signaltime", delay);
                newDevice.addParameterValue("type", switchType);
                newDevice.addParameterValue("signaltime", delay);
                try {
                    DiscoveredItemsCollection.addDiscoveredDevice(this, newDevice);
                } catch (DeviceDiscoveryServiceException ex1) {
                    LOG.warn("Could not add discovered device to discovery table: {}", ex.getMessage());
                }
            }
        } else {
            sendToDeviceByPassFilter(addressCombined, switchType, value);
        }
    }
    
    /**
     * Delivers data to the correct device.
     * This function bypasses the filer function which is going to be fased out.
     * @param deviceAddress
     * @param messageType
     * @param data 
     */
    private void sendToDeviceByPassFilter(String deviceAddress, String switchType, String data){
        LOG.trace("Passing to device node: {}, data: {}", deviceAddress, data);
        for(Device device: getRunningDevices()){
            if(device.getAddress().equals(deviceAddress)){
                ((Device)device).handleData(data, data);
            }
        }
    }

    @Override
    public void handleNewDeviceRequest(WebPresentAddExistingDeviceRequest request) throws PeripheralDriverDeviceMutationException {
        try {
            Map<String,Object> customParams = request.getCustomData();
            if(request.getDeviceId()!=0 && DiscoveredItemsCollection.hasDiscoveredDevice(this, (String)customParams.get("address"))){
                String address = (String)customParams.get("address");
                
                DeviceStructure struct = new DeviceStructure(this.getClass().getCanonicalName());
                DeviceStructure.OptionSettings settings = struct.createOptions();
                settings.addOptionValues("signaltime", (String)customParams.get("signaltime"));
                
                String localName;
                
                if(request.getName().equals("")){
                    localName = new StringBuilder(getFriendlyName()).append(" ").append((String)customParams.get("type")).toString();
                } else {
                    localName = request.getName();
                }
                this.createFromExistingDevice(request.getDeviceId(), localName, address, request.getLocationId(), request.getCategoryId(),settings.createSaveSettings());
                DiscoveredItemsCollection.removeDiscoveredDevice(this, (String)customParams.get("address"));
            }
        } catch (Exception ex){
            LOG.error("Problem creating device: {}",ex.getMessage(), ex);
            throw new PeripheralDriverDeviceMutationException("Problem creating device: "+ex.getMessage());
        }
    }

    @Override
    public boolean handleDeviceData(Device device, DeviceCommandRequest request) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void discoveryEnabled() {
        ///Not used
    }

    @Override
    public void discoveryDisabled() {
        /// Not used
    }
    
    class SendQueue {
        
        List<String> items = new ArrayList<>();
        List<String> privateQueue = Collections.synchronizedList(items);
        
        private SendQueue(){}
        
        private void put(String toSend){
            privateQueue.add(toSend);
        }
        
        private void initialHandle(){
            if(privateQueue.size()==1){
                sendDataFromQueue();
            }
        }
        
        private void done(){
            if(privateQueue.size()>0){
                privateQueue.remove(0);
            }
            if(privateQueue.size()>0){
                sendDataFromQueue();
            }
        }
        
        private void sendDataFromQueue(){
            try {
                writeBytes(privateQueue.get(0).getBytes());
                cal.setTime(new Date());
                lastDataSend.setValue(dateFormat.format(cal.getTime()));
            } catch (IOException ex) {
                LOG.error("Problem sending data, dequeueing item: {}", privateQueue.get(0));
                privateQueue.remove(0);
            }
        }
        
    }
    
}