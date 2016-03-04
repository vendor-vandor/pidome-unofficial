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
 * Primitive rule for integers.
 * @author John Sirach
 */
public class RuleIfNumberInt implements RuleIfPrimitives {

    /**
     * Original fixed value.
     */
    int subject;
    
    /**
     * Returns the current set value
     * @return 
     */
    @Override
    public Object getSetValue(){
        return subject;
    }
    
    /**
     * constructor sets fixed subject.
     * @param subject 
     */
    protected RuleIfNumberInt(int subject){
        this.subject=subject;
    }

    /**
     * Checks if the given parameter is greater then the subject.
     * @param compareTo
     * @return 
     */
    @Override
    public boolean greaterThen(Object compareTo) {
        return subject < ((int)compareTo);
    }

    /**
     * Checks if the given parameter is less then the subject.
     * @param compareTo
     * @return 
     */
    @Override
    public boolean lessThen(Object compareTo) {
        return subject > ((int)compareTo);
    }

    /**
     * Checks if the given parameter is equal to the subject.
     * @param compareTo
     * @return 
     */
    @Override
    public boolean equalsTo(Object compareTo) {
        return subject == ((int)compareTo);
    }

    /**
     * Checks if the given parameter is unequal to the subject.
     * @param compareTo
     * @return 
     */
    @Override
    public boolean differFrom(Object compareTo) {
        return subject!=((int)compareTo);
    }
    
}
