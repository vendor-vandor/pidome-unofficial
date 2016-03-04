/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.devices;

import java.util.concurrent.TimeUnit;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsSet;

/**
 *
 * @author John
 */
public interface DeviceStructProxyInterface {
    
    /**
     * Handle a command request passed back from a modifier to pass to the device.
     * @param command 
     */
    public void handleCommandRequestFromModifier(final DeviceCommandRequest command);
    
    /**
     * To pass data to the server.
     * @param notification 
     */
    public void notifyClients(DeviceNotification notification);
    
    /**
     * To pass data to the server but with the option if it is an user intention or automated dispatch.
     * @param notification 
     * @param userIntent 
     */
    public void notifyClients(DeviceNotification notification, boolean userIntent);
    
    /**
     * To pass data to a driver.
     * @param cmdGroup
     * @param cmdSet
     * @param value 
     */
    public void dispatchToDriver(String cmdGroup, String cmdSet, String value);
    
    /**
     * Dispatches raw requested data to a driver.
     * @param request 
     */
    public void dispatchToDriver(DeviceCommandRequest request);
    
    /**
     * Returns the device id.
     * @return 
     */
    public int getId();
    /**
     * Returns device name.
     * @return 
     */
    public String getName();
    
    /**
     * Returns the device set address.
     * @return 
     */
    public String getAddress();
    
    /**
     * Returns the device's name as set by an user.
     * @return 
     */
    public String getDeviceName();
    
    /**
     * Returns the location id set by an user.
     * @return 
     */
    public int getLocationId();
    
    /**
     * Get a command set details.
     * @throws org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException
     * @deprecated I'm not even going to explain.
     * @param setId
     * @return 
     */
    public DeviceControl getDeviceCommandSet(String setId) throws DeviceControlException;
    
    /**
     * Returns the controls set.
     * @return 
     */
    public DeviceControlsSet getFullCommandSet();

    /**
     * Returns the options a user has set.
     * @return 
     */
    public DeviceStructure.DeviceOptions getDeviceOptions();
    
    /**
     * Initiates a scheduler service for devices which need to schedule items.
     */
    public void initiateScheduler();
    
    /**
     * Adds an item to the scheduler.
     * An extending class must implement DeviceScheduler
     * @param runnable
     * @param interval
     * @param timeUnit
     * @throws DeviceSchedulerException 
     */
    public void scheduleItem(Runnable runnable, int interval, TimeUnit timeUnit) throws DeviceSchedulerException; 
    
    /**
     * Updates the receive time from devices.
     */
    public void updateReceive();
    
    /**
     * Updates device's send time.
     */
    public void updateSend();
    
}
