/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services.aidl.client;

/**
 *
 * @author John
 */
public interface SystemPassthroughFromRemoteServiceInterface {

    void updateGPSDistance(double distance);

    void updateConnectionStatus(String status);

    void updateClientStatus(String status);

    void broadcastServerRPCFromStream(String RPCMessage);
    
    void updateUserPresence(int presenceId);
    
    void handleUserLoggedIn();
    
    void handleUserLoggedOut();
    
    void broadcastLoginEventByString(String status, int errCode, String message);
    
    void broadcastConnectionEventByString(String status, int errCode, String message);
    
    boolean appIsInForeGround();
    
}