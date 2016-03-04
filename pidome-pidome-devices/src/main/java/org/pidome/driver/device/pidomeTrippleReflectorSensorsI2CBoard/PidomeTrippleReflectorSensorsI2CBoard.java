/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.device.pidomeTrippleReflectorSensorsI2CBoard;

import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceNotification;
import org.pidome.server.connector.drivers.devices.DeviceScheduler;
import org.pidome.server.connector.drivers.devices.DeviceSchedulerException;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;
import org.pidome.server.connector.tools.MiscImpl;

/**
 *
 * @author John
 */
public class PidomeTrippleReflectorSensorsI2CBoard extends Device implements DeviceScheduler {

    static Logger LOG = LogManager.getLogger(PidomeTrippleReflectorSensorsI2CBoard.class);
    
    boolean firstRead = true;
    
    public PidomeTrippleReflectorSensorsI2CBoard() {}
    
    
    /**
     * @inheritDoc
     */
    @Override
    public void handleData(String input, Object object) {
        if (firstRead==false){
            byte[] byteArray = (byte[])object;
            try {
                String[] data = input.split(":");
                DeviceNotification notification = new DeviceNotification();
                notification.addData("sensors", data[1], MiscImpl.byteArrayToInt(byteArray));
                dispatchToHost(notification);
            } catch (IndexOutOfBoundsException ex){
                LOG.error("Illegal return type");
            } catch (Exception ex){
                LOG.warn("Unsupported return set: {}", ex.getMessage());
            }
        }
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public void setScheduledItems() {
        LOG.debug("Scheduling sensor readings");
        try {
            this.scheduleItem(() -> {
                dispatchToDriver("sensors", "0x01", this.getAddress() + ":READ:0x01:4");/// sensor 1
                dispatchToDriver("sensors", "0x02", this.getAddress() + ":READ:0x02:4");/// sensor 2
                dispatchToDriver("sensors", "0x03", this.getAddress() + ":READ:0x03:4");/// sensor 3
                firstRead = false;
            }, 1, TimeUnit.MINUTES);
        } catch (DeviceSchedulerException ex) {
            LOG.error("PidomeTrippleReflectorSensorsI2CBoard updater not scheduled: {}", ex.getMessage());
        }
    }
    
    @Override
    public void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void shutdownDevice() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void startupDevice() {
    }
    
}
