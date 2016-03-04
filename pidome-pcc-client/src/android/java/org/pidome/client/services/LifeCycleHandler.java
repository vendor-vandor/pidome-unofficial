/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.services;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author John
 */
public class LifeCycleHandler implements ActivityLifecycleCallbacks,LifeCycleHandlerInterface {
    
    private static boolean resumed = false;
    private static boolean started = false;

    List<LifeCycleHandlerStatusListener> _listeners = new CopyOnWriteArrayList<>();
    
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        resumed = true;
        android.util.Log.w("test", "application is in foreground: " + resumed);
        notifyListeners();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        resumed = false;
        android.util.Log.w("test", "application is in foreground: " + resumed);
        notifyListeners();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        started = true;
        android.util.Log.w("test", "application is visible: " + started);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        started = false;
        android.util.Log.w("test", "application is visible: " + started);
    }
    
    /**
     * Is the app visible or not.
     * @return 
     */
    public static boolean isAppVisible() {
        return started;
    }

    /**
     * Is the app in the foreground or not.
     * @return 
     */
    public static boolean isAppInForeground() {
        return resumed;
    }

    @Override
    public boolean inForeground() {
        return isAppInForeground();
    }

    private void notifyListeners(){
        Iterator<LifeCycleHandlerStatusListener> listeners = this._listeners.iterator();
        while(listeners.hasNext()){
            listeners.next().handleInForeground(isAppInForeground());
        }
    }
    
    @Override
    public void addLifecycleListener(LifeCycleHandlerStatusListener listener) {
        if(_listeners.contains(listener)){
            _listeners.add(listener);
        }
    }

    @Override
    public void removeLifecycleListener(LifeCycleHandlerStatusListener listener) {
        _listeners.remove(listener);
    }

}