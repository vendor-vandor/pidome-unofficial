package org.pidome.server.connector.tools.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author John
 */
public class StringPropertyBindingBean implements PropertyChangeListener {
    
    
    private StringProperty property;
    
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
     * @param initialValue
     */
    public StringPropertyBindingBean(String initialValue){
        this.property = new StringProperty(initialValue);
    }
    
    /**
     * Sets a new value.
     * @param newValue 
     */
    public void setValue(String newValue){
        this.property.setValue(newValue);
    }
    
    /**
     * Shortcut for setValue.
     * @param newValue 
     */
    public void set(String newValue){
        setValue(newValue);
    }
    
    /**
     * Returns a safe string value.
     * When there is no value known it returns an empty string.
     * @return 
     */
    public String getValueSafe(){
        return (this.property.getValue()==null)?"":this.property.getValue();
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
        this.property.setValue((String)pce.getNewValue());
    }
    
    /**
     * Returns a read only version of this bean.
     * @return 
     */
    public final ReadOnlyStringPropertyBindingBean getReadOnlyBooleanPropertyBindingBean(){
        return new ReadOnlyStringPropertyBindingBean(this.property);
    }
}