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
public class UniPiRelayCommand {
    
    private String circuit;
    private int value;
    
    protected UniPiRelayCommand(String circuit, int value){
        this.circuit = circuit;
        this.value = value;
    }
    
    public final String getCircuit(){
        return this.circuit;
    }
    
    public final int getValue(){
        return this.value;
    }
    
}