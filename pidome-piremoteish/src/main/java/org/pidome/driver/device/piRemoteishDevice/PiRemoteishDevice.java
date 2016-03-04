/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.device.piRemoteishDevice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceNotification;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;

/**
 * Device class.
 * @author John
 */
public final class PiRemoteishDevice extends Device {

    static Logger LOG = LogManager.getLogger(PiRemoteishDevice.class);
    
    /**
     * Handle a command from the RPC methods.
     * handles the following commands:
     * 'A' : Auto mode
     * 'a' : Manual mode (required to command lamp/LED)
     * 'P' : Lamp on
     * 'p' : Lamp off
     * 'L' : LED on
     * 'l'  : LED off 
     * @param command
     * @throws UnsupportedDeviceCommandException 
     */
    @Override
    public void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        switch(command.getControlId()){
            case "automanual":
            case "lampswitch":
            case "ledswitch":
                dispatchToDriver(command.getGroupId(), command.getControlId(), (String)command.getCommandValueData());
            break;
        }
    }

    /**
     * Handles data from the driver.
     * Receives the following:
     * {39.8, 18.5,0.2,0.6,1110}
     * which are values for:
     * {Humidity,Temperature,voltage,current,status}
     *
     * status:
     * // Autonomous 2000 / Manual 1000 +
     * // Lamp on 200 / off 100 +
     * // LED on 20 / off 10
     *
     * So 1110->Manual mode, lamp off, LED off 
     * @param data String data.
     * @param object Drivers can pass objects, this holds it.
     */
    @Override
    public void handleData(String data, Object object) {
        String received = data.replace(" ", "").trim();
        Pattern pattern = Pattern.compile("^\\{(-?[\\d.]+),(-?[\\d.]+),(-?[\\d.]+),(-?[\\d.]+),(-?[\\d]+)\\}$");
        Matcher matcher = pattern.matcher(received);
        if (matcher.find()) {
            if(matcher.groupCount()==5){
                DeviceNotification notification = new DeviceNotification();
                notification.addData("environment","humidity", Float.valueOf(matcher.group(1)));
                notification.addData("environment","temperature", Float.valueOf(matcher.group(2)));
                notification.addData("environment","voltage", Float.valueOf(matcher.group(3)));
                notification.addData("environment","current", Float.valueOf(matcher.group(4)));

                int autonomous = Integer.valueOf(matcher.group(5));
                int lamp = (autonomous>2000)?autonomous-2000:autonomous-1000;
                int led = (lamp>200)?lamp-200:lamp-100;
                
                notification.addData("devicesettings","automanual", autonomous>2000);
                notification.addData("deviceactions","lampswitch", lamp>200);
                notification.addData("deviceactions","ledswitch", led>10);
                dispatchToHost(notification);
            }
        }
    }

    /**
     * Called before class unloading, but after handles unloading.
     */
    @Override
    public void shutdownDevice() {
        throw new UnsupportedOperationException("Not used.");
    }

    /**
     * Called before handles are started.
     */
    @Override
    public void startupDevice() {
        throw new UnsupportedOperationException("Not used.");
    }
    
}
