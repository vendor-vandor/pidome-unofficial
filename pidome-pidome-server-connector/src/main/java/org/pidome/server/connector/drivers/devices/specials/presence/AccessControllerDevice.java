/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.devices.specials.presence;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.pidome.server.connector.drivers.devices.Device;

/**
 * A PresenceStationDevice is a special device to be used within user presences which controls multiple users to be able to be authorized.
 * @author John
 */
public abstract class AccessControllerDevice extends Device {
    
    AccessControllerProxyInterface listener;
    
    private AccessControllerProxyInterface.Capabilities[] capabs;
    
    public final void registerCapabilities(AccessControllerProxyInterface.Capabilities... capabs){
        this.capabs = capabs;
        getAccessControllerProxy().registerCapabilities(this,capabs);
    }
    
    /**
     * Return a list of capabilities of a device.
     * You have to set the capabilities when a device starts, but from time to 
     * time it can also be requested.
     * @return 
     */
    public final List<AccessControllerProxyInterface.Capabilities> getCapabilities(){
        return Arrays.asList(capabs);
    }
    
    /**
     * Sets the access controller proxy.
     * @param listener 
     */
    public final void setAccessControllerListener(AccessControllerProxyInterface listener){
        if(this.listener == null){
            this.listener = listener;
        }
    }
    
    /**
     * Removes the access controller proxy.
     */
    public final void removeAccessControllerListener(){
        this.listener = null;
    }
    
    /**
     * Returns the proxy listener which gives access to access control routines.
     * @return 
     */
    public final AccessControllerProxyInterface getAccessControllerProxy(){
        return this.listener;
    }
    
    /**
     * Used to pass a person to the device.
     * This is used when a device supports on-site editing.
     * @param personToken Internally known user.
     * @throws UnsupportedOperationException When device does not support on-site editing.
     */
    public abstract void sendPerson(PersonToken personToken) throws UnsupportedOperationException;
    
    /**
     * Set a device in tampered state.
     * @param tampered true when tampered should be on.
     * @throws UnsupportedOperationException When tampered is unsupported.
     */
    public abstract void setSystemTamper(boolean tampered) throws UnsupportedOperationException;
    
    /**
     * Set a device in it's alarmed state.
     * @param alarmed true when alarmed should be on.
     */
    public abstract void setSystemAlarmed(boolean alarmed);
    
    /**
     * Set a device in it's silenced state.
     * @param silenced
     */
    public abstract void setSystemSilenced(boolean silenced);
    
    /**
     * Perform a soft device reset.
     * This is mainly a software reset.
     * @throws UnsupportedOperationException When device does not does soft reset.
     */
    public abstract void resetSystem() throws UnsupportedOperationException;
    
    /**
     * Perform a hard device reset.
     * This can trigger a an hardware reset.
     * @throws UnsupportedOperationException When device does not does hard reset.
     */
    public abstract void rebootSystem() throws UnsupportedOperationException;
    
    /**
     * Set's a device in edit mode.
     * Use this when a device supports on-site user edits, in other words adding keycards or codes can be handled by this thing.
     * @param setEdit Use true to put a device in edit mode.
     * @throws UnsupportedOperationException When device does not support on-site editing.
     */
    public abstract void setSystemEdit(boolean setEdit) throws UnsupportedOperationException;
    
    /**
     * The system time is being passed on.
     * If the device supports displaying timne/date.
     * @param date A regular Java date object
     */
    public abstract void setSystemTime(Date date) throws UnsupportedOperationException;
    
    /**
     * Pass a message to be displayed on the remote device.
     * @param message 
     */
    public abstract void sendMessage(String message);
    
    /**
     * Sends an error message to the device.
     * @param message 
     */
    public abstract void sendError(String message);
    
    /**
     * Called when a token authorization result is known.
     * @param confirmed true when success, false when not.
     * @param name The name of the confirmed one.
     */
    public abstract void sendAuthConfirmed(boolean confirmed, String name);
    
    /**
     * Called when a master card must be authorized.
     * @param confirmed 
     */
    public abstract void sendMasterAuthConfirmed(boolean confirmed);
    
    /**
     * Send the result of an edit action.
     * @param editSuccess 
     */
    public abstract void sendEditSuccess(boolean editSuccess);
    
}