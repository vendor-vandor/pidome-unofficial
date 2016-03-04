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
 * The rule subject is the item which a value is checked against.
 * The rule subject stores the subject's last known value set by run for later reference.
 * @author John Sirach
 */
public final class RuleSubject {

    Object lastKnownValue;
    RuleMethod method;
    String member;
    boolean lastResult = false;
    
    static Logger LOG = LogManager.getLogger(RuleSubject.class);
    
    /**
     * Constructs a new rule subject
     * @param member The member of the rule
     * @param method Rule method on how the subject is matched by value
     */
    public RuleSubject(String member, RuleMethod method){
        this(member, method, null);
    }
    
    /**
     * Constructs a new rule subject
     * @param member The member of the rule
     * @param method Rule method on how the subject is matched by value
     * @param initialValue
     */
    public RuleSubject(String member, RuleMethod method, Object initialValue){
        this.member = member;
        this.method  = method;
        this.lastKnownValue = initialValue;
        run(lastKnownValue);
    }
    
    /**
     * Returns true if the subject is member of the rule.
     * @param subject
     * @return 
     */
    public final boolean isSubject(String subject){
        return subject.equals(this.member);
    }
    
    /**
     * Returns the member name.
     * @return 
     */
    public final String getMember(){
        return member;
    }
    
    /**
     * Returns the rule method.
     * @return 
     */
    public final RuleMethod getMethod(){
        return method;
    }
    
    
    /**
     * Runs the rule based on the given value with the method defined  in the constructor.
     * @param value
     * @return 
     */
    public final boolean run(Object value){
        if(value!=null){
            lastKnownValue = value;
            lastResult = method.run(value);
        } else {
            LOG.trace("Returning last known result: {} in equation {} with matchtype {} with matchsubject {}", lastResult, this.member, method.getUsedMethod(), method.getSetSubjectValue());
            return lastResult;
        }
        LOG.trace("Returning new result: {} for value {} in equation {} with matchtype {} with matchsubject {}", lastResult, value, this.member, method.getUsedMethod(), method.getSetSubjectValue());
        return lastResult;
    }
    
}
