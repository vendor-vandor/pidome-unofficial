/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.dashboard;

import java.util.Map;

/**
 * The dashboard macro item only holds information about the item.
 * @author John
 */
public class DashboardMacroItem extends DashboardItem {

    /**
     * The macro id.
     */
    int macroId = 0;
    
    /**
     * Constructor.
     */
    public DashboardMacroItem() {
        super(ItemType.MACRO);
    }

    /**
     * Sets the item's config.
     * @param config Set config from RPC as map.
     */
    @Override
    protected void setConfig(Map<String, Object> config) {
        this.macroId = Integer.valueOf((String)config.get("data-id"));
    }
    
    /**
     * Returns the macro id.
     * @return int macro id.
     */
    public final int getMacroId(){
        return this.macroId;
    }
    
}