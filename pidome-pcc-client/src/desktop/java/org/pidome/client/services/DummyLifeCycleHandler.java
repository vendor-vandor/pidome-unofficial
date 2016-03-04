/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services;

/**
 *
 * @author John
 */
public class DummyLifeCycleHandler implements LifeCycleHandlerInterface {

    @Override
    public boolean inForeground() {
        /// Can always return true as on the desktop the app can keep on switching scenes.
        return true;
    }

    @Override
    public void addLifecycleListener(LifeCycleHandlerStatusListener listener) {
        ///Not needed on desktop.
    }

    @Override
    public void removeLifecycleListener(LifeCycleHandlerStatusListener listener) {
        ///Not needed on desktop.
    }
    
}
