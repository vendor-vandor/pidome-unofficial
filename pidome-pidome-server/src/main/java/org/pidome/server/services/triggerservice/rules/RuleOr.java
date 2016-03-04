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
 * This holds OR subject rules. 
 * This checks if one of the rules matches the given criteria.
 * @author John Sirach
 */
public final class RuleOr extends Rule {

    public RuleOr(RuleSubject... subjects){
        ruleSubjects.addAll(Arrays.asList(subjects));
    }

    /**
     * Run the rule for a subject.
     * First the asked subject is checked. This is because even when false. The last known data is needed which is stored in check.
     * This could be needed when the flow is in continuous mode.
     * @param subject
     * @param value
     * @return 
     */
    @Override
    public final boolean run(String subject, Object value) {
        for(int i=0; i < ruleSubjects.size(); i++){
            if(ruleSubjects.get(i).isSubject(subject)){
                if(ruleSubjects.get(i).run(value)) return true;
            }
        }
        for(int i=0; i < ruleSubjects.size(); i++){
            /// check historical data by giving value null.
            if (ruleSubjects.get(i).run(null)) return true;
        }
        return false;
    }
    
}
