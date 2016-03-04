/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.http.rpc;

import java.util.Map;
import org.pidome.server.services.clients.remoteclient.RemoteClient;

/**
 * Class supplying authentication parameters.
 * @author John
 */
public class PiDomeJSONRPCAuthentificationParameters {
    
    /**
     * Endpoint type.
     */
    private RemoteClient.DeviceType authType;
    
    /**
     * The login name.
     */
    private String loginname;
    
    /**
     * The type.
     */
    private String type;
    
    /**
     * The client info.
     */
    private String clientInfo;
    
    /**
     * The key supplied.
     */
    private String key;
    
    /**
     * The username.
     */
    private String username;
    
    /**
     * The user password.
     */
    private String password;
    
    /**
     * This contains the login override boolean.
     * This is used when an user is already logged in at another location but
     * should be overridden by the new location.
     */
    private boolean overrideLogin = false;
    
    /**
     * Constructor setting the auth endpoint type.
     * @param type 
     */
    PiDomeJSONRPCAuthentificationParameters(RemoteClient.DeviceType type){
        this.authType = type;
    }

    /**
     * @return the authType
     */
    public RemoteClient.DeviceType getAuthType() {
        return authType;
    }

    /**
     * @param authType the authType to set
     */
    public void setAuthType(RemoteClient.DeviceType authType) {
        this.authType = authType;
    }

    /**
     * @return the loginname
     */
    public String getLoginname() {
        return loginname;
    }

    /**
     * @param loginname the loginname to set
     */
    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the clientInfo
     */
    public String getClientInfo() {
        return clientInfo;
    }

    /**
     * @param clientInfo the clientInfo to set
     */
    public void setClientInfo(String clientInfo) {
        this.clientInfo = clientInfo;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the overrideLogin
     */
    public boolean isOverrideLogin() {
        return overrideLogin;
    }

    /**
     * @param overrideLogin the overrideLogin to set
     */
    public void setOverrideLogin(boolean overrideLogin) {
        this.overrideLogin = overrideLogin;
    }
    
}