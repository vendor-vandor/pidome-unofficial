package org.pidome.server.connector.tools.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author John
 */
public class IntegerPropertyBindingBean implements PropertyChangeListener {
    
    private IntegerProperty property;
    
    /**
     * When bound this will be the child bean supplying new values to this bean.
     */
    private ObjectPropertyBindingBean boundPropertySupplier;

    /**
     * Constructor with empty value
     */
    public IntegerPropertyBindingBean(){
        this.property = new IntegerProperty();
    }
   
    /**
     * Constructor with initial value
     * @param initialValue
     */
    public IntegerPropertyBindingBean(int initialValue){
        this.property = new IntegerProperty(initialValue);
    }
    
    /**
     * Sets a new value.
     * @param newValue 
     */
    public void setValue(int newValue){
        this.property.setValue(newValue);
    }
    
    /**
     * Shortcut for setValue.
     * @param newValue 
     */
    public void set(int newValue){
        setValue(newValue);
    }
    
    /**
     * Returns the property value.
     * @return 
     */
    public int getValue(){
        return this.property.getValue();
    }
    
    /**
     * Shortcut for getValue.
     * @return 
     */
    public int get(){
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
     * @param prop 
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
     * @param pce 
     */
    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        this.property.setValue((int)pce.getNewValue());
    }
    
    /**
     * Returns a read only version of this bean.
     * @return 
     */
    public final ReadOnlyIntegerPropertyBindingBean getReadOnlyBooleanPropertyBindingBean(){
        return new ReadOnlyIntegerPropertyBindingBean(this.property);
    }
}