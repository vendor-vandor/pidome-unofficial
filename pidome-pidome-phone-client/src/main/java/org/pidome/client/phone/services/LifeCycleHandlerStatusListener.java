/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.services;

/**
 *
 * @author John
 */
public interface LifeCycleHandlerStatusListener {
    /**
     * Signal for the scene handler to switch scenes or not depending on the app's focus.
     * The scene handler will switch scenes when the previous focus was false.
     * @param focus 
     */
    public void handleInForeground(boolean focus);
}
