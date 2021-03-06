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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
     * @param bytes
     * @return float
     */
    public static float byteArrayToFloat(byte[] bytes) {   // Byte to float conversion
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();   
    }

    /**
     * Converts a byte array to a float
     * @param bytes
     * @return float
     */
    public static int byteArrayToInt(byte[] bytes) {   // Byte to float conversion
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
    
    /**
     * Make first letter capital.
     * @param subject
     * @return 
     */
    final public static String ucfirst(String subject){
        return Character.toUpperCase(subject.charAt(0)) + subject.substring(1);
    }
    
}
