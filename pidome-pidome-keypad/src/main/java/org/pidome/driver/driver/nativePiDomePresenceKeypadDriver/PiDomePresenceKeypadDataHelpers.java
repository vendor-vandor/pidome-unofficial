/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativePiDomePresenceKeypadDriver;

/**
 *
 * @author John
 */
public class PiDomePresenceKeypadDataHelpers {
    public enum PersonRequestType {
        /**
         * Person request.
         */
        PE,
        /**
         * Next person.
         */
        PN,
        /**
         * Previous person.
         */
        PP;
    }
    
    public enum TokenActionTypes {
        ADD_CARD,
        REMOVE_CARD,
        ADD_CODE,
        REMOVE_CODE,
        AUTH_MASTER_CARD,
        AUTH_NORMAL_CARD,
        AUTH_MASTER_PIN,
        AUTH_NORMAL_PIN
    }
    
}
