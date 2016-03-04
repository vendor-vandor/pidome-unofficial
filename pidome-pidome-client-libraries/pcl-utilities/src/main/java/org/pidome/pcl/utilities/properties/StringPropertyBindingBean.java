package org.pidome.pcl.utilities.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * String property binding bean support.
 * @author John
 */
public class StringPropertyBindingBean implements PropertyChangeListener {

    /**
     * The String property.
     */
    private StringProperty property = new StringProperty("");
    
    /**
     * When bound this will be the child bean supplying new values to this bean.
     */
    private ObjectPropertyBindingBean boundPropertySupplier;

    /**
     * Constructor with empty value
     */
    public StringPropertyBindingBean(){
        this.property = new StringProperty();
    }
   
    /**
     * Constructor with initial value
     * @param initialValue Initial value
     */
    public StringPropertyBindingBean(String initialValue){
        this.property = new StringProperty(initialValue);
    }
    
    /**
     * Sets a new value.
     * @param newValue The new value.
     */
    public void setValue(String newValue){
        this.property.setValue(newValue);
    }
    
    /**
     * Shortcut for setValue.
     * @param newValue the new value
     */
    public void set(String newValue){
        setValue(newValue);
    }
    
    /**
     * Returns a safe string value.
     * When there is no value known it returns an empty string.
     * @return the current value, if there is no value known an empty string.
     */
    public String getValueSafe(){
        return (this.property.getValue()==null)?"":this.property.getValue();
    }
    
    /**
     * Returns the property value.
     * @return The current value.
     */
    public String getValue(){
        return this.property.getValue();
    }
    
    /**
     * Shortcut for getValue.
     * @return The current value.
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
    
    /**
     * Adds a listener for sake of binding.
     * @param supplier property to bind to.
     */
    private void addBindListener(ObjectPropertyBindingBean supplier) {
        if(this.boundPropertySupplier == null){
            this.boundPropertySupplier = supplier;
            this.boundPropertySupplier.addPropertyChangeListener(this);
        }
    }
    
    /**
     * Removes a listener.
     * @param listener removes the listener for the bound property.
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
     * @param pce bound property change event.
     */
    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        this.property.setValue((String)pce.getNewValue());
    }
    
    /**
     * Returns a read only version of this bean.
     * @return A read only version of this property.
     */
    public final ReadOnlyStringPropertyBindingBean getReadOnlyBooleanPropertyBindingBean(){
        return new ReadOnlyStringPropertyBindingBean(this.property);
    }
}