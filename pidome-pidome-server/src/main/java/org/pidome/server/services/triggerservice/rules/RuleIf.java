/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
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

package org.pidome.server.services.triggerservice.rules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple if rules.
 * This convenience class supports Strings, int or floats. An int is a number which is devides to an whole. 
 * @author John Sirach
 */
public class RuleIf extends RuleMethod {
    
    static Logger LOG = LogManager.getLogger(RuleIf.class);
    
    /**
     * Sets the primitive rule used.
     */
    RuleIfPrimitives primitiveRule;
    /**
     * Sets the rule check method.
     */
    Method RuleCheckMethod;
    
    /**
     * Sets the rule
     * @param subject A string, int or float to check against.
     * @param method Method used for checking.
     */
    public RuleIf(float subject, Method method){
        RuleCheckMethod = method;
        primitiveRule = new RuleIfNumberFloat(subject);
    }
    
    /**
     * Sets the rule
     * @param subject A string, int or float to check against.
     * @param method Method used for checking.
     */
    public RuleIf(int subject, Method method){
        RuleCheckMethod = method;
        primitiveRule = new RuleIfNumberInt(subject);
    }
    
    /**
     * Sets the rule
     * @param subject A string, int or float to check against.
     * @param method Method used for checking.
     */
    public RuleIf(String subject, Method method){
        RuleCheckMethod = method;
        primitiveRule = new RuleIfString(subject);
    }
    
    /**
     * Sets the rule
     * @param subject A string, int or float to check against.
     * @param method Method used for checking.
     */
    public RuleIf(boolean subject, Method method){
        RuleCheckMethod = method;
        primitiveRule = new RuleIfBoolean(subject);
    }
    
    /**
     * Returns the value that has been set where values are checked against.
     * @return
     */
    @Override
    public final Object getSetSubjectValue(){
        return this.primitiveRule.getSetValue();
    }
    
    /**
     * Returns the method used.
     * @return 
     */
    @Override
    public final Method getUsedMethod(){
        return this.RuleCheckMethod;
    }
    
    /**
     * Runs the rule based on the given rule method.
     * @param subject
     * @return 
     */
    @Override
    public final boolean run(Object subject){
        switch(RuleCheckMethod){
            case GREATERTHEN:
                return greaterThen(subject);
            case LESSTHEN:
                return lessThen(subject);
            case DIFFER:
                return differFrom(subject);
            default:
                return equalsTo(subject);
        }
    }
    
    /**
     * Checks if parameter is greater then subject.
     * @param subject
     * @return 
     */
    final boolean greaterThen(Object subject){
        return primitiveRule.greaterThen(subject);
    }
    
    /**
     * Checks if parameter is less then the subject.
     * @param subject
     * @return 
     */
    final boolean lessThen(Object subject){
        return primitiveRule.lessThen(subject);
    }
    
    /**
     * Checks if the parameter equals the subject.
     * @param subject
     * @return 
     */
    final boolean equalsTo(Object subject){
        return primitiveRule.equalsTo(subject);
    }
    
    /**
     * Checks if the parameter differs from the subject.
     * @param subject
     * @return 
     */
    final boolean differFrom(Object subject){
        return primitiveRule.differFrom(subject);
    }
    
}
