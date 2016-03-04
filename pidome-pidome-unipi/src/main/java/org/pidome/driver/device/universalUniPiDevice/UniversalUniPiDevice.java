/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.device.universalUniPiDevice;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import org.pidome.plugins.devices.uniPi.UniPiAnalogInputCommand;
import org.pidome.plugins.devices.uniPi.UniPiAnalogOutputCommand;
import org.pidome.plugins.devices.uniPi.UniPiDigitalInputCommand;
import org.pidome.plugins.devices.uniPi.UniPiRelayCommand;
import org.pidome.plugins.devices.uniPi.UniPiTemperatureCommand;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceNotification;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;

/**
 *
 * @author John
 */
public class UniversalUniPiDevice  extends Device {

    DecimalFormatSymbols mySymbols = new DecimalFormatSymbols();
    
    public UniversalUniPiDevice(){
        super();
        mySymbols.setDecimalSeparator('.');
    }
    
    
    @Override
    public void handleCommandRequest(DeviceCommandRequest dcr) throws UnsupportedDeviceCommandException {
        /// only analog outputs and relays do output.
        switch(dcr.getGroupId()){
            case "relay":
                try {
                    Integer.parseInt(dcr.getControlId());
                    this.dispatchToDriver(dcr);
                } catch (NumberFormatException ex){
                    throw new UnsupportedDeviceCommandException("Use a number (int) in the control id which correspondents with the relay on the board");
                }
            break;
            case "ao":
                try {
                    float value = Float.parseFloat(dcr.getCommandValueData().toString());
                    if(value >= 0.0 && value <= 10.0){
                        this.dispatchToDriver(dcr);
                    } else {
                        throw new UnsupportedDeviceCommandException("The analog output is from and including 0.0 to 10.0");
                    }
                } catch (NumberFormatException ex){
                    throw new UnsupportedDeviceCommandException("An analog output should be numeric");
                }
            break;
            default:
                throw new UnsupportedDeviceCommandException("Only 'relay' and 'ao' (analog out) are supported");
        }
    }

    @Override
    public void handleData(String supplement, Object o) {
        if(o instanceof UniPiRelayCommand){
            DeviceNotification not = new DeviceNotification();
            not.addData("relay", ((UniPiRelayCommand)o).getCircuit(), ((UniPiRelayCommand)o).getValue()==1);
            this.dispatchToHost(not);
        } else if(o instanceof UniPiAnalogInputCommand){
            DeviceNotification not = new DeviceNotification();
            DecimalFormat df = new DecimalFormat("#.####");
            df.setDecimalFormatSymbols(mySymbols);
            df.setRoundingMode(RoundingMode.HALF_UP);
            not.addData("ai", ((UniPiAnalogInputCommand)o).getCircuit(), Float.valueOf(df.format(((Number)((UniPiAnalogInputCommand)o).getValue()).doubleValue())));
            this.dispatchToHost(not);
        } else if(o instanceof UniPiAnalogOutputCommand){
            DeviceNotification not = new DeviceNotification();
            not.addData("ao", ((UniPiAnalogOutputCommand)o).getCircuit(), ((UniPiAnalogOutputCommand)o).getValue());
            this.dispatchToHost(not);
        } else if(o instanceof UniPiTemperatureCommand){
            DeviceNotification not = new DeviceNotification();
            DecimalFormat df = new DecimalFormat("#.##");
            df.setDecimalFormatSymbols(mySymbols);
            df.setRoundingMode(RoundingMode.HALF_UP);
            not.addData(supplement, ((UniPiTemperatureCommand)o).getCircuit(), Float.valueOf(df.format(((Number)((UniPiTemperatureCommand)o).getValue()).doubleValue())));
            this.dispatchToHost(not);
        } else if(o instanceof UniPiDigitalInputCommand){
            DeviceNotification not = new DeviceNotification();
            not.addData("input", ((UniPiDigitalInputCommand)o).getCircuit(), ((UniPiDigitalInputCommand)o).getValue());
            this.dispatchToHost(not);
        }
    }

    @Override
    public void shutdownDevice() {
        /// goodbye.
    }

    @Override
    public void startupDevice() {
        /// start up.
    }
    
}