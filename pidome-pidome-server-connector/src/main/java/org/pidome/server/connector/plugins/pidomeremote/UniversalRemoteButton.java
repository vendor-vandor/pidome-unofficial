/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.pidomeremote;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author John
 */
public class UniversalRemoteButton extends RemoteButton {

    List<RemoteButton> actionList = new ArrayList();
    
    public UniversalRemoteButton(String Id) {
        super(Id, true);
    }
    
    /**
     * Used by universal remotes having button actions.
     * @param actionList 
     */
    public final void setButtonActionList(List<RemoteButton> actionList){
        this.actionList = actionList;
    }
    
    public final List<RemoteButton>getButtonsActionList(){
        return this.actionList;
    }
    
}
