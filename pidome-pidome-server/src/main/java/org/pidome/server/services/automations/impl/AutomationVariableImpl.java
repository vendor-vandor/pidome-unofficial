/*
 * Copyright 2014 John.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.server.services.automations.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.tools.properties.ObjectPropertyBindingBean;

/**
 *
 * @author John
 */
public abstract class AutomationVariableImpl {
    
    static Logger LOG = LogManager.getLogger(AutomationVariableImpl.class);
    
    private ObjectPropertyBindingBean object;
    private SubjectDataType type = SubjectDataType.STRING;
    
    String varName = "";
    
    /**
     * Returns the assigned var name.
     * @return 
     */
    public final String getName(){
        return this.varName;
    }
    
    /**
     * Data types.
     */
    public enum SubjectDataType {
        STRING,FLOAT,INTEGER,BOOLEAN,SETLIST;
    }
    
    /**
     * Returns is this single var is in real a var set causing traversing.
     * @return 
     */
    public final boolean isVarSet(){
        return (getDataType()==SubjectDataType.SETLIST);
    }
    
    /**
     * Constructor for a SimpleObjectProperty.
     * @param var 
     * @param name 
     */
    public AutomationVariableImpl(ObjectPropertyBindingBean var, String name){
        object = var;
        this.varName = name;
    }
    
    /**
     * Sets the datatype.
     * @param type 
     */
    public void setDataType(SubjectDataType type){
        this.type = type;
    }
    
    /**
     * Returns the data type.
     * @return 
     */
    public SubjectDataType getDataType (){
        return this.type;
    }
    
    /**
     * Destroys links.
     */
    public abstract void destroy();
    
    /**
     * Bye bye.
     */
    public void unlink(){
        this.object = null;
    }
    
    /**
     * Returns the object property.
     * @return 
     */
    public ObjectPropertyBindingBean getProperty(){
        return this.object;
    }
    
    public final void set(Object object){
        LOG.trace("Setting new object value: {}", object);
        if(object==null){
            this.object.setValue(null);
        } else {
            switch(type){
                case STRING:
                    this.object.setValue(object.toString());
                break;
                case INTEGER:
                    this.object.setValue(Integer.valueOf(object.toString()));
                break;
                case FLOAT:
                    this.object.setValue(Float.valueOf(object.toString()));
                break;
                case BOOLEAN:
                    this.object.setValue(Boolean.valueOf(object.toString().toLowerCase()));
                break;
                default:
                    this.object.setValue(object.toString());
                break;
            }
        }
    }
    
}