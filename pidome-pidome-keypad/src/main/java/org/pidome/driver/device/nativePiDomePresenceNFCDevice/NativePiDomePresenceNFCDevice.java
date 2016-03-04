/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.device.nativePiDomePresenceNFCDevice;

import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.pidome.driver.driver.nativePiDomePresenceKeypadDriver.KeypadTimeUpdateControl;
import org.pidome.driver.driver.nativePiDomePresenceKeypadDriver.KeypadTokenAuthenticationControl;
import org.pidome.driver.driver.nativePiDomePresenceKeypadDriver.KeypadTokenEditSuccessControl;
import org.pidome.driver.driver.nativePiDomePresenceKeypadDriver.PersonTokenControl;
import org.pidome.driver.driver.nativePiDomePresenceKeypadDriver.PiDomePresenceKeypadDataHelpers;
import org.pidome.driver.driver.nativePiDomePresenceKeypadDriver.PiDomePresenceKeypadDataHelpers.PersonRequestType;
import org.pidome.driver.driver.nativePiDomePresenceKeypadTools.CommonPiDomeKeypadFunctions;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceNotification;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroupException;
import org.pidome.server.connector.drivers.devices.specials.presence.AccessControllerDevice;
import org.pidome.server.connector.drivers.devices.specials.presence.AccessControllerProxyInterface;
import org.pidome.server.connector.drivers.devices.specials.presence.PersonToken;
import org.pidome.server.connector.shareddata.SharedServerTimeService;
import org.pidome.server.connector.shareddata.SharedServerTimeServiceListener;

/**
 *
 * @author John
 */
public class NativePiDomePresenceNFCDevice extends AccessControllerDevice implements CommonPiDomeKeypadFunctions {

    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(NativePiDomePresenceNFCDevice.class);
    
    /**
     * Function handling time updates.
     */
    SharedServerTimeServiceListener timeHandler = this::handleTimeUpdater;
    
    private void handleTimeUpdater(){
        try {
            DeviceCommandRequest request = new DeviceCommandRequest(new KeypadTimeUpdateControl());
            request.setCommandValue(true);
            handleCommandRequest(request);
        } catch (DeviceControlException | UnsupportedDeviceCommandException | DeviceControlsGroupException ex) {
            ////
        }
        ///
        
    }
    
    @Override
    public void sendPerson(PersonToken pt) throws UnsupportedOperationException {
        try {
            DeviceCommandRequest request = new DeviceCommandRequest(new PersonTokenControl());
            request.setCommandValue(PersonRequestType.PE);
            request.setCommandValueData(pt);
            handleCommandRequest(request);
        } catch (DeviceControlException | UnsupportedDeviceCommandException | DeviceControlsGroupException ex) {
            LOG.error("Could not pass person: {}", ex.getMessage());
        }
    }

    @Override
    public void setSystemTamper(boolean bln) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSystemAlarmed(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSystemSilenced(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resetSystem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void rebootSystem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSystemEdit(boolean bln) throws UnsupportedOperationException {
        /*
        try {
            DeviceCommandRequest request = new DeviceCommandRequest();
            request.setControl(this.getFullCommandSet().getControlsGroup("actions").getDeviceControl("edittoggle"));
            request.setGroupId("actions");
            request.setControlId("edittoggle");
            request.setCommandValue(bln);
            request.setCommandValueData(bln);
            handleCommandRequest(request);
        } catch (UnsupportedDeviceCommandException ex) {
            throw new  UnsupportedOperationException("Unsupported command");
        } catch (DeviceControlsGroupException ex) {
            throw new  UnsupportedOperationException("Unsupported selected group");
        } catch (DeviceControlException ex) {
            throw new  UnsupportedOperationException("Unsupported selected control");
        }
        */
    }

    @Override
    public void setSystemTime(Date date) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public final void sendMessage(String string) {
        try {
            DeviceCommandRequest request = new DeviceCommandRequest(this.getFullCommandSet().getControlsGroup("data").getDeviceControl("message"));
            request.setGroupId("data");
            request.setCommandValue(string);
            request.setCommandValueData(string);
            handleCommandRequest(request);
        } catch (UnsupportedDeviceCommandException ex) {
            throw new  UnsupportedOperationException("Unsupported command");
        } catch (DeviceControlsGroupException ex) {
            throw new  UnsupportedOperationException("Unsupported selected group");
        } catch (DeviceControlException ex) {
            throw new  UnsupportedOperationException("Unsupported selected control");
        }
    }

    @Override
    public final void sendError(String string) {
        try {
            DeviceCommandRequest request = new DeviceCommandRequest(this.getFullCommandSet().getControlsGroup("data").getDeviceControl("error"));
            request.setGroupId("data");
            request.setCommandValue(string);
            request.setCommandValueData(string);
            handleCommandRequest(request);
        } catch (UnsupportedDeviceCommandException ex) {
            throw new  UnsupportedOperationException("Unsupported command");
        } catch (DeviceControlsGroupException ex) {
            throw new  UnsupportedOperationException("Unsupported selected group");
        } catch (DeviceControlException ex) {
            throw new  UnsupportedOperationException("Unsupported selected control");
        }
    }

    @Override
    public final void sendAuthConfirmed(boolean bln, String name) {
        try {
            DeviceCommandRequest request = new DeviceCommandRequest(new KeypadTokenAuthenticationControl());
            request.setCommandValue(bln);
            request.setCommandValueData(name);
            handleCommandRequest(request);
        } catch (DeviceControlException | UnsupportedDeviceCommandException | DeviceControlsGroupException ex) {
            LOG.error("Could not pass auth result: {}", ex.getMessage());
        }
    }

    @Override
    public final void sendEditSuccess(boolean success){
        try {
            DeviceCommandRequest request = new DeviceCommandRequest(new KeypadTokenEditSuccessControl());
            request.setCommandValue(success);
            handleCommandRequest(request);
        } catch (DeviceControlException | UnsupportedDeviceCommandException | DeviceControlsGroupException ex) {
            LOG.error("Could not send edit result '{}': {}", success, ex.getMessage(), ex);
        }
    }

    @Override
    public void sendMasterAuthConfirmed(boolean bln) {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void handleCommandRequest(DeviceCommandRequest dcr) throws UnsupportedDeviceCommandException {
        this.dispatchToDriver(dcr);
    }

    @Override
    public void handleData(String string, Object o) {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public final void shutdownDevice() {
        SharedServerTimeService.removeListener(timeHandler);
    }

    @Override
    public final void startupDevice() {
        SharedServerTimeService.addListener(timeHandler);
        registerCapabilities(AccessControllerProxyInterface.Capabilities.NFC,
                             AccessControllerProxyInterface.Capabilities.MSG,
                             AccessControllerProxyInterface.Capabilities.MSG_MULTI,
                             AccessControllerProxyInterface.Capabilities.BEEP,
                             AccessControllerProxyInterface.Capabilities.SET_ALARM);
    }

    @Override
    public final void handleTokenActionData(PiDomePresenceKeypadDataHelpers.TokenActionTypes action, int uid, char[] tokenData){
        LOG.info("NFCpad action: {}, uid: {}, code: {}", action.toString(), uid, tokenData);
        switch(action){
            case ADD_CARD:
                getAccessControllerProxy().addToken(this, uid, PersonToken.TokenType.NFC, tokenData);
            break;
            case AUTH_NORMAL_CARD:
                getAccessControllerProxy().authorizeToken(this, PersonToken.TokenType.NFC, tokenData);
            break;
            default:
                this.sendError("Error$Unsupported");
            break;
        }
    }

    @Override
    public void handleCustomData(String data) {
        try {
            String[] splitted = data.trim().split(":");
            DeviceNotification notification = new DeviceNotification();
            if(splitted[1].equals("TEMP")){
                notification.addData("env", "temp", Float.parseFloat(splitted[3]));
            } else if(splitted[1].equals("LUX")){
                notification.addData("env", "lux", Float.parseFloat(splitted[3]));
            }
            dispatchToHost(notification);
        } catch (Exception ex){
            /// custom data not supported.
        }
    }

    public final void handleKeypadData(String group, String control, Object data){
        DeviceNotification notification = new DeviceNotification();
        notification.addData(group, control, data);
        dispatchToHost(notification);
    }

    @Override
    public void handlePersonRequest(PersonRequestType type) {
        /// not used
    }

}