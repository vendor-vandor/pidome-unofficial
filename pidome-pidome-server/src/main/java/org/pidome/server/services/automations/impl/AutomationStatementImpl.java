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

package org.pidome.server.services.automations.impl;

import java.util.List;

/**
 *
 * @author John
 */
public abstract class AutomationStatementImpl {
    
    String name;
    
    public AutomationStatementImpl(String name){
        this.name = name;
    }
    
    public String getName(){
        return this.name;
    }
    
    public abstract boolean run();
    
    public abstract boolean lastResult();
    
    public abstract void destroy();
    
    public abstract List<AutomationVariableImpl> getVariablesList();
    
}
