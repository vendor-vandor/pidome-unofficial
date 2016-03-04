/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.presencedevices;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pidome.server.connector.plugins.IllegalFileLocationException;
import org.pidome.server.connector.plugins.PluginBase;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCUtils;

/**
 *
 * @author John
 */
public abstract class PresenceDevicePlugin extends PluginBase {
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(PresenceDevicePlugin.class);
    
    List<SecurityToken> tokenBase;
    
    /**
     * Called by the system to load the tokens.
     * If not used leave the function empty.
     */
    public abstract void initializeTokens();
    
    /**
     * Called by the system to authorize someone.
     * @param authorizationCode
     * @return The token authorized when the person is found.
     * @throws org.pidome.server.connector.plugins.presencedevices.InvalidTokenException 
     */
    public abstract boolean authorizePerson(String authorizationCode) throws InvalidTokenException;
    
    /**
     * Called by the system to add a token.
     * @param personId
     * @param authorizationCode 
     * @return  
     * @throws org.pidome.server.connector.plugins.presencedevices.InvalidTokenException When token already exists.
     */
    public abstract boolean addToken(int personId, String authorizationCode) throws InvalidTokenException;
    
    /**
     * Called by the system to remove a token.
     * @param personId
     * @param authorizationCode 
     * @return  
     * @throws org.pidome.server.connector.plugins.presencedevices.InvalidTokenException When token is not found.
     */
    public abstract boolean removeToken(int personId, String authorizationCode) throws InvalidTokenException;
    
    /**
     * Locks a token out to be used.
     * @param block
     * @param tokenId
     * @return 
     * @throws org.pidome.server.connector.plugins.presencedevices.InvalidTokenException
     */
    public abstract boolean blockToken(boolean block, String tokenId) throws InvalidTokenException;
    
    /**
     * Used to block a token.
     * @param block 
     * @param tokenId 
     * @return  
     * @throws org.pidome.server.connector.plugins.presencedevices.InvalidTokenException 
     */
    public final boolean lockToken(boolean block, String tokenId) throws InvalidTokenException {
        for(SecurityToken token:tokenBase){
            if(token.getTokenId().equals(tokenId)){
                token.block(block);
                return true;
            }
        }
        LOG.error("Could lock/unlock ({}) with the given token identification as the token is not found", block);
        throw new InvalidTokenException("Token not found");
    }
    
    /**
     * Use this to identify someone.
     * @param authorizationCode Code used to identify a token for a person.
     * @return  The token found.
     * @throws org.pidome.server.connector.plugins.presencedevices.InvalidTokenException 
     */
    public final SecurityToken identifyPerson(String authorizationCode) throws InvalidTokenException {
        for(SecurityToken token:tokenBase){
            if(token.hasToken(authorizationCode)){
                return token;
            }
        }
        LOG.error("Could not identify an user, given token identification not found");
        throw new InvalidTokenException("Token not found");
    }
    
    /**
     * Loads tokens.
     * @throws org.pidome.server.connector.plugins.presencedevices.TokenLoadException
     */
    public final void load() throws TokenLoadException {
        getTokens();
    }
    
    /**
     * Returns if tokens are loaded.
     * @return 
     */
    public final boolean tokensLoaded(){
        return tokenBase!=null;
    }
    
    /**
     * Stores a token.
     * @param personId
     * @param authorizationCode 
     * @throws org.pidome.server.connector.plugins.presencedevices.InvalidTokenException 
     */
    public final void storeToken(int personId, String authorizationCode) throws InvalidTokenException {
        if(authorizationCode.contains("\"") || authorizationCode.contains("\\")){
            throw new InvalidTokenException("Illegal character used. Can not use '\"' and '\\' characters");
        }
        for(SecurityToken token:tokenBase){
            if(token.hasToken(authorizationCode)){
                LOG.error("Could not add token for person id: {}, token already exists", personId);
                throw new InvalidTokenException("Token already exists");
            }
        }
        SecurityToken token = new SecurityToken(java.util.UUID.randomUUID().toString(), personId, authorizationCode, false);
        tokenBase.add(token);
        storeTokens();
    }
    
    /**
     * Stores the tokens.
     */
    private void storeTokens(){
        ArrayList<Map<String,Object>> tokenList = new ArrayList<>();
        for(SecurityToken token:tokenBase){
            Map<String,Object> tokenMap = new HashMap<>();
            tokenMap.put("id", token.getTokenId());
            tokenMap.put("personid", token.getPersonId());
            tokenMap.put("tokencode", token.getTokenCode());
            tokenMap.put("blocked", token.isBlocked());
            tokenList.add(tokenMap);
        }
        try {
            String tokenSet = PidomeJSONRPCUtils.createNonRPCMethods(tokenList);
            File keyFile = getFile("tokens");
            try (BufferedWriter output = new BufferedWriter(new FileWriter(keyFile))) {
                output.write(tokenSet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (PidomeJSONRPCException | IllegalFileLocationException | IOException ex) {
            LOG.error("Could not store tokens because of: {}", ex.getMessage(), ex);
        }
    }
    
    /**
     * Removes a token.
     * @param personId
     * @param authorizationCode 
     * @throws org.pidome.server.connector.plugins.presencedevices.InvalidTokenException 
     */
    public final void deleteToken(int personId, String authorizationCode) throws InvalidTokenException {
        SecurityToken toRemove = null;
        for(SecurityToken token:tokenBase){
            if(token.hasToken(authorizationCode)){
                toRemove = token;
            }
        }
        if(toRemove!=null){
            tokenBase.remove(toRemove);
        }
        LOG.error("Could not delete token for person id: {}", personId);
        throw new InvalidTokenException("Token not found");
    }
    
    /**
     * Returns the tokens.
     */
    private void getTokens() throws TokenLoadException {
        try {
            File keyFile = getFile("tokens");
            StringBuilder result = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(keyFile))) {
                char[] buf = new char[1024];
                int r = 0;
                while ((r = reader.read(buf)) != -1) {
                    result.append(buf, 0, r);
                }
            } catch (FileNotFoundException ex) {
                LOG.error("Could not load tokens because of: {}", ex.getMessage(), ex);
                throw new TokenLoadException("Could not load tokens: {}" + ex.getMessage());
            } catch (IOException ex) {
                LOG.error("Could not load tokens because of: {}", ex.getMessage(), ex);
                throw new TokenLoadException("Could not load tokens: {}" + ex.getMessage());
            }
            tokenBase = new ArrayList<>();
            JSONParser parser = new JSONParser();
            JSONArray tokenset = (JSONArray)parser.parse(result.toString());
            Object[] objects = PidomeJSONRPCUtils.jsonParamsToObjectArray(tokenset);
            List<Object> tokenSetArray = Arrays.asList(objects);
            for(Object tokenObject:tokenSetArray){
                Map<String,Object> tokenMap = (Map<String,Object>)tokenObject;
                tokenBase.add(new SecurityToken((String)tokenMap.get("id"), (int)tokenMap.get("personid"), (String)tokenMap.get("tokencode"), (boolean)tokenMap.get("blocked")));
            }
        } catch (IllegalFileLocationException | IOException | ParseException ex) {
            LOG.error("Could not load tokens because of: {}", ex.getMessage(), ex);
            throw new TokenLoadException("Could not load tokens: {}" + ex.getMessage());
        }
    }
    
    public static class SecurityToken {
        
        private final int personId;
        private final String tokenCode;
        private boolean blocked;
        
        private String tokenId;
        
        public SecurityToken(String tokenId, int personId, String tokenCode, boolean blocked){
            this.personId  = personId;
            this.tokenCode = tokenCode;
            this.blocked   = blocked;
            this.tokenId   = tokenId;
        }
        
        public final String getTokenId(){
            return tokenId;
        }
        
        private void block(boolean block){
            this.blocked = block;
        }
        
        private String getTokenCode(){
            return tokenCode;
        }
        
        public final boolean isBlocked(){
            return blocked;
        }
        
        public final boolean hasToken(String tokenCode){
            return tokenCode.equals(this.tokenCode);
        }
        
        public final int getPersonId(){
            return personId;
        }
        
    }
    
}