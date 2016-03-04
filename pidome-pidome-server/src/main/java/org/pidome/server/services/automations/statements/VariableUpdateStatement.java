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

package org.pidome.server.services.automations.statements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.automations.impl.AutomationVariableImpl;

/**
 *
 * @author John
 */
public class VariableUpdateStatement extends AutomationStatement {

    static Logger LOG = LogManager.getLogger(VariableUpdateStatement.class);
    
    private AutomationVariableImpl varSet;
    private String value;
    
    public VariableUpdateStatement(AutomationVariableImpl var, String value){
        super(new StringBuilder("UpdateVarStatement_").append(var.getName()).append("_").append(value).toString());
        this.varSet = var;
        this.value = value;
    }
    
    @Override
    public boolean run() {
        try {
            LOG.trace("Updating variable with datatype {}: {}", this.varSet.getDataType(), value);
            switch(this.varSet.getDataType()){
                case STRING:
                    if(!this.varSet.getProperty().getValue().equals(String.valueOf(value))) this.varSet.set(String.valueOf(value));
                case INTEGER:
                    if(this.varSet.getProperty().getValue()!=Integer.valueOf(value)) this.varSet.set(Integer.valueOf(value));
                case FLOAT:
                    if(this.varSet.getProperty().getValue()!=Float.valueOf(value)) this.varSet.set(Float.valueOf(value));
                case BOOLEAN:
                    if(this.varSet.getProperty().getValue()!=Boolean.valueOf(value)) this.varSet.set(Boolean.valueOf(value));
                default:
                    if(!this.varSet.getProperty().getValue().equals(value)) this.varSet.set(value);
            }
        } catch (Exception ex){
            LOG.error("Problem setting new var to {} with: {}, reason: {}", this.varSet.getDataType(), value, ex.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void destroy() {
        /// not used;
    }
    
}
