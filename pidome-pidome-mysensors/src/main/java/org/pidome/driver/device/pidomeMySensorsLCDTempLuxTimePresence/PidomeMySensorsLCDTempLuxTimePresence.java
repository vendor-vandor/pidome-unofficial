/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.device.pidomeMySensorsLCDTempLuxTimePresence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.driver.device.pidomeNativeMySensorsDevice14.PidomeNativeMySensorsDevice14;
import org.pidome.mysensors.PidomeNativeMySensorsDeviceResources14;
import org.pidome.server.connector.drivers.devices.DeviceNotification;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.shareddata.SharedServerTimeService;
import org.pidome.server.connector.shareddata.SharedServerTimeServiceListener;
import org.pidome.server.connector.shareddata.SharedUserStatusService;
import org.pidome.server.connector.shareddata.SharedUserStatusServiceListener;

/**
 *
 * @author John
 */
public class PidomeMySensorsLCDTempLuxTimePresence extends PidomeNativeMySensorsDevice14 implements SharedUserStatusServiceListener,SharedServerTimeServiceListener {
    
    static Logger LOG = LogManager.getLogger(PidomeMySensorsLCDTempLuxTimePresence.class);
    private PidomeNativeMySensorsDeviceResources14 resources = new PidomeNativeMySensorsDeviceResources14();
    
    public PidomeMySensorsLCDTempLuxTimePresence(){
        super();
    }
    
    @Override
    public void setNewUserStatus(int currentStatusId, String currentStatusName) {
        dispatchToDriver("1", String.valueOf(getResources().getIntByVar("VAR_1")), String.valueOf(currentStatusId));
    }
    
    @Override
    public void handleData(String data, Object object) {
        String[] lineParts = data.split(";");
        try {
            String subType = getResources().getVarType(Integer.parseInt(lineParts[4]));
            DeviceControl command = getFullCommandSet().getControlsGroup(lineParts[1]).getDeviceControl(subType);
            DeviceNotification notification = new DeviceNotification();
            switch(subType){
                case "V_VAR1":
                    if (lineParts[2].equals("2")){
                        dispatchToDriver(lineParts[1], String.valueOf(getResources().getIntByVar("VAR_1")), String.valueOf(SharedUserStatusService.getCurrentStatusId()));
                        notification.addData(lineParts[1], subType, SharedUserStatusService.getCurrentStatusId());
                    }
                break;
                default:
                    switch(command.getDataType()){
                        case STRING:
                        case HEX:
                            notification.addData(lineParts[1], subType, lineParts[5]);
                        break;
                        case INTEGER:
                            notification.addData(lineParts[1], subType, Integer.valueOf(lineParts[5]));
                        break;
                        case FLOAT:
                            notification.addData(lineParts[1], subType, Float.valueOf(lineParts[5]));
                        break;
                        case BOOLEAN:
                            notification.addData(lineParts[1], subType, Boolean.valueOf(lineParts[5]));
                        break;
                    }
                break;
            }
            dispatchToHost(notification);
        } catch (Exception ex){
            LOG.error("Error handling: {}, error: {}", data, ex.getMessage(), ex);
        }
    }
    
    @Override
    public final void shutdownDevice() {
        SharedUserStatusService.removeListener(this);
        SharedServerTimeService.removeListener(this);
    }
    
    @Override
    public final void startupDevice(){
        SharedUserStatusService.addListener(this);
        SharedServerTimeService.addListener(this);
        dispatchToDriver("1", String.valueOf(getResources().getIntByVar("VAR_1")), String.valueOf(SharedUserStatusService.getCurrentStatusId()));
    }

    @Override
    public void handleNewTimeServiceMinute() {
        dispatchToDriver("255;3", "1", String.valueOf(SharedServerTimeService.getEpochSeconds()));
    }
    
}