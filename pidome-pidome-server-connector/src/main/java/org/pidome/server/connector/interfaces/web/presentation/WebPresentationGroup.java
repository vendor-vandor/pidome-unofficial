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
public final class WebPresentationGroup {
    
    List<WebPresentation> collection = new ArrayList<>();
            
    String title = "";
    String description = "";
    
    public WebPresentationGroup(String title, String description){
        this.title = title;
        this.description = description;
    }
    
    /**
     * Adds an item to the group.
     * @param item 
     */
    public final void add(WebPresentation item){
        if(!collection.contains(item)){
            collection.add(item);
        }
    }
    
    /**
     * Returns the group title.
     * @return 
     */
    public final String getTitle(){
        return this.title;
    }
    
    /**
     * Returns the description.
     * @return 
     */
    public final String getDescription(){
        return this.description;
    }
    
    /**
     * Returns the collection.
     * @return 
     */
    public final List<WebPresentation> getCollection(){
        return this.collection;
    }
    
}
