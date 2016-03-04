/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.interfaces.web.presentation;

/**
 * base presentation class.
 * @author John
 */
public abstract class WebPresentation {

    private String label;
    private Object value;
    
    public enum TYPE {
        CUSTOM_FUNCTION,
        SIMPLE_NVP, // simple name - value pair
        LIST_NVP, // List simple <name - value> pair
        COMPLEX_NVP // complex list<name - list<name - value>> pairs.
    }

    private TYPE type;
    
    /**
     * Constructor.
     * @param type
     * @param label 
     */
    public WebPresentation(TYPE type, String label){
        this.type = type;
        this.label = label;
    }
    
    /**
     * Returns the type.
     * @return 
     */
    public final TYPE getType(){
        return type;
    }

    /**
     * Sets the type.
     * @param value 
     */
    public abstract void setValue(Object value);
    
    /**
     * Sets the presentation object.
     * @param value 
     */
    protected final void setPresentationValue(Object value){
        this.value = value;
    }
    
    /**
     * Returns the presentation object.
     * @return 
     */
    public Object getPresentationValue(){
        return this.value;
    }
    
    /**
     * Returns the presentation label.
     * @return 
     */
    public final String getLabel(){
        return this.label;
    }
    
}
