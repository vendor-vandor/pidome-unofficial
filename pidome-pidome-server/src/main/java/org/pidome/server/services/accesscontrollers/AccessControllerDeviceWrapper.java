/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.accesscontrollers;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.apache.logging.log4j.LogManager;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.UnknownDeviceException;
import org.pidome.server.connector.drivers.devices.specials.presence.AccessControllerDevice;
import org.pidome.server.connector.drivers.devices.specials.presence.AccessControllerProxyInterface;
import org.pidome.server.connector.drivers.devices.specials.presence.PersonToken;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.services.clients.persons.Person;
import org.pidome.server.services.clients.persons.PersonsManagement;
import org.pidome.server.services.clients.persons.PersonsManagementException;
import org.pidome.server.system.db.DB;
import org.pidome.server.system.hardware.devices.DeviceStruct;

/**
 *
 * @author John
 */
public class AccessControllerDeviceWrapper {
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(AccessControllerDeviceWrapper.class);
    
    private final int wrappedDeviceid;
    private final int controllerId;
    private List<AccessControllerProxyInterface.Capabilities> capabilities = new ArrayList<>();
    
    
    protected AccessControllerDeviceWrapper(int controllerId, int wrappedDeviceId){
        this.wrappedDeviceid = wrappedDeviceId;
        this.controllerId = controllerId;
    }
    
    /**
     * Sends an edit enabled/disabled signal to the remote device.
     * @param enabled
     * @throws UnknownDeviceException 
     */
    public final void sendEditEnabled(boolean enabled) throws UnknownDeviceException{
        ((AccessControllerDevice)((DeviceStruct)DeviceService.getDevice(wrappedDeviceid)).getDevice()).setSystemEdit(enabled);
    }
    
    /**
     * Sends a message to the remote device.
     * @param message The message to be send.
     * @throws UnknownDeviceException 
     */
    public final void sendMessage(String message) throws UnknownDeviceException{
        ((AccessControllerDevice)((DeviceStruct)DeviceService.getDevice(wrappedDeviceid)).getDevice()).sendMessage(message);
    }
    
    /**
     * Sends an user to the remote device.
     * @param userId
     * @throws PersonsManagementException
     * @throws UnknownDeviceException 
     */
    public final void sendUser(int userId) throws PersonsManagementException, UnknownDeviceException {
        Person user = PersonsManagement.getInstance().getPerson(userId);
        AccessControllerToken token = new AccessControllerToken(controllerId,
                                                                userId,
                                                                user.getLoginName().equals("admin"),
                                                                user.getFirstName());
        ((AccessControllerDevice)((DeviceStruct)DeviceService.getDevice(wrappedDeviceid)).getDevice()).sendPerson(token);
    }
    
    /**
     * Sets device extended capabilities.
     * @param cpbltss 
     */
    protected final void setCapabilities(AccessControllerProxyInterface.Capabilities... cpbltss){
        capabilities = Arrays.asList(cpbltss);
    }
    
    /**
     * Returns a list of capabilities.
     * @return 
     */
    public final List<AccessControllerProxyInterface.Capabilities> getCapabilities(){
        return new ArrayList<>(capabilities);
    }
    
    /**
     * Returns the device id of the wrapped device.
     * @return 
     */
    public final int getControllerId(){
        return this.controllerId;
    }
    
    public final int getWrappedDeviceId(){
        return this.wrappedDeviceid;
    }
    
    public final Device getWrappedDevice() throws UnknownDeviceException {
        return ((DeviceStruct)DeviceService.getDevice(wrappedDeviceid)).getDevice();
    }
    
    protected int authorizeToken(PersonToken.TokenType tt, char[] c) throws UnknownDeviceException {
        int result = 0;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
            PreparedStatement prep = fileDBConnection.prepareStatement("SELECT count(ts.id) AS `knowntoken`,ts.person FROM tokensets ts INNER JOIN tokenset_has_devices thd ON ts.id=thd.tokenset WHERE thd.tokendevice=? AND ts.tokentype=? AND ts.content=? LIMIT 1")) {
            prep.setInt(1, this.controllerId);
            prep.setString(2, tt.toString());
            prep.setBytes(3, createHash(c));
            try (ResultSet rsResult = prep.executeQuery()) {
                if (rsResult.next() && rsResult.getInt("knowntoken")>0) {
                    result = rsResult.getInt("person");
                } else {
                    LOG.debug("Unknown token");
                }
            } catch (Exception ex){
                LOG.error("Could not check if token already exists: {}", ex.getMessage(), ex);
            }
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            LOG.error("Could not check if token already exists: {}", ex.getMessage(), ex);
        }
        return result;
    }

    protected boolean supportsRemoteAdmin(){
        return capabilities.contains(AccessControllerProxyInterface.Capabilities.REMOTE_ADMIN);
    }

    protected boolean supportsMasterToken(){
        return capabilities.contains(AccessControllerProxyInterface.Capabilities.REMOTE_ADMIN);
    }
    
    protected boolean hasMasterToken() throws UnknownDeviceException {
        boolean result = false;
        if(supportsMasterToken()){
            try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                PreparedStatement prep = fileDBConnection.prepareStatement("SELECT count(ts.id) AS `knowntoken` FROM tokensets ts INNER JOIN tokenset_has_devices thd ON ts.id=thd.tokenset WHERE thd.tokendevice=? AND ts.master=1 AND ts.person=1 LIMIT 1")) {
                prep.setInt(1, this.controllerId);
                try (ResultSet rsResult = prep.executeQuery()) {
                    if (rsResult.next() && rsResult.getInt("knowntoken")>0) {
                        result = true;
                    } else {
                        LOG.info("Unknown token");
                    }
                } catch (Exception ex){
                    LOG.error("Could not check if token already exists: {}", ex.getMessage(), ex);
                }
            } catch (SQLException ex) {
                LOG.error("Could not check if token already exists: {}", ex.getMessage(), ex);
            }
        }
        return result;
    }
    
    protected boolean authorizeMasterToken(PersonToken.TokenType tt, char[] c) throws UnknownDeviceException {
        boolean result = false;
        if(supportsMasterToken()){
            try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                PreparedStatement prep = fileDBConnection.prepareStatement("SELECT count(ts.id) AS `knowntoken` FROM tokensets ts INNER JOIN tokenset_has_devices thd ON ts.id=thd.tokenset WHERE thd.tokendevice=? AND (ts.tokentype=? AND ts.content=? AND ts.master=1 AND ts.person=1) LIMIT 1")) {
                prep.setInt(1, this.controllerId);
                prep.setString(2, tt.toString());
                prep.setBytes(3, createHash(c));
                try (ResultSet rsResult = prep.executeQuery()) {
                    if (rsResult.next() && rsResult.getInt("knowntoken")>0) {
                        result = true;
                    } else {
                        LOG.info("Unknown token");
                    }
                } catch (Exception ex){
                    LOG.error("Could not check if token already exists: {}", ex.getMessage(), ex);
                }
            } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
                LOG.error("Could not check if token already exists: {}", ex.getMessage(), ex);
            }
        } else {
            return true;
        }
        return result;
    }
    
    private boolean knownToken(PersonToken.TokenType tt, int personId, char[] c) throws UnknownDeviceException {
        boolean result = false;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
            PreparedStatement prep = fileDBConnection.prepareStatement("SELECT count(id) AS `knowntoken` FROM tokensets WHERE (person=? AND tokentype=? AND content=?) LIMIT 1")) {
            prep.setInt(1, personId);
            prep.setString(2, tt.toString());
            prep.setBytes(3, createHash(c));
            try (ResultSet rsResult = prep.executeQuery()) {
                if (rsResult.next() && rsResult.getInt("knowntoken")>0) {
                    result = true;
                } else {
                    LOG.info("Unknown token");
                }
            } catch (Exception ex){
                LOG.error("Could not check if token already exists: {}", ex.getMessage(), ex);
            }
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            LOG.error("Could not check if token already exists: {}", ex.getMessage(), ex);
        }
        return result;
    }
    
    private boolean knownToken(PersonToken.TokenType tt, char[] c) throws UnknownDeviceException {
        boolean result = false;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
            PreparedStatement prep = fileDBConnection.prepareStatement("SELECT count(id) AS `knowntoken` FROM tokensets WHERE (tokentype=? AND content=?) LIMIT 1")) {
            prep.setString(1, tt.toString());
            prep.setBytes(2, createHash(c));
            try (ResultSet rsResult = prep.executeQuery()) {
                if (rsResult.next() && rsResult.getInt("knowntoken")>0) {
                    result = true;
                } else {
                    LOG.info("Unknown token");
                }
            } catch (Exception ex){
                LOG.error("Could not check if token already exists: {}", ex.getMessage(), ex);
            }
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            LOG.error("Could not check if token already exists: {}", ex.getMessage(), ex);
        }
        return result;
    }
    
    public final List<AccessControllerToken> getAccessTokens() throws UnknownDeviceException {
        List<AccessControllerToken> tokenlist = new ArrayList<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
            PreparedStatement prep = fileDBConnection.prepareStatement("SELECT ts.id, ts.person, p.firstname as personname, ts.tokentype,ts.master FROM tokensets ts "
                                                                 + "INNER JOIN persons p ON p.id=ts.person "
                                                                 + "INNER JOIN tokenset_has_devices thd ON thd.tokenset=ts.id "
                                                                      + "WHERE thd.tokendevice=?")) {
            prep.setInt(1, this.controllerId);
            try (ResultSet rsResult = prep.executeQuery()) {
                while(rsResult.next()){
                    AccessControllerToken token = new AccessControllerToken(rsResult.getInt("id"),
                                                                            rsResult.getInt("person"),
                                                                            rsResult.getBoolean("master"),
                                                                            rsResult.getString("personname"));
                    token.setTokenType(rsResult.getString("tokentype"));
                    tokenlist.add(token);
                }
            } catch (Exception ex){
                LOG.error("Could not check if token already exists: {}", ex.getMessage(), ex);
            }
        } catch (SQLException ex) {
            LOG.error("Could not check if token already exists: {}", ex.getMessage(), ex);
        }
        return tokenlist;
    }
    
    protected boolean removeTokenByDevice(PersonToken.TokenType tt, int personId, char[] c) throws UnknownDeviceException {
        boolean result = false;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
            PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM tokensets WHERE person=? AND tokentype=? AND content=?")) {
            prep.setInt(1, personId);
            prep.setString(2, tt.toString());
            prep.setBytes(3, createHash(c));
            prep.executeUpdate();
            try {
                Map<String, Object> sendObject = new HashMap<String, Object>() {
                    {
                        put("id", controllerId);
                        put("user", PersonsManagement.getInstance().getPerson(personId).getFirstName());
                    }
                };
                ClientMessenger.send("AccessControllerService","removeToken", 0, sendObject);
            } catch (PersonsManagementException ex) {
                Logger.getLogger(AccessControllerDeviceWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            LOG.error("Could not check if token already exists: {}", ex.getMessage(), ex);
        }
        return result;
    }
    
    protected boolean removeTokenById(PersonToken.TokenType tt, int personId, int tokenId) throws UnknownDeviceException {
        boolean result = false;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
            PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM tokensets WHERE id=? AND person=? AND tokentype=?")) {
            prep.setInt(1, tokenId);
            prep.setInt(2, personId);
            prep.setString(3, tt.toString());
            prep.executeUpdate();
            try {
                Map<String, Object> sendObject = new HashMap<String, Object>() {
                    {
                        put("id", controllerId);
                        put("user", PersonsManagement.getInstance().getPerson(personId).getFirstName());
                    }
                };
                ClientMessenger.send("AccessControllerService","removeToken", 0, sendObject);
            } catch (PersonsManagementException ex) {
                Logger.getLogger(AccessControllerDeviceWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            LOG.error("Problem removing token {}", ex.getMessage(), ex);
        }
        return result;
    }
    
    /**
     * Adds a token to the database.
     * This function checks first if the device is alive and if the specific token does not already exists.
     * @param tt
     * @param personId
     * @param c
     * @return
     * @throws UnknownDeviceException 
     */
    protected boolean addToken(PersonToken.TokenType tt, int personId, char[] c) throws UnknownDeviceException {
        ///Check if wrapped device is stille there.
        getWrappedDevice();
        
        boolean result = false;
        if(!knownToken(tt, c)){
            try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                 PreparedStatement prep = fileDBConnection.prepareStatement("INSERT INTO tokensets ('person','tokentype','content','master') values (?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                prep.setInt(1, personId);
                prep.setString(2, tt.toString());
                prep.setBytes(3,createHash(c));
                prep.setBoolean(4,personId==1);
                prep.executeUpdate();
                try (ResultSet rs = prep.getGeneratedKeys()) {
                    if (rs.next()) {
                        try (PreparedStatement prepCombine = fileDBConnection.prepareStatement("INSERT INTO tokenset_has_devices ('tokenset','tokendevice') values (?,?)")){
                            prepCombine.setInt(1, rs.getInt(1));
                            prepCombine.setInt(2, controllerId);
                            prepCombine.executeUpdate();
                        }
                        try {
                            Map<String, Object> sendObject = new HashMap<String, Object>() {
                                {
                                    put("id", controllerId);
                                    put("user", PersonsManagement.getInstance().getPerson(personId).getFirstName());
                                }
                            };
                            ClientMessenger.send("AccessControllerService","addToken", 0, sendObject);
                        } catch (PersonsManagementException ex) {
                            Logger.getLogger(AccessControllerDeviceWrapper.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } catch (SQLException ex) {
                    LOG.error("Problem binding token to device, please use the web interface: {}", ex.getMessage(), ex);
                }
                result = true;
            } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
                LOG.error("Could not store token: {}", ex.getMessage(), ex);
            }
        }
        return result;
    }
    
    /* 
     * Password Hashing With PBKDF2 (http://crackstation.net/hashing-security.htm).
     * Copyright (c) 2013, Taylor Hornby
     * All rights reserved.
     *
     * Redistribution and use in source and binary forms, with or without 
     * modification, are permitted provided that the following conditions are met:
     *
     * 1. Redistributions of source code must retain the above copyright notice, 
     * this list of conditions and the following disclaimer.
     *
     * 2. Redistributions in binary form must reproduce the above copyright notice,
     * this list of conditions and the following disclaimer in the documentation 
     * and/or other materials provided with the distribution.
     *
     * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
     * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
     * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
     * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
     * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
     * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
     * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
     * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
     * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
     * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
     * POSSIBILITY OF SUCH DAMAGE.
     */
    /*
     * PBKDF2 salted password hashing.
     * Author: havoc AT defuse.ca
     * www: http://crackstation.net/hashing-security.htm
     */
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";

    // The following constants may be changed without breaking existing hashes.
    private static final int HASH_BYTE_SIZE = 24;
    private static final int PBKDF2_ITERATIONS = 1000;

    /**
     * Returns a salted PBKDF2 hash of the password.
     *
     * @param password the password to hash
     * @return a salted PBKDF2 hash of the password
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.spec.InvalidKeySpecException
     */
    private static byte[] createHash(char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Hash the password
        byte[] hash = pbkdf2(password, new String(password).getBytes(), PBKDF2_ITERATIONS, HASH_BYTE_SIZE);
        // format iterations:salt:hash
        return (toHex(hash)).getBytes();
    }

    /**
     * Computes the PBKDF2 hash of a password.
     *
     * @param password the password to hash.
     * @param salt the salt
     * @param iterations the iteration count (slowness factor)
     * @param bytes the length of the hash to compute in bytes
     * @return the PBDKF2 hash of the password
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }

    /**
     * Converts a byte array into a hexadecimal string.
     *
     * @param array the byte array to convert
     * @return a length*2 character string encoding the byte array
     */
    private static String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }
    
}