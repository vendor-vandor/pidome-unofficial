/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.device.nativePiDomePresenceKeypadDevice;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.logging.log4j.LogManager;
import org.pidome.driver.driver.nativePiDomePresenceKeypadDriver.KeypadMasterTokenAuthenticationControl;
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
import org.pidome.server.connector.drivers.devices.specials.presence.AccessControllerProxyInterface.Capabilities;
import org.pidome.server.connector.drivers.devices.specials.presence.PersonToken;
import org.pidome.server.connector.shareddata.SharedServerTimeService;
import org.pidome.server.connector.shareddata.SharedServerTimeServiceListener;

/**
 *
 * @author John
 */
public final class NativePiDomePresenceKeypadDevice extends AccessControllerDevice implements CommonPiDomeKeypadFunctions {

    private int currentPersonrequestedId = 0;
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(NativePiDomePresenceKeypadDevice.class);
    
    private PersonRequestType lastRequestType;
    
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
            LOG.error("Could not compose control for Time Updater: {}", ex.getMessage());
        }
        ///
        
        ///
        
    }
    
    /**
     * All functionalities are dispatched to the driver immediately.
     * @param dcr
     * @throws UnsupportedDeviceCommandException 
     */
    @Override
    public final void handleCommandRequest(DeviceCommandRequest dcr) throws UnsupportedDeviceCommandException {
        this.dispatchToDriver(dcr);
    }

    @Override
    public final void handleData(String string, Object o) {
        /// Not used, this device has special handling immediately passed by the driver.
    }

    public final void handleKeypadData(String group, String control, Object data){
        DeviceNotification notification = new DeviceNotification();
        notification.addData(group, control, data);
        dispatchToHost(notification);
    }
    
    public final void handleTokenActionData(PiDomePresenceKeypadDataHelpers.TokenActionTypes action, int uid, char[] tokenData){
        LOG.debug("Keypad action: {}, uid: {}", action.toString(), uid);
        switch(action){
            case ADD_CARD:
                getAccessControllerProxy().addToken(this, uid, PersonToken.TokenType.NFC, tokenData);
            break;
            case REMOVE_CARD:
                getAccessControllerProxy().removeToken(this, uid, PersonToken.TokenType.NFC, tokenData);
            break;
            case ADD_CODE:
                getAccessControllerProxy().addToken(this, uid, PersonToken.TokenType.PIN, tokenData);
            break;
            case REMOVE_CODE:
                getAccessControllerProxy().removeToken(this, uid, PersonToken.TokenType.PIN, tokenData);
            break;
            case AUTH_MASTER_CARD:
                getAccessControllerProxy().authorizeMasterToken(this, PersonToken.TokenType.NFC, tokenData);
            break;
            case AUTH_MASTER_PIN:
                getAccessControllerProxy().authorizeMasterToken(this, PersonToken.TokenType.PIN, tokenData);
            break;
            case AUTH_NORMAL_CARD:
                getAccessControllerProxy().authorizeToken(this, PersonToken.TokenType.NFC, tokenData);
            break;
            case AUTH_NORMAL_PIN:
                getAccessControllerProxy().authorizeToken(this, PersonToken.TokenType.PIN, tokenData);
            break;
        }
    }
    
    @Override
    public void sendMasterAuthConfirmed(boolean bln) {
        try {
            KeypadMasterTokenAuthenticationControl authControl = new KeypadMasterTokenAuthenticationControl();
            DeviceCommandRequest request = new DeviceCommandRequest(authControl);
            request.setCommandValue(bln);
            request.setCommandValueData(bln);
            handleCommandRequest(request);
        } catch (DeviceControlException | UnsupportedDeviceCommandException | DeviceControlsGroupException ex) {
            LOG.error("Could not pass auth result: {}", ex.getMessage());
        }
    }
    
    public final void handlePersonRequest(PersonRequestType type){
        lastRequestType = type;
        switch(type){
            case PE:
                getAccessControllerProxy().firstPerson(this);
            break;
            case PN:
                getAccessControllerProxy().nextPerson(this, currentPersonrequestedId);
            break;
            case PP:
                getAccessControllerProxy().previousPerson(this, currentPersonrequestedId);
            break;
        }
    }
    
    @Override
    public final void shutdownDevice() {
        SharedServerTimeService.removeListener(timeHandler);
    }

    @Override
    public final void startupDevice() {
        SharedServerTimeService.addListener(timeHandler);
        registerCapabilities(Capabilities.values());
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
    public final void sendPerson(PersonToken pt) throws UnsupportedOperationException {
        try {
            DeviceCommandRequest request = new DeviceCommandRequest(new PersonTokenControl());
            request.setCommandValue(lastRequestType);
            request.setCommandValueData(pt);
            currentPersonrequestedId = pt.getPersonId();
            handleCommandRequest(request);
        } catch (DeviceControlException | UnsupportedDeviceCommandException | DeviceControlsGroupException ex) {
            LOG.error("Could not pass person: {}", ex.getMessage());
        }
    }

    @Override
    public final void setSystemTamper(boolean bln) throws UnsupportedOperationException {
        try {
            DeviceCommandRequest request;
            if(bln){
                request = new DeviceCommandRequest(this.getFullCommandSet().getControlsGroup("actions").getDeviceControl("tamper"));
            } else {
                request = new DeviceCommandRequest(this.getFullCommandSet().getControlsGroup("actions").getDeviceControl("resettamperalarm"));
            }
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
    public final void setSystemAlarmed(boolean bln) {
        try {
            DeviceCommandRequest request;
            if(bln){
                request = new DeviceCommandRequest(this.getFullCommandSet().getControlsGroup("actions").getDeviceControl("alarm"));
            } else {
                request = new DeviceCommandRequest(this.getFullCommandSet().getControlsGroup("actions").getDeviceControl("resettamperalarm"));
            }
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
    public final void resetSystem() throws UnsupportedOperationException {
        try {
            DeviceCommandRequest request = new DeviceCommandRequest(this.getFullCommandSet().getControlsGroup("actions").getDeviceControl("reset"));
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
    public final void rebootSystem() throws UnsupportedOperationException {
        try {
            DeviceCommandRequest request = new DeviceCommandRequest(this.getFullCommandSet().getControlsGroup("actions").getDeviceControl("reboot"));
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
    public final void setSystemEdit(boolean bln) throws UnsupportedOperationException {
        try {
            DeviceCommandRequest request = new DeviceCommandRequest(this.getFullCommandSet().getControlsGroup("actions").getDeviceControl("edittoggle"));
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
    }

    @Override
    public final void setSystemTime(Date date) throws UnsupportedOperationException {
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
    public final void setSystemSilenced(boolean bln) {
        try {
            DeviceCommandRequest request = new DeviceCommandRequest(this.getFullCommandSet().getControlsGroup("settings").getDeviceControl("silencetoggle"));
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
    }
 
    @Override
    public final void sendEditSuccess(boolean success){
        try {
            DeviceCommandRequest request = new DeviceCommandRequest(new KeypadTokenEditSuccessControl());
            request.setCommandValue(success);
            handleCommandRequest(request);
        } catch (DeviceControlException | UnsupportedDeviceCommandException | DeviceControlsGroupException ex) {
            LOG.error("Could not edit result '{}': {}", success, ex.getMessage());
        }
    }

    @Override
    public void handleCustomData(String data) {
        /// No not yet.
    }
    
}