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
public class ObjectProperty<Type> {
    
    /**
     * Holds boolean value
     */
    private Type value;
    
    /**
     * Property change support listener.
     */
    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    
    /**
     * Empty constructor.
     */
    protected ObjectProperty(){}
    
    /**
     * Constructor for initial value.
     * @param value 
     */
    protected ObjectProperty(Type value){
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
    protected final Type getValue(){
        return value;
    }
    
    /**
     * Set's the property value and notifies listeners.
     * @param newValue 
     */
    protected void setValue(Type newValue){
        synchronized (this) {
            Type oldValue = this.value;
            this.value = newValue;
            propertyChangeSupport.firePropertyChange("value", oldValue, newValue);
        }
    }
    
}
