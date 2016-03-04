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

/**
 * Used as a primitive if rule (all though a string is not a primitive).
 * @author John Sirach
 */
public class RuleIfString implements RuleIfPrimitives {
    
    /**
     * The fixed value where the rule is checked against
     */
    String subject;
    
    /**
     * Returns the current set value
     * @return 
     */
    @Override
    public Object getSetValue(){
        return subject;
    }
    
    /**
     * Constructor sets the fixed subject;
     * @param subject 
     */
    protected RuleIfString(String subject){
        this.subject=subject;
    }

    /**
     * Checks if the given parameter is more in length.
     * @param compareTo
     * @return 
     */
    @Override
    public boolean greaterThen(Object compareTo) {
        return subject.length() < ((String)compareTo).length();
    }

    /**
     * Checks if the given parameter is shorter in length.
     * @param compareTo
     * @return 
     */
    @Override
    public boolean lessThen(Object compareTo) {
        return subject.length() > ((String)compareTo).length();
    }

    /**
     * Checks if the given parameter equals the subject.
     * @param compareTo
     * @return 
     */
    @Override
    public boolean equalsTo(Object compareTo) {
        return subject.equals(((String)compareTo));
    }

    /**
     * Checks if the given parameter differs from the subject.
     * @param compareTo
     * @return 
     */
    @Override
    public boolean differFrom(Object compareTo) {
        return !subject.equals(((String)compareTo));
    }
}
