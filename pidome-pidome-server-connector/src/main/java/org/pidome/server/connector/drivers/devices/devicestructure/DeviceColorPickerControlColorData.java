/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.devices.devicestructure;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.tools.ColorImpl;

/**
 * Convenience class to turn a specific color command to other color types.
 * @author John
 */
public class DeviceColorPickerControlColorData {
    
    static Logger LOG = LogManager.getLogger(DeviceColorPickerControlColorData.class);
    
    Map<String,Object> finalColor = new HashMap<>();
    
    public DeviceColorPickerControlColorData(Object value){
        Map<String,Object> colorSet = (Map<String,Object>)value;
        
        Map<String,Double> finalHsb = new HashMap<>();
        finalHsb.put("h", 0.0d);
        finalHsb.put("s", 0.0d);
        finalHsb.put("b", 0.0d);
        
        Map<String,Integer> finalRGB = new HashMap<>();
        finalRGB.put("r", 0);
        finalRGB.put("g", 0);
        finalRGB.put("b", 0);
        
        String finalHex = "#000000";

        Long finalKelvin = 0L;
        
        try {
            if(colorSet.containsKey("hex")){
                finalHex = (String)colorSet.get("hex");
                int[] rgbFromHex = ColorImpl.hexToRgb((String)colorSet.get("hex"));
                finalRGB.put("r", rgbFromHex[0]);
                finalRGB.put("g", rgbFromHex[1]);
                finalRGB.put("b", rgbFromHex[2]);
                float[] fromHex = ColorImpl.rgbToHsb(rgbFromHex);
                finalHsb.put("h", fromHex[0]/1d);
                finalHsb.put("s", fromHex[1]/1d);
                finalHsb.put("b", fromHex[2]/1d);
            } else if (colorSet.containsKey("r") && colorSet.containsKey("g") && colorSet.containsKey("b")){
                finalRGB.put("r", ((Number)colorSet.get("r")).intValue());
                finalRGB.put("g", ((Number)colorSet.get("g")).intValue());
                finalRGB.put("b", ((Number)colorSet.get("b")).intValue());
                int[] list = new int[3];
                list[0] = finalRGB.get("r");
                list[1] = finalRGB.get("g");
                list[2] = finalRGB.get("b");
                finalHex = ColorImpl.RGBToHex(list);
                float[] fromRGB = ColorImpl.rgbToHsb(list);
                finalHsb.put("h", fromRGB[0]/1d);
                finalHsb.put("s", fromRGB[1]/1d);
                finalHsb.put("b", fromRGB[2]/1d);
            } else if (colorSet.containsKey("h") && colorSet.containsKey("s") && colorSet.containsKey("b")){
                finalHsb.put("h", ((Number)colorSet.get("h")).doubleValue());
                finalHsb.put("s", ((Number)colorSet.get("s")).doubleValue());
                finalHsb.put("b", ((Number)colorSet.get("b")).doubleValue());
                int[] RGBFromHSB = ColorImpl.HsbtoRgb((float)(finalHsb.get("h")/1f), (float)(finalHsb.get("s")/1f), (float)(finalHsb.get("b")/1f));
                finalRGB.put("r", RGBFromHSB[0]);
                finalRGB.put("g", RGBFromHSB[1]);
                finalRGB.put("b", RGBFromHSB[2]);
                finalHex = ColorImpl.RGBToHex(RGBFromHSB);
            } else if (colorSet.containsKey("kelvin")){
                finalKelvin = (Long)colorSet.get("kelvin");
                double[] fromKelvin = ColorImpl.kelvinToHsb((Long)colorSet.get("kelvin"));
                finalHsb.put("h", fromKelvin[0]/1d);
                finalHsb.put("s", fromKelvin[1]/1d);
                finalHsb.put("b", fromKelvin[2]/1d);
                int[] RGBFromHSB = ColorImpl.HsbtoRgb((float)(finalHsb.get("h")/1f), (float)(finalHsb.get("s")/1f), (float)(finalHsb.get("b")/1f));
                finalRGB.put("r", RGBFromHSB[0]);
                finalRGB.put("g", RGBFromHSB[1]);
                finalRGB.put("b", RGBFromHSB[2]);
                finalHex = ColorImpl.RGBToHex(RGBFromHSB);
            }
        } catch (Exception ex){
            LOG.error("Could not convert: {}", ex.getMessage(), ex);
        }
        finalColor.put("hex", finalHex);
        finalColor.put("rgb", finalRGB);
        finalColor.put("hsb", finalHsb);
        finalColor.put("kelvin", finalKelvin);
    }
    
    protected final Map<String,Object> getFullColorSet(){
        return finalColor;
    }
    
    /**
     * Returns the raw color map to be used for notifications.
     * @return 
     */
    public final Map<String,Object> getNotificationColorMap(){
        return Collections.unmodifiableMap(finalColor);
    }
    
    /**
     * Return the RGB values
     * This function returns a map with the keys r,g and b
     * @return 
     */
    public final Map<String,Integer> getRGB(){
        return (Map<String,Integer>)finalColor.get("rgb");
    }
    
    /**
     * Return the RGB values
     * This function returns a map with the keys h,s and b
     * @return 
     */
    public final Map<String,Double> getHSB(){
        return (Map<String,Double>)finalColor.get("hsb");
    }
    
    /**
     * Returns current kelvin value.
     * Kelvin value is only available when kelvin is used to set the data. Otherwise always returns 0.
     * @return 
     */
    public final Long getKelvin(){
        return (Long)finalColor.get("kelvin");
    }
    
    /**
     * Returns current hex value.
     * Returns the hex values as #000000
     * @return 
     */
    public final String getHex(){
        return (String)finalColor.get("hex");
    }
    
}