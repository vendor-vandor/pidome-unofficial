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
 * Rule methods.
 * These methods checks if values are greater,less,equals or not equals the given value.
 * @author John Sirach
 */
public abstract class RuleMethod {
    
    public enum Method {
        GREATERTHEN,
        LESSTHEN,
        EQUALS,
        DIFFER
    }
    
    /**
     * Returns the used method.
     * @return 
     */
    public abstract Method getUsedMethod();
    
    /**
     * Returns the value that has been set where values are checked against.
     * @return
     */
    public abstract Object getSetSubjectValue();
    
    /**
     * Runs the event rule against the set subject
     * @param subject
     * @return
     */
    public abstract boolean run(Object subject);
}
