/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativePiDomePresenceKeypadDriver;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.logging.log4j.LogManager;
import org.pidome.driver.driver.nativePiDomePresenceKeypadDriver.PiDomePresenceKeypadDataHelpers.TokenActionTypes;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.specials.presence.PersonToken;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDataEvent;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryInterface;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralDriverDeviceMutationException;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriver;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentAddExistingDeviceRequest;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentCustomFunctionInterface;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentCustomFunctionRequest;
import org.pidome.server.connector.shareddata.SharedPresenceService;
import org.pidome.server.connector.shareddata.SharedServerTimeService;

/**
 *
 * @author John
 */
public final class NativePiDomePresenceKeypadDriver extends PeripheralSoftwareDriver implements PeripheralSoftwareDriverInterface,DeviceDiscoveryInterface,WebPresentCustomFunctionInterface {

    /**
     * Logger used.
     */
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(NativePiDomePresenceKeypadDriver.class);
    
    /**
     * Date format used to be send.
     */
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    /**
     * Time format used to be send.
     */
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    
    /**
     * If the keypad is running or not.
     */
    private boolean running = false;
    
    /**
     * If the keypad is in edit mode or not.
     */
    private boolean editMode = false;
    
    /**
     * Very first message.
     */
    private boolean firstTime = false;
    
    public NativePiDomePresenceKeypadDriver(){
        
    }
    
    @Override
    public boolean sendData(String string, String string1) throws IOException {
        /// not used
        return false;
    }

    @Override
    public boolean sendData(String string) throws IOException {
        LOG.trace("TO keypad (running: "+running+"): " + string);
        if(running){
            this.writeBytes(new StringBuilder(string).append("\n").toString().getBytes("ASCII"));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean handleDeviceData(Device device, String string, String string1, String string2) throws IOException {
        //not used
        return false;
    }
    
    /**
     * Receives the command from the end device controlling the keypad.
     * Most controls will be hidden/secured and only available with plugin mappings.
     * @param device
     * @param dcr
     * @return
     * @throws IOException 
     */
    @Override
    public boolean handleDeviceData(Device device, DeviceCommandRequest dcr) throws IOException {
        try {
            switch(dcr.getControlId()){
                case "beep":
                    sendData("SET:BEEP");
                break;
                case "reboot":
                    sendData("SET:REBOOT");
                break;
                case "reset":
                    sendData("SET:RESET");
                break;
                case "alarm":
                    sendData("SET:ALARM");
                break;
                case "tamper":
                    sendData("SET:TAMPER");
                break;
                case "resettamperalarm":
                    sendData("SET:CLEAR");
                break;
                case "silencetoggle":
                    sendData("SET:SILENT");
                break;
                case "edittoggle":
                    sendData(((boolean)dcr.getCommandValue())?"EDIT:START":"EDIT:STOP");
                break;
                case "custompersontokencontrol":
                    PersonToken token = (PersonToken)dcr.getCommandValueData();
                    sendData(new StringBuilder(((PiDomePresenceKeypadDataHelpers.PersonRequestType)dcr.getCommandValue()).toString()).append(":").append(token.getPersonId()).append(":").append(token.getPersonName()).toString());
                break;
                case "message":
                    sendData(new StringBuilder("SET:CLR:").append(dcr.getCommandValue()).toString());
                break;
                case "error":
                    passErrorAndExit("Error", dcr.getCommandValue().toString());
                break;
                case "customtokenauthcontrol":
                    sendData(new StringBuilder("SET:CLR:").append(dcr.getCommandValueData()).toString());
                    new Timer().schedule(new TimerTask(){
                        @Override
                        public final void run(){
                            try {
                                sendData(new StringBuilder("SET:TIME:").append(timeFormat.format(SharedServerTimeService.getCalendarDate())).toString());
                                sendData(new StringBuilder("SET:DATE:").append(dateFormat.format(SharedServerTimeService.getCalendarDate())).toString());
                            } catch (IOException ex) {
                                LOG.error("Could not send default text after auth: {}", ex.getMessage(), ex);
                            }
                        }
                    }, 3000);
                break;
                case "custommastertokenauthcontrol":
                    sendData(new StringBuilder("AUTH:").append(((boolean)dcr.getCommandValue())?"CONFIRM":"FAIL").toString());
                break;
                case "customcontroltimeupdater":
                    /// Do not collide with initialization message.
                    if(!firstTime){
                        sendData(new StringBuilder("SET:TIME:").append(timeFormat.format(SharedServerTimeService.getCalendarDate())).toString());
                        sendData(new StringBuilder("SET:DATE:").append(dateFormat.format(SharedServerTimeService.getCalendarDate())).toString());
                    }
                break;
                case "customtokeneditsuccesscontrol":
                    sendData(((boolean)dcr.getCommandValue())?"CONFIRM":"FAIL");
                break;
            }
        } catch (Exception ex){
            if(dcr!=null){
                LOG.error("The control instance '{}' failed to be handled: {}", dcr.getClass().getName(), ex.getMessage(), ex);
            } else {
                LOG.error("Erro handling device control: {}", ex.getMessage(), ex);
            }
        }
        return true;
    }
    
    /**
     * Data received from driver.
     * @param oEvent 
     */
    @Override
    public void driverBaseDataReceived(PeripheralHardwareDataEvent oEvent) {
        try {
            switch(oEvent.getEventType()){
                case PeripheralHardwareDataEvent.DATA_RECEIVED:
                    String[] allData = oEvent.getStringData().split("\n");
                    if(allData!=null && allData.length>0){
                        for(int i=0;i<allData.length;i++){
                            String data = allData[i].trim();
                            LOG.trace("FROM keypad: " + data);
                            if(!data.startsWith("ACK") && !data.trim().equals("")){
                                switch(data){
                                    case "PING":
                                        if(running==false){
                                            firstTime = true;
                                        }
                                        running = true;
                                        sendData("PONG");
                                        if(firstTime){
                                            new Timer().schedule(new TimerTask(){
                                                @Override
                                                public final void run(){
                                                    new Timer().schedule(new TimerTask(){
                                                        @Override
                                                        public final void run(){
                                                            try {
                                                                sendData(new StringBuilder("SET:TIME:").append(timeFormat.format(SharedServerTimeService.getCalendarDate())).toString());
                                                                sendData(new StringBuilder("SET:DATE:").append(dateFormat.format(SharedServerTimeService.getCalendarDate())).toString());
                                                                sendData("SET:CLR:" + SharedPresenceService.getCurrentStatusName());
                                                                firstTime = false;
                                                            } catch (IOException ex) {
                                                                LOG.error("Could not send initial date and/or time and/or presence: {}", ex.getMessage(), ex);
                                                            }
                                                            firstTime = false;
                                                        }
                                                    }, 5000);
                                                    try {
                                                        sendData("SET:CLR:Initialized.$PiDome, welcome.");
                                                    } catch (IOException ex) {
                                                        LOG.error("Could not send initial welcome: {}", ex.getMessage(), ex);
                                                    }
                                                }
                                            }, 1000);
                                        }
                                    break;
                                    case "EDIT:START":
                                        editMode = true;
                                        LOG.debug("Keypad in edit mode");
                                        handoverDeviceData("actions", "edittoggle", editMode);
                                    break;
                                    case "EDIT:STOP":
                                        editMode = false;
                                        LOG.debug("Keypad in operation mode");
                                        handoverDeviceData("actions", "edittoggle", editMode);
                                        try {
                                            /// Let the user known stuff is ready for 3 seconds and then send the new date and time.
                                            Thread.sleep(3000);
                                            sendData(new StringBuilder("SET:TIME:").append(timeFormat.format(SharedServerTimeService.getCalendarDate())).toString());
                                            sendData(new StringBuilder("SET:DATE:").append(dateFormat.format(SharedServerTimeService.getCalendarDate())).toString());
                                            sendData("SET:CLR:" + SharedPresenceService.getCurrentStatusName());
                                        } catch (InterruptedException ex) {
                                            ///// do nothing
                                        }
                                    break;
                                    default:
                                        if (data.startsWith("SILENT:")){
                                            handoverDeviceData("settings", "silencetoggle", data.split(":")[1].equals("1"));
                                        } else if (data.startsWith("KEY:CODE:")){
                                            handoverTokenActionData(TokenActionTypes.AUTH_NORMAL_PIN, 0, data.split(":")[2].toCharArray());
                                        } else if (data.startsWith("NFC:")){
                                            handoverTokenActionData(TokenActionTypes.AUTH_NORMAL_CARD, 0, data.split(":")[1].toCharArray());
                                        } else if (data.startsWith("AUTH:NFC")){
                                            handoverTokenActionData(TokenActionTypes.AUTH_MASTER_CARD, 0, data.split(":")[2].toCharArray());
                                        } else if (data.startsWith("AUTH:CODE")){
                                            handoverTokenActionData(TokenActionTypes.AUTH_MASTER_PIN, 0, data.split(":")[2].toCharArray());
                                        } else if (data.startsWith("CUSTOM:")){
                                            handoverCustomData(data);
                                        } else {
                                            ///// When in edit mode:
                                            try {
                                                if (data.startsWith("PE")){
                                                    /// get first person:
                                                    handoverDevicePersonRequestData(PiDomePresenceKeypadDataHelpers.PersonRequestType.PE);
                                                } else if (data.startsWith("PP")){
                                                    /// Get previous person:
                                                    handoverDevicePersonRequestData(PiDomePresenceKeypadDataHelpers.PersonRequestType.PP);
                                                } else if (data.startsWith("PN")){
                                                    /// Get next person:
                                                    handoverDevicePersonRequestData(PiDomePresenceKeypadDataHelpers.PersonRequestType.PN);
                                                } else {
                                                    String[] editData = data.split(":");
                                                    switch(editData[0]){
                                                        case "NC":
                                                            /// New Code
                                                            handoverTokenActionData(TokenActionTypes.ADD_CODE, Integer.parseInt(editData[1]), editData[2].toCharArray());
                                                        break;
                                                        case "NN":
                                                            /// New Nfc card
                                                            handoverTokenActionData(TokenActionTypes.ADD_CARD, Integer.parseInt(editData[1]), editData[2].toCharArray());
                                                        break;
                                                        case "RC":
                                                            /// Remove Code
                                                            handoverTokenActionData(TokenActionTypes.REMOVE_CODE, Integer.parseInt(editData[1]), editData[2].toCharArray());
                                                        break;
                                                        case "RN":
                                                            /// Remove Nfc card
                                                            handoverTokenActionData(TokenActionTypes.REMOVE_CARD, Integer.parseInt(editData[1]), editData[2].toCharArray());
                                                        break;
                                                        default:
                                                            if (!data.startsWith("ACK")){
                                                                LOG.warn("Keypad action '{}' is unsupported",editData[0]);
                                                                passErrorAndExit("Error", "Not Supported");
                                                            }
                                                        break;
                                                    }
                                                    editData = new String[0];
                                                }
                                            } catch (Exception ex){
                                                passErrorAndExit("Error$Server error", ex);
                                            }
                                        }
                                    break;
                                }
                            }
                        }
                    }
                break;
                case PeripheralHardwareDataEvent.READ_TIMEOUT:
                    LOG.error("Received a read timeout! Device disconnected or issues, assuming not available and/or tampered.");
                    running = false;
                break;
            }
        } catch (IOException ex) {
            LOG.error("Could not send data to keypad, consider it tampered or not available.");
            running = false;
        }
    }

    private void passErrorAndExit(String firstLine, String secondLine) throws IOException {
        sendData(new StringBuilder("SET:CLR:").append(firstLine).append("$").append(secondLine).toString());
        try {
            /// Let the user know it is my fault before putting out of edit mode.
            Thread.sleep(2000);
        } catch (InterruptedException ex1) {
            ///// do nothing
        }
        sendData("ERROR");
    }
    
    private void passErrorAndExit(String error, Exception ex) throws IOException {
        LOG.error("Could not handle keypad command: {}", ex.getMessage(), ex);
        sendData(new StringBuilder("SET:CLR:").append(error).toString());
        try {
            /// Let the user know it is my fault before putting out of edit mode.
            Thread.sleep(2000);
        } catch (InterruptedException ex1) {
            ///// do nothing
        }
        sendData("ERROR");
    }
    
    private void handoverCustomData(String data){
        org.pidome.driver.driver.nativePiDomePresenceKeypadTools.CommonPiDomeKeypadFunctions device;
        if(getRunningDevices().size()==1){
            try {
                device = (org.pidome.driver.driver.nativePiDomePresenceKeypadTools.CommonPiDomeKeypadFunctions)getRunningDevices().get(0);
                device.handleCustomData(data);
            } catch (Exception ex){
                LOG.error("Possible illegal device access, halting: {}", ex.getMessage(), ex);
            }
        } else {
            LOG.error("Possible illegal amount or no devices running, there can/must be only one. halting");
        }
    }
    
    private void handoverTokenActionData(TokenActionTypes action, int uid, char[] tokenData){
        org.pidome.driver.driver.nativePiDomePresenceKeypadTools.CommonPiDomeKeypadFunctions device;
        if(getRunningDevices().size()==1){
            try {
                device = (org.pidome.driver.driver.nativePiDomePresenceKeypadTools.CommonPiDomeKeypadFunctions)getRunningDevices().get(0);
                device.handleTokenActionData(action, uid, tokenData);
            } catch (Exception ex){
                LOG.error("Possible illegal device access, halting: {}", ex.getMessage(), ex);
            }
        } else {
            LOG.error("Possible illegal amount or no devices running, there can/must be only one. halting");
        }
    }
    
    private void handoverDeviceData(String group, String control, Object data){
        org.pidome.driver.driver.nativePiDomePresenceKeypadTools.CommonPiDomeKeypadFunctions device;
        if(getRunningDevices().size()==1){
            try {
                device = (org.pidome.driver.driver.nativePiDomePresenceKeypadTools.CommonPiDomeKeypadFunctions)getRunningDevices().get(0);
                device.handleKeypadData(group, control, data);
            } catch (Exception ex){
                LOG.error("Possible illegal device access, halting: {}", ex.getMessage(), ex);
            }
        } else {
            LOG.error("Possible illegal amount or no devices running, there can/must be only one. halting");
        }
    }
    
    private void handoverDevicePersonRequestData(PiDomePresenceKeypadDataHelpers.PersonRequestType persRequest){
        org.pidome.driver.driver.nativePiDomePresenceKeypadTools.CommonPiDomeKeypadFunctions device;
        if(getRunningDevices().size()==1){
            try {
                device = (org.pidome.driver.driver.nativePiDomePresenceKeypadTools.CommonPiDomeKeypadFunctions)getRunningDevices().get(0);
                device.handlePersonRequest(persRequest);
            } catch (Exception ex){
                LOG.error("Possible illegal device access, halting: {}", ex.getMessage(), ex);
            }
        } else {
            LOG.error("Possible illegal amount of devices running, there can be only one. halting");
        }
    }

    @Override
    public void handleNewDeviceRequest(WebPresentAddExistingDeviceRequest wpaedr) throws PeripheralDriverDeviceMutationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handleCustomFunctionRequest(WebPresentCustomFunctionRequest wpcfr) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void discoveryEnabled() {
        /// Not used
    }

    @Override
    public void discoveryDisabled() {
        /// Not used
    }
}