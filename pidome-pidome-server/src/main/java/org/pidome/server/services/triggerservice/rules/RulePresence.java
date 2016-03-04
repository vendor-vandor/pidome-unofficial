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

package org.pidome.server.services.triggerservice.rules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John
 */
public class RulePresence extends RuleMethod {

    int checkPrecense;
    Method ruleCheckMethod;
    
    static Logger LOG = LogManager.getLogger(RulePresence.class);

    
    public RulePresence(int subject, Method method){
        this.ruleCheckMethod = method;
        this.checkPrecense = subject;
    }
    
    /**
     * Returns the method used.
     * @return 
     */
    @Override
    public final Method getUsedMethod(){
        return this.ruleCheckMethod;
    }
    
    /**
     * Returns the value that has been set where values are checked against.
     * @return
     */
    @Override
    public final Object getSetSubjectValue(){
        return this.checkPrecense;
    }
    
    @Override
    public final boolean run(Object subject) {
        boolean result = false;
        switch (ruleCheckMethod) {
            case EQUALS:
                result = checkPrecense==(int)subject;
            break;
            case DIFFER:
                result = checkPrecense!=(int)subject;
            break;
        }
        LOG.trace("Checked if {} {} {}: {}", subject, ruleCheckMethod, checkPrecense, result);
        return result;
    }
    
}
