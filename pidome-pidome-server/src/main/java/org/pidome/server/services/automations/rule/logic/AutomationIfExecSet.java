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

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.automations.impl.AutomationStatementImpl;
import org.pidome.server.services.automations.impl.AutomationVariableImpl;
import org.pidome.server.services.automations.rule.logic.compare.AutomationCompareList;

/**
 *
 * @author John
 */
public class AutomationIfExecSet {
    
    static Logger LOG = LogManager.getLogger(AutomationIfExecSet.class);
    
    AutomationCompareList ifRule;
    
    List<AutomationStatementImpl> execList = new ArrayList();
    
    boolean hasRun     = false;
    boolean lastResult = false;
    
    public final void destroy(){
        if(ifRule!=null) ifRule.destroy();
        for(AutomationStatementImpl exec:execList){
            exec.destroy();
        }
        execList.clear();
    }
    
    protected List<AutomationVariableImpl> getVariablesList(){
        List<AutomationVariableImpl> returnList = new ArrayList<>();
        if(ifRule!=null){
            returnList = ifRule.getVariablesList();
        }
        if(execList!=null){
            for(AutomationStatementImpl execItem:execList){
                List<AutomationVariableImpl> items = execItem.getVariablesList();
                for(AutomationVariableImpl item:items){
                    if(!returnList.contains(item)){
                        returnList.add(item);
                    }
                }
            }
        }
        return returnList;
    }
    
    /**
     * Sets the if rule.
     * @param ifRule 
     */
    public final void setIf(AutomationCompareList ifRule){
        this.ifRule = ifRule;
    }
    
    /**
     * Sets the execution collection.
     * @param execList 
     */
    public final void setRunList(List<AutomationStatementImpl> execList){
        this.execList = execList;
    }
    
    /**
     * Adds an item to the list to be executed.
     * @param exec 
     */
    public final void addToRunList(AutomationStatementImpl exec){
        execList.add(exec);
    }
    
    /**
     * Runs the if statement.
     * @return 
     */
    public final boolean check(){
        /// when the if rule == null it is an else statement where no if is attached to. this does mean
        /// That every time the rule is checked it is executed.
        if(this.ifRule==null){
            lastResult = true;
            hasRun = false;
        } else {
            lastResult = this.ifRule.run();
            if(!lastResult){
                hasRun = false;
            }
        }
        return lastResult;
    }
    
    public void runExecList(){
        if(!hasRun){
            hasRun = true;
            for(AutomationStatementImpl exec:execList){
                /// Only execute executable items.
                if(!(exec instanceof AutomationIf)){
                    LOG.trace("Running exec list from if: {}", exec.getName());
                    exec.run();
                }
            }
        }
        ///Always run if statements.
        for(AutomationStatementImpl exec:execList){
            if((exec instanceof AutomationIf)){
                LOG.trace("Running exec list from if: {}", exec.getName());
                exec.run();
            }
        }
    }
    
    public void runExecListForced(){
        for(AutomationStatementImpl exec:execList){
            LOG.trace("Running exec list from if (forced): {}", exec.getName());
            exec.run();
        }
    }
    
}
