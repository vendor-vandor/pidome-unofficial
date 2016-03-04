/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.plugins.devices.uniPi;

/**
 *
 * @author John
 */
public class UniPiTemperatureCommand {
    
    private String circuit;
    private float value;
    
    protected UniPiTemperatureCommand(String circuit, float value){
        this.circuit = circuit;
        this.value = value;
    }
    
    public final String getCircuit(){
        return this.circuit;
    }
    
    public final float getValue(){
        return this.value;
    }
    
}