/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.devices;

/**
 *
 * @author John
 */
public class BasicDataForDeviceContainer {
 
    private final String group;
    private final String control;
    private Object value;
    
    private Object[] colorRGB;
    private Object[] colorHSB;
    
    public BasicDataForDeviceContainer(String group, String control){
        this.group = group;
        this.control = control;
    }
    
    public final String getGroupId(){
        return this.group;
    }
    
    public final String getControlId(){
        return this.control;
    }
    
    public final void setValue(Object value){
        this.value = value;
    }
    
    public final Object getValue(){
        return this.value;
    }
    
    public final void setRGBColorValue(Object r, Object g, Object b){
        colorRGB = new Object[3];
        colorRGB[0] = r;
        colorRGB[1] = g;
        colorRGB[2] = b;
    }

    public final boolean isRGBColor(){
        return colorRGB!=null;
    }
    
    public final Object[] getRGBColor(){
        return this.colorRGB;
    }
    
    public final void setHSBColorValue(Object h, Object s, Object b){
        colorHSB = new Object[3];
        colorHSB[0] = h;
        colorHSB[1] = s;
        colorHSB[2] = b;
    }
    
    public final boolean isHSBColor(){
        return colorHSB!=null;
    }
    
    public final Object[] getHSBColor(){
        return this.colorHSB;
    }
    
    public final boolean isColorData(){
        return (colorHSB != null && colorHSB != null );
    }
    
}