/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.devices.specials.presence;

import org.pidome.server.connector.drivers.devices.specials.presence.PersonToken.TokenType;

/**
 * functions needed to be supported by the hosting platform.
 * @author John
 */
public interface AccessControllerProxyInterface {

    public enum Capabilities {
        REMOTE_ADMIN("Device admin", "Token administration via device"),
        SET_ALARM("Alarm","Alarm notifications"),
        SET_TAMPER("Tamper","Tampering notifications"),
        RESET("Reset","Reset device from the server"),
        REBOOT("Reboot","Reboot device from the server"),
        SHARE_TOKENS("Share tokens","Share tokens between devices"),
        BEEP("Sound","Device supports single audible feedback from the server"),
        BEEP_PITCH("Pitched sound","Device supports multiple audible feedback from the server"),
        MSG("Messages","The server is able to send message visible on the device"),
        MSG_MULTI("Multiline messages","The server is able to send multiline messages visible on the device"),
        NFC("NFC","NFC card reader"),
        CODE("Keypad","Keypad with code");
        
        private final String description;
        private final String name;
        
        private Capabilities(String name, String description){
            this.description = description;
            this.name = name;
        }
        
        public final String getName(){
            return this.name;
        }
        
        public final String getDescription(){
            return this.description;
        }
        
    }
    
    /**
     * A device must register supplemental capabilities.
     * By default only scan card is available, if a remote device has the capability 
     * to execute admin functionalities the capability REMOTE_ADMIN should be registered.
     * @param device
     * @param capabilities 
     */
    public void registerCapabilities(AccessControllerDevice device, Capabilities... capabilities);
    
    /**
     * A person request from the remote device.
     * This request should be responded with passPerson.
     * @param device The AccessControllerDevice requesting the information.
     * @throws UnsupportedOperationException 
     */
    public void firstPerson(AccessControllerDevice device);
    /**
     * Pass on the next person in line.
     * This request should be responded with passPerson.
     * @param device The AccessControllerDevice requesting the information.
     * @param currentPerson The current one so we know who's next.
     * @throws UnsupportedOperationException 
     */
    public void nextPerson(AccessControllerDevice device, int currentPerson);
    /**
     * Pass on the previous person in line.
     * This request should be responded with passPerson.
     * @param device The AccessControllerDevice requesting the information.
     * @param currentPerson The current one so we know who's previous.
     * @throws UnsupportedOperationException 
     */
    public void previousPerson(AccessControllerDevice device, int currentPerson);
    
    /**
     * Called by the remote device if it is tampered with.
     * @param device The AccessControllerDevice requesting the information.
     * @param tampered true when tampered should be on.
     * @throws UnsupportedOperationException When tampered is unsupported.
     */
    public void setSystemTamper(AccessControllerDevice device, boolean tampered);
    
    /**
     * Called by the remote device if an alarm should be raised.
     * @param device The AccessControllerDevice requesting the information.
     * @param alarmed true when alarmed should be on.
     */
    public void setSystemAlarmed(AccessControllerDevice device, boolean alarmed);
    
    /**
     * Notification the system is in edit mode.
     * @param device The AccessControllerDevice requesting the information.
     * @param setEdit True when in edit mode.
     * @throws UnsupportedOperationException When device does not support on-site editing.
     */
    public void setSystemEdit(AccessControllerDevice device, boolean setEdit) throws UnsupportedOperationException;
    
    /**
     * Authorize a token for an user
     * The device/code of the device/driver is responsible to encrypt.
     * @param device The AccessControllerDevice requesting the information.
     * @param token Type type of token used, currently pin and nfc.
     * @param tokenContent The token identification.
     * @return true when authorization succeeds.
     */
    public boolean authorizeToken(AccessControllerDevice device, TokenType token, char[] tokenContent);
    
    /**
     * Authorize a master token.
     * This can be used when access controller devices require a master key/code to enable edit mode.
     * The device/code of the device/driver is responsible to encrypt.
     * @param device The AccessControllerDevice requesting the information.
     * @param token Type type of token used, currently pin and nfc.
     * @param tokenContent The token identification.
     * @return true when authorization succeeds.
     */
    public boolean authorizeMasterToken(AccessControllerDevice device, TokenType token, char[] tokenContent);
    
    /**
     * Returns true if a master token exists.
     * @param acd The device where the master token is registered for, or not.
     * @return 
     */
    public boolean hasMasterToken(AccessControllerDevice acd);
    
    /**
     * Removes a token from an user.
     * @param device The AccessControllerDevice requesting the information.
     * @param uid The persons uid
     * @param token The Token type in question
     * @param tokenContent information identifying the token.
     * @return 
     */
    public boolean removeToken(AccessControllerDevice device, int uid, TokenType token, char[] tokenContent);
    
    /**
     * Add a token for an user.
     * @param device  The AccessControllerDevice requesting the information.
     * @param uid The person's uid.
     * @param token The token type in question.
     * @param tokenContent Identifier for the token.
     * @return 
     */
    public boolean addToken(AccessControllerDevice device, int uid, TokenType token, char[] tokenContent);
    
}