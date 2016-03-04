/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafxports.android.FXActivity;

/**
 * Convenience class for service connection.
 * @author John
 */
public class SystemServiceConnector implements ServiceConnection {

    private static SystemServiceConnector connector = new SystemServiceConnector();
    
    /**
     * Listeners for when the service is connected.
     */
    private static final List<SystemServiceConnectorListener> serviceListeners = new ArrayList<>();
    
    /**
     * Called when the service is connected.
     * @param component
     * @param binder 
     */
    @Override
    public void onServiceConnected(ComponentName component, IBinder binder) {
        Log.i("SystemServiceConnector", "Connected to service");
        Iterator<SystemServiceConnectorListener> listeners = serviceListeners.iterator();
        while (listeners.hasNext()) {
            listeners.next().handleSystemServiceConnected((SystemService.BindExposer)binder);
        }
    }

    /**
     * Called when the service is disconnected
     * @param arg0 
     */
    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        Iterator<SystemServiceConnectorListener> listeners = serviceListeners.iterator();
        while (listeners.hasNext()) {
            listeners.next().handleSystemServiceDisConnected();
        }
    }
 
    /**
     * Adds a service connected listener.
     * @param listener 
     */
    public static void addServiceListener(SystemServiceConnectorListener listener){
        if(!serviceListeners.contains(listener)){
            serviceListeners.add(listener);
        }
    }
    
    /**
     * Removes a service connected listener.
     * @param listener 
     */
    public static void removeServiceListener(SystemServiceConnectorListener listener){
        serviceListeners.remove(listener);
    }
    
    /**
     * Calls for a binding.
     * Be aware that a successful binding signals all listener! So act to it.
     */
    public static void bind(){
        FXActivity activity = FXActivity.getInstance();
        Intent intent = new Intent(activity.getApplicationContext(), SystemService.class);
        if(!SystemService.isServiceRunning()){
            Log.i("SystemServiceConnector", "Service not running, starting");
            activity.startService(intent);
        }
        Log.i("SystemServiceConnector", "Starting SystemService bind");
        activity.bindService(intent, connector, Context.BIND_AUTO_CREATE);
    }
    
    /**
     * Unbinding from the service.
     */
    public static void unBind(){
        Log.i("SystemServiceConnector", "Stopping SystemService bind");
        FXActivity.getInstance().unbindService(connector);
    }
    
}