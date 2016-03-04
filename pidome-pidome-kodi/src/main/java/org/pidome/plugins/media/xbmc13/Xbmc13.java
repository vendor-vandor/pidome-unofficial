/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.plugins.media.xbmc13;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.plugins.media.xbmc12.Xbmc12;
import org.pidome.server.connector.plugins.media.MediaException;

/**
 *
 * @author John
 */
public class Xbmc13 extends Xbmc12 {
    
    static Logger LOG = LogManager.getLogger(Xbmc13.class);
    
    /**
     * Handles an XBMC connection event.
     * @param event 
     */
    @Override
    public void handleConnectionEvent(String event) {
        switch(event){
            case "CONNECTED":
                String[] supplement = new String[2];
                supplement[0] = "PiDome";
                supplement[1] = "Connected with plugin XBMC 13";
                try {
                    handleServerCommand(ServerCommand.MESSAGE, (Object[]) supplement);
                    handlePlayerCommand(PlayerCommand.CURRENT, (String) null);
                    super.setRunning(true);
                } catch (MediaException ex){
                    LOG.error("Plugin startup was not correct: {}", ex.getMessage());
                }
            break;
            case "DISCONNECTED":
                super.setRunning(false);
            break;
        }
    }
    
}
