/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.utilities.properties;

import java.beans.PropertyChangeSupport;

/**
 * Object property change support.
 * @author John
 * @param <Type> Object type to be used for this property.
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
     * @param value Initial value.
     */
    protected ObjectProperty(Type value){
        this.value = value;
    }
    
    /**
     * Returns the property change support.
     * @return Property change support.
     */
    protected final PropertyChangeSupport getProperty(){
        return propertyChangeSupport;
    }
    
    /**
     * Returns the current value.
     * @return Current value of Type.
     */
    protected final Type getValue(){
        return value;
    }
    
    /**
     * Set's the property value and notifies listeners.
     * @param newValue New value of Type
     */
    protected void setValue(Type newValue){
        synchronized (this) {
            Type oldValue = this.value;
            this.value = newValue;
            propertyChangeSupport.firePropertyChange("value", oldValue, newValue);
        }
    }
    
}
