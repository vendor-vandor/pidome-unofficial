/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.plugins.sms.smstools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.plugins.messengers.MessengerException;
import org.pidome.server.connector.plugins.messengers.sms.SMSMessengerBase;
import org.pidome.plugins.freeform.utilityMeasurement.UtilityMeasurement;

/**
 *
 * @author John
 */
public class Smstools extends SMSMessengerBase {
    
    static Logger LOG = LogManager.getLogger(UtilityMeasurement.class);
    
    /**
     * Constructor.
     */
    public Smstools(){
        constructSettings();
    }
    
    /**
     * Sends an sms message.
     * @param message
     * @throws MessengerException 
     */
    @Override
    public final void sendSmsMessage(String message) throws MessengerException {
        super.sendSmsMessage(message);
        LOG.debug("Send SMS message to: {}, with message: {}", getSendPhoneNumber(), message);
    }

    /**
     * Prepares for deletion.
     */
    @Override
    public void prepareDelete() {
        /// Not used
    }

    @Override
    public void prepareWebPresentation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasGraphData() {
        return false;
    }
    
}
