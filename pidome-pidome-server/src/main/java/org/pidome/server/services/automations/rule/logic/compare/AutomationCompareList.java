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

package org.pidome.server.services.automations.rule.logic.compare;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.automations.impl.AutomationVariableImpl;

/**
 *
 * @author John
 */
public final class AutomationCompareList extends CompareBase {
    
    List<CompareBase> list = new ArrayList<>();
    
    static Logger LOG = LogManager.getLogger(AutomationCompareList.class);
    
    private ListType type = ListType.OR;
    
    boolean result = false;

    public enum ListType{
        AND,OR;
    }
    
    public AutomationCompareList(ListType listType){
        this.type = listType;
    }
    
    @Override
    public final List<AutomationVariableImpl> getVariablesList(){
        List<AutomationVariableImpl> varsList = new ArrayList<>();
        for (CompareBase compare:list){
            for(AutomationVariableImpl varImpl:compare.getVariablesList()){
                if(!varsList.contains(varImpl)){
                    varsList.add(varImpl);
                }
            }
        }
        return varsList;
    }
    
    public final void add(CompareBase compare){
        if(!list.contains(compare)){
            list.add(compare);
        }
    }
    
    @Override
    public boolean run(){
        LOG.trace("Running compare list rulesets. Number of rules: {}", list.size());
        if(list.isEmpty()) { 
            result = false;
            return false; 
        }
        switch(type){
            case OR:
                result = false;
                for(CompareBase compare:list){
                    if(compare.run()){
                        result = true;
                        break;
                    }
                }
            break;
            case AND:
                result = true;
                for(CompareBase compare:list){
                    if(!compare.run()){
                        result = false;
                        break;
                    }
                }
            break;
            default:
                result = false;
            break;
        }
        LOG.debug("Rule compare list result with check type {}: {}", type, result);
        return result;
    }
 
    public final void destroy(){
        for(CompareBase compare:list){
            compare.destroy();
        }
        list.clear();
    }
    
    
    @Override
    public final String toString(){
        StringBuilder newString = new StringBuilder("AutomationCompareList: ");
        for(CompareBase item: list){
            newString.append(item.toString()).append(", ");
        }
        return newString.toString();
    }
    
}
