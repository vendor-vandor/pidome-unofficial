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
import org.pidome.misc.utils.TimeUtils;

/**
 *
 * @author John Sirach
 */
public class RuleTime extends RuleMethod {

    Method RuleCheckMethod;
    int calculatedTime;
    String baseTime;
    String occurrence;
    String calculation = "00:00";
    String subject;
    
    static Logger LOG = LogManager.getLogger(RuleTime.class);
    
    /**
     * Constructs the rule.
     * @param subject
     * @param calculation
     * @param method 
     */
    public RuleTime(String subject, String calculation, Method method){
        this.RuleCheckMethod = method;
        this.calculation = calculation;
        this.subject = subject;
        switch(subject){
            case "SUNSET":
                baseTime = TimeUtils.getSunset();
                calculatedTime = Integer.valueOf((TimeUtils.calcTimeDiff(baseTime, this.calculation)).replace(":", ""));
            break;
            case "SUNRISE":
                baseTime = TimeUtils.getSunrise();
                calculatedTime = Integer.valueOf((TimeUtils.calcTimeDiff(baseTime, this.calculation)).replace(":", ""));
            break;
            case "FIXED":
                baseTime = this.calculation;
                calculatedTime = Integer.valueOf(this.calculation.replace(":", ""));
            break;
        }
    }
    
    /**
     * Returns the value that has been set where values are checked against.
     * This method should not be used as a valid calculation resource but as a
     * represented value because it can contains internally calculated values.
     * @return
     */
    @Override
    public final Object getSetSubjectValue(){
        return baseTime + "(" + this.calculation + ")";
    }
    
    /**
     * Returns the method used.
     * @return 
     */
    @Override
    public final Method getUsedMethod(){
        return this.RuleCheckMethod;
    }
    
    /**
     * Updates the sunrise or sunset values.
     */
    public final void updateVariableTimes(){
        switch(subject){
            case "SUNSET":
                baseTime = TimeUtils.getSunset();
                calculatedTime = Integer.valueOf((TimeUtils.calcTimeDiff(baseTime, this.calculation)).replace(":", ""));
            break;
            case "SUNRISE":
                baseTime = TimeUtils.getSunrise();
                calculatedTime = Integer.valueOf((TimeUtils.calcTimeDiff(baseTime, this.calculation)).replace(":", ""));
            break;
        }
    }
    
    /**
     * The subject where to match against.
     * The subject always is like "WEEKEND_01:20","ALL_07:00" or "MON_10:00" etc...
     * @param subject
     * @return 
     */
    @Override
    public final boolean run(Object subject){
        int calcultateAgainst = Integer.valueOf(((String)subject).replace(":", ""));
        switch (RuleCheckMethod) {
            case GREATERTHEN:
                return greaterThen(calcultateAgainst);
            case LESSTHEN:
                return lessThen(calcultateAgainst);
            case DIFFER:
                return differFrom(calcultateAgainst);
            default:
                return equalsTo(calcultateAgainst);
        }
    }

    final boolean greaterThen(int subject){
        return subject > calculatedTime;
    }
    
    final boolean lessThen(int subject){
        return subject < calculatedTime;
    }
    
    final boolean differFrom(int subject){
        return subject != calculatedTime;
    }
    
    final boolean equalsTo(int subject){
        return subject == calculatedTime;
    }
    
}
