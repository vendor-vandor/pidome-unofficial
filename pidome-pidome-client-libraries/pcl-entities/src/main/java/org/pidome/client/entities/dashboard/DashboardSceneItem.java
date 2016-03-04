/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.dashboard;

import java.util.Map;

/**
 * The dashboard scene item only holds information about the item.
 * @author John
 */
public class DashboardSceneItem extends DashboardItem {

    /**
     * The scene id.
     */
    int sceneId = 0;
    
    /**
     * Constructor.
     */
    public DashboardSceneItem() {
        super(ItemType.SCENE);
    }

    /**
     * Sets the item's config.
     * @param config Set config from RPC as map.
     */
    @Override
    protected void setConfig(Map<String, Object> config) {
        this.sceneId = Integer.valueOf((String)config.get("data-id"));
    }
    
    /**
     * Returns the scene id.
     * @return int scene id.
     */
    public final int getSceneId(){
        return this.sceneId;
    }
    
}