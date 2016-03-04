/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.pidomeremote;

/**
 *
 * @author John
 */
public class DefaultRemoteButton extends RemoteButton {

    /**
     * Creates a button.
     * @param id 
     */
    public DefaultRemoteButton(String id) {
        super(id);
    }

    /**
     * Sets the action belonging to this button.
     * This function is used in a default remote.
     * @param action 
     */
    public final void setButtonAction(String action){
        this.buttonAction = action;
    }
    
    /**
     * Returns the action belonging to the button.
     * @return 
     */
    public final String getButtonAction(){
        return this.buttonAction;
    }
    
}
