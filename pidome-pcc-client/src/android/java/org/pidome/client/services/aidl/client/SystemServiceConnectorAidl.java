/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services.aidl.client;

import org.pidome.client.services.aidl.service.SystemServiceAidlInterface;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafxports.android.FXActivity;
import org.pidome.client.services.aidl.service.SystemServiceAidl;

/**
 * Convenience class for service connection.
 * @author John
 */
public class SystemServiceConnectorAidl {

    private RemoteServiceConnection connector = new RemoteServiceConnection();
    private SystemServiceAidlInterface service;
    
    private SystemPassthroughFromRemoteServiceInterface proxy;
    
    /**
     * Listeners for when the service is connected.
     */
    private static final List<SystemServiceConnectorListenerAidl> serviceListeners = new ArrayList<>();
    
    private ClientCallbacksServiceAidl clientCallBackCollection = new ClientCallbacksServiceAidl.Stub() {

        @Override
        public void updateGPSDistance(double distance) throws RemoteException {
            SystemServiceConnectorAidl.this.proxy.updateGPSDistance(distance);
        }

        @Override
        public void updateConnectionStatus(String status) throws RemoteException {
            SystemServiceConnectorAidl.this.proxy.updateConnectionStatus(status);
        }

        @Override
        public void updateClientStatus(String status) throws RemoteException {
            SystemServiceConnectorAidl.this.proxy.updateClientStatus(status);
        }

        @Override
        public void broadcastServerRPCFromStream(String RPCMessage) throws RemoteException {
            SystemServiceConnectorAidl.this.proxy.broadcastServerRPCFromStream(RPCMessage);
        }

        @Override
        public void updateUserPresence(int presenceId) throws RemoteException {
            SystemServiceConnectorAidl.this.proxy.updateUserPresence(presenceId);
        }

        @Override
        public void handleUserLoggedIn() throws RemoteException {
            SystemServiceConnectorAidl.this.proxy.handleUserLoggedIn();
        }

        @Override
        public void handleUserLoggedOut() throws RemoteException {
            SystemServiceConnectorAidl.this.proxy.handleUserLoggedOut();
        }

        @Override
        public void broadcastLoginEvent(String status, int errCode, String message) throws RemoteException {
            SystemServiceConnectorAidl.this.proxy.broadcastLoginEventByString(status, errCode, message);
        }

        @Override
        public void broadcastConnectionEvent(String status, int errCode, String message) throws RemoteException {
            SystemServiceConnectorAidl.this.proxy.broadcastConnectionEventByString(status, errCode, message);
        }
        
        @Override
        public boolean appIsInForeGround() throws RemoteException {
            return SystemServiceConnectorAidl.this.proxy.appIsInForeGround();
        }

    };
    
    protected final void registerPassthrough(SystemPassthroughFromRemoteServiceInterface proxy){
        this.proxy = proxy;
    }

    protected final void deRegisterPassthrough(){
        this.proxy = null;
    }
    
    class RemoteServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder boundService) {
            service = SystemServiceAidlInterface.Stub.asInterface((IBinder) boundService);
            try {
                service.registerCallBack(clientCallBackCollection);
            } catch (RemoteException ex) {
                Logger.getLogger(SystemServiceConnectorAidl.class.getName()).log(Level.SEVERE, null, ex);
            }
            Log.d("SystemServiceConnectorAidl", "onServiceConnected() connected");
            Iterator<SystemServiceConnectorListenerAidl> listeners = serviceListeners.iterator();
            while (listeners.hasNext()) {
                listeners.next().handleSystemServiceConnected(service);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
            Log.d("SystemServiceConnectorAidl", "onServiceDisconnected() disconnected");
            Toast.makeText(FXActivity.getInstance(), "Service disconnected! restart app", Toast.LENGTH_LONG).show();
            Iterator<SystemServiceConnectorListenerAidl> listeners = serviceListeners.iterator();
            while (listeners.hasNext()) {
                listeners.next().handleSystemServiceDisConnected();
            }
        }
    }
 
    /**
     * Adds a service connected listener.
     * @param listener 
     */
    public static void addServiceListener(SystemServiceConnectorListenerAidl listener){
        if(!serviceListeners.contains(listener)){
            serviceListeners.add(listener);
        }
    }
    
    /**
     * Removes a service connected listener.
     * @param listener 
     */
    public static void removeServiceListener(SystemServiceConnectorListenerAidl listener){
        serviceListeners.remove(listener);
    }
    
    /**
     * Calls for a binding.
     * Be aware that a successful binding signals all listeners! So act to it.
     */
    public void bind(){
        FXActivity activity = FXActivity.getInstance();
        Intent intent = new Intent(activity.getApplicationContext(), SystemServiceAidl.class);
        activity.startService(intent);
        Log.i("SystemServiceConnectorAidl", "Starting SystemService bind");
        activity.bindService(intent, connector, Context.BIND_AUTO_CREATE);
    }
    
    /**
     * Unbinding from the service.
     */
    public void unBind(){
        Log.i("SystemServiceConnectorAidl", "Stopping SystemService bind");
        FXActivity.getInstance().unbindService(connector);
    }
    
}