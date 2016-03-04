/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.utilities.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * An object property binding bean of Type.
 * @author John
 * @param <Type> Bean with the contents of Type.
 */
public class ObjectPropertyBindingBean<Type> implements PropertyChangeListener {
    
    
    private ObjectProperty<Type> property = new ObjectProperty<>();
    
    /**
     * When bound this will be the child bean supplying new values to this bean.
     */
    private ObjectPropertyBindingBean boundPropertySupplier;

    
    /**
     * Constructor with empty value
     */
    public ObjectPropertyBindingBean(){
        this.property = new ObjectProperty();
    }
   
    /**
     * Constructor with initial value
     * @param initialValue initial value
     */
    public ObjectPropertyBindingBean(Type initialValue){
        this.property = new ObjectProperty(initialValue);
    }
    
    /**
     * Sets a new value.
     * @param newValue new value
     */
    public void setValue(Type newValue){
        this.property.setValue(newValue);
    }
    
    /**
     * Shortcut for setValue.
     * @param newValue new value
     */
    public void set(Type newValue){
        setValue(newValue);
    }
    
    /**
     * Returns the property value.
     * @return Current value
     */
    public Type getValue(){
        return this.property.getValue();
    }
    
    /**
     * Shortcut for getValue.
     * @return Current Value.
     */
    public Type get(){
        return getValue();
    }
    
    /**
     * Adds a pure property listener.
     * @param listener Property listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener){
        this.property.getProperty().addPropertyChangeListener(listener);
    }
    
    /**
     * Removes a pure property listener.
     * @param listener Property listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener){
        this.property.getProperty().removePropertyChangeListener(listener);
    }
    
    /**
     * Adds a listener for sake of binding.
     * @param listener 
     */
    private void addBindListener(ObjectPropertyBindingBean supplier) {
        if(this.boundPropertySupplier == null){
            this.boundPropertySupplier = supplier;
            this.boundPropertySupplier.addPropertyChangeListener(this);
        }
    }
    
    /**
     * Removes a listener.
     * @param listener 
     */
    private void removeBindChangeListener(ObjectPropertyBindingBean supplier) {
        supplier.removePropertyChangeListener(this);
    }

    /**
     * Create an unique binding.
     * @param prop Property to bind to.
     */
    public void bind(ObjectPropertyBindingBean prop){
        addBindListener(prop);
    }
    
    /**
     * Unbind.
     */
    public void unbind(){
        if(this.boundPropertySupplier != null){
            removeBindChangeListener(this.boundPropertySupplier);
        }
    }
    
    /**
     * Update this property if it is bound to another property and listening.
     * @param pce Bound property change event.
     */
    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        this.property.setValue((Type)pce.getNewValue());
    }
    
    /**
     * Returns a read only version of this bean.
     * @return Read only version of this bean.
     */
    public final ReadOnlyObjectPropertyBindingBean<Type> getReadOnlyBooleanPropertyBindingBean(){
        return new ReadOnlyObjectPropertyBindingBean(this.property);
    }
    
}