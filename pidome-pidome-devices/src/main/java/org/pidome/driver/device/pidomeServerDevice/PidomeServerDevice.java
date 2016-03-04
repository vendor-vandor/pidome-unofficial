/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.device.pidomeServerDevice;

import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceNotification;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;



/**
 *
 * @author John Sirach
 */
public class PidomeServerDevice extends Device {

    static Logger LOG = LogManager.getLogger(PidomeServerDevice.class);
    
    public PidomeServerDevice(){}
    
    @Override
    public void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        throw new UnsupportedDeviceCommandException("Unsupported device action");
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public void handleData(String data, Object object) {
        LOG.debug("Handling: {} - {}", data, object);
        String[] partsData = data.split(";");
        DeviceNotification notification = new DeviceNotification();
        for (String sendData : partsData) {
            String[] sendSplitted = sendData.split(":");
            if(sendSplitted[0].equals("uptime")){
                Long totalSeconds = (long)(Float.parseFloat(sendSplitted[1]))/1;
                int day = (int)TimeUnit.SECONDS.toDays(totalSeconds);        
                long hours = TimeUnit.SECONDS.toHours(totalSeconds) - (day *24);
                long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) - (TimeUnit.SECONDS.toHours(totalSeconds)* 60);
                long seconds = TimeUnit.SECONDS.toSeconds(totalSeconds) - (TimeUnit.SECONDS.toMinutes(totalSeconds) * 60);
                notification.addData("values", "uptime", new StringBuilder().append(day).append(" days ")
                                                                            .append(hours).append(":")
                                                                            .append(minutes).append(":")
                                                                            .append(seconds).toString());
            } else {
                notification.addData("values", sendSplitted[0], Float.parseFloat(sendSplitted[1]));
            }
        }
        dispatchToHost(notification);
    }

    @Override
    public void shutdownDevice() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void startupDevice() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}