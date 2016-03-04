/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.client.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.AppProperties;
import org.pidome.client.config.AppPropertiesException;
import org.pidome.client.system.rpc.PidomeJSONRPC;
import org.pidome.client.system.rpc.PidomeJSONRPCException;

/**
 *
 * @author John Sirach
 */
public final class ClientSession {

    boolean loggedIn = false;

    String sessionKey;
    static String clientName = "client";
    private static String clientPass = "pass";
    
    boolean inLoginError = false;
    String loginErrorMessage = "";
    int loginError = 0;
    
    Map<String,Object> data;
    
    static Logger LOG = LogManager.getLogger(ClientSession.class);
    
    public ClientSession(){
        try {
            clientName = AppProperties.getProperty("system", "client.login");
            clientPass = AppProperties.getProperty("system", "client.pass");
        } catch (AppPropertiesException ex) {
            LOG.error("Problem setting predefined client name, defaulting: " + ex.getMessage());
        }
    }
    
    public final void setClientName(String clientName){
        ClientSession.clientName = clientName;
    }
    
    public static String getClientName(){
        return clientName;
    }
    
    public final String getAuthString() throws ClientSessionException {
        return getAuthString(clientName, clientPass);
    }
    
    public final String getAuthString(String client, String password) throws ClientSessionException {
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("loginname", client);
                put("password", password);
                put("type", "DISPLAY");
                put("key", "");
                put("clientinfo", "PiDome Client");
            }
        };
        try {
            return PidomeJSONRPC.createExecMethod("ClientService.signOn", "ClientService.signOn", sendObject);
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not create signon: {}", sendObject);
            throw new ClientSessionException(ex.getMessage());
        }
    }
    
    public final void setData(Map<String,Object> data){
        this.data = data;
        parse();
    }

    public final void logOut(){
        loggedIn = false;
        inLoginError = false;
        LOG.debug("Logged out");
    }
    
    final void parse(){
        inLoginError = false;
        LOG.debug("Code: {}, Key: {}, Message: {}", data.get("code"),data.get("key"),data.get("message"));
        if(data!=null){
            loginError = ((Long)data.get("code")).intValue();
            switch(loginError){
                case 200:
                    sessionKey = (String)data.get("key");
                    loggedIn = true;
                    loginErrorMessage = (String)data.get("message");
                    LOG.debug("Logged in: {}, {}", clientName, sessionKey);
                    AppProperties.setProperty("system", "client.login", clientName);
                    AppProperties.setProperty("system", "client.firstrun", "false");
                    try {
                        AppProperties.store("system", null);
                    } catch (IOException ex) {
                        LOG.error("Could not save properties file: " + ex.getMessage());
                    }
                break;
                case 202:
                    sessionKey = (String)data.get("key");
                    loggedIn = false;
                    loginErrorMessage = (String)data.get("message");
                    inLoginError = true;
                    LOG.debug("Awaiting approval: {}, {}", clientName, sessionKey);
                    AppProperties.setProperty("system", "client.login", clientName);
                break;
                case 401:
                    sessionKey = (String)data.get("key");
                    loggedIn = false;
                    loginErrorMessage = (String)data.get("message");
                    inLoginError = true;
                    LOG.debug("Signon error: {}, {}", clientName, sessionKey);
                break;
                default:
                    loginError = (int)data.get("code");
                    loginErrorMessage = (String)data.get("message");
                    loggedIn = false;
                    inLoginError = true;
                    LOG.debug("Login error: {}", loginError);
                break;
            }
        } else {
            loggedIn = false;
            loginErrorMessage = "Unknown authentication";
        }
    }
    
    public final boolean loginError(){
        return inLoginError;
    }
    
    public final int getLoginError(){
        return loginError;
    }
    
    public final String getErrorMessage(){
        return loginErrorMessage;
    }
    
    public final String getSessionName(){
        return clientName;
    }
    
    public final String getSessionKey(){
        return sessionKey;
    }
    
    public final boolean loggedIn(){
        return loggedIn;
    }
    
    
}
