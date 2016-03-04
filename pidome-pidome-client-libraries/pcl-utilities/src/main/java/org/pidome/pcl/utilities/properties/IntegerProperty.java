/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.utilities.properties;

import java.beans.PropertyChangeSupport;

/**
 * Integer property support.
 * @author John
 * @param <Integer> an integer value.
 */
public class IntegerProperty<Integer> {
    
    /**
     * Holds boolean value
     */
    private int value;
    
    /**
     * Property change support listener.
     */
    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    
    /**
     * Empty constructor.
     */
    protected IntegerProperty(){}
    
    /**
     * Constructor for initial value.
     * @param value initial value.
     */
    protected IntegerProperty(int value){
        this.value = value;
    }
    
    /**
     * Returns the property change support.
     * @return integer property.
     */
    protected final PropertyChangeSupport getProperty(){
        return propertyChangeSupport;
    }
    
    /**
     * Returns the current value.
     * @return current value.
     */
    protected final int getValue(){
        return value;
    }
    
    /**
     * Set's the property value and notifies listeners.
     * @param newValue New value.
     */
    protected void setValue(int newValue){
        synchronized (this) {
            int oldValue = this.value;
            this.value = newValue;
            propertyChangeSupport.firePropertyChange("value", oldValue, newValue);
        }
    }
}
