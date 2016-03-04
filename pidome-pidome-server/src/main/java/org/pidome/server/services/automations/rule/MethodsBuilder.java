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
import org.pidome.server.connector.tools.properties.ObjectPropertyBindingBean;
import org.pidome.server.services.automations.impl.AutomationStatementImpl;
import org.pidome.server.services.automations.impl.AutomationVariableImpl;
import org.pidome.server.services.automations.rule.logic.AutomationIf;
import org.pidome.server.services.automations.rule.logic.AutomationIfExecSet;
import org.pidome.server.services.automations.statements.DeviceExecStatement;
import org.pidome.server.services.automations.statements.HTTPHitStatement;
import org.pidome.server.services.automations.statements.RemoteExecStatement;
import org.pidome.server.services.automations.statements.RunMacroStatement;
import org.pidome.server.services.automations.statements.SendNotificationStatement;
import org.pidome.server.services.automations.statements.SendPushbulletStatement;
import org.pidome.server.services.automations.statements.SendSMSStatement;
import org.pidome.server.services.automations.statements.SetPresenceStatement;
import org.pidome.server.services.automations.statements.SetUserStatusStatement;
import org.pidome.server.services.automations.statements.VariableUpdateStatement;
import org.pidome.server.services.automations.statements.WOLExecStatement;
import org.pidome.server.services.automations.variables.DeviceAutomationVariable;
import org.pidome.server.services.automations.variables.TextAutomationVariable;
import org.pidome.server.system.dayparts.DayPartsService;
import org.pidome.server.system.presence.PresenceService;
import org.pidome.server.system.userstatus.UserStatusService;

/**
 *
 * @author John
 */
public class MethodsBuilder {
    
    static Logger LOG = LogManager.getLogger(MethodsBuilder.class);
    
    protected static void createStaticStatements(AutomationIf automationIf, AutomationIfExecSet parentIf, Map<String,Map<String,Object>> statement, Map<String,AutomationVariableImpl> variables){
        switch((String)statement.get("attributes").get("type")){
            case "send_url":
                List<TextAutomationVariable> contents = new ArrayList();
                String sendUrl = "";
                String sendMethod = "";
                List<Map<String,Map<String,Object>>> contentList = (List<Map<String,Map<String,Object>>>)statement.get("childs");
                for(int j=0;j<contentList.size();j++){
                    for(Map.Entry<String,Map<String,Object>> data:contentList.get(j).entrySet()){
                        switch(data.getKey()){
                            case "field":
                                switch((String)((Map<String,String>)data.getValue().get("attributes")).get("name")){
                                    case "method_type":
                                        sendMethod = data.getValue().get("value").toString();
                                    break;
                                    case "url":
                                        sendUrl = data.getValue().get("value").toString();
                                    break;
                                }
                            break;
                            case "value":
                                switch((String)((Map<String,String>)data.getValue().get("attributes")).get("name")){
                                    case "Parameters":
                                        contents.addAll(MethodsBuilder.createStringFromBlocks((List)data.getValue().get("childs"),variables));
                                    break;
                                }
                            break;
                        }
                    }
                }
                if(!sendUrl.isEmpty() && !sendMethod.isEmpty() && !contents.isEmpty()){
                    parentIf.addToRunList(MethodsBuilder.createHTTPHitMethod(sendUrl,sendMethod,contents));
                    LOG.debug("Added send url with url: {}, method: {}, params: {} in statement list", sendUrl, sendMethod,contents);
                }
            break;
            case "set_userpresence":
                int presenceId = Integer.valueOf(((ArrayList<Map<String,Map<String,String>>>)statement.get("childs")).get(0).get("field").get("value"));
                parentIf.addToRunList(new SetPresenceStatement(presenceId));
                LOG.debug("Added set presence to {} in statement list", presenceId);
            break;
            case "set_userstatus":
                int statusId = Integer.valueOf(((ArrayList<Map<String,Map<String,String>>>)statement.get("childs")).get(0).get("field").get("value"));
                parentIf.addToRunList(new SetUserStatusStatement(statusId));
                LOG.debug("Added set user status to {} in statement list", statusId);
            break;
            case "variables_set":
                LOG.debug("Found for variable set: {}", statement.get("childs"));
                String varName = ((ArrayList<Map<String,Map<String,String>>>)statement.get("childs")).get(0).get("field").get("value");
                if(variables.containsKey(varName)){
                    String value = (((ArrayList<Map<String,Map<String,ArrayList<Map<String,Map<String,ArrayList<Map<String,Map<String,String>>>>>>>>>)statement.get("childs")).get(1).get("value").get("childs").get(0).get("block").get("childs").get(0).get("field").get("value"));
                    LOG.debug("Adding variable set: {} - {}", varName, value);
                    parentIf.addToRunList(new VariableUpdateStatement(variables.get(varName), value));
                    LOG.debug("Added variable {} update to {} in statement list", varName, value);
                }
            break;
            case "send_notification":
                List<TextAutomationVariable> message = new ArrayList();
                String messageType = "";
                String messageSubject = "";
                List<Map<String,Map<String,Object>>> content = (List<Map<String,Map<String,Object>>>)statement.get("childs");
                for(int j=0;j<content.size();j++){
                    for(Map.Entry<String,Map<String,Object>> data:content.get(j).entrySet()){
                        switch(data.getKey()){
                            case "field":
                                switch((String)((Map<String,String>)data.getValue().get("attributes")).get("name")){
                                    case "message_type":
                                        messageType = data.getValue().get("value").toString();
                                    break;
                                    case "message_subject":
                                        messageSubject = data.getValue().get("value").toString();
                                    break;
                                }
                            break;
                            case "value":
                                switch((String)((Map<String,String>)data.getValue().get("attributes")).get("name")){
                                    case "Message":
                                        message = MethodsBuilder.createStringFromBlocks((List)data.getValue().get("childs"),variables);
                                    break;
                                }
                            break;
                        }
                    }
                }
                if(!message.isEmpty() && !messageType.isEmpty() && !messageSubject.isEmpty()){
                    parentIf.addToRunList(MethodsBuilder.createSendNotification(messageType,messageSubject,message));
                    LOG.debug("Added send notification with type: {}, subject: {}, message: {} in statement list", messageType, messageSubject,message);
                }
            break;
            case "send_wol":
                List<Map<String,Map<String,Object>>> wolContent = (List<Map<String,Map<String,Object>>>)statement.get("childs");
                
                String macAddr = "";
                String macPort = "";

                for(int j=0;j<wolContent.size();j++){
                    for(Map.Entry<String,Map<String,Object>> data:wolContent.get(j).entrySet()){
                        switch(data.getKey()){
                            case "field":
                                switch((String)((Map<String,String>)data.getValue().get("attributes")).get("name")){
                                    case "macaddress":
                                        macAddr = data.getValue().get("value").toString();
                                    break;
                                    case "macport":
                                        macPort = data.getValue().get("value").toString();
                                    break;
                                }
                            break;
                        }
                    }
                }
                
                if(!macAddr.isEmpty() && !macPort.isEmpty()){
                    parentIf.addToRunList(MethodsBuilder.createSendWolMessage(macAddr, macPort));
                    LOG.debug("Added send WOL message to {} on port {} in statement list",macAddr, macPort);
                }
            break;
            case "send_sms":
                List<TextAutomationVariable> smsMessage = new ArrayList();
                List<Map<String,Map<String,Object>>> smsContent = (List<Map<String,Map<String,Object>>>)statement.get("childs");
                for(int j=0;j<smsContent.size();j++){
                    for(Map.Entry<String,Map<String,Object>> data:smsContent.get(j).entrySet()){
                        switch(data.getKey()){
                            case "value":
                                switch((String)((Map<String,String>)data.getValue().get("attributes")).get("name")){
                                    case "Message":
                                        smsMessage = MethodsBuilder.createStringFromBlocks((List)data.getValue().get("childs"),variables);
                                    break;
                                }
                            break;
                        }
                    }
                }
                if(!smsMessage.isEmpty()){
                    parentIf.addToRunList(MethodsBuilder.createSMSNotification(smsMessage));
                    LOG.debug("Added send SMS notification with message: {} in statement list",smsMessage);
                }
            break;
            case "send_pushbullet":
                List<TextAutomationVariable> pushBMessage = new ArrayList();
                List<Map<String,Map<String,Object>>> pushBContent = (List<Map<String,Map<String,Object>>>)statement.get("childs");
                for(int j=0;j<pushBContent.size();j++){
                    for(Map.Entry<String,Map<String,Object>> data:pushBContent.get(j).entrySet()){
                        switch(data.getKey()){
                            case "value":
                                switch((String)((Map<String,String>)data.getValue().get("attributes")).get("name")){
                                    case "Message":
                                        pushBMessage = MethodsBuilder.createStringFromBlocks((List)data.getValue().get("childs"),variables);
                                    break;
                                }
                            break;
                        }
                    }
                }
                if(!pushBContent.isEmpty()){
                    parentIf.addToRunList(MethodsBuilder.createPushbulletNotification(pushBMessage));
                    LOG.debug("Added send Pushbullet notification with message: {} in statement list",pushBMessage);
                }
            break;
            default:
                String[] action = ((String)statement.get("attributes").get("type")).split(":");
                try {
                    switch(action[0]){
                        ///runMacro:3
                        case "runMacro":
                            parentIf.addToRunList(new RunMacroStatement(Integer.parseInt(action[1])));
                            LOG.debug("Added send run maco: {} to statement list", action[1]);
                        break;
                        ///device_exec:24:deviceactions:deviceswitch
                        case "device_exec":
                            Object command = ((Map<String,Map<String,Object>>)((ArrayList)statement.get("childs"))
                                    .get(0))
                                    .get("field")
                                    .get("value");
                            parentIf.addToRunList(new DeviceExecStatement(Integer.parseInt(action[1]),action[2],action[3],command));
                            LOG.debug("Added device id {} action with group {}, control: {} and action: {} to statement list", action[1],action[2],action[3],command);
                        break;
                        case "runRemote":
                            Object remoteCommand = ((Map<String,Map<String,Object>>)((ArrayList)statement.get("childs"))
                                    .get(0))
                                    .get("field")
                                    .get("value");
                            parentIf.addToRunList(new RemoteExecStatement(Integer.parseInt(action[1]),(String)remoteCommand));
                            LOG.debug("Added send run remote with button: {} to statement list", action[1], remoteCommand);
                        break;
                    }
                } catch (NullPointerException ex){
                    LOG.error("Issue in creating an exec statement: {}", ex.getMessage(), ex);
                } catch (Exception ex){
                    LOG.error("Error creating split statement: {}", ex.getMessage(), ex);
                }
            break;
        }
    }
    
    /**
     * Creates a send WOL package execution statement.
     * @param mac The mac address destination
     * @param port The port of the destination.
     * @return The new WOL exec statement.
     */
    protected static AutomationStatementImpl createSendWolMessage(String mac, String port){
        return new WOLExecStatement(mac, port);
    }
    
    /**
     * Creates the send notification type.
     * @param type
     * @param subject
     * @param message
     * @return 
     */
    protected static AutomationStatementImpl createSendNotification(String type, String subject, List<TextAutomationVariable> message){
        SendNotificationStatement send = new SendNotificationStatement();
        send.setType(type);
        send.setTitle(subject);
        send.setMessage(message);
        return send;
    }
    
    /**
     * Http url Hit statement.
     * @param url
     * @param method
     * @param contents
     * @return 
     */
    protected static AutomationStatementImpl createHTTPHitMethod(String url, String method, List<TextAutomationVariable> contents){
        HTTPHitStatement send = new HTTPHitStatement();
        send.setUrl(url);
        send.setMethod(method);
        send.setContent(contents);
        return send;
    }
    
    /**
     * Creates the send notification type.
     * @param message
     * @return 
     */
    protected static AutomationStatementImpl createSMSNotification(List<TextAutomationVariable> message){
        SendSMSStatement send = new SendSMSStatement();
        send.setMessage(message);
        return send;
    }
    
    /**
     * Sends a pushbullet notification.
     * @param message
     * @return 
     */
    protected static AutomationStatementImpl createPushbulletNotification(List<TextAutomationVariable> message){
        SendPushbulletStatement send = new SendPushbulletStatement();
        send.setMessage(message);
        return send;
    }
    
    /**
     * Creates strings from blocks
     * @param blocks
     * @param variables
     * @return
     */
    protected static List<TextAutomationVariable> createStringFromBlocks(List blocks,Map<String,AutomationVariableImpl> variables){
        List<TextAutomationVariable> fullString = new ArrayList<>();
        for(int i=0;i<blocks.size();i++){
            Map<String,Map<String,Object>> curBlock = (Map<String,Map<String,Object>>)blocks.get(i);
            try {
                switch(((Map<String,String>)curBlock.get("block").get("attributes")).get("type")){
                    case "text":
                        ObjectPropertyBindingBean var = new ObjectPropertyBindingBean();
                        var.setValue(((Map<String,Map<String,String>>)((ArrayList)curBlock.get("block").get("childs")).get(0)).get("field").get("value"));
                        TextAutomationVariable addText = new TextAutomationVariable(var);
                        fullString.add(addText);
                    break;
                    case "variables_get":
                        String varName = ((Map<String,Map<String,String>>)((ArrayList)curBlock.get("block").get("childs")).get(0)).get("field").get("value");
                        if(variables.containsKey(varName)){
                            fullString.add(new TextAutomationVariable(variables.get(varName).getProperty()));
                        }
                    break;
                    case "text_join":
                        for(int j=0; j<((ArrayList)curBlock.get("block").get("childs")).size();j++){
                            Map<String,Map<String,List>> moreValues = (Map<String,Map<String,List>>)((ArrayList)curBlock.get("block").get("childs")).get(j);
                            if(moreValues.containsKey("value") && moreValues.get("value").containsKey("childs") && moreValues.get("value").get("childs").size()>0){
                                List<TextAutomationVariable> addVars = createStringFromBlocks(moreValues.get("value").get("childs"),variables);
                                if(addVars.size()>0){
                                    fullString.addAll(addVars);
                                }
                            }
                        }
                    break;
                    case "send_url_parameter":
                        for(int j=0; j<((ArrayList)curBlock.get("block").get("childs")).size();j++){
                            Map<String,Map<String,Object>> moreValues = (Map<String,Map<String,Object>>)((ArrayList)curBlock.get("block").get("childs")).get(j);
                            if(moreValues.containsKey("field") && ((Map<String,String>)moreValues.get("field").get("attributes")).get("name").equals("name")){
                                ObjectPropertyBindingBean nameContent = new ObjectPropertyBindingBean();
                                nameContent.setValue((String)moreValues.get("field").get("value"));
                                TextAutomationVariable paramName = new TextAutomationVariable(nameContent);
                                fullString.add(paramName);
                            }
                            if(moreValues.containsKey("value") && moreValues.get("value").containsKey("childs") && ((List)moreValues.get("value").get("childs")).size()>0){
                                List<TextAutomationVariable> addVars = createStringFromBlocks(((List)moreValues.get("value").get("childs")),variables);
                                if(addVars.size()>0){
                                    TextAutomationVariable values = new TextAutomationVariable(new ObjectPropertyBindingBean());
                                    values.addVarSet(addVars);
                                    fullString.add(values);
                                }
                            }
                        }
                    break;
                    default:
                        try {
                            String[] otherReq = ((Map<String,String>)curBlock.get("block").get("attributes")).get("type").split(":");
                            switch(otherReq[0]){
                                case "device_eq":
                                    DeviceAutomationVariable deviceAuto = new DeviceAutomationVariable(Integer.parseInt(otherReq[1]), otherReq[2], otherReq[3]);
                                    fullString.add(new TextAutomationVariable(deviceAuto.getProperty()));
                                break;
                                case "current_userstatus":
                                    fullString.add(new TextAutomationVariable(UserStatusService.getCurrentUserStatusTextProperty()));
                                break;
                                case "current_userpresence":
                                    fullString.add(new TextAutomationVariable(PresenceService.getCurrentPresenceTextProperty()));
                                break;
                                case "current_daypart":
                                    fullString.add(new TextAutomationVariable(DayPartsService.getCurrentDaypartTextProperty()));
                                break;
                            }
                        } catch (Exception ex){
                            
                        }
                    break;
                }
            } catch (NullPointerException ex){
                LOG.error("Error in create string composition: {}", ex.getMessage(), ex);
            }
        }
        return fullString;
    }
    
    
    
}
