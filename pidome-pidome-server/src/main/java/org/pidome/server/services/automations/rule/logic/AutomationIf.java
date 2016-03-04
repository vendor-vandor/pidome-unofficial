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

/**
 *
 * @author John
 */
public class AutomationIf extends AutomationStatementImpl {

    static Logger LOG = LogManager.getLogger(AutomationIf.class);
    
    List<AutomationIfExecSet> ifSet = new ArrayList<>();
    
    List<AutomationIfExecSet> elseExec = new ArrayList();
    
    AutomationIf parent;
    
    private boolean lastResult = false;
    
    public AutomationIf(){
        super("RunningAutomationIf");
    }
    
    @Override
    public final List<AutomationVariableImpl> getVariablesList(){
        List<AutomationVariableImpl> varsList = new ArrayList<>();
        for (AutomationIfExecSet ifExecSet:ifSet){
            for(AutomationVariableImpl varImpl:ifExecSet.getVariablesList()){
                if(!varsList.contains(varImpl)){
                    varsList.add(varImpl);
                }
            }
        }
        for (AutomationIfExecSet ifExecSet:elseExec){
            for(AutomationVariableImpl varImpl:ifExecSet.getVariablesList()){
                if(!varsList.contains(varImpl)){
                    varsList.add(varImpl);
                }
            }
        }
        return varsList;
    }
    
    /**
     * Runs the if statements to view if any is true.
     * @return 
     */
    @Override
    public boolean run(){
        boolean internalResult = false;
        if(this.parent==null || this.parent.lastResult()){
            LOG.trace("Parent rule was null("+(this.parent==null)+")/true");
            for(AutomationIfExecSet run:ifSet){
                if(!internalResult && run.check()){
                    lastResult = true;
                    LOG.trace("Last if rule was true, running exec list: {}", run.getClass().getName());
                    run.runExecList();
                    internalResult = true;
                } else {
                    run.check();
                    LOG.trace("Last if rule was false");
                }
            }
            if(internalResult==false){
                lastResult = false;
                for(AutomationIfExecSet runElse:elseExec){
                    LOG.trace("Running else rule, running exec list: {}", runElse.getClass().getName());
                    if(runElse.check()){
                        runElse.runExecList();
                    }
                }
            }
        } else {
            lastResult = false;
            LOG.trace("Parent rule was false");
        }
        return lastResult;
    }
    
    /**
     * Returning the last known result, used by child if\'s
     * @return 
     */
    @Override
    public boolean lastResult(){
        return lastResult;
    }
    
    /**
     * Adds a parent.
     * @param parent 
     */
    public final void addParent(AutomationIf parent){
        this.parent = parent;
    }
    
    /**
     * Adds an if execution set.
     * @param set 
     */
    public final void addIfExecSet(AutomationIfExecSet set){
        ifSet.add(set);
    }
    
    /**
     * Adds an else statement to be executed when if\'s are false.
     * @param elseExec 
     */
    public final void addElseSet(AutomationIfExecSet elseExec){
        this.elseExec.add(elseExec);
    }
    
    /**
     * Destroys the if statement.
     */
    @Override
    public void destroy(){
        for(AutomationIfExecSet run:ifSet){
            run.destroy();
        }
        for(AutomationIfExecSet run:elseExec){
            run.destroy();
        }
    }
    
}