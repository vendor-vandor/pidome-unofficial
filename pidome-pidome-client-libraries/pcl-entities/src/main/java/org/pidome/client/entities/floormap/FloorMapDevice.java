/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.floormap;

import org.pidome.client.entities.devices.Device;

/**
 * A device used for visualization on a floor map.
 * @author John
 */
public class FloorMapDevice {
    
    /**
     * The device this floor map device is bound to.
     */
    private Device device;
    
    /**
     * The X location on the map.
     */
    private final double x;
    
    /**
     * The Y location on the map.
     */
    private final double y;
    
    /**
     * Floormap device constructor.
     * @param device The device this is bound to.
     * @param x The X location on the map.
     * @param y The Y location on the map.
     */
    protected FloorMapDevice(Device device, double x, double y){
        this.device = device;
        this.x = x;
        this.y = y;
    }
    
    /**
     * Return the bound device.
     * @return Device the bound device.
     */
    public final Device getDevice(){
        return this.device;
    }
    
    /**
     * Returns the X location on a floor map.
     * @return double x location.
     */
    public final double getX(){
        return this.x;
    }
    
    /**
     * Returns the Y location on a floor map.
     * @return double y location.
     */
    public final double getY(){
        return this.y;
    }
    
    /**
     * Destroy inner content.
     */
    protected final void destroy(){
        this.device = null;
    }
    
}
