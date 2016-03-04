/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComOregon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.pidome.driver.driver.nativeRFXComDriver.RFXComBasicPacketParser;

/**
 *
 * @author John
 */
public class RFXComOregonTools {
 
    /**
     * Calculate temperature.
     * @param high
     * @param low
     * @return 
     */
    protected static double getTemperature(byte high, byte low){
        
        double temp = ((high & 0x7F) << 8 | (low & 0xFF)) * 0.1;
        if ((high & 0x80) != 0){
            return -new BigDecimal(temp).setScale(2, RoundingMode.HALF_UP).doubleValue();
        } else {
            return new BigDecimal(temp).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
    }
    
    /**
     * Return pressure.
     * @param high
     * @param low
     * @return 
     */
    protected static double getPressure(byte high, byte low){
	int barHigh = Integer.parseInt(RFXComBasicPacketParser.decodeSingleByte(high),16);
	int barLow = Integer.parseInt(RFXComBasicPacketParser.decodeSingleByte(low),16);
        int finalBarHigh = barHigh & ~(1 << 7) << 8;
        return new BigDecimal(( finalBarHigh + barLow )).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
    
    /**
     * Returns battery status.
     * @param battCombiInt
     * @return 
     */
    protected static int getBatteryStatus(int battCombiInt){
        return battCombiInt & 0x0F;
    }
 
    /**
     * Get signal.
     * @param battCombiInt
     * @return 
     */
    protected static int getSignalStatus(int battCombiInt){
        return (battCombiInt  & 0xF0) >> 4;
    }
    
}
