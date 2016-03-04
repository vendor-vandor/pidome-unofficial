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
public class SliderDataBuilder {
    
    private final Number min;
    private final Number max;
    
    public SliderDataBuilder(Number min, Number max){
        this.min = min;
        this.max = max;
    }
    
    public final Number getMin(){
        return this.min;
    }
    
    public final Number getMax(){
        return this.max;
    }
    
}