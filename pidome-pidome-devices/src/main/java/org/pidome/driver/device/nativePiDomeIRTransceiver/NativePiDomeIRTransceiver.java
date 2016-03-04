/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.device.nativePiDomeIRTransceiver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceNotification;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;

/**
 *
 * @author John
 */
public class NativePiDomeIRTransceiver extends Device {

    static Logger LOG = LogManager.getLogger(NativePiDomeIRTransceiver.class);

    @Override
    public void shutdownDevice() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void startupDevice() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public enum RUNMODE {
        USB,I2C
    }
    
    RUNMODE runMode;
    
    public NativePiDomeIRTransceiver(){}
    
    @Override
    public void prepare(boolean firstPrep){
        setRunMode(RUNMODE.USB);
        DeviceNotification notification = new DeviceNotification();
        notification.addData("ACTIONDATA","RECONOFF", false);
        dispatchToHost(notification);
    }
    
    public final void setRunMode(RUNMODE runMode){
        this.runMode = runMode;
    }
    
    @Override
    public void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        switch(command.getControlType()){
            case TOGGLE:
                switch(command.getControlId()){
                    case "RECONOFF":
                        this.dispatchToDriver(command.getGroupId(), command.getControlId(),"REC");
                    break;
                }
            break;
            case BUTTON:
                switch(command.getControlId()){
                    case "TSTSIGNAL":
                        this.dispatchToDriver(command.getGroupId(), command.getControlId(),"TST");
                    break;
                    case "CNFSIGNAL":
                        this.dispatchToDriver(command.getGroupId(), command.getControlId(),"CNF");
                    break;
                }
            break;
            case DATA:
                switch(command.getControlId()){
                    case "SENDDATA":
                        this.dispatchToDriver(command.getGroupId(), command.getControlId(),command.getCommandValue().toString());
                    break;
                }
            break;
        }
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public void handleData(String data, Object object) {
        LOG.debug("Got data: {}", data);
        DeviceNotification notification = new DeviceNotification();
        switch(data.trim()){
            case "RECON":
                notification.addData("ACTIONDATA","RECONOFF", true);
            break;
            case "RECOFF":
                notification.addData("ACTIONDATA","RECONOFF", false);
            break;
            case "RCVD":
                notification.addData("ACTIONDATA","LASTDATA", data);
            break;
            case "CNF":
                notification.addData("ACTIONDATA","CNFSIGNAL", data);
            break;
            default:
                notification.addData("ACTIONDATA","LASTDATA", data);
            break;
        }
        dispatchToHost(notification);
    }
    
}