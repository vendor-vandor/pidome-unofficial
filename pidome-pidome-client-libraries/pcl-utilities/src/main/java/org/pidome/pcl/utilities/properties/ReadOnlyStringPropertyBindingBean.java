/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.utilities.properties;

import java.beans.PropertyChangeListener;

/**
 * Read only String property support.
 * @author John
 */
public class ReadOnlyStringPropertyBindingBean {
    
    /**
     * Original property.
     */
    private StringProperty property = new StringProperty();
   
    /**
     * Constructor with initial value
     * @param property original property.
     */
    protected ReadOnlyStringPropertyBindingBean(StringProperty property){
        this.property = property;
    }

    /**
     * Returns the property value.
     * @return Current value, when no data is present an empty string.
     */
    public String getValueSafe(){
        return (this.property==null||this.property.getValue().isEmpty())?"":this.property.getValue();
    }
    
    /**
     * Returns the property value.
     * @return current value.
     */
    public String getValue(){
        return this.property.getValue();
    }
    
    /**
     * Shortcut for getValue.
     * @return Current value
     */
    public String get(){
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