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

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John
 */
public final class AutomationScheduledIf extends AutomationIf {

    static Logger LOG = LogManager.getLogger(AutomationScheduledIf.class);
    
    private ScheduledThreadPoolExecutor executor;
    
    private int length;
    private TimeUnit type;
    
    boolean internalResult = false;
    
    private boolean running = false;
    
    enum IntervalType {
        SECONDS,MINUTES,HOURS,DAYS;
    }
    
    public AutomationScheduledIf(int length, String type) {
        this.length = length;
        switch(type){
            case "SECONDS":
                this.type = TimeUnit.SECONDS;
            break;
            case "MINUTES":
                this.type = TimeUnit.MINUTES;
            break;
            case "HOURS":
                this.type = TimeUnit.HOURS;
            break;
            case "DAYS":
                this.type = TimeUnit.DAYS;
            break;
            default:
                this.type = TimeUnit.MINUTES;
            break;
        }
    }
    
    /**
     * Runs the if statements to view if any is true.
     * If the statement is true an executor is created with the constructor values with a one shot runnable.
     * When false the executor is destroyed.
     * @return 
     */
    @Override
    public boolean run(){
        for(AutomationIfExecSet run:ifSet){
            if(this.parent==null || this.parent.lastResult()){
                if(run.check()){
                    internalResult = true;
                    LOG.debug("Last if rule was true, starting executor with {} {}", length, this.type);
                    if(executor==null){
                        executor = new CustomScheduledThreadPoolExecutor(1);
                        executor.schedule(createExecRunnable(), length, this.type);
                    }
                } else {
                    LOG.debug("This if rule was false, shutting down executor if alive.");
                    if(executor!=null){
                        if(running){
                            //// Let the runnable finish;
                            executor.shutdown();
                        } else {
                            //// Do not let the runnable start.
                            executor.shutdownNow();
                        }
                        executor = null;
                    }
                    internalResult = false;
                }
            } else {
                LOG.debug("Last if rule was false, shutting down executor if alive.");
                if(executor!=null){
                    if(running){
                        //// Let the runnable finish;
                        executor.shutdown();
                    } else {
                        //// Do not let the runnable start.
                        executor.shutdownNow();
                    }
                    executor = null;
                }
                internalResult = false;
            }
        }
        return internalResult;
    }
    
    /**
     * Returning the last known result, used by child if\'s
     * @return 
     */
    @Override
    public boolean lastResult(){
        return internalResult;
    }
    
    private Runnable createExecRunnable(){
        Runnable run = () -> {
            running = true;
            LOG.debug("Running timed if rule");
            if(this.parent==null || this.parent.lastResult()){
                for(AutomationIfExecSet runRule:ifSet){
                    LOG.trace("Running exec list from scheduled if");
                    runRule.runExecListForced();
                }
            } else {
                LOG.debug("Parent is false, aborting execution of scheduled sequence.");
            }
            running = false;
        };
        return run;      
    }
    
    /**
     * Destroys the if statement.
     */
    @Override
    public void destroy(){
        if(executor!=null){
            executor.shutdownNow();
            executor = null;
        }
        super.destroy();
    }
    
    private class CustomScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

        private CustomScheduledThreadPoolExecutor(int threadPool){
            super(threadPool);
        }
        
        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            AutomationScheduledIf.this.executor = null;
        }

    }
    
}