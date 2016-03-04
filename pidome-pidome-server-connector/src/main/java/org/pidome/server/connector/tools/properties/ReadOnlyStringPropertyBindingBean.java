/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.tools.properties;

import java.beans.PropertyChangeListener;

/**
 *
 * @author John
 */
public class ReadOnlyStringPropertyBindingBean {
    
    private StringProperty property = new StringProperty();
   
    /**
     * Constructor with initial value
     * @param property
     */
    protected ReadOnlyStringPropertyBindingBean(StringProperty property){
        this.property = property;
    }
    
    /**
     * Returns the property value.
     * @return 
     */
    public String getValue(){
        return this.property.getValue();
    }
    
    /**
     * Shortcut for getValue.
     * @return 
     */
    public String get(){
        return getValue();
    }
    
    /**
     * Adds a pure property listener.
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener){
        this.property.getProperty().addPropertyChangeListener(listener);
    }
    
    /**
     * Removes a pure property listener.
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener){
        this.property.getProperty().removePropertyChangeListener(listener);
    }
}