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

package org.pidome.client.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John Sirach
 */
public class ColorImpl {
    
    static Logger LOG = LogManager.getLogger(ColorImpl.class);
    
    static String commaSeparatedRGBPattern = "^(\\d{3}),(\\d{3}),(\\d{3})$";
    static final int HEXLENGTH = 6;
    static final String hexaDecimalPattern = "^#([\\da-fA-F]{1,8})$";    
    
    /**
     * Calculate The HSB value based on a temperature, below 15 degrees is fixed 12000 and above 27 degrees is fixed 1000.
     * @param temp temperature in degrees.
     * @return Array[0]=H, Array[1]=S and Array[2]=B
     */
    public static float[] tempToHsb(double temp){
        long kelvin = 0;
        if(temp<=15){
            kelvin = 12000;
        } else if(temp>=15 && temp <= 27){
            kelvin = (long)MathImpl.map(temp, 27, 15, 1000, 12000);
        } else {
            kelvin = 1000;
        }
        LOG.info("Got kelvin: {}", kelvin);
        return kelvinToHsb(kelvin);
    }

    /**
     * Calculate The HSB value based on a temperature, below 15 degrees is fixed 1000 and above 27 degrees is fixed 12000.
     * @param temp temperature in degrees.
     * @return Array[0]=H, Array[1]=S and Array[2]=B
     */
    public static float[] tempToHsbInverted(double temp){
        long kelvin = 0;
        if(temp<=15){
            kelvin = 1000;
        } else if(temp>=15 && temp <= 27){
            kelvin = (long)MathImpl.map(temp, 15, 27, 1000, 12000);
        } else {
            kelvin = 12000;            
        }
        return kelvinToHsb(kelvin);
    }
    
    /**
     * Calculate th HSB based on Kelvin degrees (reasonable OK between the 1000 and 40000).
     * @param kelvin Kelvin value
     * @return Array[0]=H, Array[1]=S and Array[2]=B
     */
    public static float[] kelvinToHsb(long kelvin){
        double tmpCalc;
        double r;
        double g;
        double b;
        
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
        Color c = Color.rgb((int)r, (int)g, (int)b);
        float[] f = new float[3];
        f[0] = (float)c.getHue();
        f[1] = (float)c.getSaturation();
        f[2] = (float)c.getBrightness();
        return f;
    }
    
    public static int[] hexToRgb(String hexForRGBConversion) {
        int rgbValue[] = new int[3];
        Pattern hexPattern = Pattern.compile(hexaDecimalPattern);
        Matcher hexMatcher = hexPattern.matcher(hexForRGBConversion);
 
        if (hexMatcher.find()) {
            int hexInt = Integer.valueOf(hexForRGBConversion.substring(1), 16);
 
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
     * String MUST be #RRGGBB
     * @param colorStr
     * @return 
     */
    public static Color hexToRgbColor(String colorStr) {
        try {
            return Color.rgb(
                    Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                    Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                    Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
        } catch (Exception ex){
            return Color.color(0,0,0);
        }
    }
    
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
            //// not using awt because current java pi implementation does not support awt functions.
            hexValue = String.format("#%02x%02x%02x", red, green, blue);
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
    
    public static String RGBToHex(float r, float g, float b) {
        String rs = setZeroLeading(Integer.toHexString((int)(r * 256)),2);
        String gs = setZeroLeading(Integer.toHexString((int)(g * 256)),2);
        String bs = setZeroLeading(Integer.toHexString((int)(b * 256)),2);
        return (rs.equals("100")?"ff":rs) + (gs.equals("100")?"ff":gs) + (bs.equals("100")?"ff":bs);
    }
    
    public static String hsbToHex(float hue, float saturation, float value) {
        hue = ((float)1/(float)360) * hue;
        
        int h = (int)(hue * 6);
        float f = hue * 6 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);

        switch (h) {
          case 0: return RGBToHex(value, t, p);
          case 1: return RGBToHex(q, value, p);
          case 2: return RGBToHex(p, value, t);
          case 3: return RGBToHex(p, q, value);
          case 4: return RGBToHex(t, p, value);
          case 5: return RGBToHex(value, p, q);
          default: throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + h + ", " + saturation + ", " + value);
        }
    }

    public static String setZeroLeading(String sNumber, int lengthWithLead) {
        while(sNumber.length() < lengthWithLead){
            sNumber = "0" + sNumber;
        }
        return sNumber;
    }
    
    public static int[] HsbToRgb(float hue, float saturation, float value) {

        hue = ((float)1/(float)360) * hue;
        
        int h = (int)(hue * 6);
        float f = hue * 6 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);

        switch (h) {
          case 0: return rgbToIntArray(value, t, p);
          case 1: return rgbToIntArray(q, value, p);
          case 2: return rgbToIntArray(p, value, t);
          case 3: return rgbToIntArray(p, q, value);
          case 4: return rgbToIntArray(t, p, value);
          case 5: return rgbToIntArray(value, p, q);
          default: throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + value);
        }
    }

    public static int[] rgbToIntArray(float r, float g, float b) {
        int[] rgb = new int[3];
        rgb[0] = (int)r;
        rgb[1] = (int)g;
        rgb[2] = (int)b;
        return rgb;
    }    
    
}
