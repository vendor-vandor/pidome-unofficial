/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.server.connector.drivers.devices;

import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsSet;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The base device functions for end developers to make things easier.
 * @author John Sirach
 */
public abstract class Device {
    
    /**
     * For tracking the device status.
     */
    public enum DeviceStatus {
        /**
         * The device is ok.
         */
        OK,
        /**
         * The device needs some attention.
         */
        WARNING,
        /**
         * There is a serious device issue.
         */
        ERROR,
        /**
         * Status is known and is dead.
         */
        DEAD,
        /**
         * One of the controls is timing out.
         */
        CONTROL_TIMEOUT;
    }
    
    /**
     * The struct proxy for communications.
     */
    private DeviceStructProxyInterface structProxy;
    
    /**
     * The current device status.
     */
    private DeviceStatus status = DeviceStatus.OK;
    
    /**
     * The device status reason.
     */
    private String deviceStatusReason = "No problems found";
    
    /**
     * The logger.
     */
    static Logger LOG = LogManager.getLogger(Device.class);
    
    /**
     * Returns the device status.
     * @return 
     */
    public final DeviceStatus getDeviceStatus(){
        return this.status;
    }
    
    /**
     * Returns the status textual reason as set by the devices/drivers.
     * @return 
     */
    public final String getDeviceStatusText(){
        return this.deviceStatusReason;
    }
    
    /**
     * Convenience function to set the device status to ok.
     */
    public final void setDeviceStatusOk(){
        this.status = DeviceStatus.OK;
    }
    
    /**
     * Sets the device status.
     * @param status The new device status.
     * @param reason The reason fr this status.
     * @throws org.pidome.server.connector.drivers.devices.UnsupportedDeviceStatusException 
     */
    public final void setDeviceStatus(DeviceStatus status, String reason) throws  UnsupportedDeviceStatusException {
        if(reason != null && !reason.isEmpty()){
            this.status = status;
            this.deviceStatusReason = reason;
        } else {
            throw new UnsupportedDeviceStatusException("A reason may not be empty");
        }
    }
    
    /**
     * Endpoint devices should handle the command that has been issued.
     * If a modifier is known to be active the data will be passed to the modifier
     * instead of the device. After the modifier is finished with the data the modifier
     * will pass on the device.
     * @param command 
     * @throws org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException 
     */
    public abstract void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException;
    
    /**
     * Endpoint devices should handle the command that has been issued.
     * This function is called from the proxy after a modifier has fnished data handling
     * and passes the modified data back.
     * @param command
     * @throws UnsupportedDeviceCommandException 
     */
    public final void handleCommandRequestFromModifier(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        structProxy.handleCommandRequestFromModifier(command);
    }
    
    /**
     * Handles the data coming from drivers.
     * @param data
     * @param object 
     */
    public abstract void handleData(String data, Object object);

    /**
     * Passes data to devices.
     * @param data
     * @param object 
     */
    public final void passToDevice(String data, Object object){
        structProxy.updateReceive();
        handleData(data, object);
    }
    
    /**
     * Used to shutdown devices.
     */
    public abstract void shutdownDevice();
    
    /**
     * Used to shutdown devices.
     */
    public abstract void startupDevice();
    
    /**
     * Return the device id.
     * @return 
     */
    public final int getId(){
        return structProxy.getId();
    }

    /**
     * Returns the device name.
     * @return 
     */
    public final String getName(){
        return structProxy.getName();
    }
    
    /**
     * Returns the device address.
     * @return 
     */
    public final String getAddress(){
        return structProxy.getAddress();
    }
    
    /**
     * Returns the device name set by an user.
     * @return 
     */
    public final String getDeviceName(){
        return structProxy.getDeviceName();
    }
    
    /**
     * Returns the location id set by an user.
     * @return 
     */
    public final int getLocationId(){
        return structProxy.getLocationId();
    }
    
    /**
     * Overwrite this function for device preparation of things that can't be done in the constructor.
     * @param firstPrepare
     */
    public void prepare(boolean firstPrepare){}
    
    /**
     * Dispatching the data to the corresponding driver.
     * @param group
     * @param set
     * @param data 
     */
    public final void dispatchToDriver(String group, String set, String data){
        structProxy.dispatchToDriver(group, set, data);
        structProxy.updateSend();
    }
    
    /**
     * Dispatching the data to the corresponding driver.
     * @param request
     */
    public final void dispatchToDriver(DeviceCommandRequest request){
        structProxy.dispatchToDriver(request);
        structProxy.updateSend();
    }
    
    /**
     * Dispatching the data to the corresponding driver.
     * @param group
     * @param set
     * @param data 
     * @param userIntent 
     */
    public final void dispatchToDriver(String group, String set, String data, boolean userIntent){
        structProxy.dispatchToDriver(group, set, data);
        structProxy.updateSend();
    }
    
    /**
     * Dispatching the data to the corresponding driver.
     * @param request
     * @param userIntent
     */
    public final void dispatchToDriver(DeviceCommandRequest request, boolean userIntent){
        structProxy.dispatchToDriver(request);
        structProxy.updateSend();
    }
    
    /**
     * Returns the full control set.
     * @return 
     */
    public final DeviceControlsSet getFullCommandSet(){
        return structProxy.getFullCommandSet();
    }
    
    /**
     * Returns the device options.
     * @return 
     */
    public final DeviceStructure.DeviceOptions getDeviceOptions(){
        return structProxy.getDeviceOptions();
    }
    
    /**
     * Notifies the host of new data.
     * @param notification
     */
    public final void dispatchToHost(DeviceNotification notification){
        dispatchToHost(notification, true);
    }
    
    /**
     * Notifies the host of new data but with the option if it is an user intention or automated dispatch.
     * When the user intent is false it means it is an automated process.
     * @param notification
     * @param userIntent
     */
    public final void dispatchToHost(DeviceNotification notification, boolean userIntent){
        structProxy.updateReceive();
        structProxy.notifyClients(notification, userIntent);
    }
    
    /**
     * Initiates a scheduler.
     */
    public final void initiateScheduler(){
        structProxy.initiateScheduler();
    }
    
    /**
     * Adds a runnable to the scheduler.
     * The extending class must implement DeviceScheduler.
     * @param runnable
     * @param interval
     * @param timeUnit
     * @throws DeviceSchedulerException 
     */
    public final void scheduleItem(Runnable runnable, int interval, TimeUnit timeUnit) throws DeviceSchedulerException {
        if(this instanceof DeviceScheduler){
            structProxy.scheduleItem(runnable, interval, timeUnit);
        }
    }
    
    /**
     * Used by the server for device linking.
     * @param structProxy 
     */
    public final void setStructProxy(DeviceStructProxyInterface structProxy){
        if(this.structProxy==null){
            this.structProxy = structProxy;
        }
    }
    
}