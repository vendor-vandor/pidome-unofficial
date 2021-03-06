package org.pidome.pcl.utilities.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A boolean property like class.
 * @author John
 */
public class BooleanPropertyBindingBean implements PropertyChangeListener {
    
    private BooleanProperty property = new BooleanProperty();
    
    /**
     * When bound this will be the child bean supplying new values to this bean.
     */
    private ObjectPropertyBindingBean boundPropertySupplier;

    
    /**
     * Constructor with empty value
     */
    public BooleanPropertyBindingBean(){
        this.property = new BooleanProperty();
    }
   
    /**
     * Constructor with initial value
     * @param initialValue initial value
     */
    public BooleanPropertyBindingBean(boolean initialValue){
        this.property = new BooleanProperty(initialValue);
    }
    
    /**
     * Sets a new value.
     * @param newValue new boolean value.
     */
    public void setValue(boolean newValue){
        this.property.setValue(newValue);
    }
    
    /**
     * Shortcut for setValue.
     * @param newValue new boolean value.
     */
    public void set(boolean newValue){
        setValue(newValue);
    }
    
    /**
     * Returns the property value.
     * @return returns the current value.
     */
    public boolean getValue(){
        return this.property.getValue();
    }
    
    /**
     * Shortcut for getValue.
     * @return returns the current value.
     */
    public boolean get(){
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
     * @param listener Property listener
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
     * @param pce Change event from an other bound property.
     */
    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        this.property.setValue((boolean)pce.getNewValue());
    }
    
    /**
     * Returns a read only version of this bean.
     * @return A read only property.
     */
    public final ReadOnlyBooleanPropertyBindingBean getReadOnlyBooleanPropertyBindingBean(){
        return new ReadOnlyBooleanPropertyBindingBean(this.property);
    }
    
}