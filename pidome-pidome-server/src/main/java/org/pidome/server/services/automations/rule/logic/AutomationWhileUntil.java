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

package org.pidome.server.services.automations.rule.logic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John
 */
public class AutomationWhileUntil extends AutomationIf {

    static Logger LOG = LogManager.getLogger(AutomationWhileUntil.class);
    
    private boolean lastResult = false;
    
    private boolean runMode;
    
    public AutomationWhileUntil(String mode){
        switch(mode){
            case "WHILE":
                runMode = true;
            break;
            default:
                runMode = false;
            break;
        }
        LOG.debug("Running WhileUntil in {} mode", runMode);
    }

    /**
     * Runs the if statements to view if any is true.
     * @return 
     */
    @Override
    public boolean run(){
        if(this.parent==null || this.parent.lastResult()){
            LOG.trace("Parent rule was null/true");
            for(AutomationIfExecSet run:ifSet){
                if(run.check()){
                    lastResult = runMode;
                    LOG.trace("Last if rule was true");
                    LOG.trace("Running exec list from whileuntil if");
                    run.runExecListForced();
                    return lastResult;
                } else {
                    LOG.trace("Last if rule was false");
                }
            }
        } else {
            LOG.trace("Parent rule was false");
        }
        lastResult = !runMode;
        return lastResult;
    }

    @Override
    public boolean lastResult() {
        return lastResult;
    }
    
}