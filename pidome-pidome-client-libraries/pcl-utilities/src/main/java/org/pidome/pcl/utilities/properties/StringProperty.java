/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.utilities.properties;

import java.beans.PropertyChangeSupport;

/**
 * String property support.
 * @author John
 */
public class StringProperty {
    
    /**
     * Holds boolean value
     */
    private String value;
    
    /**
     * Property change support listener.
     */
    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    
    /**
     * Empty constructor.
     */
    protected StringProperty(){}
    
    /**
     * Constructor for initial value.
     * @param value initial value.
     */
    protected StringProperty(String value){
        this.value = value;
    }
    
    /**
     * Returns the property change support.
     * @return returns the property.
     */
    protected final PropertyChangeSupport getProperty(){
        return propertyChangeSupport;
    }
    
    /**
     * Returns the current value.
     * @return current value.
     */
    protected final String getValue(){
        return value;
    }
    
    /**
     * Set's the property value and notifies listeners.
     * @param newValue the new value.
     */
    protected void setValue(String newValue){
        synchronized (this) {
            String oldValue = this.value;
            this.value = newValue;
            propertyChangeSupport.firePropertyChange("value", oldValue, newValue);
        }
    }
}
