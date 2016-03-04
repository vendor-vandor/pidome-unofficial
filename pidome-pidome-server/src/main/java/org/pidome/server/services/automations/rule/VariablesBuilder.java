/*
 * Copyright 2015 John.
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
package org.pidome.server.services.automations.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.pidome.server.connector.tools.properties.ObjectPropertyBindingBean;
import org.pidome.server.services.automations.impl.AutomationVariableImpl;
import static org.pidome.server.services.automations.rule.AutomationRule.LOG;
import org.pidome.server.services.automations.variables.AutomationVariable;

/**
 *
 * @author John
 */
public class VariablesBuilder {
    
    /**
     * Creates the variables defined.
     * @param varNodes 
     * @return  
     */
    protected static Map<String,AutomationVariableImpl> createVariables(ArrayList varNodes){
        Map<String,AutomationVariableImpl> variables = new HashMap<>();
        for(int i=0; i<varNodes.size();i++){
            Map<String,Map<String,Map<String,Object>>> varBase = (Map<String,Map<String,Map<String,Object>>>)varNodes.get(i);
            for(Map.Entry<String,Map<String,Map<String,Object>>> varBlock:varBase.entrySet()){
                if(varBlock.getKey().equals("block") && varBlock.getValue().get("attributes").get("type").equals("variables_set")){
                    ArrayList<Map<String,Map<String,Object>>> varListSet = (ArrayList)varBlock.getValue().get("childs");
                    String varName  = "";
                    String varValue = "";
                    String varType  = "";
                    LOG.trace("Initial variable set: {}", varListSet);
                    for(Map<String,Map<String,Object>> vars:varListSet){
                        for(Map.Entry<String,Map<String,Object>> var:vars.entrySet()){
                            switch(var.getKey()){
                                case "field":
                                    varName = (String)var.getValue().get("value");
                                break;
                                case "value":
                                    Map<String,Map<String,ArrayList<Map<String,Map<String,String>>>>> valueSet = (Map<String,Map<String,ArrayList<Map<String,Map<String,String>>>>>)((ArrayList)var.getValue().get("childs")).get(0);
                                    varValue = valueSet.get("block").get("childs").get(0).get("field").get("value");
                                    varType = ((Map<String,String>)valueSet.get("block").get("attributes")).get("type");
                                break;
                                case "next":
                                    variables.putAll(createVariables((ArrayList)var.getValue().get("childs")));
                                break;
                            }
                        }
                    }
                    if(!varName.equals("") && !varValue.equals("")){
                        LOG.trace("Found var type: {}", varType);
                        ObjectPropertyBindingBean set = new ObjectPropertyBindingBean();
                        AutomationVariable setVar = new AutomationVariable(set, varName);
                        switch(varType){
                            case "math_number":
                                if(varValue.contains(".")){
                                    setVar.setDataType(AutomationVariableImpl.SubjectDataType.FLOAT);
                                } else {
                                    setVar.setDataType(AutomationVariableImpl.SubjectDataType.INTEGER);
                                }
                                set.setValue(varValue);
                            break;
                            case "logic_boolean":
                                setVar.setDataType(AutomationVariableImpl.SubjectDataType.BOOLEAN);
                                set.setValue(varValue.toLowerCase());
                            break;
                            default:
                                setVar.setDataType(AutomationVariableImpl.SubjectDataType.STRING);
                                set.setValue(varValue);
                            break;
                        }
                        variables.put(varName, setVar);
                    }
                }
            }
        }
        LOG.debug("Created variables: {}", variables);
        return variables;
    }
    
}
