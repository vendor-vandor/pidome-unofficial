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

package org.pidome.server.services.automations.rule;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.pidome.server.services.automations.variables.DeviceAutomationVariable;
import org.pidome.server.services.automations.variables.AutomationVariable;
import org.pidome.server.services.automations.impl.AutomationVariableImpl;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.tools.XMLTools;
import org.pidome.server.connector.tools.properties.ObjectPropertyBindingBean;
import org.pidome.server.services.automations.AutomationRulesException;
import org.pidome.server.services.automations.rule.logic.AutomationIf;
import org.pidome.server.services.automations.rule.logic.AutomationIfExecSet;
import org.pidome.server.services.automations.rule.logic.AutomationScheduledIf;
import org.pidome.server.services.automations.rule.logic.AutomationWhileUntil;
import org.pidome.server.services.automations.rule.logic.compare.AutomationCompareList;
import org.pidome.server.services.automations.rule.logic.compare.AutomationComparison;
import org.pidome.server.services.automations.variables.MediaAutomationVariable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import org.xml.sax.SAXException;

/**
 *
 * @author John
 */
public class AutomationRule extends AutomationIf implements PropertyChangeListener {
    
    static Logger LOG = LogManager.getLogger(AutomationRule.class);
    
    private int id;
    private String name;
    private String description;
    private String xmlString;
    private boolean active;
    
    boolean ruleError = false;
    
    Map<String,AutomationVariableImpl>    localVariables = new HashMap<>();
    Map<String,ObjectPropertyBindingBean> ruleVariables  = new HashMap<>();
    List<AutomationIf> rules                     = new ArrayList<>();
    
    List<PropertyChangeEvent> currentChangeEvent = new ArrayList<>();
    
    /**
     * The list of ALL the known comparisons in this rule is being traced.
     * There is a listener connected to these comparisons because when a property
     * changes the main rule is notified so it can run from bottom up so it can
     * also check if the first rule still applies.
     * @param id
     * @param name
     * @param description
     * @param active
     * @param ruleSet
     * @throws org.pidome.server.services.automations.AutomationRulesException
     */
    public AutomationRule(int id, String name, String description, boolean active, String ruleSet) throws AutomationRulesException {
        try {
            this.id = id;
            this.name = name;
            this.description = description;
            this.active = active;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            this.xmlString = ruleSet;
            createRuleSet(db.parse(new InputSource(new ByteArrayInputStream(this.xmlString.getBytes("utf-8")))));
            LOG.debug("Total root rules size: {}", rules.size());
            LOG.debug("Running pre-flight, maybe something is already true, you never know");
            for(AutomationIf ifRule:rules){
                ifRule.run();
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new AutomationRulesException("Can not start building the rule: " + ex.getMessage());
        }
    }
    
    public final int getId(){
        return this.id;
    }
    
    @Override
    public final String getName(){
        return this.name;
    }
    
    @Override
    public final boolean run(){
        return active;
    }
    
    @Override
    public final boolean lastResult(){
        return active;
    }
    
    @Override
    public final void destroy(){
        super.destroy();
        active = false;
        for(AutomationIf ifRule:rules){
            ifRule.destroy();
        }
        rules.clear();
        localVariables.clear();
        for(ObjectPropertyBindingBean bean:ruleVariables.values()){
            bean.removePropertyChangeListener(this);
        }
        ruleVariables.clear();
    }
    
    /**
     * The notification from one of the rules triggering a full rule check.
     * @param pce The current property change event.
     */
    public final void notifyUpdate(final PropertyChangeEvent pce){
        if(!currentChangeEvent.contains(pce)){
            Runnable run = () -> {
                currentChangeEvent.add(pce);
                for(AutomationIf rule:rules){
                    rule.run();
                }
                currentChangeEvent.remove(pce);
            };
            run.run();
        }
    }
    
    /**
     * Converts the xml document to variables and rules.
     * @param xml
     * @throws AutomationRulesException 
     */
    private void createRuleSet(Document xml) throws AutomationRulesException {
        try {
            Map<String,Map<String,Object>> baseSet = (Map<String,Map<String,Object>>)((ArrayList)XMLToObjects(xml.getChildNodes().item(0)).get("xml").get("childs")).get(0);
            for(Map<String,Map<String,Map<String,Object>>> nodeSet : ((ArrayList<Map<String,Map<String,Map<String,Object>>>>)baseSet.get("block").get("childs"))){
                for(Map.Entry<String,Map<String,Map<String,Object>>> node:nodeSet.entrySet()){
                    switch((String)node.getValue().get("attributes").get("name")){
                        case "dev_var_assign":
                            localVariables.putAll(VariablesBuilder.createVariables((ArrayList)node.getValue().get("childs")));
                        break;
                        case "rulelist":
                            createRulesAndExec(this,(ArrayList)node.getValue().get("childs"));
                        break;
                    }
                }
            }
            /**
             * Fill the local var list to listen to.
             */
            for(AutomationIf ifItem: rules){
                List<AutomationVariableImpl> varList = ifItem.getVariablesList();
                for(AutomationVariableImpl varImpl:varList){
                    if(!ruleVariables.containsKey(varImpl.getName())){
                        ruleVariables.put(varImpl.getName(), varImpl.getProperty());
                    }
                }
            }
            /// Attach this main parent rule as the main listener for changes and act on them
            for(ObjectPropertyBindingBean bean:ruleVariables.values()){
                bean.addPropertyChangeListener(this);
            }
        } catch (NullPointerException ex){
            LOG.error("Incorrent xml setup: {}", ex.getMessage(), ex);
            throw new AutomationRulesException("Incorrent xml setup: " + ex.getMessage());
        }
    }
    

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        notifyUpdate(pce);
    }
    
    /**
     * Entry for creating the rules and runnables.
     * @param statementNodes 
     */
    private AutomationIf createRulesAndExec(AutomationIf parent, ArrayList statementNodes) throws AutomationRulesException {
        for(Map<String,Map<String,Map<String,Object>>> blockNode:(ArrayList<Map<String,Map<String,Map<String,Object>>>>)statementNodes){
            LOG.debug("Going to create an if statement based on: {}", (String)blockNode.get("block").get("attributes").get("type"));
            switch((String)blockNode.get("block").get("attributes").get("type")){
                case "controls_if":
                    AutomationIf defaultIf = new AutomationIf();
                    defaultIf.addParent(parent);
                    createIfStatement(defaultIf,parent,(ArrayList<Map<String,Object>>)blockNode.get("block").get("childs"));
                    if(parent instanceof AutomationRule){
                        rules.add(defaultIf);
                    }
                    return defaultIf;

                case "control_time_if":
                    int length = Integer.valueOf(((ArrayList<Map<String,Map<String,String>>>)blockNode.get("block").get("childs")).get(0).get("field").get("value"));
                    String type = ((ArrayList<Map<String,Map<String,String>>>)blockNode.get("block").get("childs")).get(1).get("field").get("value");
                    
                    AutomationIf timedIf = new AutomationScheduledIf(length, type);
                    timedIf.addParent(parent);
                    createIfStatement(timedIf,parent,(ArrayList<Map<String,Object>>)blockNode.get("block").get("childs"));
                    if(parent instanceof AutomationRule){
                        rules.add(timedIf);
                    }
                    return timedIf;
                    
                case "controls_whileUntil":
                    String whileType = ((ArrayList<Map<String,Map<String,String>>>)blockNode.get("block").get("childs")).get(0).get("field").get("value");
                    
                    LOG.debug("Creating a control_time_if of type: {}", whileType);
                    
                    AutomationWhileUntil whileIf = new AutomationWhileUntil(whileType);
                    whileIf.addParent(parent);
                    createIfStatement(whileIf,parent,(ArrayList<Map<String,Object>>)blockNode.get("block").get("childs"));
                    
                    if(parent instanceof AutomationRule){
                        rules.add(whileIf);
                        LOG.debug("controls_whileUntil is a main parent rule");
                    } else {
                        LOG.debug("controls_whileUntil is a child rule");
                    }
                    return whileIf;
            }
        }
        throw new AutomationRulesException("No rule statement found");
    }
    
    /**
     * Creates an if statement.
     * @param ifData 
     */
    private void createIfStatement(AutomationIf automationIf, AutomationIf automationIfParent, ArrayList<Map<String,Object>> ifData){
        Map<String,AutomationIfExecSet> ifMap = new HashMap<>();
        for(Map<String,Object> blockListData:ifData){
            for(Map.Entry<String, Object> blockData:blockListData.entrySet()){
                switch(blockData.getKey()){
                    case "value":
                        //// Here now the check values are created.
                        AutomationIfExecSet execSet = new AutomationIfExecSet();
                        
                        String ifName = ((Map<String,Map<String,String>>)blockData.getValue()).get("attributes").get("name");
                        
                        if(ifName.equals("BOOL")){
                            ifName = "IF";
                        }
                        ifMap.put(ifName, execSet);
                        LOG.debug("Creating if mapping: {}", ifName);
                                
                        ArrayList<Map<String,Map<String,Map<String,Object>>>> valuesSet = (ArrayList<Map<String,Map<String,Map<String,Object>>>>)((Map<String,Object>)blockData.getValue()).get("childs");
                        for(Map<String,Map<String,Map<String,Object>>> values: valuesSet){
                            switch((String)values.get("block").get("attributes").get("type")){
                                case "if_userstatus":
                                    LOG.trace("Creating if_userstatus: {}", values);
                                    AutomationCompareList compareUserStatusList = new AutomationCompareList(AutomationCompareList.ListType.AND);
                                    AutomationComparison compareUserStatus = ComparesBuilder.createUserStatusCompare(this,((ArrayList<Map<String,Map<String,Object>>>)values.get("block").get("childs")));
                                    compareUserStatusList.add(compareUserStatus);
                                    ifMap.get(ifName).setIf(compareUserStatusList);
                                break;
                                case "if_customevent":
                                    LOG.trace("Creating if_customevent: {}", values);
                                    AutomationCompareList compareCustomEventList = new AutomationCompareList(AutomationCompareList.ListType.AND);
                                    AutomationComparison compareCustomEvent = ComparesBuilder.createCustomEventCompare(this,((ArrayList<Map<String,Map<String,Object>>>)values.get("block").get("childs")));
                                    compareCustomEventList.add(compareCustomEvent);
                                    ifMap.get(ifName).setIf(compareCustomEventList);
                                break;
                                case "if_weatherstatus":
                                    LOG.trace("Creating if_weatherstatus: {}", values);
                                    AutomationCompareList compareWeatherStatusList = new AutomationCompareList(AutomationCompareList.ListType.AND);
                                    AutomationComparison compareWeatherStatus = ComparesBuilder.createWeatherStatusCompare(this,((ArrayList<Map<String,Map<String,Object>>>)values.get("block").get("childs")));
                                    compareWeatherStatusList.add(compareWeatherStatus);
                                    ifMap.get(ifName).setIf(compareWeatherStatusList);
                                break;
                                case "if_userpresence":
                                    LOG.trace("Creating if_userpresence: {}", values);
                                    AutomationCompareList comparePresenceList = new AutomationCompareList(AutomationCompareList.ListType.AND);
                                    AutomationComparison comparePresence = ComparesBuilder.createPresenceCompare(this,((ArrayList<Map<String,Map<String,Object>>>)values.get("block").get("childs")));
                                    comparePresenceList.add(comparePresence);
                                    ifMap.get(ifName).setIf(comparePresenceList);
                                break;
                                case "if_daypart":
                                    LOG.trace("Creating if_daypart: {}", values);
                                    AutomationCompareList compareDaypartList = new AutomationCompareList(AutomationCompareList.ListType.AND);
                                    AutomationComparison compareDaypart = ComparesBuilder.createDaypartCompare(this,((ArrayList<Map<String,Map<String,Object>>>)values.get("block").get("childs")));
                                    compareDaypartList.add(compareDaypart);
                                    ifMap.get(ifName).setIf(compareDaypartList);
                                break;
                                case "sunset":
                                case "sunrise":
                                    LOG.trace("Creating if sun(set/rise): {}", values);
                                    AutomationCompareList compareSunsetRiseList = new AutomationCompareList(AutomationCompareList.ListType.AND);
                                    AutomationComparison compareSunriseSetpart = ComparesBuilder.createSunriseSetCompare(this,(String)values.get("block").get("attributes").get("type"), ((ArrayList<Map<String,Map<String,Object>>>)values.get("block").get("childs")));
                                    compareSunsetRiseList.add(compareSunriseSetpart);
                                    ifMap.get(ifName).setIf(compareSunsetRiseList);
                                break;
                                case "timeset":
                                    LOG.trace("Creating if timeset: {}", values);
                                    AutomationCompareList compareTimeSetList = new AutomationCompareList(AutomationCompareList.ListType.AND);
                                    AutomationComparison compareTimeSetpart = ComparesBuilder.createTimeSetCompare(this,((ArrayList<Map<String,Map<String,Object>>>)values.get("block").get("childs")));
                                    compareTimeSetList.add(compareTimeSetpart);
                                    ifMap.get(ifName).setIf(compareTimeSetList);
                                break;
                                case "day_of_week":
                                    LOG.trace("Creating if day of week: {}", values);
                                    AutomationCompareList compareDayOfWeekSetList = new AutomationCompareList(AutomationCompareList.ListType.AND);
                                    AutomationComparison compareDayOfWeekSetpart = ComparesBuilder.createDayOfWeekSetCompare(this,((ArrayList<Map<String,Map<String,Object>>>)values.get("block").get("childs")));
                                    compareDayOfWeekSetList.add(compareDayOfWeekSetpart);
                                    ifMap.get(ifName).setIf(compareDayOfWeekSetList);
                                break;
                                case "logic_compare":
                                    LOG.trace("Creating logic_compare: {}", values);
                                    AutomationCompareList compareLocalList = new AutomationCompareList(AutomationCompareList.ListType.AND);
                                    try {
                                        AutomationComparison compare = ComparesBuilder.createLogicCompare(this,((ArrayList<Map<String,Map<String,Object>>>)values.get("block").get("childs")),this.localVariables);
                                        compareLocalList.add(compare);
                                    } catch (Exception ex) {
                                        LOG.error("Failed to create a logic_compare rule: {}", ex);
                                    }
                                    ifMap.get(ifName).setIf(compareLocalList);
                                break;
                                case "logic_operation":
                                    LOG.trace("Creating logic_operation: {}", values);
                                    AutomationCompareList operationLocalList = ComparesBuilder.createLogicOperation(this,values,this.localVariables);
                                    ifMap.get(ifName).setIf(operationLocalList);
                                break;
                                default:
                                    String[] value = ((String)values.get("block").get("attributes").get("type")).split(":");
                                    try {
                                        switch(value[0]){
                                            case "device_eq":
                                                DeviceAutomationVariable var1 = new DeviceAutomationVariable(Integer.parseInt(value[1]), value[2], value[3]);
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
                                                    String var_device = ((List<Map<String,Map<String,String>>>)values.get("block").get("childs")).get(0).get("field").get("value");
                                                    if(var_device!=null){
                                                        ObjectPropertyBindingBean set = new ObjectPropertyBindingBean();
                                                        set.setValue(var_device);
                                                        AutomationVariable var2 = new AutomationVariable(set, "DeviceEqual");
                                                        AutomationCompareList compareDeviceList = new AutomationCompareList(AutomationCompareList.ListType.OR);
                                                        AutomationComparison compare = new AutomationComparison(this,AutomationComparison.CheckType.EQUAL, var1, var2); 
                                                        compareDeviceList.add(compare);
                                                        ifMap.get(ifName).setIf(compareDeviceList);
                                                    }
                                                } catch (Exception ex){
                                                    //// This is an logic compare and not a device internal if.
                                                }
                                            break;
                                            case "if_media":
                                                try {
                                                    MediaAutomationVariable varMedia1 = new MediaAutomationVariable(Integer.parseInt(value[1]));
                                                    String var_media = ((List<Map<String,Map<String,String>>>)values.get("block").get("childs")).get(1).get("field").get("value");
                                                    ObjectPropertyBindingBean set = new ObjectPropertyBindingBean();
                                                    set.setValue(var_media);
                                                    AutomationVariable varMedia2 = new AutomationVariable(set, "MediaEqual");
                                                    AutomationCompareList compareDeviceList = new AutomationCompareList(AutomationCompareList.ListType.OR);
                                                    AutomationComparison compare = new AutomationComparison(this,AutomationComparison.CheckType.EQUAL, varMedia1, varMedia2); 
                                                    compareDeviceList.add(compare);
                                                    ifMap.get(ifName).setIf(compareDeviceList);
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
                    break;
                    case "statement":
                        ArrayList<Map<String,Map<String,Map<String,Object>>>> statementSet = (ArrayList<Map<String,Map<String,Map<String,Object>>>>)((Map<String,Object>)blockData.getValue()).get("childs");
                        String doName = ((Map<String,Map<String,String>>)blockData.getValue()).get("attributes").get("name").replace("DO", "IF");
                        LOG.debug("Adding statements to if mapping: {}", doName);
                        if(ifMap.containsKey(doName)){
                            createStatements(automationIf, ifMap.get(doName),statementSet);
                            automationIf.addIfExecSet(ifMap.get(doName));
                            LOG.debug("Added statements to if mapping: {}", doName);
                        } else {
                            LOG.debug("Having an else exec list: {}", doName);
                            
                            //// Used when there is no if statement defined.
                            AutomationIfExecSet elseExecSet = new AutomationIfExecSet();
                            createStatements(automationIf, elseExecSet,statementSet);
                            automationIf.addElseSet(elseExecSet);
                        }
                    break;
                    case "next":
                    try {
                        createRulesAndExec(automationIfParent,((Map<String,ArrayList>)blockData.getValue()).get("childs"));
                    } catch (AutomationRulesException ex) {
                        LOG.error("Error in recursing from root if statement: {}", ex.getMessage());
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * Creates the statements.
     * @param parentIf
     * @param statementSet 
     */
    private void createStatements(AutomationIf automationIf, AutomationIfExecSet parentIf, ArrayList<Map<String,Map<String,Map<String,Object>>>> statementSet){
        for(int i=0;i<statementSet.size();i++){
            Map<String,Map<String,Object>> statement = statementSet.get(i).get("block");
            if(statement.get("attributes").get("type")!=null){
                switch((String)statement.get("attributes").get("type")){
                    case "control_time_if":
                    case "controls_if":
                    case "controls_whileUntil":
                        LOG.debug("Recursing to new {} ",statement.get("attributes").get("type"));
                        try {
                            AutomationIf impl = createRulesAndExec(automationIf,(ArrayList)statementSet);
                            parentIf.addToRunList(impl);
                            LOG.debug("Added new if statement: {} in statement list",impl.getName());
                        } catch (AutomationRulesException ex) {
                            LOG.error("Error recursing from internal statements list: {}", ex.getMessage());
                        }
                        LOG.debug("Recursing done");
                    break;
                    default:
                        MethodsBuilder.createStaticStatements(automationIf, parentIf, statement, this.localVariables);
                    break;
                }
            }
            try {
                for(int z=0;z<((ArrayList<Map<String,Object>>)statement.get("childs")).size();z++){
                    Map<String,Map<String,Object>> check = ((ArrayList<Map<String,Map<String,Object>>>)statement.get("childs")).get(z);
                    if(check.containsKey("next")){
                        LOG.debug("Multiple statements, recursing");
                        createStatements(automationIf, parentIf, ((ArrayList)check.get("next").get("childs")));
                    }
                }
            } catch (Exception ex){
                //// no next field
            }
        }
    }
    
    /**
     * Maps the dom object to arraylists and hashmaps.
     * @param node
     * @return 
     */
    private static Map<String,Map<String,Object>> XMLToObjects(Node node){
        if(node.getNodeType()==Node.ELEMENT_NODE){
            Map<String,Map<String,Object>> set = new HashMap<>();
            Map<String,Object> collection = new HashMap<>();
            collection.put("attributes", XMLTools.getNodeAttributes(node));
            collection.put("childs", new ArrayList());
            if(node.hasChildNodes()){
                for(int i=0;i<node.getChildNodes().getLength();i++){
                    Node nextNode = node.getChildNodes().item(i);
                    if(nextNode.getNodeType()==Node.ELEMENT_NODE){
                        Map<String,Map<String,Object>> nodeSet = XMLToObjects(nextNode);
                        if(nodeSet!=null){
                            ((ArrayList)collection.get("childs")).add(nodeSet);
                        }
                    }
                }
            }
            if(((ArrayList)collection.get("childs")).isEmpty()){
                collection.put("value", node.getTextContent());
            }
            set.put(node.getNodeName(), collection);
            return set;
        } else {
            return null;
        }
    }
    
}