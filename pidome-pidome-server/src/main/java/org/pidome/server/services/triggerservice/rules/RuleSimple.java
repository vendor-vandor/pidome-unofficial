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
 * A simple if statement rule.
 * @author John Sirach
 */
public final class RuleSimple extends Rule {

    static Logger LOG = LogManager.getLogger(RuleSimple.class);
    
    /**
     * Constructs a simple rule, this is a single if rule.
     * @param subject The subject created.
     */
    public RuleSimple(RuleSubject... subject){
        ruleSubjects.add(subject[0]);
    }

    /**
     * Runs the rule if matches.
     * @param subject
     * @param value
     * @return 
     */
    @Override
    public final boolean run(String subject, Object value) {
        return (ruleSubjects.get(0).isSubject(subject) && ruleSubjects.get(0).run(value));
    }

}
