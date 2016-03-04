/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.media;

import org.pidome.server.connector.plugins.media.MediaPlugin.ItemType;

/**
 *
 * @author John
 */
public final class MediaVideoItem extends MediaItem {
    
    ItemType type = ItemType.VIDEO;
        
    /**
     * Returns the ItemType for instances which can not identify the type;
     * @return 
     */
    @Override
    public final ItemType getItemType(){
        return type;
    }
    
}
