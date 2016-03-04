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

package org.pidome.server.services.automations.rule.logic.compare;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.automations.impl.AutomationVariableImpl;
import org.pidome.server.services.automations.rule.AutomationRule;

/**
 *
 * @author John
 */
public class AutomationComparison extends CompareBase {
    
    static Logger LOG = LogManager.getLogger(AutomationComparison.class);
    
    private AutomationVariableImpl var;
    private AutomationVariableImpl subject;
    private CheckType type = CheckType.EQUAL;
    
    AutomationRule rule;
    
    boolean result = false;

    public enum CheckType {
        LESS, LESS_EQUAL, EQUAL, EQUAL_MORE, MORE, UNEQUAL;
    }
    
    @Override
    protected final List<AutomationVariableImpl> getVariablesList(){
        List<AutomationVariableImpl> list = new ArrayList<>();
        list.add(subject);
        list.add(var);
        return list;
    }
    
    public AutomationComparison(AutomationRule rule, CheckType type, AutomationVariableImpl subject, AutomationVariableImpl var){
        this.rule = rule;
        this.subject = subject;
        this.var = var;
        this.type = type;
        this.var.setDataType(subject.getDataType());
    }
    
    @Override
    public final void destroy(){
        subject.destroy();
        subject.unlink();
        var.destroy();
        var.unlink();
    }
    
    /**
     * Does a check with the latest known variable content.
     */
    protected final boolean run(){
        LOG.trace("Having new subject data checking as {} with rule: {}, check type: {}, subject '{}'={}, var '{}'={}",rule.getName(),subject.getDataType(), type, subject.getName(), subject.getProperty().getValue(), var.getName(), var.getProperty().getValue());
        try {
            switch(subject.getDataType()){
                case STRING:
                    result = stringCheck(subject.getProperty().getValue().toString(), var.getProperty().getValue().toString());
                break;
                case INTEGER:
                    result = intCheck(Integer.valueOf(subject.getProperty().getValue().toString()),Integer.valueOf(var.getProperty().getValue().toString()));
                break;
                case FLOAT:
                    result = floatCheck(Float.valueOf(subject.getProperty().getValue().toString()),Float.valueOf(var.getProperty().getValue().toString()));
                break;
                case BOOLEAN:
                    result = booleanCheck(Boolean.valueOf(subject.getProperty().getValue().toString()),Boolean.valueOf(var.getProperty().getValue().toString()));
                break;
                default:
                    result = false;
                break;
            }
        } catch (Exception ex){
            result = false;
            LOG.warn("Could not check (initial) value, item not (yet?) available or wrong datatype: {}", ex.getMessage());
        }
        LOG.trace("Comparison result: {}", result);
        return result;
    }
    
    private boolean stringCheck(String newValue, String against){
        switch(type){
            case LESS:
                return newValue.length() < against.length();
            case LESS_EQUAL:
                return newValue.length() <= against.length();
            case EQUAL:
                return newValue.equals(against);
            case EQUAL_MORE:
                return newValue.length() >= against.length();
            case MORE:
                return newValue.length() > against.length(); 
            case UNEQUAL:
                return !newValue.equals(against);
            default:
                return false;
        }
    }

    private boolean intCheck(int newValue, int against){
        switch(type){
            case LESS:
                return newValue < against;
            case LESS_EQUAL:
                return newValue <= against;
            case EQUAL:
                return newValue == against;
            case EQUAL_MORE:
                return newValue >= against;
            case MORE:
                return newValue > against; 
            case UNEQUAL:
                return newValue != against;
            default:
                return false;
        }
    }
    
    private boolean floatCheck(float newValue, float against){
        switch(type){
            case LESS:
                return newValue < against;
            case LESS_EQUAL:
                return newValue <= against;
            case EQUAL:
                return newValue == against;
            case EQUAL_MORE:
                return newValue >= against;
            case MORE:
                return newValue > against;
            case UNEQUAL:
                return newValue != against;
            default:
                return false;
        }
    }

    private boolean booleanCheck(boolean newValue, boolean against){
        switch(type){
            case EQUAL:
                return newValue == against;
            case UNEQUAL:
                return newValue != against;
            default:
                return false;
        }
    }
    
    @Override
    public final String toString(){
        StringBuilder newString = new StringBuilder("AutomationComparison of type ").append(type.toString()).append(": ");
        newString.append("Subject: ").append(subject.getName()).append(" -> ").append(subject.getProperty().getValue());
        newString.append(", Var against: ").append(var.getName()).append(" -> ").append(var.getProperty().getValue());
        return newString.toString();
    }
    
}
