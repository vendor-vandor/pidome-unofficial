/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.dashboard;

import java.util.Map;

/**
 *
 * @author John
 */
public class DashboardSpacerItem extends DashboardItem {

    /**
     * Text used in spacer to mark it as a header.
     */
    private String headerText = "";
    
    public DashboardSpacerItem() {
        super(ItemType.SPACER);
    }

    /**
     * Set's this spacer configuration.
     * @param config Dashboard spacer config as map from RPC.
     */
    @Override
    protected void setConfig(Map<String, Object> config) {
        if(config.containsKey("data-content")){
            headerText = (String)config.get("data-content");
        }
    }
    
    /**
     * Check if this spacer is a header.
     * @return boolean true if header.
     */
    public final boolean isHeader(){
        return !headerText.isEmpty();
    }
    
    /**
     * Return the header text.
     * @return header string.
     */
    public final String getHeaderText(){
        return this.headerText;
    }
}