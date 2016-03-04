/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.utilities.properties;

import java.beans.PropertyChangeListener;

/**
 * Read only object property support.
 * @author John
 * @param <Type> Object of type Type.
 */
public class ReadOnlyObjectPropertyBindingBean<Type> {
    
    /**
     * Original property.
     */
    private ObjectProperty<Type> property = new ObjectProperty<>();
   
    /**
     * Constructor with initial value
     * @param property Original property.
     */
    protected ReadOnlyObjectPropertyBindingBean(ObjectProperty<Type> property){
        this.property = property;
    }
    
    /**
     * Returns the property value.
     * @return Current value.
     */
    public Type getValue(){
        return this.property.getValue();
    }
    
    /**
     * Shortcut for getValue.
     * @return Current value.
     */
    public Type get(){
        return getValue();
    }
    
    /**
     * Adds a pure property listener.
     * @param listener Property change listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener){
        this.property.getProperty().addPropertyChangeListener(listener);
    }
    
    /**
     * Removes a pure property listener.
     * @param listener Property change listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener){
        this.property.getProperty().removePropertyChangeListener(listener);
    }

}