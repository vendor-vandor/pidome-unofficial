/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.interfaces.web.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author John
 */
public final class WebPresentationGroups {
    
    List<WebPresentationGroup> collection = new ArrayList<>();
    
    /**
     * Returns the presentation group.
     * @return 
     */
    public final List<WebPresentationGroup> getList(){
        return this.collection;
    }
    
    /**
     * Adds an item to the group.
     * @param item 
     */
    public final void add(WebPresentationGroup item){
        if(!collection.contains(item)){
            collection.add(item);
        }
    }
    
}
