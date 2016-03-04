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
public class UniPiDigitalInputCommand {
    
    private String circuit;
    private boolean value;
    
    protected UniPiDigitalInputCommand(String circuit, boolean value){
        this.circuit = circuit;
        this.value = value;
    }
    
    public final String getCircuit(){
        return this.circuit;
    }
    
    public final boolean getValue(){
        return this.value;
    }
    
}