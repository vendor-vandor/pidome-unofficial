/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.automations.variables;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.tools.properties.ObjectPropertyBindingBean;
import org.pidome.server.services.events.CustomEvent;
import org.pidome.server.services.events.EventService;
import org.pidome.server.services.events.EventServiceListener;

/**
 *
 * @author John
 */
public class CustomEventVariable extends AutomationVariable implements EventServiceListener {

    int listenTo;
    
    static Logger LOG = LogManager.getLogger(CustomEventVariable.class);
    
    public CustomEventVariable(ObjectPropertyBindingBean var, int eventId) {
        super(var, "CustomEventListener_"+eventId);
        this.listenTo = eventId;
        EventService.getInstance().addListener(this);
    }

    @Override
    public void handleCustomEvent(CustomEvent event) {
        if(this.listenTo == event.getId()){
            LOG.trace("handling a custom event");
            this.set(true);
            this.set(false);
        }
    }
 
    @Override
    public final void destroy(){
        EventService.getInstance().removeListener(this);
        this.unlink();
    }
    
}
