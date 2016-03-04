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

package org.pidome.server.connector.tools;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author John Sirach
 */
public class ColorImpl {
    
    static String commaSeparatedRGBPattern = "^(\\d{1,3}),(\\d{1,3}),(\\d{1,3})$";
    static final int HEXLENGTH = 6;
    static final String hexaDecimalPattern = "^#([\\da-fA-F]{1,8})$";    
    
    /**
     * Calculate The HSB value based on a temperature, below 15 degrees is fixed
     * 3800 and above 27 degrees is fixed 40000.
     * @param temp temperature in degrees.
     * @return Array[0]=H, Array[1]=S and Array[2]=B
     */
    public static double[] tempToHsb(String temp){
        return kelvinToHsb(getKelvinFromTemp(temp));
    }

    /**
     * Calculate The HSB value based on a temperature, below 15 degrees is fixed
     * 3800 and above 27 degrees is fixed 40000.
     * @param temp temperature in degrees.
     * @return Array[0]=H, Array[1]=S and Array[2]=B
     */
    public static double[] tempToRgb(String temp){
        return kelvinToRgb(getKelvinFromTemp(temp));
    }
    
    /**
     * Return the kelvins from a given temperature
     * @param temp
     * @return The mapped kelvin value (may not be as exact as the real kelvins scale)
     */
    public static long getKelvinFromTemp(String temp){
        double iTemp = Double.parseDouble(temp);
        long kelvin = 0;
        if(iTemp<=15){
            kelvin = 2700;
        } else if(iTemp>=15 && iTemp <= 27){
            kelvin = (int)MathImpl.map(iTemp, 15, 27, 2700, 27000);
        } else {
            kelvin = 27000;            
        }
        return kelvin;
    }
    
    /**
     * Convert a kelvin color to rgb.
     * @param kelvin
     * @return 
     */
    public static double[] kelvinToRgb(long kelvin){
        return kelvinToColor(kelvin, "rgb");
    }
    
    /**
     * Convert a kelvin color to hsb color.
     * @param kelvin
     * @return 
     */
    public static double[] kelvinToHsb(long kelvin){
        return kelvinToColor(kelvin, "hsb");
    }
    
    /**
     * Calculate th HSB based on Kelvin degrees (reasonable OK between the 1000 and 40000).
     * @param kelvin Kelvin value
     * @param colorType
     * @return Array[0]=H, Array[1]=S and Array[2]=B when colorType hsb otherwise rgb
     */
    public static double[] kelvinToColor(long kelvin, String colorType){
        double tmpCalc;
        double r;
        double g;
        double b;
        float[] hsbvals = null;
        
        if(kelvin>40000) kelvin = 40000;
        if(kelvin<1000) kelvin = 1000;

        kelvin = kelvin / 100;
        
        /// red
        if(kelvin<= 66){
            r = 255;
        } else {
            tmpCalc = kelvin - 60;
            tmpCalc = 329.698727446 * Math.pow(tmpCalc,-0.1332047592);
            r = tmpCalc;
            if(r<0) r = 0;
            if(r>255) r = 255;
        }
        /// green
        if(kelvin <= 66){
            tmpCalc = kelvin;
            tmpCalc = 99.4708025861 * Math.log(tmpCalc) - 161.1195681661;
            g = tmpCalc;
            if(g<0) g = 0;
            if(g>255) g = 255;
        } else {
            tmpCalc = kelvin - 60;
            tmpCalc = 288.1221695283 * Math.pow(tmpCalc,-0.0755148492);
            g = tmpCalc;
            if(g<0) g = 0;
            if(g>255) g = 255;
        }
        /// blue
        if(kelvin >= 66){
            b = 255;
        } else if(kelvin <= 19){
            b = 0;
        } else {
            tmpCalc = kelvin - 10;
            tmpCalc = 138.5177312231 * Math.log(tmpCalc) - 305.0447927307;
            b = tmpCalc;
            if(b < 0) b = 0;
            if(b > 255) b = 255;
        }
        double[] rgb = new double[3];
        switch(colorType){
            case "hsb":
                float[] floatVals = java.awt.Color.RGBtoHSB((int)r, (int)g, (int)b, hsbvals);
                rgb[0] = (double)floatVals[0];
                rgb[1] = (double)floatVals[1];
                rgb[2] = (double)floatVals[2];
                return rgb;
            default:
                rgb[0] = (int)r;
                rgb[1] = (int)g;
                rgb[2] = (int)b;
            return rgb;
        }
    }
    
    /**
     * Hex to rgb.
     * @param hexForRGBConversion
     * @return 
     */
    public static int[] hexToRgb(String hexForRGBConversion) {
        int rgbValue[] = new int[3];
        Pattern hexPattern = Pattern.compile(hexaDecimalPattern);
        Matcher hexMatcher = hexPattern.matcher(hexForRGBConversion);
 
        if (hexMatcher.find()) {
            int hexInt = Integer.valueOf(hexForRGBConversion.substring(1), 16).intValue();
 
            int r = (hexInt & 0xFF0000) >> 16;
            int g = (hexInt & 0xFF00) >> 8;
            int b = (hexInt & 0xFF);
            
            rgbValue[0] = r;
            rgbValue[1] = g;
            rgbValue[2] = b;
            
        } else {
            rgbValue[0] = 0;
            rgbValue[1] = 0;
            rgbValue[2] = 0;
            return rgbValue;
        }
        return rgbValue;
 
    }
 
    /**
     * Convert an rgb to hsb.
     * @param rgb
     * @return 
     */
    public static float[] rgbToHsb(int[] rgb){
        float[] hsbvals = null;
        return java.awt.Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsbvals);
    }
    
    /**
     * RGB to hex.
     * @param rgbForHexConversion
     * @return 
     */
    public static String RGBToHex(String rgbForHexConversion) {
        String hexValue = "";
        Pattern rgbPattern = Pattern.compile(commaSeparatedRGBPattern);
        Matcher rgbMatcher = rgbPattern.matcher(rgbForHexConversion);
 
        int red;
        int green;
        int blue;
        if (rgbMatcher.find()) {
            red = Integer.parseInt(rgbMatcher.group(1));
            green = Integer.parseInt(rgbMatcher.group(2));
            blue = Integer.parseInt(rgbMatcher.group(3));
            Color color = new Color(red, green, blue);
            hexValue = Integer.toHexString(color.getRGB() & 0xffffff);
            int numberOfZeroesNeededForPadding = HEXLENGTH - hexValue.length();
            String zeroPads = "";
            for (int i = 0; i < numberOfZeroesNeededForPadding; i++) {
                zeroPads += "0";
            }
            hexValue = "#" + zeroPads + hexValue;

        } else {
            hexValue = "#000000";
        }
        return hexValue;
    }
    
    /**
     * RGB to hex.
     * @return 
     */
    public static String RGBToHex(int[] rgb) {
        String hexValue = "";
        Color color = new Color(rgb[0], rgb[1], rgb[2]);
        hexValue = Integer.toHexString(color.getRGB() & 0xffffff);
        int numberOfZeroesNeededForPadding = HEXLENGTH - hexValue.length();
        String zeroPads = "";
        for (int i = 0; i < numberOfZeroesNeededForPadding; i++) {
            zeroPads += "0";
        }
        hexValue = "#" + zeroPads + hexValue;
        return hexValue;
    }
    
    /**
     * Returns the rgb values of an hsb color.
     * @param hue
     * @param saturation
     * @param brightness
     * @return int[0] = red,int[1] = green,int[2] = blue
     */
    public static int[] HsbtoRgb(float hue, float saturation, float brightness){
        int[] rgb = new int[3];
        int color = java.awt.Color.HSBtoRGB(hue, saturation, brightness);
        rgb[0] = (color>>16)&0xFF;
        rgb[1] = (color>>8)&0xFF;
        rgb[2] = color&0xFF;
        return rgb;
    }
    
}
