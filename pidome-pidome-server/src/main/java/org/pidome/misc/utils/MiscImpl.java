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

package org.pidome.misc.utils;

import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John Sirach
 */
public class MiscImpl {
    
    static Logger LOG = LogManager.getLogger(MiscImpl.class);
    
    /**
     * Adds zeros before a string to result in a fixed string length.
     * Example usage:
     * String sMyNumber = "10.00";
     * String sNumberWithZerosLeading = MiscImpl.setZeroLead(sMyNumber, sMyNumber.length()+2); /// to add two leading zeros
     * String sNumberWithZerosLeading = MiscImpl.setZeroLead(sMyNumber, 5); /// Will add no zeros because length is already 5, but if sMyNumber="1.00" it will add one zero resulting in "01.00"
     * @param sNumber Number as string
     * @param lengthWithLead The total length to return including leading zeros
     * @return string with zeros if needed
     */
    public static String setZeroLeading(String sNumber, int lengthWithLead) {
        while(sNumber.length() < lengthWithLead){
            sNumber = "0" + sNumber;
        }
        return sNumber;
    }
    
    /**
     * Converts a byte array to a float
     * @param b
     * @return float
     */
    public static float byteArrayToFloat(byte[] b) {   // Byte to float conversion
        return ByteBuffer.wrap(b).getFloat();   
    }
    
    /**
     * Converts a byte array to a float
     * @param b
     * @return float
     */
    public static double byteArrayToDouble(byte[] b) {   // Byte to float conversion
        return ByteBuffer.wrap(b).getDouble();
    }
    
    /**
     * Puts a byte array into a hex string.
     * @param array
     * @return 
     */
    public static String byteArrayToHexString(byte[] array){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            int v = array[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase();
    }
    
}
