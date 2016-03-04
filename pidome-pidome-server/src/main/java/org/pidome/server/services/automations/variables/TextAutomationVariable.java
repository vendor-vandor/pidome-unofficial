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

package org.pidome.server.services.automations.variables;

import java.util.ArrayList;
import java.util.List;
import org.pidome.server.connector.tools.properties.ObjectPropertyBindingBean;

/**
 *
 * @author John
 */
public class TextAutomationVariable extends AutomationVariable {

    List<TextAutomationVariable> varSet = new ArrayList<>();
    
    public TextAutomationVariable(ObjectPropertyBindingBean var) {
        super(var, "TextVar");
    }
    
    @Override
    public void unlink(){
        super.unlink();
        if(getDataType()==SubjectDataType.SETLIST){
            if(varSet!=null){
                varSet.clear();
            }
        }
    }
    
    /**
     * Sets a list of vars in a single var.
     * @param list 
     */
    public void addVarSet(List<TextAutomationVariable> list){
        this.varSet = list;
        this.setDataType(SubjectDataType.SETLIST);
    }
    
    /**
     * returns the var set.
     * @return 
     */
    public final List<TextAutomationVariable> getVarSet(){
        return this.varSet;
    }
    
}