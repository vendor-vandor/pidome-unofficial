/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.peripherals.pidomeNativeUSBSerial;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TooManyListenersException;
import jssc.SerialNativeInterface;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDataEvent;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriver;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriverInterface;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;

/**
 *
 * @author John
 */
public class PidomeNativeUSBSerial extends PeripheralHardwareDriver implements PeripheralHardwareDriverInterface, SerialPortEventListener {

    String peripheralPort = null;
    
    /**
     * Get logger.
     */
    static Logger LOG = LogManager.getLogger(PidomeNativeUSBSerial.class);
    
    private SerialPort port;
    private int serialSpeed = SerialPort.BAUDRATE_19200;
    private int databits    = SerialPort.PARITY_NONE;
    private int stopbits    = SerialPort.STOPBITS_1;
    private int parity      = SerialPort.PARITY_NONE;
    
    private boolean noOp    = false;
    
    boolean started = false;
    boolean opened  = false;
    
    Thread internalBlockedListener;
    
    public PidomeNativeUSBSerial() throws PeripheralHardwareException {
        prepare();
        PeripheralOption noOp = new PeripheralOption("No operation", PeripheralOption.OPTION_SELECT);
        noOp.addSelectOption(0, "No (default)", "0");
        noOp.addSelectOption(1, "Yes", "1");
        addPeripheralOption("noop", noOp);
        PeripheralOption portSpeed = new PeripheralOption("Set port speed", PeripheralOption.OPTION_SELECT);
        portSpeed.addSelectOption(SerialPort.BAUDRATE_300, "300 baud", SerialPort.BAUDRATE_300);
        portSpeed.addSelectOption(SerialPort.BAUDRATE_600, "600 baud", SerialPort.BAUDRATE_600);
        portSpeed.addSelectOption(SerialPort.BAUDRATE_1200, "1200 baud", SerialPort.BAUDRATE_1200);
        portSpeed.addSelectOption(SerialPort.BAUDRATE_4800, "4800 baud", SerialPort.BAUDRATE_4800);
        portSpeed.addSelectOption(SerialPort.BAUDRATE_9600, "9600 baud", SerialPort.BAUDRATE_9600);
        portSpeed.addSelectOption(SerialPort.BAUDRATE_14400, "14400 baud", SerialPort.BAUDRATE_14400);
        portSpeed.addSelectOption(SerialPort.BAUDRATE_19200, "19200 baud", SerialPort.BAUDRATE_19200);
        portSpeed.addSelectOption(SerialPort.BAUDRATE_38400, "38400 baud", SerialPort.BAUDRATE_38400);
        portSpeed.addSelectOption(SerialPort.BAUDRATE_57600, "57600 baud", SerialPort.BAUDRATE_57600);
        portSpeed.addSelectOption(SerialPort.BAUDRATE_115200, "115200 baud", SerialPort.BAUDRATE_115200);
        addPeripheralOption("portspeed", portSpeed);
        
        PeripheralOption dataBits = new PeripheralOption("Set Data bits", PeripheralOption.OPTION_SELECT);
        dataBits.addSelectOption(SerialPort.DATABITS_5, "5", SerialPort.DATABITS_5);
        dataBits.addSelectOption(SerialPort.DATABITS_6, "6", SerialPort.DATABITS_6);
        dataBits.addSelectOption(SerialPort.DATABITS_7, "7", SerialPort.DATABITS_7);
        dataBits.addSelectOption(SerialPort.DATABITS_8, "8", SerialPort.DATABITS_8);
        addPeripheralOption("databits", dataBits);
        
        PeripheralOption stopBits = new PeripheralOption("Set Stop bits", PeripheralOption.OPTION_SELECT);
        stopBits.addSelectOption(SerialPort.STOPBITS_1, "1", SerialPort.STOPBITS_1);
        stopBits.addSelectOption(SerialPort.STOPBITS_2, "2", SerialPort.STOPBITS_2);
        stopBits.addSelectOption(SerialPort.STOPBITS_1_5, "1.5", SerialPort.STOPBITS_1_5);
        addPeripheralOption("stopbits", stopBits);
        
        PeripheralOption parity = new PeripheralOption("Set Parity", PeripheralOption.OPTION_SELECT);
        parity.addSelectOption(SerialPort.PARITY_NONE, "None", SerialPort.PARITY_NONE);
        parity.addSelectOption(SerialPort.PARITY_ODD, "Odd", SerialPort.PARITY_ODD);
        parity.addSelectOption(SerialPort.PARITY_EVEN, "Even", SerialPort.PARITY_EVEN);
        parity.addSelectOption(SerialPort.PARITY_MARK, "Mark", SerialPort.PARITY_MARK);
        parity.addSelectOption(SerialPort.PARITY_SPACE, "Space", SerialPort.PARITY_SPACE);
        addPeripheralOption("parity", parity);
        
    }

    @Override
    public void putPeripheralOptions(Map<String,String> optionSet){
        LOG.debug("Got driver option(s) set: {}", optionSet);
        if(optionSet.containsKey("noop")){
            noOp = optionSet.get("noop").equals("1");
        }
        if(!noOp){
            if(optionSet.containsKey("portspeed")){
                setPortSpeed(Integer.valueOf(optionSet.get("portspeed")));
            }
            if(optionSet.containsKey("databits")){
                setDataBits(Integer.valueOf(optionSet.get("databits")));
            }
            if(optionSet.containsKey("stopbits")){
                setStopBits(Integer.valueOf(optionSet.get("stopbits")));
            }
            if(optionSet.containsKey("parity")){
                setParity(Integer.valueOf(optionSet.get("parity")));
            }
        }
    }
    
    /**
     * Sets the port speed. Must be called before initDriver().
     * @param portSpeed 
     */
    public final void setPortSpeed(int portSpeed){
        serialSpeed = portSpeed;
    }

    /**
     * Sets the data bits. Must be called before initDriver(). 
     * @param databits
     */
    public final void setDataBits(int databits){
        this.databits = databits;
    }
    
    /**
     * Sets the stop bits. Must be called before initDriver(). 
     * @param stopbits
     */
    public final void setStopBits(int stopbits){
        this.stopbits = stopbits;
    }
    
    /**
     * Sets the parity. Must be called before initDriver(). 
     * @param parity
     */
    public final void setParity(int parity){
        this.parity = parity;
    }
    
    @Override
    public void initDriver() throws PeripheralHardwareException {
        if(!noOp){
            try {
                LOG.info("Initializing hardware driver: {}", this.getFriendlyName());
                System.setProperty(SerialNativeInterface.PROPERTY_JSSC_NO_TIOCEXCL,SerialNativeInterface.PROPERTY_JSSC_NO_TIOCEXCL);
                LOG.debug("Port '{}' init", peripheralPort);
                peripheralPort = getPort();
                port = new SerialPort(peripheralPort);
                LOG.debug("Port '{}' ready for data. Started with speed: {}, data bits: {}, stop bits: {}, parity: {}",peripheralPort,serialSpeed, databits, stopbits, parity);
            } catch (UnsatisfiedLinkError ex){
                throw new PeripheralHardwareException("Could not load serial communication library: " + ex.getMessage());
            }
        }
        LOG.info("Serial running in noOp mode (Connection handled by peripheral software driver instead of peripheral hardware driver) on port '{}': {}", this.getPort(), noOp);
    }
    
    @Override
    public void releaseDriver(){
        if(!noOp){
            LOG.debug("Releasing driver");
            closePort();
        }
    }
    
    @Override
    public void startDriver() throws PeripheralHardwareException {
        if(!noOp){
            LOG.info("Starting hardware driver: {}", this.getFriendlyName());
            try {
                port.openPort();
                port.setParams(serialSpeed, databits, stopbits, parity);
                opened = true;
            } catch (SerialPortException ex) {
                throw new PeripheralHardwareException("Could not open port: " + ex.getMessage());
            }
            if(port!=null){
                try {
                    startInternalListener();
                } catch (TooManyListenersException | SerialPortException ex) {
                    throw new PeripheralHardwareException("Can not communicate on port '" + peripheralPort + "'. Did you use the correct baudrate?");
                }
                started = true;
                LOG.debug("Driver started");
            } else {
                throw new PeripheralHardwareException("Port is not opened, driver will not start");
            }
        }
    }
    
    @Override
    public void stopDriver() {
        if(!noOp){
            LOG.info("Stopping hardware driver: {}", this.getFriendlyName());
            stopInternalListener();
        }
    }

    private synchronized void startInternalListener() throws TooManyListenersException, SerialPortException {
        if(port!=null){
            if (internalBlockedListener != null && internalBlockedListener.isAlive()) {
                internalBlockedListener.interrupt();
            }
            internalBlockedListener = new Thread() {
                @Override
                public final void run() {
                    Thread.currentThread().setName(PidomeNativeUSBSerial.this.getFriendlyName()+":"+PidomeNativeUSBSerial.this.getPort());
                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        boolean reading = false;
                        while(opened){
                            if(port.getInputBufferBytesCount()==0 && reading == true){
                                dispatchData(PeripheralHardwareDataEvent.DATA_RECEIVED,bos);
                                bos = new ByteArrayOutputStream();
                                reading = false;
                            } else if(port.getInputBufferBytesCount()>0){
                                bos.write(port.readBytes(port.getInputBufferBytesCount()));
                                Thread.sleep(10);
                                reading = true;
                            } else {
                                bos.write(port.readBytes(1));
                                Thread.sleep(10);
                                reading = true;
                            }
                        }
                    } catch (SerialPortException ex) {
                        LOG.error("Port reading error (by failure or driver stop), bailing out: {}, ({})", ex.getMessage(), ex.getMethodName(), ex);
                        closePort();
                    } catch (InterruptedException | IOException | NullPointerException ex) {
                        LOG.error("Port reading error (IOError in byte stream), bailing out: {}", ex.getMessage(), ex);
                        closePort();
                    }
                }
            };
            internalBlockedListener.start();
        }
    }
    
    private synchronized void stopInternalListener(){
        if(port!=null){
            try {
                if (internalBlockedListener != null && internalBlockedListener.isAlive()) {
                    internalBlockedListener.interrupt();
                }
            } catch (NullPointerException e){
                //// this one is thrown when the event listener has already been removed (because of data recieve error for example)
            }
        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        //// Future advanced usage.
    }
    
    @Override
    public final String readPort() throws IOException {
        /// Not used in this class.
        throw new IOException("Not used, use the event handler");
    }
    
    /**
     * Write to port in bytes
     * @param bytes
     * @throws IOException 
     */
    @Override
    public void writePort(byte[] bytes) throws IOException {
        if(!noOp){
            if(port!=null){
                try {
                    port.writeBytes(bytes);
                } catch (SerialPortException ex) {
                    throw new IOException("Can not write, Serial Exception: " + ex.getMessage());
                }
            } else {
                LOG.error("Port is not available");
            }
        }
    }
    
    public final void closePort(){
        if(!noOp){
            opened = false;
            LOG.info("Closing hardware driver: {}", this.getFriendlyName());
            if(port!=null){
                LOG.info("Closing port '" + peripheralPort + "'");
                try {
                    port.closePort();
                } catch (NullPointerException ex){
                    LOG.error("Bad peripheral disconnect? Possible port lock happening.");
                } catch (SerialPortException ex) {
                    LOG.error("Port closing error: ", ex);
                }
                port = null;
                LOG.info("Connection on port '" + peripheralPort + "' closed");
            } else {
                LOG.info("No known connection on '" + peripheralPort + "' or port already closed.");
            }
        }
    }
}

