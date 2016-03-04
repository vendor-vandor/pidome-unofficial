/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.devices;

/**
 *
 * @author John
 */
public class InstalledDevice {
    
    private final String name;
    
    private final int id;
    
    protected InstalledDevice(int id, String name){
        this.name = name;
        this.id = id;
    }
    
    public final int getId(){
        return this.id;
    }
    
    public final String getName(){
        return this.name;
    }
    
}