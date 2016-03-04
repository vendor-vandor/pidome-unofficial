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

import java.util.Arrays;

/**
 * Rule used for checking if all the give rule subjects complies the given rules.
 * If one of the rules does not comply it returns false;
 * @author John Sirach
 */
public final class RuleAnd extends Rule {

    /**
     * Array of Rule subjects.
     * @param subjects 
     */
    public RuleAnd(RuleSubject... subjects){
        ruleSubjects.addAll(Arrays.asList(subjects));
    }

    /**
     * Runs the AND rule.
     * This function runs rules until it reaches a false and then returns immediately, after first setting and checking the subject.
     * @param subject
     * @param value
     * @return 
     */
    @Override
    public final boolean run(String subject, Object value) {
        for(RuleSubject ruleSubject:ruleSubjects){
            if(ruleSubject.isSubject(subject)){
                boolean result = ruleSubject.run(value);
                if(!result) return false;
            }
        }
        for(RuleSubject ruleSubject:ruleSubjects){
            if(!ruleSubject.isSubject(subject)){
                boolean result = ruleSubject.run(null);
                if(!result) return false;
            }
        }
        return true;
    }

}
