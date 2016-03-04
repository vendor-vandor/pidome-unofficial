/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.tools.properties;

import java.beans.PropertyChangeSupport;

/**
 *
 * @author John
 */
public class IntegerProperty {
    
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
     * @param value 
     */
    protected IntegerProperty(int value){
        this.value = value;
    }
    
    /**
     * Returns the property change support.
     * @return 
     */
    protected final PropertyChangeSupport getProperty(){
        return propertyChangeSupport;
    }
    
    /**
     * Returns the current value.
     * @return 
     */
    protected final int getValue(){
        return value;
    }
    
    /**
     * Set's the property value and notifies listeners.
     * @param newValue 
     */
    protected void setValue(int newValue){
        synchronized (this) {
            int oldValue = this.value;
            this.value = newValue;
            propertyChangeSupport.firePropertyChange("value", oldValue, newValue);
        }
    }
}
