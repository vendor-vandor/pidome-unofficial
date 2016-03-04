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

package org.pidome.server.services.triggerservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.pidome.misc.utils.TimeUtils;
import org.pidome.server.connector.drivers.devices.UnknownDeviceException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroupException;
import org.pidome.server.connector.plugins.messengers.MessengerException;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.services.macros.MacroService;
import org.pidome.server.services.plugins.MessengerPluginService;
import org.pidome.server.services.triggerservice.rules.Rule;
import org.pidome.server.services.triggerservice.rules.RuleAnd;
import org.pidome.server.services.triggerservice.rules.RuleDayPart;
import org.pidome.server.services.triggerservice.rules.RuleIf;
import org.pidome.server.services.triggerservice.rules.RuleMethod;
import org.pidome.server.services.triggerservice.rules.RuleOr;
import org.pidome.server.services.triggerservice.rules.RulePresence;
import org.pidome.server.services.triggerservice.rules.RuleSimple;
import org.pidome.server.services.triggerservice.rules.RuleSubject;
import org.pidome.server.services.triggerservice.rules.RuleTime;
import org.pidome.server.system.audit.Notifications;
import org.pidome.server.system.dayparts.DayPartException;
import org.pidome.server.system.dayparts.DayPartsService;
import org.pidome.server.system.hardware.devices.Devices;
import org.pidome.server.system.presence.PresenceException;
import org.pidome.server.system.presence.PresenceService;
import org.pidome.server.system.userstatus.UserStatusException;
import org.pidome.server.system.userstatus.UserStatusService;

/**
 * Handle actions which can be triggered by member values.
 * A trigger is something dat can be executed when something is happening in the server. This can be a device value or a plugin acting. 
 * Even system states are events.
 * When a trigger is given rules, and these rules match, action can be taken to command (multiple) devices and/or plugins.
 * 
 * The simplest example is: When watching a movie and the phone rings let the answer machine do it's work. Or when i pause the movie when it's night or the curtains are closed turn on the lights.
 * 
 * @author John Sirach
 */
public final class TriggerEvent {
 
    public enum Occurrence {
        /**
         * When conditions are met actions will be ran.
         * This mode will continuously run the action when all the rules are true. Example:
         * true: run,
         * true: run,
         * true: run,
         * false: do not run,
         * true: run,
         * true: run,
         * etc....
         */
        CONTINUOUS,
        /**
         * Actions will ran only at the first occurrence of met conditions.
         * Meaning an action will be ran once until the event is reset (can only be done with once and thresholds). Example:
         * true: run,
         * true: do not run,
         * true: do not run,
         * false: do not run,
         * true: do not run,
         * true: do not run,
         * etc....
         */
        ONCE,
        /**
         * Actions will be ran the first time a condition is met and again when a rule has failed and becomes true again.
         * Actions will be ran this way:
         * true: run,
         * true: do not run,
         * true: do not run,
         * false: do not run,
         * true: run,
         * true: do not run,
         * etc....
         * This is default behavior.
         */
        TOGGLE,
        /**
         * Actions will be ran after a threshold of the first positive run.
         * This means that a threshold of 10 minutes will cause the actions to be ran again after these 10 minutes has past despite conditions which are met or not.
         */
        THRESHOLDTIME
    }    
    
    /**
     * Rules list for the event.
     */
    List<Rule> rules = new ArrayList();
    /**
     * Trigger database
     */
    TriggerDB db = new TriggerDB();
    /**
     * Trigger name.
     */
    String triggerName;
    /**
     * Event description.
     */
    String description;
    /**
     * Trigger id.
     */
    int triggerId;
    /**
     * primary array of the rules
     */
    ArrayList ruleset = new ArrayList();
    /**
     * primary array of the executables
     */
    ArrayList execs = new ArrayList();
    /**
     * How often the rule action occurs.
     * This defaults to toggle, which will likely be most used. 
     */
    Occurrence occurrence = Occurrence.TOGGLE;
    /**
     * How the rules should be matched together.
     */
    String rulesMatch = "and";
    /**
     * A list where rule subjects are added to for quick check if a specific rule exists.
     * This helps in not running unnesacairy rules
     */
    ArrayList<String> ruleOccurrence = new ArrayList();
    /**
     * Threshold time.
     * The time to pass before the event runs the actions again. Only applies when running with Occurrence.THRESHOLDTIME and defaults to 10 minutes.
     * This means the event handler will run the actions at thresholdTime+1ms. When set at 10 then at the 10th minute plus one millisecond the actions can be ran again.
     */
    long thresholdTime = 10*60*1000;
    
    /**
     * Indicates if the run actions have occurred.
     */
    boolean occurred = false;
    
    /**
     * Last occurrence time in milliseconds from 1970.
     */
    long lastOccurrenceTime = 0;
    
    /**
     * The amount of subjects used in total.
     */
    int subjectCount = 0;

    /**
     * Inner class holding the actions to be ran.
     */
    EventRunActions runActions;
    
    /**
     * Check if the current trigger is being edited.
     * This edit switch is only when a trigger is being saved/deleted to the database.
     */
    boolean inEdit = false;
    
    /**
     * Logger
     */
    static Logger LOG = LogManager.getLogger(TriggerEvent.class);
    
    /**
     * Creates a trigger.
     * @param triggerId
     * @param triggerName
     * @param description 
     * @param ruleset 
     * @param rulesMatch 
     * @param occurrence 
     * @param execs 
     * @throws org.pidome.server.services.triggerservice.TriggerException 
     */
    public TriggerEvent(int triggerId, String triggerName, String description, Occurrence occurrence, String rulesMatch, ArrayList ruleset, ArrayList execs) throws TriggerException {
        this.triggerId = triggerId;
        this.triggerName = triggerName;
        this.description = description;
        this.ruleset = ruleset;
        this.execs = execs;
        this.occurrence = occurrence;
        this.rulesMatch = rulesMatch;
        createRulesetFromJSON();
        runActions = new EventRunActions();
    }
    
    /**
     * Creates the rules from the ruleset.
     * @throws TriggerException 
     */
    final void createRulesetFromJSON() throws TriggerException {
        LOG.debug("Got {} parent rules in {} ", ruleset.size(), this.triggerName);
        for (Object rulesetItem : ruleset) {
            Map<String,Object> rule = (Map<String,Object>) rulesetItem;
            switch ((String) rule.get("matchtype")) {
                case "simple":
                    createSimpleRuleSet((ArrayList) rule.get("collection"));
                    break;
                case "or":
                    createOrRuleSet((ArrayList) rule.get("collection"));
                    break;
                case "and":
                    createAndRuleSet((ArrayList) rule.get("collection"));
                    break;
                default:
                    throw new TriggerException("Unsupported rule type: " + (String) rule.get("matchtype"));
            }
        }

    }
    
    /**
     * Returns the array list setup
     * @return 
     */
    public final ArrayList getTriggerRulesetSetup(){
        return this.ruleset;
    }
    
    /**
     * Get the amount of subjects in total.
     * @return 
     */
    public final int getSubjectsCount(){
        return this.subjectCount;
    }
    
    /**
     * Returns the last occurrence of the actions taken.
     * @return 
     */
    public final String getLastOccurrence(){
        if(lastOccurrenceTime==0){
            return "Never";
        } else {
            DateTime dt = new DateTime(new Date(lastOccurrenceTime));
            LocalDateTime ldt = dt.toLocalDateTime();
            return TimeUtils.compose24Hours(ldt.getHourOfDay(), ldt.getMinuteOfHour()) + " " + TimeUtils.composeDDMMYYYYDate(ldt.getDayOfMonth(), ldt.getMonthOfYear(), ldt.getYear());
        }
    }
    
    /**
     * Creates a simple ruleset, contains one rule.
     * @param subject
     * @throws TriggerException 
     */
    final void createSimpleRuleSet(ArrayList ruleSet) throws TriggerException {
        LOG.debug("Creating SIMPLE rule");
        RuleSubject[] list = createRuleSubjects(ruleSet);
        if(list.length>0) this.rules.add(new RuleSimple(list));
    }    
    
    /**
     * Creates a ruleset where one of the rules must succeed.
     * @param subject
     * @throws TriggerException 
     */
    final void createOrRuleSet(ArrayList ruleSet) throws TriggerException {
        LOG.debug("Creating OR rules");
        RuleSubject[] list = createRuleSubjects(ruleSet);
        if(list.length>0) this.rules.add(new RuleOr(list));
    }    
    
    /**
     * Creates a ruleset where ALL the rules must succeed.
     * @param subject
     * @throws TriggerException 
     */
    final void createAndRuleSet(ArrayList ruleSet) throws TriggerException {
        LOG.debug("Creating AND rules");
        RuleSubject[] list = createRuleSubjects(ruleSet);
        if(list.length>0) this.rules.add(new RuleAnd(list));
    }    
    
    final RuleSubject[] createRuleSubjects(ArrayList rules) throws TriggerException {
        List<RuleSubject> ruleSubjects = new ArrayList();
        LOG.trace("Creating values equation from the following data: {}", rules);
        for (Object ruleItem : rules) {
            Map<String, Object> rule = (Map<String, Object>) ruleItem;
            String ruleName = "";
            try {
                RuleSubject ruleSubject = null;
                switch ((String) rule.get("itemtype")) {
                    case "device":
                        ruleName += "device_";
                        String id = (String) rule.get("deviceid").toString();
                        String group = (String) rule.get("group");
                        String set = (String) rule.get("command");
                        RuleMethod.Method equationType = RuleMethod.Method.EQUALS;
                        switch ((String) rule.get("matchtype")) {
                            case "GREATERTHEN":
                                equationType = RuleMethod.Method.GREATERTHEN;
                                break;
                            case "LESSTHEN":
                                equationType = RuleMethod.Method.LESSTHEN;
                                break;
                            case "EQUALS":
                                equationType = RuleMethod.Method.EQUALS;
                                break;
                            case "DIFFER":
                                equationType = RuleMethod.Method.DIFFER;
                                break;
                            default:
                                throw new TriggerException("Unsupported equation: " + (String) rule.get("matchtype"));
                        }
                        LOG.trace("Have a device equation with id: {}, group: {}, set: {}, value: {}, method: {}", id, group, set, rule.get("matchvalue"), equationType);
                        ruleName += id + "_" + group + "_" + set;
                        if(rule.get("matchvalue") instanceof Long || rule.get("matchvalue") instanceof Float || rule.get("matchvalue") instanceof Double ){
                            ruleSubject = new RuleSubject(ruleName, new RuleIf(((Number)rule.get("matchvalue")).floatValue(), equationType)); 
                            ruleSubjects.add(ruleSubject);
                            subjectCount++;
                        } else if(rule.get("matchvalue") instanceof String){
                            ruleSubject = new RuleSubject(ruleName, new RuleIf((String)rule.get("matchvalue"), equationType));
                            ruleSubjects.add(ruleSubject);
                            subjectCount++;
                        } else if(rule.get("matchvalue") instanceof Boolean){
                            ruleSubject = new RuleSubject(ruleName, new RuleIf((boolean)rule.get("matchvalue"), equationType));
                            ruleSubjects.add(ruleSubject);
                            subjectCount++;
                        } else {
                            throw new TriggerException("Unsupported data type for device equation: " + rule.get("matchvalue"));
                        }
                        LOG.debug("Created equation for {}", ruleName);
                        try {
                            ruleSubject.run(Devices.getDevice(Integer.valueOf(id)).getFullCommandSet().getControlsGroup(group).getDeviceControl(set).getValue());
                        } catch (UnknownDeviceException | DeviceControlsGroupException | DeviceControlException ex) {
                            LOG.error("Could not check current value: {}", ex.getMessage());
                        }
                        break;
                    case "daytime":
                        ruleName = "daytime_";
                        switch ((String) rule.get("matchtype")) {
                            case "GREATERTHEN":
                                equationType = RuleMethod.Method.GREATERTHEN;
                                break;
                            case "LESSTHEN":
                                equationType = RuleMethod.Method.LESSTHEN;
                                break;
                            case "EQUALS":
                                equationType = RuleMethod.Method.EQUALS;
                                break;
                            case "DIFFER":
                                equationType = RuleMethod.Method.DIFFER;
                                break;
                            default:
                                throw new TriggerException("Unsupported equation: " + (String) rule.get("matchtype"));
                        }
                        ruleName += (String) rule.get("timetype") + "_" + (String)rule.get("occurrence");
                        switch ((String) rule.get("timetype")) {
                            case "FIXED":
                                LOG.trace("Have a time equation with value: {}, method: {}, days: {}", rule.get("matchvalue"), equationType, (String)rule.get("occurrence"));
                                ruleSubjects.add(new RuleSubject(ruleName, new RuleTime("FIXED", (String)rule.get("matchvalue"),equationType), TimeUtils.getCurrentMilitaryTime()));
                                subjectCount++;
                                break;
                            case "SUNRISE":
                            case "SUNSET":
                                String showTime;
                                if(((String)rule.get("timetype")).equals("SUNRISE")){
                                    showTime = TimeUtils.getSunrise();
                                } else {
                                    showTime = TimeUtils.getSunset();
                                }
                                String timeMatching = (String) rule.get("timemod") + (String) rule.get("matchvalue");
                                ruleSubject = new RuleSubject(ruleName, new RuleTime((String)rule.get("timetype"),timeMatching, equationType),TimeUtils.getCurrentMilitaryTime());
                                ruleSubjects.add(ruleSubject);
                                subjectCount++;
                                LOG.debug("Have a time {} equation with value: {} diff {}, method: {}, days: {}", (String)rule.get("timetype"), showTime, timeMatching, equationType, rule.get("occurrence"));
                                break;
                        }
                        break;
                    case "presence":
                        ruleName = "PRESENCE";
                        int matchValue = ((Long)rule.get("matchvalue")).intValue();
                        switch ((String) rule.get("matchtype")) {
                            case "EQUALS":
                                equationType = RuleMethod.Method.EQUALS;
                                break;
                            case "DIFFER":
                                equationType = RuleMethod.Method.DIFFER;
                                break;
                            default:
                                throw new TriggerException("Unsupported equation: " + (String) rule.get("matchtype"));
                        }
                        LOG.trace("Have a presence - {} equation with method: {}", rule.get("matchvalue"), equationType);
                        int initialValue = 0;
                        try {
                            initialValue = PresenceService.current().getId();
                        } catch (PresenceException ex){
                            LOG.error("Could not get initial presence value, equation unreliable until presence change: {}", ex.getMessage());
                        }
                        ruleSubjects.add(new RuleSubject(ruleName, new RulePresence(matchValue, equationType), initialValue));
                        subjectCount++;
                        break;
                    case "daypart":
                        ruleName = "DAYPART";
                        int matchDayPartValue = ((Long)rule.get("matchvalue")).intValue();
                        switch ((String) rule.get("matchtype")) {
                            case "EQUALS":
                                equationType = RuleMethod.Method.EQUALS;
                                break;
                            case "DIFFER":
                                equationType = RuleMethod.Method.DIFFER;
                                break;
                            default:
                                throw new TriggerException("Unsupported equation: " + (String) rule.get("matchtype"));
                        }
                        LOG.trace("Have a day part - {} equation with method: {}", rule.get("matchvalue"), equationType);
                        int initialDayPartValue = 0;
                        try {
                            initialDayPartValue = DayPartsService.current().getId();
                        } catch (DayPartException ex){
                            LOG.error("Could not get initial day part value, equation unreliable until day part change: {}", ex.getMessage());
                        }
                        ruleSubjects.add(new RuleSubject(ruleName, new RuleDayPart(matchDayPartValue, equationType), initialDayPartValue));
                        subjectCount++;
                        break;
                    case "userstatus":
                        ruleName = "USERSTATUS";
                        int matchUserStatusValue = ((Long)rule.get("matchvalue")).intValue();
                        switch ((String) rule.get("matchtype")) {
                            case "EQUALS":
                                equationType = RuleMethod.Method.EQUALS;
                                break;
                            case "DIFFER":
                                equationType = RuleMethod.Method.DIFFER;
                                break;
                            default:
                                throw new TriggerException("Unsupported equation: " + (String) rule.get("matchtype"));
                        }
                        LOG.trace("Have a user status - {} equation with method: {}", rule.get("matchvalue"), equationType);
                        int initialUserStatusValue = 0;
                        try {
                            initialUserStatusValue = UserStatusService.current().getId();
                        } catch (UserStatusException ex){
                            LOG.error("Could not get initial user status value, equation unreliable until user status change: {}", ex.getMessage());
                        }
                        ruleSubjects.add(new RuleSubject(ruleName, new RuleDayPart(matchUserStatusValue, equationType), initialUserStatusValue));
                        subjectCount++;
                        break;
                    case "mediaplugin":
                        ruleName = "MEDIACOMMAND_" + (String) rule.get("mediaId").toString() + "_" + (String)rule.get("matchvalue");
                        String commandValue = ((String)rule.get("matchvalue"));
                        switch ((String) rule.get("matchtype")) {
                            case "EQUALS":
                                equationType = RuleMethod.Method.EQUALS;
                                break;
                            default:
                                throw new TriggerException("Unsupported equation: " + (String) rule.get("matchtype"));
                        }
                        LOG.debug("Have a media command - {} equation with method: {}", commandValue, equationType);
                        ruleSubjects.add(new RuleSubject(ruleName, new RuleIf((String)rule.get("matchvalue"), equationType)));
                        subjectCount++;
                        break;
                    default:
                        throw new TriggerException("Unsupported item type: " + (String) rule.get("itemtype"));
                }
            } catch (TriggerException ex){
                LOG.error("Equation discarded because of: {}", ex.getMessage());
            }
            if(!ruleName.equals("") && !ruleOccurrence.contains(ruleName)){
                ruleOccurrence.add(ruleName);
            }
        }
        LOG.trace("Total amount of equations created: {}", ruleSubjects.size());
        if(ruleSubjects.isEmpty()){
            LOG.warn("Trigger with empty equations created.");
        }
        return ruleSubjects.toArray(new RuleSubject[ruleSubjects.size()]);
    }
    
    /**
     * Updates triggers with the latest sunrise and sunset values.
     */
    public final void updateVariableTimedTriggers(){
        for (Rule rule : rules) {
            List<RuleSubject> ruleList = rule.getRules();
            for (RuleSubject ruleListItem : ruleList) {
                RuleMethod method = ruleListItem.getMethod();
                if(method instanceof RuleTime){
                    ((RuleTime)method).updateVariableTimes();
                }
            }
        }
    }
    
    /**
     * Sets the occurrence of the trigger to run the trigger actions.
     * 
     * @param occ 
     */
    public final void setOccurrence(Occurrence occ){
        occurrence = occ;
    }
    
    /**
     * Returns current occurrence setting.
     * @return 
     */
    public final Occurrence getOccurrence(){
        return occurrence;
    }
    
    /**
     * Returns the method how the rules should be matched.
     * @return 
     */
    public final String getRulesMatch(){
        return this.rulesMatch;
    }
    
    /**
     * Sets the threshold time in minutes.
     * When a trigger action has been executed at 11:34:20.000 and threshold is set at 10 minutes, the next earliest possible action run is at 11:34:20.001
     * @param thresholdTime 
     */
    public final void setThreshold(int thresholdTime){
        this.thresholdTime = (thresholdTime*60*1000);
    }
    
    /**
     * Add a rule to the event.
     * @param rule 
     */
    public final void addRule(Rule rule){
        rules.add(rule);
    }
    
    public final int getTriggerId(){
        return this.triggerId;
    }
    
    /**
     * Returns the name of the trigger.
     * @return 
     */
    public final String getTriggerName(){
        return this.triggerName;
    }
    
    /**
     * Returns the description.
     * @return 
     */
    public final String getTriggerDescription(){
        return this.description;
    }
    
    /**
     * Return the run actions instance.
     * @return 
     */
    public final EventRunActions getRunActions(){
        return runActions;
    }
    
    /**
     * Get the amount of rules.
     * @return 
     */
    public final int getRulesCount(){
        return this.rules.size();
    }
    
    /**
     * Get the amount of actions.
     * @return 
     */
    public final int getActionsCount(){
        return this.runActions.getTriggerActions().size();
    }
    
    /**
     * Checks if an event has the specified member.
     * @param member
     * @return 
     */
    public final boolean hasMember(String member){
        if(ruleOccurrence.contains(member)){
            for (Rule rule : rules) {
                for (RuleSubject subject:rule.getRules()) {
                    if (subject.isSubject(member)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Returns true/false if the current trigger is being edited.
     * @return 
     */
    public final boolean inEdit(){
        return inEdit;
    }
    
    public final void edit(boolean edit){
        inEdit = edit;
    }
    
    /**
     * Handles an event and sets the value for the member for future references.
     * When the rule is true, it executes the set commands.
     * @param member
     * @param value
     * @return 
     */
    public final boolean handleEvent(String member, Object value){
        if(ruleOccurrence.contains(member)){
            LOG.debug("Checking for value match: {} - {} in trigger event '{}'", member, value, this.getTriggerName());
            switch(this.rulesMatch){
                case "simple":
                    if(rules.size()>0){
                        if(rules.get(0).run(member, value)) return executeByResult();
                    }
                case "or":
                    for (Rule rule : rules) {
                        if (rule.run(member, value)) {
                            return executeByResult();
                        }
                    }
                break;
                case "and":
                    for (Rule rule : rules) {
                        if(!rule.run(member, value)){
                            return false;
                        }
                    }
                    return executeByResult();
            }
            if(occurrence==Occurrence.TOGGLE) occurred = false;
        }
        return false;
    }
    
    /**
     * Executes the event actions if occurrence met.
     */
    final boolean executeByResult(){
        switch(occurrence){
            case CONTINUOUS:
                run();
            break;
            case ONCE:
            case TOGGLE:
                if(!occurred) run();
            break;
            case THRESHOLDTIME:
                if(threshold()){
                    run();
                }
            break;
            default:
                return true;
        }
        return true;
    }    
    
    /**
     * Returns true when the threshold has been met.
     * @return 
     */
    final boolean threshold(){
        return lastOccurrenceTime==0 || lastOccurrenceTime < (lastOccurrenceTime - (thresholdTime*60*1000));
    }
    
    /**
     * Runs the event action(s)
     */
    final void run(){
        occurred = true;
        lastOccurrenceTime = new TimeUtils().getTime();
        Runnable goActions = () -> {
            runActions.runActions();
        };
        goActions.run();
    }
    
    /**
     * Returns the action list.
     * @return 
     */
    public final List<Map<String,Object>> getActionsList(){
        return this.execs;
    }
    
    /**
     * Helper class for running actions.
     */
    public final class EventRunActions implements Serializable {
    
        List<Map<String,Object>> triggerActions = new ArrayList();
        
        /**
         * Constructor.
         * @throws TriggerException 
         */
        public EventRunActions() throws TriggerException {
            if(execs.isEmpty()){
                throw new TriggerException("Trigger actions are not set");
            }
        }
        
        /**
         * Returns a list of actions for the trigger.
         * @return 
         */
        public final List<Map<String,Object>> getTriggerActions(){
            return execs;
        }
        
        /**
         * Runs the actions defined for the current trigger.
         */
        public final void runActions(){
            LOG.info("Executing trigger actions for trigger: {}", triggerName);
            boolean runDeviceBatch = false;
            for (Object execItem : execs) {
                Map<String,Object> exec = (Map<String,Object>) execItem;
                switch((String)exec.get("itemtype")){
                    case "device":
                        runDeviceBatch = true;
                        LOG.debug("Adding command to batch: {}", exec);
                        DeviceService.addBatch(((Long)exec.get("deviceid")).intValue(),
                                (String)exec.get("group"),
                                (String)exec.get("command"),
                                (String)exec.get("value"), 
                                (String)exec.get("extra"),
                                "trigger_" + triggerId);
                        break;
                    case "macro":
                        MacroService.runMacro(((Long)exec.get("macroid")).intValue());
                        break;
                    case "presence":
                        try {
                            PresenceService.activateGlobalPresence(((Long)exec.get("presenceid")).intValue());
                        } catch (PresenceException ex) {
                            LOG.error("Could not activate presence id {}", exec.get("presenceid"));
                        }
                        break;
                    case "daypart":
                        try {
                            DayPartsService.setDayPart(((Long)exec.get("daypartid")).intValue());
                        } catch (DayPartException ex) {
                            LOG.error("Could not activate day part id {}", exec.get("daypartid"));
                        }
                        break;
                    case "userstatus":
                        try {
                            UserStatusService.setUserStatus(((Long)exec.get("userstatusid")).intValue());
                        } catch (UserStatusException ex) {
                            LOG.error("Could not activate user status id {}", exec.get("daypartid"));
                        }
                        break;
                    case "messengerplugin":
                        try {
                            switch((String)exec.get("type")){
                                case "sms":
                                    MessengerPluginService.getInstance().sendSmsMessage((String)exec.get("message"));
                                break;
                            }
                        } catch (MessengerException ex) {
                            LOG.error("Could not send sms message {}", exec.get("message"));
                        }
                        break;
                }
            }
            if(runDeviceBatch==true){
                DeviceService.runBatch("trigger_" + triggerId);
            }
        }
        
    }
    
}
