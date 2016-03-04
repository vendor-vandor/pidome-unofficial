/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.interfaces.web.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Media plugin configuration option.
 */
public final class WebOption {

    /**
     * Possible field types for plugins.
     */
    public enum WebOptionConfigurationFieldType {
        URL,IP_ADDRESS,STRING,PASSWORD,INT,DOUBLE,DEVICEDATA,SELECT,CHECKBOX,BOOLEAN,TIME;
    }
    
    String name        = "";
    String description = "";
    String optionId    = "";
    String optionValue = "";
    String defaultValue= "";
    Map<String,String> optionSet = new HashMap<>();
    WebOptionConfigurationFieldType fieldType = WebOptionConfigurationFieldType.STRING;
    
    /**
     * Constructor setting id, name and description.
     *
     * @param optionId
     * @param name
     * @param description
     * @param type
     */
    public WebOption(String optionId, String name, String description, WebOptionConfigurationFieldType type) {
        this.optionId = optionId;
        this.name = name;
        this.description = description;
        this.fieldType = type;
    }

    /**
     * Constructor setting id, name and description including an optionSet if field is a select field.
     * @param optionId
     * @param name
     * @param description
     * @param type
     * @param optionSet
     */
    public WebOption(String optionId, String name, String description, WebOptionConfigurationFieldType type, Map<String,String> optionSet) {
        if(type==WebOptionConfigurationFieldType.SELECT){
            this.optionId = optionId;
            this.name = name;
            this.description = description;
            this.fieldType = type;
            this.optionSet = optionSet;
        }
    }
    
    /**
     * Set's an options default value.
     * Should be set as a string because it is used in the web interface. Results
     * are automatically converted to the correct primitive.
     * @param value 
     */
    public final void setDefaultValue(String value){
        defaultValue = value;
    }
    
    /**
     * Returns the default value.
     * Defaults to empty string.
     * @return 
     */
    public final String getDefaultValue(){
        return this.defaultValue;
    }
    
    /**
     * Returns the field type.
     * @return 
     */
    public final WebOptionConfigurationFieldType getFieldType(){
        return this.fieldType;
    }
    
    public final Map<String,String>getSet(){
        return optionSet;
    }
    
    /**
     * Returns the option value
     * @return 
     */
    public final String getValue(){
        return (this.optionValue==null || this.optionValue.equals(""))?this.getDefaultValue():this.optionValue;
    }
    
    /**
     * Sets the option value 
     * @param value
     */
    public final void setValue(String value){
        this.optionValue = value;
    }
    
    /**
     * Gets the option's id.
     *
     * @return
     */
    public final String getId() {
        return optionId;
    }

    /**
     * Gets the option's name.
     *
     * @return
     */
    public final String getOptionName() {
        return this.name;
    }

    /**
     * Gets the option's description.
     *
     * @return
     */
    public final String getOptionDescription() {
        return this.description;
    }

}
