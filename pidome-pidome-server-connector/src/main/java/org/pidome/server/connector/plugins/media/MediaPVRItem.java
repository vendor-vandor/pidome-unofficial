/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.media;

/**
 *
 * @author John
 */
public class MediaPVRItem extends MediaItem {
    
    MediaPlugin.ItemType type = MediaPlugin.ItemType.PVR;
        
    /**
     * Returns the ItemType for instances which can not identify the type;
     * @return 
     */
    @Override
    public final MediaPlugin.ItemType getItemType(){
        return type;
    }
}