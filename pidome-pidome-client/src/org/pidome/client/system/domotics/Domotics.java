/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.domotics.components.DomComponent;
import org.pidome.client.system.domotics.components.macros.Macros;

/**
 *
 * @author John Sirach
 */
public final class Domotics extends DomComponents {

    public Domotics(){}
    
    static Logger LOG = LogManager.getLogger(Domotics.class);
    
    public final void initialize() throws DomoticsException {
        try {
            LOG.debug("Starting init");
            createComponentsFromInit(getInitXml());
            LOG.debug("Done init");
        } catch (DomResourceException | DomComponentsException ex) {
            throw new DomoticsException("Problem initializing: " + ex.getMessage());
        }
    }
    
    public final synchronized void startMacro(){
        LOG.debug("starting event dispatchers for components");
        ((Macros)getComponent("macros", "macros")).startMacros();
    }
    
    public static DomComponent getComponent(String type, String id){
        return componentCollection.get(type).get(id);
    }
    
    public static DomComponent getServer(){
        return getComponent("server", "server");
    }
}
