/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativeRFXComDriver;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.driver.device.rfxcom.RFXComDevice;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.PacketProtocolException;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.PacketProtocolBase;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComLighting1.RFXComLighting1Handler;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComLighting2.RFXComLighting2Handler;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComLighting5.RFXComLighting5Handler;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComOregon.RFXComOregon50Handler;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComOregon.RFXComOregon51Handler;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComOregon.RFXComOregon52Handler;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComOregon.RFXComOregon54Handler;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComOregon.RFXComOregon55Handler;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComOregon.RFXComOregon56Handler;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceStructure;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDataEvent;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryInterface;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredItemsCollection;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralDriverDeviceMutationException;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriver;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentSimpleNVP;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentationGroup;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentAddExistingDeviceRequest;

/**
 * Driver for RFXCom.
 * @author John
 */
public class NativeRFXComDriver extends PeripheralSoftwareDriver implements PeripheralSoftwareDriverInterface,DeviceDiscoveryInterface {

    Logger LOG = LogManager.getLogger(NativeRFXComDriver.class);
    
    WebPresentationGroup present = new WebPresentationGroup("Gateway info", "Gateway information");
    WebPresentationGroup sensors = new WebPresentationGroup("New discovered devices", "This has been moved to the Device Discovery page. Select the RFXCom driver and press Enable discovery.");
    
    Calendar cal = new GregorianCalendar(TimeZone.getDefault(), Locale.getDefault());
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    
    WebPresentSimpleNVP lastData     = new WebPresentSimpleNVP("Last receive time");
    WebPresentSimpleNVP lastDataSend = new WebPresentSimpleNVP("Last send time");
    
    private final ExecutorService dataSendExecutor = Executors.newFixedThreadPool(1);
    
    private boolean rfxComReady = false;
    
    public NativeRFXComDriver(){
        dateFormat.setCalendar(cal);
        lastData.setValue("00-00-0000 00:00");
        lastDataSend.setValue("00-00-0000 00:00");
        present.add(lastData);
        present.add(lastDataSend);
        this.addWebPresentationGroup(present);
        this.addWebPresentationGroup(sensors);
    }
    
    @Override
    public boolean sendData(String data, String prefix) throws IOException {
        ///Not used yet
        return false;
    }

    @Override
    public boolean sendData(String data) throws IOException {
        return false;
    }

    @Override
    public final void driverStart(){
        try {
            LOG.info("Sending RFXCom reset");
            this.writeBytes(getResetCommand());
            try {
                ///Wait at 500 ms for second command.
                Thread.sleep(500);
                try {
                    LOG.info("Sending status request");
                    this.writeBytes(getStatusRequestCommand());
                } catch (IOException ex){
                    LOG.error("Could not correctly send status request, please restart driver");
                }
            } catch (InterruptedException ex) {
                LOG.error("Please restart driver as it did not correctly waited during initialization");
            }
        } catch (IOException ex) {
            LOG.error("Could not send RFX stop command for initialization");
        }
    }
    
    @Override
    public boolean handleDeviceData(Device device, DeviceCommandRequest request) throws IOException {
        RFXComDevice deviceHandle = (RFXComDevice)device;
        PacketProtocolBase handler = null;
        try {
            switch(deviceHandle.getPacketType()){
                case LIGHTING1:
                    handler = new RFXComLighting1Handler(this,deviceHandle,request);
                break;
                case LIGHTING2:
                    handler = new RFXComLighting2Handler(this,deviceHandle,request);
                break;
                case LIGHTING5:
                    handler = new RFXComLighting5Handler(this,deviceHandle,request);
                break;
            }
        } catch (Exception ex){
            LOG.error("Supplied device control problem: {}", ex.getMessage());
            return false;
        }
        if(handler!=null){
            try {
                final byte[] toSend = handler.getSend();
                dataSendExecutor.submit(() -> {
                    try {
                        LOG.debug("RFXCom debug send: {}", RFXComBasicPacketParser.decodeAllBytes(toSend));
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException ex) {
                            LOG.warn("Could not execute the 50 miliseconds throttling. ({})", ex.getMessage());
                        }
                        if(this.writeBytes(toSend)){
                            cal.setTime(new Date());
                            lastDataSend.setValue(dateFormat.format(cal.getTime()));
                        }
                    } catch (IOException ex) {
                        LOG.error("Could not send data: {}", ex.getMessage());
                    }
                });
                return true;
            } catch (PacketProtocolException ex) {
                LOG.error("Protocol error: {}", ex.getMessage());
                return false;
            }
        }
        return false;
    }
    
    
    /**
     * Handle raw device data.
     * @param oEvent 
     */
    @Override
    public void driverBaseDataReceived(PeripheralHardwareDataEvent oEvent){
        try {
            LOG.debug("Got event from RFXCom device: {}", oEvent.getEventType());
            switch(oEvent.getEventType()){
                case "DATA_RECEIVED":
                    Runnable run = () -> {
                        cal.setTime(new Date());
                        this.lastData.setValue(dateFormat.format(cal.getTime()));
                        byte[] bytes = oEvent.getByteArrayOutputStream().toByteArray();
                        int start  = 0;
                        LOG.debug("RFXCom debug receive: {}", RFXComBasicPacketParser.decodeAllBytes(bytes));
                        try {
                            int length = (bytes[start] & 0xFF)+1; ///Get the bytes body length + the body length byte(+1).
                            LOG.trace("Going to handle parth with start: {}, length: {}", start, length);
                            byte[] part = Arrays.copyOfRange(bytes, start, start+length);
                            start = start+length;
                            LOG.debug("RFXCom part handle: {} (length: {})", RFXComBasicPacketParser.decodeAllBytes(part), part.length);
                            RFXComBasicPacketParser parser = new RFXComBasicPacketParser(part);
                            try {
                                parser.decode();
                                PacketProtocolBase handler;
                                try {
                                    switch(parser.getPacketType()){
                                        case RFXCOM_IFACE:
                                            handler = new RFXComProtoIfaceHandler(this,parser);
                                        break;
                                        case RFXCOM_TRANS_MSG:
                                        case RFXCOM_CTRL:
                                            handler = new RFXComProtoDummyHandler(this,parser);
                                        break;
                                        case LIGHTING1:
                                            handler = new RFXComLighting1Handler(this,parser);
                                        break;
                                        case LIGHTING2:
                                            handler = new RFXComLighting2Handler(this,parser);
                                        break;
                                        case LIGHTING5:
                                            handler = new RFXComLighting5Handler(this,parser);
                                        break;
                                        case OREGON50:
                                            handler = new RFXComOregon50Handler(this,parser);
                                        break;
                                        case OREGON51:
                                            handler = new RFXComOregon51Handler(this,parser);
                                        break;
                                        case OREGON52:
                                            handler = new RFXComOregon52Handler(this,parser);
                                        break;
                                        case OREGON54:
                                            handler = new RFXComOregon54Handler(this,parser);
                                        break;
                                        case OREGON55:
                                            handler = new RFXComOregon55Handler(this,parser);
                                        break;
                                        case OREGON56:
                                            handler = new RFXComOregon56Handler(this,parser);
                                        break;
                                        default:
                                            throw new PacketProtocolException("Unsupported protocol: " + parser.getPacketTypeByteChar());
                                    }
                                    Runnable runReceived = () -> { 
                                        try { 
                                            handler.handleReceived();
                                        } catch (PacketProtocolException ex) {
                                            LOG.error("Error in handling received data: {}", ex.getMessage(), ex);
                                        }
                                    };
                                    runReceived.run();
                                } catch (NullPointerException ex){
                                    LOG.warn("Handling an unknown protocol type. Raw data: {}", parser.toString());
                                }
                            } catch (RFXComBasicPacketParserException ex) {
                                LOG.error("Problem parsing packet into basic structure: {}, raw: {}", ex.getMessage(), parser.toString());
                            } catch (PacketProtocolException ex) {
                                LOG.error("Protocol error: {}. Raw: {}", ex.getMessage(), parser.toString());
                            }
                        } catch (ArrayIndexOutOfBoundsException ex){
                            //// We are att the end of the array;
                            LOG.trace("Reached end of byte array: {}",RFXComBasicPacketParser.decodeAllBytes(bytes));
                        }
                    };
                    run.run();
                break;
            }
        } catch (Exception ex){
            LOG.error("Unhandled exception in driver: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Not used.
     * @param device
     * @param group
     * @param set
     * @param deviceData
     * @return
     * @throws IOException 
     */
    @Override
    public boolean handleDeviceData(Device device, String group, String set, String deviceData) throws IOException {
        return false;
    }

    /**
     * Handles device additions from external.
     * @param request
     * @throws PeripheralDriverDeviceMutationException 
     */
    @Override
    public void handleNewDeviceRequest(WebPresentAddExistingDeviceRequest request) throws PeripheralDriverDeviceMutationException {
        LOG.info("Custom data with device add: {}", request.getCustomData());
        Map<String,Object> customParams = request.getCustomData();
        if(request.getDeviceId()!=0 && DiscoveredItemsCollection.hasDiscoveredDevice(this, (String)customParams.get("address"))){
            try {
                String address = (String)customParams.get("address");
                
                DeviceStructure struct = new DeviceStructure("AdditionName");
                DeviceStructure.OptionSettings settings = struct.createOptions();
                settings.addOptionValues("type", (String)customParams.get("packettype"));
                settings.addOptionValues("subtype", (String)customParams.get("packetsubtype"));
                
                String localName;
                
                if(request.getName().equals("")){
                    localName = new StringBuilder(getFriendlyName()).append(" ").append((String)request.getCustomData().get("packettype")).append(" ").append(customParams.get("packetsubtype")).append(" device").toString();
                } else {
                    localName = request.getName();
                }
                createFromExistingDevice(request.getDeviceId(), localName, address, request.getLocationId(), request.getCategoryId(),settings.createSaveSettings());
                DiscoveredItemsCollection.removeDiscoveredDevice(this, address);
            } catch (Exception ex) {
                LOG.error("Problem creating device: {}", ex.getMessage());
                throw new PeripheralDriverDeviceMutationException("Problem creating device: " + ex.getMessage());
            }
        }
    }
    
    private static byte[] getResetCommand() {
        byte[] reset = new byte[14];
        reset[0] = 0x0D;
        reset[1] = 0x00;
        reset[2] = 0x00;
        reset[3] = 0x00;
        reset[4] = 0x00;
        reset[5] = 0x00;
        reset[6] = 0x00;
        reset[7] = 0x00;
        reset[8] = 0x00;
        reset[9] = 0x00;
        reset[10] = 0x00;
        reset[11] = 0x00;
        reset[12] = 0x00;
        reset[13] = 0x00;
        return reset;
    }

    private static byte[] getStatusRequestCommand() {
        byte[] status = new byte[14];
        status[0] = 0x0D;
        status[1] = 0x00;
        status[2] = 0x00;
        status[3] = 0x01;
        status[4] = 0x02;
        status[5] = 0x00;
        status[6] = 0x00;
        status[7] = 0x00;
        status[8] = 0x00;
        status[9] = 0x00;
        status[10] = 0x00;
        status[11] = 0x00;
        status[12] = 0x00;
        status[13] = 0x00;
        return status;
    }
    
    static byte[] getSendStartTransceiver() {
        byte[] start = new byte[14];
        start[0] = 0x0D;
        start[1] = 0x00;
        start[2] = 0x00;
        start[3] = 0x02;
        start[4] = 0x07;
        start[5] = 0x00;
        start[6] = 0x00;
        start[7] = 0x00;
        start[8] = 0x00;
        start[9] = 0x00;
        start[10] = 0x00;
        start[11] = 0x00;
        start[12] = 0x00;
        start[13] = 0x00;
        return start;
    }

    @Override
    public void discoveryEnabled() {
        /// Not used
    }

    @Override
    public void discoveryDisabled() {
        /// not used.
    }
    
    /**
     *
     * @author John
     */
    private class RFXComProtoIfaceHandler extends PacketProtocolBase {

        /**
         * Constructor for received data.
         * @param driver
         * @param parser 
         */
        public RFXComProtoIfaceHandler(NativeRFXComDriver driver, RFXComBasicPacketParser parser) {
            super(driver, parser);
        }

        /**
         * Constructor for sending data.
         * @param driver 
         * @param device 
         * @param request 
         */
        public RFXComProtoIfaceHandler(NativeRFXComDriver driver, RFXComDevice device, DeviceCommandRequest request) {
            super(driver, device, request);
        }

        /**
         * Handle received data created by the received constructor.
         * @throws PacketProtocolException 
         */
        @Override
        public void handleReceived() throws PacketProtocolException {
            switch(this.getParser().getPacketType()){
                case RFXCOM_IFACE:
                    switch(this.getParser().getPacketSubTypeByteChar()){
                        case "00":
                            switch(RFXComBasicPacketParser.decodeSingleByte(this.getParser().getMessageBody()[0])){
                                case "02":
                                    LOG.info("Got status response: {}", RFXComBasicPacketParser.decodeAllBytes(this.getParser().getMessageBody()));
                                    try {
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException ex) {
                                            java.util.logging.Logger.getLogger(NativeRFXComDriver.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                        LOG.info("Sending start RFXCom");
                                        NativeRFXComDriver.this.writeBytes(getSendStartTransceiver());
                                    } catch (IOException ex) {
                                        LOG.error("Could not send start transceiver: {}", ex.getMessage(), ex);
                                    }
                                break;
                                default:
                                    LOG.error("Received an unknown RFXCom interface mode command (2): {}", RFXComBasicPacketParser.decodeAllBytes(this.getParser().getMessageBody()));
                                break;
                            }
                        break;
                        case "FF":
                            LOG.warn("Implmentation notification, keep a copy of your appLog.txt if ecountering problems (type: {}, sub: {}): {}", this.getParser().getPacketTypeByteChar(), this.getParser().getPacketSubTypeByteChar(), RFXComBasicPacketParser.decodeAllBytes(this.getParser().getMessageBody()));
                        break;
                        default:
                            LOG.error("Invalid/Unimplmented RFXCom iface message (subtype: {}): {}", this.getParser().getPacketSubTypeByteChar(), RFXComBasicPacketParser.decodeAllBytes(this.getParser().getMessageBody()));
                        break;
                    }
                break;
                default:
                    LOG.error("RFXCom control type called for a non iface type packet (type): {}", RFXComBasicPacketParser.decodeAllBytes(this.getParser().getMessageBody()));
                break;
            }
        }

        /**
         * Return data to be send created by the send constructor.
         * @return
         * @throws PacketProtocolException 
         */
        @Override
        public byte[] getSend() throws PacketProtocolException {
            throw new PacketProtocolException("I do not send anything, just a dummy");
        }

    }
    
    
}