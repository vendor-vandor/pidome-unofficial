/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.utilities.properties;

import java.beans.PropertyChangeSupport;

/**
 * Boolean property support.
 * @author John
 */
public class BooleanProperty {
    
    /**
     * Holds boolean value
     */
    private boolean value;
    
    /**
     * Property change support listener.
     */
    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    
    /**
     * Empty constructor.
     */
    protected BooleanProperty(){}
    
    /**
     * Constructor for initial value.
     * @param value initial value.
     */
    protected BooleanProperty(boolean value){
        this.value = value;
    }
    
    /**
     * Returns the property change support.
     * @return boolean PropertyChangeSupport
     */
    protected final PropertyChangeSupport getProperty(){
        return propertyChangeSupport;
    }
    
    /**
     * Returns the current value.
     * @return Current boolean value.
     */
    protected final boolean getValue(){
        return value;
    }
    
    /**
     * Set's the property value and notifies listeners.
     * @param newValue the new boolean value
     */
    protected void setValue(boolean newValue){
        synchronized (this) {
            boolean oldValue = this.value;
            this.value = newValue;
            propertyChangeSupport.firePropertyChange("value", oldValue, newValue);
        }
    }
    
}
