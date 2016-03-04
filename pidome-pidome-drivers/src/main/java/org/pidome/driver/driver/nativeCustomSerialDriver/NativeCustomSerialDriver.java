/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.driver.nativeCustomSerialDriver;

import java.io.IOException;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDataEvent;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriver;
import org.pidome.server.connector.drivers.peripherals.software.ScriptedPeripheralSoftwareDriverForData;

/**
 *
 * @author John Sirach
 */
public class NativeCustomSerialDriver extends PeripheralSoftwareDriver implements ScriptedPeripheralSoftwareDriverForData {

    Logger LOG = LogManager.getLogger(NativeCustomSerialDriver.class);

    boolean running = false;
    boolean expectingReceive = false;
    String expectingDataType = "";
    
    private ScriptEngineManager scriptEngineManager;
    private ScriptEngine        scriptEngine;
    private Invocable           scriptEngineInvocable;
    
    @Override
    public boolean sendData(String data) throws IOException {
        return sendData(data, null);
    }

    @Override
    public boolean sendData(String data, String prefix) throws IOException {
        if(this.getHardwareDriver().isActive()){
            return this.writeBytes(data.getBytes());
        } else {
            return false;
        }
    }

    @Override
    public void driverBaseDataReceived(PeripheralHardwareDataEvent oEvent) {
        switch(oEvent.getEventType()){
            case PeripheralHardwareDataEvent.DATA_RECEIVED:
                try {
                    if(scriptEngineInvocable!=null) scriptEngineInvocable.invokeFunction("handleDriverData", oEvent.getStringData());
                } catch (ScriptException | NoSuchMethodException ex) {
                    LOG.error("Issue executing handleDriverData in script: {}", ex.getMessage(), ex);
                }
            break;
        }
    }
    
    @Override
    public boolean handleDeviceData(Device device, String group, String set, String deviceData) throws IOException {
        /// obsoleted
        return true;
    }

    @Override
    public boolean handleDeviceData(Device device, DeviceCommandRequest request) throws IOException {
        try {
            if(scriptEngineInvocable!=null) scriptEngineInvocable.invokeFunction("handleDeviceData", device, request);
        } catch (ScriptException | NoSuchMethodException ex) {
            LOG.error("Issue executing handleDeviceData in script: {}", ex.getMessage(), ex);
        }
        return true;
    }

    @Override
    public void prepareEngine() {
        scriptEngineManager = new ScriptEngineManager();
        scriptEngine        = scriptEngineManager.getEngineByName("nashorn");
    }

    @Override
    public final void driverStart(){
        LOG.info("Engine creation");
        scriptEngine.put("LOG", LOG);
        scriptEngine.put("driver", this);
        scriptEngineInvocable = (Invocable) scriptEngine;
        try {
            scriptEngineInvocable.invokeFunction("driverStart");
        } catch (ScriptException | NoSuchMethodException ex) {
            LOG.error("Issue executing driverStart in script: {}", ex.getMessage(), ex);
        }
    }

    @Override
    public final void driverStop(){
        try {
            if(scriptEngineInvocable !=null) scriptEngineInvocable.invokeFunction("driverStop");
        } catch (ScriptException | NoSuchMethodException ex) {
            LOG.error("Issue executing driverStop in script: {}", ex.getMessage());
        } catch (Exception ex){
            /// There are possible IOExceptions being thrown from the nashorn script of the hardware closes amazingly fast (like panic unmload).
            LOG.error("Non predefined exception occured: {}", ex.getMessage());
        }
    }
    
    @Override
    public void destructEngine() {
        LOG.info("Engine destruct");
        scriptEngine.put("LOG", null);
        scriptEngine.put("driver", null);
        scriptEngine          = null;
        scriptEngineInvocable = null;
        scriptEngineManager   = null;
    }

    @Override
    public void setScriptData(String script) {
        if(scriptEngine!=null){
            try {
                scriptEngine.eval(script);
            } catch (ScriptException ex) {
                LOG.error("Could not interpret script: {}", ex.getMessage(), ex);
            }
        }
    }
    
}
