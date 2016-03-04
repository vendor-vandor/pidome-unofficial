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

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.misc.utils.MinuteListener;
import org.pidome.misc.utils.TimeUtils;
import org.pidome.server.services.ServiceInterface;
import org.pidome.server.services.triggerservice.TriggerEvent.Occurrence;

/**
 * Service for handling events.
 * Events are checked against rules and if matched a command can be send to a device or plugin.
 * An event is data received or send to a device or plugin. This means an event is a trigger which acts on values.
 * @author John Sirach
 */
public class TriggerService implements ServiceInterface,MinuteListener {

    /**
     * List of possible events.
     */
    static Map<Integer,TriggerEvent> triggers = new HashMap<>();
    
    /**
     * Service availability.
     */
    static boolean running = false;
    
    /**
     * Trigger database
     */
    static TriggerDB db = new TriggerDB();
    
    /**
     * Logger.
     */
    static Logger LOG = LogManager.getLogger(TriggerService.class);
    
    /**
     * Holds the current time.
     */
    static String currentTime;
    
    /**
     * Holds the current day.
     */
    static String currentDay = "";
    
    final Map<Integer,String> weekdays = new HashMap<>();
    
    /**
     * constructor
     */
    public TriggerService(){
        weekdays.put(1, "MON");
        weekdays.put(2, "TUE");
        weekdays.put(3, "WED");
        weekdays.put(4, "TUE");
        weekdays.put(5, "FRI");
        weekdays.put(6, "SAT");
        weekdays.put(7, "SUN");
        TimeUtils.addMinuteListener(this);
    }

    public final int getTriggersAmount(){
        return triggers.size();
    }
    
    /**
     * This handles the latest time known in the server and updates any trigger with this current time if applicable.
     * @param timeutils 
     */
    @Override
    public void handleMinuteUpdate(final TimeUtils timeutils) {
        currentTime = timeutils.get24HoursTime();
        Thread triggerRun = new Thread(){
            @Override
            public final void run(){
                Integer curDay = timeutils.getDayOfWeek();
                if(!currentDay.equals(weekdays.get(curDay))){
                    currentDay = weekdays.get(curDay);
                    updateVariableTimedTriggers();
                }
                if (weekdays.containsKey(curDay)) {
                    if ((curDay == 6 || curDay == 7)) {
                        handleEvent("daytime_FIXED_WEEKEND", currentTime);
                        handleEvent("daytime_SUNRISE_WEEKEND", currentTime);
                        handleEvent("daytime_SUNSET_WEEKEND", currentTime);
                    } else if ((curDay < 6)) {
                        handleEvent("daytime_FIXED_WEEKDAY", currentTime);
                        handleEvent("daytime_SUNRISE_WEEKDAY", currentTime);
                        handleEvent("daytime_SUNSET_WEEKDAY", currentTime);
                    }
                    handleEvent("daytime_FIXED_ALL", currentTime);
                    handleEvent("daytime_SUNRISE_ALL", currentTime);
                    handleEvent("daytime_SUNSET_ALL", currentTime);
                    handleEvent("daytime_FIXED_" + currentDay, currentTime);
                    handleEvent("daytime_SUNRISE_" + currentDay, currentTime);
                    handleEvent("daytime_SUNSET_" + currentDay, currentTime);
                }
            }
        };
        triggerRun.setName("TriggerTimeRun::" + currentTime);
        triggerRun.start();
    }
    
    /**
     * Handles an event.
     * If a member (rule subject) is available in a rule that rule will be ran. This means that any rule which is true and contains the member can trigger a
     * command send to any device or plugin.
     * @param member
     * @param value 
     */
    public static void handleEvent(final String member, final Object value){
        if(running){
            for(TriggerEvent event: triggers.values()){
                if(!event.inEdit() && event.hasMember(member)){
                    event.handleEvent(member, value);
                }
            }
        }
    }
    
    /**
     * Return a event with it's rules.
     * @param triggerId
     * @return
     * @throws TriggerException 
     */
    public static TriggerEvent getTrigger(int triggerId) throws TriggerException {
        if(triggers.containsKey(triggerId)){
            return triggers.get(triggerId);
        } else {
            throw new TriggerException("Trigger does not exist");
        }
    }
    
    /**
     * Returns the action list associated with the given trigger id.
     * @param triggerId
     * @return
     * @throws TriggerException 
     */
    public static List<Map<String,Object>> getActionsList(int triggerId) throws TriggerException {
        return getTrigger(triggerId).getActionsList();
    }
    
    /**
     * Returns all triggers
     * @return 
     */
    public static Map<Integer,TriggerEvent> getTriggers() {
        return triggers;
    }
    
    /**
     * Updates any rule in any trigger which uses sunrise or sunset.
     */
    public static void updateVariableTimedTriggers(){
        LOG.debug("Updating Sunset and Sunrise to {}, {}", TimeUtils.getSunset(), TimeUtils.getSunrise());
        for (int key : triggers.keySet()) {
            if (!triggers.get(key).inEdit()) {
                triggers.get(key).updateVariableTimedTriggers();
            }
        }
    }
    
    /**
     * Updates a trigger.
     * An update is reflected immediately.
     * @param triggerId
     * @param name
     * @param description 
     * @param ruleset 
     * @param matchType 
     * @param occurrence 
     * @param exec 
     * @return  
     * @throws org.pidome.server.services.triggerservice.TriggerException 
     */
    public static boolean saveTrigger(int triggerId, String name, String description, Occurrence occurrence, String matchType, ArrayList ruleset, ArrayList exec) throws TriggerException {
        try {
            triggers.get(triggerId).edit(true);
            db.saveTrigger(triggerId, name, description, occurrence, matchType, ruleset, exec);
            triggers.put(triggerId, new TriggerEvent(triggerId, name, description, occurrence, matchType, ruleset, exec));
            triggers.get(triggerId).edit(false);
            return true;
        } catch (NullPointerException | SQLException | IOException ex) {
            throw new TriggerException("Could not update trigger: " + ex.getMessage());
        }
    }

    /**
     * Saves a new trigger.
     * A new trigger is immediately active.
     * @param name
     * @param description 
     * @param ruleset 
     * @param matchType 
     * @param occurrence 
     * @param exec 
     * @return  
     * @throws org.pidome.server.services.triggerservice.TriggerException 
     */
    public static int saveTrigger(String name, String description, Occurrence occurrence, String matchType, ArrayList ruleset, ArrayList exec) throws TriggerException {
        try {
            int triggerId = db.saveTrigger(0, name, description, occurrence, matchType, ruleset, exec);
            triggers.put(triggerId, new TriggerEvent(triggerId, name, description, occurrence, matchType, ruleset, exec));
            return triggerId;
        } catch (SQLException | IOException ex) {
            throw new TriggerException("Could not save trigger: " + ex.getMessage());
        }
    }
    
    /**
     * Deletes a trigger from the database.
     * Triggers are when removed immediately unlinked
     * @param triggerId
     * @return 
     * @throws TriggerException 
     */
    public static boolean deleteTrigger(int triggerId) throws TriggerException {
        try {
            triggers.get(triggerId).edit(true);
            db.deleteTrigger(triggerId);
            triggers.remove(triggerId);
            return true;
        } catch (NullPointerException | SQLException ex) {
            throw new TriggerException("Could not delete trigger: " + ex.getMessage());
        }
    }
    
    /**
     * Stops the event service causing none of the rules to be handled.
     */
    @Override
    public void interrupt() {
        if(running==true){
            running = false;
            triggers.clear();
        }
    }

    /**
     * Runs a trigger like it is true.
     * @param triggerId
     * @throws TriggerException 
     */
    public static void runTrigger(int triggerId) throws TriggerException {
        getTrigger(triggerId).run();
    }
    
    /**
     * Starts the service.
     */
    @Override
    public void start() {
        Thread triggerStarter = new Thread(){
            @Override
            public final void run(){
                if(running==false){
                    try {
                        Map<Integer, Map<String, Object>> storedTriggers = db.getTriggers();
                        for(int key:storedTriggers.keySet()){
                            try {
                                Occurrence occurrence;
                                switch((String)storedTriggers.get(key).get("occurrence")){
                                    case "TOGGLE":
                                        occurrence = Occurrence.TOGGLE;
                                    break;
                                    case "CONTINUOUS":
                                        occurrence = Occurrence.CONTINUOUS;
                                    break;
                                    case "ONCE":
                                        occurrence = Occurrence.ONCE;
                                    break;
                                    case "THRESHOLDTIME":
                                        occurrence = Occurrence.THRESHOLDTIME;
                                    break;
                                    default:
                                        throw new TriggerException("Unsupported occurrence: "+ storedTriggers.get(key).get("occurrence"));
                                }
                                triggers.put(key, new TriggerEvent(key,
                                                                   (String)storedTriggers.get(key).get("name"),
                                                                   (String)storedTriggers.get(key).get("description"),
                                                                   occurrence,
                                                                   (String)storedTriggers.get(key).get("matchtype"),
                                                                   (ArrayList)storedTriggers.get(key).get("rules"),
                                                                   (ArrayList)storedTriggers.get(key).get("executes")));
                            } catch (TriggerException ex) {
                                LOG.error("Trigger {} (id {}) not initialized: {}", storedTriggers.get(key).get("name"), key, ex.getMessage());
                            }
                        }
                        running = true;
                    } catch (SQLException ex) {
                        LOG.error("Could not start trigger service");
                    }
                }
            }
        };
        triggerStarter.setName("TriggerService::TriggersPreLoader");
        triggerStarter.start();
    }

    /**
     * Checks if the service is available.
     * @return 
     */
    @Override
    public boolean isAlive() {
        return running;
    }
    
    @Override
    public String getServiceName() {
        return "Trigger service";
    }
    
}
