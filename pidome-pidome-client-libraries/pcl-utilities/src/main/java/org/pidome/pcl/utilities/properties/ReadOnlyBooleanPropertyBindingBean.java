/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.utilities.properties;

import java.beans.PropertyChangeListener;

/**
 * Read only property support.
 * @author John
 */
public class ReadOnlyBooleanPropertyBindingBean {
    
    /**
     * Original boolean property.
     */
    private BooleanProperty property = new BooleanProperty();
   
    /**
     * Constructor with initial value
     * @param property The original boolean property.
     */
    protected ReadOnlyBooleanPropertyBindingBean(BooleanProperty property){
        this.property = property;
    }
    
    /**
     * Returns the property value.
     * @return the boolean value
     */
    public boolean getValue(){
        return this.property.getValue();
    }
    
    /**
     * Shortcut for getValue.
     * @return The boolean value.
     */
    public boolean get(){
        return getValue();
    }
    
    /**
     * Adds a pure property listener.
     * @param listener Property listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener){
        this.property.getProperty().addPropertyChangeListener(listener);
    }
    
    /**
     * Removes a pure property listener.
     * @param listener Property listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener){
        this.property.getProperty().removePropertyChangeListener(listener);
    }

}