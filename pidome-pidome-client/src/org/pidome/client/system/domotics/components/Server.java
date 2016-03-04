/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John Sirach
 */
public final class Server implements DomComponent {

    Map<String,String> serverInfo = new HashMap<>();
    Map<String,String> versionInfo = new HashMap<>();
    Map<String,String> initInfo = new HashMap<>();
    
    static List<ServerEventListener> _listeners = new ArrayList();
    
    static Logger LOG = LogManager.getLogger(Server.class);
    
    public Server(){}
    
    public final void setServerInfo(Map<String,String> info){
        serverInfo = info;
    }
    
    public final void setServerVersionInfo(Map<String,String> info){
        versionInfo = info;
    }

    public final void setServerInitInfo(Map<String,String> info){
        initInfo = info;
    }
    
    public final Map<String,String> getServerVersionInfo(){
        return versionInfo;
    }
    
    public static synchronized void addServerEventListener(ServerEventListener l){
        LOG.debug("Added systate event listener {}", l.getClass().getName());
        _listeners.add(l);
    }

    public static synchronized void removeServerEventListener(ServerEventListener l){
        LOG.debug("Removed systate event listener {}", l.getClass().getName());
        _listeners.remove(l);
    }
    
    public final void dispatchServerEvent(String eventType){
        Iterator listeners = _listeners.iterator();
        ServerEvent serverEvent = new ServerEvent(this, eventType);
        while (listeners.hasNext()) {
            ((ServerEventListener) listeners.next()).handleServerEvent(serverEvent);
        }
    }
    
}
