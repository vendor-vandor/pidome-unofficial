/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.peripherals.pidomeNativeI2CBus;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.system.SystemInfo;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDataEvent;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriver;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriverInterface;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;


/**
 *
 * @author John Sirach
 */
public class PidomeNativeI2CBus  extends PeripheralHardwareDriver implements PeripheralHardwareDriverInterface {

    int busNumber = -1;
    I2CBus bus;
    boolean started;
    
    /**
     * Get logger.
     */
    static Logger LOG = LogManager.getLogger(PidomeNativeI2CBus.class);
    
    public PidomeNativeI2CBus() throws PeripheralHardwareException {
        prepare();
    }
    
    @Override
    public void initDriver() throws PeripheralHardwareException {
        try {
            LOG.debug("Starting peripheral driver for revision: {}", SystemInfo.getRevision());
            try {
                if(Integer.parseInt(SystemInfo.getRevision())<4){
                    busNumber = 0;
                } else {
                    busNumber = 1;
                }
            } catch (NumberFormatException ex){
                //// All the 2.0 revisons are higher then 4 or end with a non numeric character
                busNumber = 1;
            }
            LOG.debug("Going to use bus number: {}", busNumber);
        } catch (InterruptedException | IOException ex) {
            LOG.debug("Could not determine your pi revision, can not start correct i2c bus");
            throw new PeripheralHardwareException("Could not determine your pi revision, can not start correct i2c bus");
        }
        try {
            bus = I2CFactory.getInstance(busNumber);
        } catch (IOException ex) {
            LOG.error("Could not get I2C bus {}", busNumber);
            throw new PeripheralHardwareException("Could not get I2C bus " + busNumber);
        }
    }
    
    @Override
    public PeripheralVersion getSoftwareId() throws PeripheralHardwareException {
        createPeripheralVersion("NATIVE_PIDOMEI2C_DRIVER_0.0.1");
        return getVersion();
    }
    
    @Override
    public void startDriver() throws PeripheralHardwareException {
        ////A real start is not needed, the rasp is a master and will work in an
        ////action -> reaction method, if the rasp where a slave we would create an
        ////internal listener listening for incomming data from the bus.
        started = true;
    }

    @Override
    public void stopDriver() {
        /// there is nothing to stop
        started = false;
    }

    @Override
    public String readPort() throws IOException {
        throw new UnsupportedOperationException("This, is not needed. Read and write are integrated."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public final void writePort(String string) throws IOException {
        LOG.debug("Got string for device: {}", string);
        String[] actions = string.split(":");
        try {
            int deviceId = Integer.decode(actions[0]);
            int readAddr = Integer.decode(actions[2]);
            byte writeSingleAddr = Byte.decode(actions[2]);
            int readByte;
            I2CDevice device = bus.getDevice(deviceId);
            switch(actions[1]){
                case "READ":
                    readByte = Integer.parseInt(actions[3]);
                    byte[] buffer = new byte[readByte];
                    LOG.trace("Starting read actions on device id: {}, address: {}, length: {}", deviceId, readAddr, readByte);
                    device.read(readAddr, buffer, 0, readByte);
                    LOG.trace("Received from device: {}" , buffer);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream(buffer.length);
                    bos.write(buffer, 0, buffer.length);
                    dispatchData(PeripheralHardwareDataEvent.DATA_RECEIVED,actions[0]+":"+actions[2]+":OBJECT",bos);
                break;
                case "WRITE":
                    try {
                        int intData;
                        if(actions.length==4){
                            LOG.trace("Starting write actions on device id: {}, address: {}, data: {}", deviceId, readAddr, actions[3]);
                            if(actions[3].contains(",")){
                                String [] dataParts = actions[3].split(",");
                                if(dataParts.length > 0){
                                    byte[] sendBuffer = new byte[dataParts.length];
                                    for(int i=0; i<dataParts.length;i++){
                                        if(isInt(dataParts[i])){
                                            sendBuffer[i] = (byte)Integer.parseInt(dataParts[i]);
                                        }
                                    }
                                    device.write(readAddr,sendBuffer,0,dataParts.length);
                                } else {
                                    if(isInt(actions[3])){
                                        intData = Integer.parseInt(actions[3]);
                                        device.write(readAddr,(byte)intData);
                                    } else {
                                        device.write(readAddr, actions[3].getBytes(), 0, actions[3].getBytes().length);
                                    }
                                }
                            } else {
                                if (isInt(actions[3])) {
                                    intData = Integer.parseInt(actions[3]);
                                    device.write(readAddr, (byte) intData);
                                } else {
                                    device.write(readAddr, actions[3].getBytes(), 0, actions[3].getBytes().length);
                                }
                            }
                        } else if(actions.length==3) {
                            LOG.trace("Starting write actions on device id: {}, address: {}", deviceId, writeSingleAddr);
                            device.write(writeSingleAddr);
                        }
                    } catch (IndexOutOfBoundsException ex){
                        LOG.error("I2C command not well specified, incorrect amount of parameters");
                    }
                break;
            }
        } catch (IndexOutOfBoundsException ex){
            throw new IOException("Incorrect command parameter");
        } catch (NullPointerException ex){
            throw new IOException("There is no I2C bus available");
        }
    }
    
    @Override
    public void releaseDriver() {
        try {
            /// releasing is not needed
            bus.close();
        } catch (IOException ex) {
            LOG.error("Could not close the I2C bus");
        }
    }
    
    boolean isInt(String data){
        return data.matches("[0-9]+");
    }

    @Override
    public void writePort(byte[] bytes) throws IOException {
        writePort(new String(bytes));
    }
    
}
