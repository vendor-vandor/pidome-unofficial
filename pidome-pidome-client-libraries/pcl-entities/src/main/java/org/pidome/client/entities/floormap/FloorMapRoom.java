/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.floormap;

/**
 * A room in a floor map.
 * @author John
 */
public class FloorMapRoom {
 
    final double locX;
    final double locY;
    final double width;
    final double height;
    
    final String name;
    
    final int id;
    
    protected FloorMapRoom(int floorId, String name, double locX, double locY, double width, double height){
        this.locX = locX;
        this.locY = locY;
        this.width = width;
        this.height = height;
        this.name = name;
        this.id = floorId;
    }
    
    public final int getId(){
        return this.id;
    }
    
    public final String getName(){
        return name;
    }
    
    public final double getWidth(){
        return width;
    }
    
    public final double getHeight(){
        return height;
    }
    
    public final double getLocX(){
        return locX;
    }
    
    public final double getLocY(){
        return locY;
    }
    
}