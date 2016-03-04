/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The lifecycle handler registers if the application is in the foreground or background.
 * @author John
 */
public class IOSLifeCycleHandler implements LifeCycleHandlerInterface {

    List<LifeCycleHandlerStatusListener> _listeners = new CopyOnWriteArrayList<>();
    
    /**
     * Returns true if the app is in the foreground
     * @return 
     */
    @Override
    public boolean inForeground() {
        
        return true;
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
    
    /**
     * Let's listeners know if the app is in the foreground or not.
     * @param isInForeGround 
     */
    private void notifyListeners(boolean isInForeGround){
        Iterator<LifeCycleHandlerStatusListener> listeners = this._listeners.iterator();
        while(listeners.hasNext()){
            listeners.next().handleInForeground(isInForeGround);
        }
    }
    
}