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

package org.pidome.driver.peripherals.pidomeNativeArduino;

import java.util.Map;
import jssc.SerialPort;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.driver.peripherals.pidomeNativeUSBSerial.PidomeNativeUSBSerial;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;

/**
 *
 * @author John Sirach
 */
public class PidomeNativeArduino extends PidomeNativeUSBSerial {

    static Logger LOG = LogManager.getLogger(PidomeNativeArduino.class);
    
    public PidomeNativeArduino() throws PeripheralHardwareException {
        prepare();
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
    }
    
    @Override
    public final void putPeripheralOptions(Map<String,String> optionSet){
        LOG.debug("Got driver option(s) set: {}", optionSet);
        if(optionSet.containsKey("portspeed")){
            setPortSpeed(Integer.valueOf(optionSet.get("portspeed")));
        }
        setDataBits(SerialPort.DATABITS_8);
        setStopBits(SerialPort.STOPBITS_1);
        setParity(SerialPort.PARITY_NONE);
    }
    
}