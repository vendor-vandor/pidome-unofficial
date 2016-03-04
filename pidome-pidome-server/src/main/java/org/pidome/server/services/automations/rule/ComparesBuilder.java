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
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.misc.utils.TimeUtils;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.tools.properties.ObjectPropertyBindingBean;
import org.pidome.server.services.automations.impl.AutomationVariableImpl;
import org.pidome.server.services.automations.rule.logic.compare.AutomationCompareList;
import org.pidome.server.services.automations.rule.logic.compare.AutomationComparison;
import org.pidome.server.services.automations.variables.AutomationVariable;
import org.pidome.server.services.automations.variables.CustomEventVariable;
import org.pidome.server.services.automations.variables.DeviceAutomationVariable;
import org.pidome.server.services.automations.variables.MediaAutomationVariable;
import org.pidome.server.services.automations.variables.SunriseSunsetCalulatorVariable;
import org.pidome.server.services.automations.variables.WeatherStatusVariable;
import org.pidome.server.services.plugins.WeatherPluginService;
import org.pidome.server.system.dayparts.DayPartsService;
import org.pidome.server.system.presence.PresenceService;
import org.pidome.server.system.userstatus.UserStatusService;

/**
 *
 * @author John
 */
public class ComparesBuilder {
    
    static Logger LOG = LogManager.getLogger(ComparesBuilder.class);
    
    /**
     * Routines for creating a logical comparison.
     * @param rule
     * @param logicData
     * @param variables
     * @return
     * @throws Exception 
     */
    protected static AutomationComparison createLogicCompare(AutomationRule rule,ArrayList<Map<String,Map<String,Object>>> logicData, Map<String,AutomationVariableImpl> variables) throws Exception {
        AutomationVariableImpl var1 = null;
        AutomationVariableImpl var2 = null;
        AutomationComparison.CheckType type = AutomationComparison.CheckType.EQUAL;
        LOG.trace("Creating in createLogicCompare: {}", logicData);
        try {
            switch(((Map<String,String>)logicData.get(1).get("field").get("attributes")).get("name")){
                case "userpresence":
                    return createPresenceCompare(rule,logicData);
                case "userstatus":
                    return createUserStatusCompare(rule,logicData);
                case "daypart":
                    return createDaypartCompare(rule,logicData);
            }
        } catch (Exception ex){
            //// Resume non direct compare.
        }
        for(int i = 0; i < logicData.size(); i++){
            Map<String,Map<String,Object>> logicFields = logicData.get(i);
            for(Map.Entry<String,Map<String,Object>> logicField:logicFields.entrySet()){
                switch(logicField.getKey()){
                    case "field":
                        switch((String)logicField.getValue().get("value")){
                            case "EQ":
                                type = AutomationComparison.CheckType.EQUAL;
                            break;
                            case "NEQ":
                                type = AutomationComparison.CheckType.UNEQUAL;
                            break;
                            case "LT":
                                type = AutomationComparison.CheckType.LESS;
                            break;
                            case "LTE":
                                type = AutomationComparison.CheckType.LESS_EQUAL;
                            break;
                            case "GT":
                                type = AutomationComparison.CheckType.MORE;
                            break;
                            case "GTE":
                                type = AutomationComparison.CheckType.EQUAL_MORE;
                            break;
                            default:
                                type = AutomationComparison.CheckType.EQUAL;
                            break;
                        }
                    break;
                    case "value":
                        Map<String,Map<String,Map<String,Object>>> valueData = (Map<String,Map<String,Map<String,Object>>>)((ArrayList<Map<String,Map<String,Map<String,Object>>>>)logicField.getValue().get("childs")).get(0);
                        switch((String)valueData.get("block").get("attributes").get("type")){
                            case "math_number":
                            case "logic_boolean":
                            case "text":
                                Object object = ((List<Map<String,Map<String,Object>>>)valueData.get("block").get("childs")).get(0).get("field").get("value");
                                if(object!=null){
                                    
                                    ObjectPropertyBindingBean set = new ObjectPropertyBindingBean();
                                    AutomationVariableImpl varTypeTemp = new AutomationVariable(set, (String)valueData.get("block").get("attributes").get("type") + "_Var");
                                    
                                    LOG.trace("Using var type: {}", (String)valueData.get("block").get("attributes").get("type"));
                                    switch((String)valueData.get("block").get("attributes").get("type")){
                                        case "math_number":
                                            if(((String)object).contains(".")){
                                                varTypeTemp.setDataType(AutomationVariableImpl.SubjectDataType.FLOAT);
                                            } else {
                                                varTypeTemp.setDataType(AutomationVariableImpl.SubjectDataType.INTEGER);
                                            }
                                            set.setValue(object);
                                        break;
                                        case "logic_boolean":
                                            varTypeTemp.setDataType(AutomationVariableImpl.SubjectDataType.BOOLEAN);
                                            set.setValue(((String)object).toLowerCase());
                                        break;
                                        default:
                                            varTypeTemp.setDataType(AutomationVariableImpl.SubjectDataType.STRING);
                                            set.setValue((String)object);
                                        break;
                                    }
                                    if(((Map<String,String>)logicFields.get("value").get("attributes")).get("name").equals("A")){
                                        var1 = varTypeTemp;
                                    } else {
                                        var2 = varTypeTemp;
                                    }
                                }
                            break;
                            case "variables_get":
                                String var_get_object = ((List<Map<String,Map<String,String>>>)valueData.get("block").get("childs")).get(0).get("field").get("value");
                                if(var_get_object!=null){
                                    if(variables.containsKey(var_get_object)){
                                        if(((Map<String,String>)logicFields.get("value").get("attributes")).get("name").equals("A")){
                                            var1 = variables.get(var_get_object);
                                        } else {
                                            var2 = variables.get(var_get_object);
                                        }
                                    }
                                }
                            break;
                            default:
                                String[] value = ((String)valueData.get("block").get("attributes").get("type")).split(":");
                                switch(value[0]){
                                    case "device_eq":
                                        DeviceAutomationVariable tmp = new DeviceAutomationVariable(Integer.parseInt(value[1]), value[2], value[3]);
                                        switch(tmp.getDeviceDataType()){
                                            case STRING:
                                            case HEX:
                                                tmp.setDataType(AutomationVariableImpl.SubjectDataType.STRING);
                                            break;
                                            case INTEGER:
                                                tmp.setDataType(AutomationVariableImpl.SubjectDataType.INTEGER);
                                            break;
                                            case FLOAT:
                                                tmp.setDataType(AutomationVariableImpl.SubjectDataType.FLOAT);
                                            break;
                                            case BOOLEAN:
                                                tmp.setDataType(AutomationVariableImpl.SubjectDataType.BOOLEAN);
                                            break;
                                            default:
                                                tmp.setDataType(AutomationVariableImpl.SubjectDataType.STRING);
                                            break;
                                        }
                                        if(((Map<String,String>)logicFields.get("value").get("attributes")).get("name").equals("A")){
                                            var1 = tmp;
                                        } else {
                                            var2 = tmp;
                                        }
                                    break;
                                }
                            break;
                        }
                    break;
                }
            }
        }
        if(var1!=null && var2!=null && type!= null){
            return new AutomationComparison(rule,type, var1, var2);
        } else {
            LOG.warn("Incorrect/multi logic_compare setup. Remark: This is default behavior in an IF combination with both Logic (<,=,>,=<,=>) compares and logic operations (AND,OR), and not an error. The logic operation will be passed on and reported back to the rule.");
            LOG.warn("Failing logic data collection, following vars - var1: {}, var2: {}, type: {} fullSet: {}", var1, var2, type, logicData);
            throw new Exception("Incorrect logic_compare setup, check log (unless logic compare and operations are combined)");
        }
    }
    
    /**
     * Creates a logic operation set
     * @param rule
     * @param values
     * @param variables
     * @return 
     */
    protected static AutomationCompareList createLogicOperation(AutomationRule rule,Map<String,Map<String,Map<String,Object>>> values, Map<String,AutomationVariableImpl> variables){
        AutomationCompareList operationLocalList;
        LOG.debug("Creating an operationLocalList as type: {}", ((ArrayList<Map<String,Map<String,String>>>)values.get("block").get("childs")).get(0).get("field").get("value").trim());
        switch(((ArrayList<Map<String,Map<String,String>>>)values.get("block").get("childs")).get(0).get("field").get("value").trim()){
            case "AND":
                operationLocalList = new AutomationCompareList(AutomationCompareList.ListType.AND);
            break;
            default:
                operationLocalList = new AutomationCompareList(AutomationCompareList.ListType.OR);
            break;
        }
        try {
            for(int w=1; w<((ArrayList<Map<String,Map<String,Map<String,Object>>>>)values.get("block").get("childs")).size();w++){

                Map<String,Map<String,Map<String,Object>>> tmpData = ((ArrayList<Map<String,Map<String,Map<String,Object>>>>)values.get("block").get("childs")).get(w);
                Map<String,Map<String,Map<String,Object>>> workData= ((ArrayList<Map<String,Map<String,Map<String,Object>>>>)tmpData.get("value").get("childs")).get(0);
            
                LOG.trace("\n\nNeed to work on: {}\n\n", workData);
                
                switch((String)workData.get("block").get("attributes").get("type")){
                    case "if_userstatus":
                        LOG.debug("Creating createLogicOperation: if_userstatus: {}", workData);
                        AutomationComparison compareUserStatus = ComparesBuilder.createUserStatusCompare(rule,((ArrayList<Map<String,Map<String,Object>>>)workData.get("block").get("childs")));
                        operationLocalList.add(compareUserStatus);
                    break;
                    case "if_weatherstatus":
                        LOG.debug("Creating createLogicOperation: if_weatherstatus: {}", workData);
                        AutomationComparison compareWeatherStatus = ComparesBuilder.createWeatherStatusCompare(rule,((ArrayList<Map<String,Map<String,Object>>>)workData.get("block").get("childs")));
                        operationLocalList.add(compareWeatherStatus);
                    break;
                    case "if_userpresence":
                        LOG.debug("Creating createLogicOperation: if_userpresence: {}", workData);
                        AutomationComparison comparePresence = ComparesBuilder.createPresenceCompare(rule,((ArrayList<Map<String,Map<String,Object>>>)workData.get("block").get("childs")));
                        operationLocalList.add(comparePresence);
                    break;
                    case "if_daypart":
                        LOG.debug("Creating createLogicOperation: if_daypart: {}", workData);
                        AutomationComparison compareDaypart = ComparesBuilder.createDaypartCompare(rule,((ArrayList<Map<String,Map<String,Object>>>)workData.get("block").get("childs")));
                        operationLocalList.add(compareDaypart);
                    break;
                    case "sunset":
                    case "sunrise":
                        LOG.debug("Creating createLogicOperation: sunset/sunrise: {}", workData);
                        AutomationComparison compareSunriseSet = ComparesBuilder.createSunriseSetCompare(rule,(String)workData.get("block").get("attributes").get("type"), ((ArrayList<Map<String,Map<String,Object>>>)values.get("block").get("childs")));
                        operationLocalList.add(compareSunriseSet);
                    break;
                    case "timeset":
                        LOG.debug("Creating createLogicOperation: timeset: {}", workData);
                        AutomationComparison compareTimeSet = ComparesBuilder.createTimeSetCompare(rule,((ArrayList<Map<String,Map<String,Object>>>)workData.get("block").get("childs")));
                        operationLocalList.add(compareTimeSet);
                    break;
                    case "day_of_week":
                        LOG.debug("Creating createLogicOperation: if day_of_week: {}", workData);
                        AutomationComparison compareDayOfWeekSet = ComparesBuilder.createDayOfWeekSetCompare(rule,((ArrayList<Map<String,Map<String,Object>>>)workData.get("block").get("childs")));
                        operationLocalList.add(compareDayOfWeekSet);
                    break;
                    case "logic_compare":
                        LOG.debug("Creating createLogicOperation: traverse to logic compare: {}", workData);
                        ArrayList<Map<String,Map<String,Object>>> operationList = (ArrayList<Map<String,Map<String,Object>>>)workData.get("block").get("childs");
                        AutomationComparison compare = ComparesBuilder.createLogicCompare(rule,operationList,variables);
                        operationLocalList.add(compare);
                    break;
                    case "logic_operation":
                        LOG.debug("Creating createLogicOperation: traverse to logic compare: {}", workData);
                        AutomationCompareList compareLogic = ComparesBuilder.createLogicOperation(rule,workData,variables);
                        operationLocalList.add(compareLogic);
                    break;
                    default:
                        String[] value = ((String)workData.get("block").get("attributes").get("type")).split(":");
                        try {
                            switch(value[0]){
                                case "device_eq":
                                    LOG.debug("Creating createLogicOperation: device_eq: {}", workData);
                                    DeviceAutomationVariable var1 = new DeviceAutomationVariable(Integer.parseInt(value[1]), value[2], value[3]);
                                    LOG.trace("createLogicCompare device equals datatype = {}", var1.getDeviceDataType());
                                    switch(var1.getDeviceDataType()){
                                        case STRING:
                                        case HEX:
                                            var1.setDataType(AutomationVariableImpl.SubjectDataType.STRING);
                                        break;
                                        case INTEGER:
                                            var1.setDataType(AutomationVariableImpl.SubjectDataType.INTEGER);
                                        break;
                                        case FLOAT:
                                            var1.setDataType(AutomationVariableImpl.SubjectDataType.FLOAT);
                                        break;
                                        case BOOLEAN:
                                            var1.setDataType(AutomationVariableImpl.SubjectDataType.BOOLEAN);
                                        break;
                                        default:
                                            var1.setDataType(AutomationVariableImpl.SubjectDataType.STRING);
                                        break;
                                    }
                                    try {
                                        String var_device = ((List<Map<String,Map<String,String>>>)workData.get("block").get("childs")).get(0).get("field").get("value");
                                        if(var_device!=null){
                                            ObjectPropertyBindingBean set = new ObjectPropertyBindingBean();
                                            set.setValue(var_device);
                                            AutomationVariable var2 = new AutomationVariable(set, "DeviceEqual");
                                            compare = new AutomationComparison(rule,AutomationComparison.CheckType.EQUAL, var1, var2); 
                                            operationLocalList.add(compare);
                                        }
                                    } catch (Exception ex){
                                        LOG.error("Could not create device rule rule: {}", ex.getMessage());
                                    }
                                break;
                                case "if_media":
                                    LOG.debug("Creating createLogicOperation: if_media: {}", workData);
                                    try {
                                        MediaAutomationVariable varMedia1 = new MediaAutomationVariable(Integer.parseInt(value[1]));
                                        String var_media = ((List<Map<String,Map<String,String>>>)workData.get("block").get("childs")).get(1).get("field").get("value");
                                        ObjectPropertyBindingBean set = new ObjectPropertyBindingBean();
                                        set.setValue(var_media);
                                        AutomationVariable varMedia2 = new AutomationVariable(set, "MediaEqual");
                                        compare = new AutomationComparison(rule,AutomationComparison.CheckType.EQUAL, varMedia1, varMedia2); 
                                        operationLocalList.add(compare);
                                    } catch (Exception ex){
                                        LOG.error("Could not create media rule: {}", ex.getMessage());
                                    }
                                break;
                            }
                        } catch (Exception ex){

                        }
                    break;
                }
            }
        } catch (Exception ex){
            LOG.error("(createLogicOperation) This error is for debugging purposes and shows a non correct working rule '{}': {}", rule.getName(), ex.getMessage(), ex);
        }
        LOG.trace("Local operation list created: {} ", operationLocalList);
        return operationLocalList;
    }
    
    /**
     * Compare user status.
     * @param rule
     * @param logicData
     * @return 
     */
    protected static AutomationComparison createUserStatusCompare(AutomationRule rule,ArrayList<Map<String,Map<String,Object>>> logicData){
        LOG.debug("user status compare: {}", logicData);
        
        String fieldType = (String)logicData.get(0).get("field").get("value");
        int status = Integer.parseInt((String)logicData.get(1).get("field").get("value"));
        
        ObjectPropertyBindingBean set = new ObjectPropertyBindingBean();
        set.setValue(status);
        AutomationVariable var2 = new AutomationVariable(set, "UserStatusVar");
        
        AutomationVariable var1 = new AutomationVariable(UserStatusService.getCurrentUserStatusIdProperty(),"CurrentUserStatusVar");
        var1.setDataType(AutomationVariableImpl.SubjectDataType.INTEGER);
        
        AutomationComparison.CheckType type;
        
        switch(fieldType){
            case "equals":
                type = AutomationComparison.CheckType.EQUAL;
            break;
            default:
                type = AutomationComparison.CheckType.UNEQUAL;
            break;
        }
        return new AutomationComparison(rule,type, var1, var2);
    }    
    
    /**
     * An weather status var.
     * @param rule
     * @param logicData
     * @return 
     */
    protected static AutomationComparison createWeatherStatusCompare(AutomationRule rule,ArrayList<Map<String,Map<String,Object>>> logicData){
        LOG.debug("weather status compare: {}", logicData);
        
        String fieldType = (String)logicData.get(0).get("field").get("value");
        int status = Integer.parseInt((String)logicData.get(1).get("field").get("value"));
        
        ObjectPropertyBindingBean set = new ObjectPropertyBindingBean();
        set.setValue(status);
        AutomationVariable var2 = new AutomationVariable(set, "WeatherStatusVar");
        
        
        ObjectPropertyBindingBean currentValue = new ObjectPropertyBindingBean();
        Map<Integer,Map<String,Object>> pluginsList = WeatherPluginService.getInstance().getPlugins();
        try {
            for(int key: pluginsList.keySet()){
                currentValue.setValue(WeatherPluginService.getInstance().getPlugin(key).getCurrentWeatherData().getStateIcon().getBaseValue());
                break; /// There is always nly one plugin active.
            }
        } catch (PluginException ex){
            /// No plugin active now.
        }
        AutomationVariable var1 = new WeatherStatusVariable(currentValue);
        var1.setDataType(AutomationVariableImpl.SubjectDataType.STRING);
        
        AutomationComparison.CheckType type;
        
        switch(fieldType){
            case "equals":
                type = AutomationComparison.CheckType.EQUAL;
            break;
            default:
                type = AutomationComparison.CheckType.UNEQUAL;
            break;
        }
        return new AutomationComparison(rule,type, var1, var2);
    }
    
    /**
     * Compare user status.
     * @param rule
     * @param logicData
     * @return 
     */
    protected static AutomationComparison createCustomEventCompare(AutomationRule rule,ArrayList<Map<String,Map<String,Object>>> logicData){
        LOG.debug("createCustomEventCompare: {}", logicData);
        
        String fieldType = (String)logicData.get(0).get("field").get("value");
        int eventId = Integer.parseInt((String)logicData.get(1).get("field").get("value"));
        
        ObjectPropertyBindingBean set = new ObjectPropertyBindingBean();
        set.setValue(true);
        AutomationVariable var2 = new AutomationVariable(set, "CustomEventId");
        
        ObjectPropertyBindingBean currentValue = new ObjectPropertyBindingBean();
        currentValue.setValue(false);
        CustomEventVariable var1 = new CustomEventVariable(currentValue,eventId);
        
        var1.setDataType(AutomationVariableImpl.SubjectDataType.BOOLEAN);
        
        AutomationComparison.CheckType type;
        
        switch(fieldType){
            case "equals":
                type = AutomationComparison.CheckType.EQUAL;
            break;
            default:
                type = AutomationComparison.CheckType.UNEQUAL;
            break;
        }
        return new AutomationComparison(rule,type, var1, var2);
    }    
    
    /**
     * Compare user presence
     * @param rule
     * @param logicData
     * @return 
     */
    protected static AutomationComparison createPresenceCompare(AutomationRule rule,ArrayList<Map<String,Map<String,Object>>> logicData){
        LOG.debug("user presence compare: {}", logicData);
        
        String fieldType = (String)logicData.get(0).get("field").get("value");
        
        int status = Integer.parseInt((String)logicData.get(1).get("field").get("value"));
        ObjectPropertyBindingBean set = new ObjectPropertyBindingBean();
        set.setValue(status);
        AutomationVariable var2 = new AutomationVariable(set, "PresenceVar");
        
        AutomationVariable var1 = new AutomationVariable(PresenceService.getCurrentPresenceIdProperty(), "CurrentPresenceVar");
        var1.setDataType(AutomationVariableImpl.SubjectDataType.INTEGER);
        
        AutomationComparison.CheckType type;
        
        switch(fieldType){
            case "equals":
                type = AutomationComparison.CheckType.EQUAL;
            break;
            default:
                type = AutomationComparison.CheckType.UNEQUAL;
            break;
        }
        return new AutomationComparison(rule,type, var1, var2);
    }   
    
    
    /**
     * Compare day parts.
     * @param logicData
     * @return 
     */
    protected static AutomationComparison createDaypartCompare(AutomationRule rule,ArrayList<Map<String,Map<String,Object>>> logicData){
        LOG.debug("day part compare: {}", logicData);
        
        String fieldType = (String)logicData.get(0).get("field").get("value");
        
        int status = Integer.parseInt((String)logicData.get(1).get("field").get("value"));
        ObjectPropertyBindingBean set = new ObjectPropertyBindingBean();
        set.setValue(status);
        AutomationVariable var2 = new AutomationVariable(set,"DaypartVar");
        
        AutomationVariable var1 = new AutomationVariable(DayPartsService.getCurrentDaypartIdProperty(), "CurrentDaypartVar");
        var1.setDataType(AutomationVariableImpl.SubjectDataType.INTEGER);
        
        AutomationComparison.CheckType type;
        
        switch(fieldType){
            case "equals":
                type = AutomationComparison.CheckType.EQUAL;
            break;
            default:
                type = AutomationComparison.CheckType.UNEQUAL;
            break;
        }
        return new AutomationComparison(rule,type, var1, var2);
    }
    
    /**
     * Compare Time against calculated sunrise/set.
     * @param riseorset
     * @param logicData
     * @return 
     */
    protected static AutomationComparison createSunriseSetCompare(AutomationRule rule,String riseorset,ArrayList<Map<String,Map<String,Object>>> logicData){

        LOG.debug("sun(rise/set) compare: {}", logicData);
        
        //// Time composer which is set to calculate against.
        String time = new StringBuilder((String)logicData.get(1).get("field").get("value"))
                                        .append(":")
                                        .append((String)logicData.get(2).get("field").get("value"))
                                        .toString();
        
        // Add or substract
        String calctype = (String)logicData.get(0).get("field").get("value");
        
        LOG.debug("Calulating {} with {} {}", riseorset,calctype,time);
        
        ObjectPropertyBindingBean set = new ObjectPropertyBindingBean();
        ObjectPropertyBindingBean riseorsetproperty;
        switch(riseorset){
            case "sunrise":
                riseorsetproperty = TimeUtils.getCurrentSunriseProperty();
            break;
            default:
                riseorsetproperty = TimeUtils.getCurrentSunsetProperty();
            break;
        }
        
        set.setValue(TimeUtils.calcTimeDiff((String)riseorsetproperty.getValue(), (calctype.equals("MIN"))?"-"+time:"+"+time));
        LOG.debug("Final {} check set to {}", riseorset,set.getValue());        
        
        SunriseSunsetCalulatorVariable var2 = new SunriseSunsetCalulatorVariable(set,riseorsetproperty,calctype,time);
        
        AutomationVariable var1 = new AutomationVariable(TimeUtils.getCurrentTimeProperty(), "CurrentTime");
        var1.setDataType(AutomationVariableImpl.SubjectDataType.STRING);
        
        return new AutomationComparison(rule,AutomationComparison.CheckType.EQUAL, var1, var2);
    }
    
    /**
     * Checks if set time is the current time.
     * @param logicData
     * @return 
     */
    protected static AutomationComparison createTimeSetCompare(AutomationRule rule,ArrayList<Map<String,Map<String,Object>>> logicData){
        LOG.debug("Time compare: {}", logicData);
        //// Time composer which is set to calculate against.
        int time = Integer.valueOf(new StringBuilder((String)logicData.get(1).get("field").get("value"))
                                        .append((String)logicData.get(2).get("field").get("value"))
                                        .toString());
        
        // Add or substract
        String calctype = (String)logicData.get(0).get("field").get("value");
        ObjectPropertyBindingBean set = new ObjectPropertyBindingBean();
        set.setValue(time);
        AutomationVariable var2 = new AutomationVariable(set, "TimeVar");
        AutomationComparison.CheckType checkType = AutomationComparison.CheckType.EQUAL;
        switch(calctype){
            case "LATER":
                checkType = AutomationComparison.CheckType.MORE;
            break;
            case "EARLIER":
                checkType = AutomationComparison.CheckType.LESS;
            break;
        }
        AutomationVariable var1 = new AutomationVariable(TimeUtils.getCurrentTimeAsIntProperty(), "CurrentTimeVar");
        var1.setDataType(AutomationVariableImpl.SubjectDataType.INTEGER);
        return new AutomationComparison(rule,checkType, var1, var2);
    }
    
    /**
     * Creates a day of week compare.
     * @param logicData
     * @return 
     */
    protected static AutomationComparison createDayOfWeekSetCompare(AutomationRule rule,ArrayList<Map<String,Map<String,Object>>> logicData){
        LOG.debug("Time compare: {}", logicData);
        //// Time composer which is set to calculate against.
        String dayname = (String)logicData.get(0).get("field").get("value");;
        
        ObjectPropertyBindingBean set = new ObjectPropertyBindingBean();
        set.setValue(dayname);
        
        AutomationVariable var1 = new AutomationVariable(set,"DayofWeekVar");
        AutomationVariable var2;
        
        switch(dayname){
            case "ALL":
                ObjectPropertyBindingBean setAll = new ObjectPropertyBindingBean();
                setAll.setValue(dayname);
                var2 = new AutomationVariable(setAll, "CurrentDayVar");
            break;
            case "WEEKEND":
            case "WEEKDAY":
                var2 = new AutomationVariable(TimeUtils.getWeekDayTypeProperty(), "CurrentDayWeekendWeekdayVar");
            break;
            default:
                var2 = new AutomationVariable(TimeUtils.getCurrentShortDayNameProperty(), "CurrentDayVar");
            break;
        }
        var1.setDataType(AutomationVariableImpl.SubjectDataType.STRING);
        return new AutomationComparison(rule,AutomationComparison.CheckType.EQUAL, var1, var2);
    }
    
}
