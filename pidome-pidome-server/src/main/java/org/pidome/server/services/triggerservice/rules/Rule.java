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

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for a rule promising functionalities.
 * @author John Sirach
 */
public abstract class Rule {

    /**
     * List of rule subjects.
     */
    List<RuleSubject> ruleSubjects = new ArrayList();
    
    /**
     * Returns a list of members containing in rules.
     * @return 
     */
    public final List<String> getMembers(){
        List<String> list = new ArrayList();
        for(RuleSubject subject:ruleSubjects){
            list.add(subject.getMember());
        }
        return list;
    }

    /**
     * Returns a list of rules.
     * @return 
     */
    public final List<RuleSubject> getRules(){
        return ruleSubjects;
    }
    
    /**
     * Runs the rule method(s) set in the rule constructors.
     * @param subject
     * @param value
     * @return
     */
    public abstract boolean run(String subject, Object value);
    
}
