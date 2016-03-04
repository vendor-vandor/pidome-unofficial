/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.devices.devicestructure.builder;

/**
 *
 * @author John
 */
public class ColorPickerButtonBuilder {
    
    private final String label;
    private final String value;
    
    public ColorPickerButtonBuilder(String label, String value){
        this.label = label;
        this.value = value;
    }
    
    public final String getLabel(){
        return this.label;
    }
    
    public final String getValue(){
        return this.value;
    }
    
}