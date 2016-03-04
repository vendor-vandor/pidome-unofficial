/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.utilities.properties;

import java.beans.PropertyChangeListener;

/**
 * Read only integer property support.
 * @author John
 */
public class ReadOnlyIntegerPropertyBindingBean {
    
    /**
     * Original property.
     */
    private IntegerProperty property = new IntegerProperty();
   
    /**
     * Constructor with initial value
     * @param property Original property.
     */
    protected ReadOnlyIntegerPropertyBindingBean(IntegerProperty property){
        this.property = property;
    }
    
    /**
     * Returns the property value.
     * @return current value.
     */
    public int getValue(){
        return this.property.getValue();
    }
    
    /**
     * Shortcut for getValue.
     * @return Current value.
     */
    public int get(){
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
