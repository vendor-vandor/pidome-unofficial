/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities;

/**
 * Listener interface for entity broadcasts.
 * @author John
 */
public interface EntityErrorBroadcastListener {
    /**
     * Handles an entity error.
     * These errors are used when Entities fail to execute commands.
     * @param title Error title.
     * @param message  Error message.
     * @param ex The original exception thrown.
     */
    public void handleEntityError(String title, String message, Exception ex);
}
