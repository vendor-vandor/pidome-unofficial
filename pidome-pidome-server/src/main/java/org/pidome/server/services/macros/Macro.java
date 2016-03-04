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

package org.pidome.server.services.macros;

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
import org.pidome.server.connector.plugins.media.MediaException;
import org.pidome.server.connector.plugins.media.MediaPlugin;
import org.pidome.server.connector.plugins.pidomeremote.PiDomeRemoteButtonException;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.services.plugins.MediaPluginService;
import org.pidome.server.services.plugins.RemotesPluginService;
import org.pidome.server.system.dayparts.DayPartException;
import org.pidome.server.system.dayparts.DayPartsService;
import org.pidome.server.system.presence.PresenceException;
import org.pidome.server.system.presence.PresenceService;
import org.pidome.server.system.userstatus.UserStatusException;
import org.pidome.server.system.userstatus.UserStatusService;

/**
 * @author John Sirach
 */
public final class Macro {

    /**
     * Macro name.
     */
    String macroName;
    /**
     * Event description.
     */
    String description;
    /**
     * Macro id.
     */
    int macroId;
    /**
     * primary array of the executables
     */
    ArrayList execs = new ArrayList();
    /**
     * Last occurrence time in milliseconds from 1970.
     */
    long lastOccurrenceTime = 0;
    /**
     * Inner class holding the actions to be ran.
     */
    MacroRunActions runActions;
    /**
     * Check if the current Macro is being edited.
     * This edit switch is only when a Macro is being saved/deleted to the database.
     */
    boolean inEdit = false;
    /**
     * Set if this macro is a favorite macro.
     */
    boolean isfavorite = false;
    /**
     * Logger
     */
    static Logger LOG = LogManager.getLogger(Macro.class);
    
    /**
     * Creates a Macro.
     * @param macroId
     * @param macroName
     * @param description 
     * @param isfavorite 
     * @param execs 
     * @throws org.pidome.server.services.macros.MacroException 
     */
    public Macro(int macroId, String macroName, String description, boolean isfavorite, ArrayList execs) throws MacroException {
        this.macroId = macroId;
        this.macroName = macroName;
        this.description = description;
        this.isfavorite = isfavorite;
        this.execs = execs;
        runActions = new MacroRunActions();
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
     * Returns the  macro id.
     * @return 
     */
    public final int getMacroId(){
        return this.macroId;
    }
    
    /**
     * Returns the name of the macro.
     * @return 
     */
    public final String getMacroName(){
        return this.macroName;
    }
    
    /**
     * Returns the description.
     * @return 
     */
    public final String getDescription(){
        return this.description;
    }
    
    /**
     * Returns if favorite.
     * @return 
     */
    public boolean getIsFavorite(){
        return this.isfavorite;
    }

    /**
     * Sets if favorite. 
     * @param favorite
     */
    public void setFavorite(boolean favorite){
        this.isfavorite = favorite;
    }
    
    /**
     * Return the run actions instance.
     * @return 
     */
    public final MacroRunActions getRunActions(){
        return runActions;
    }
    
    /**
     * Get the amount of run actions.
     * @return 
     */
    public final int getActionsCount(){
        return this.runActions.getMacroActions().size();
    }
    
    /**
     * Returns true/false if the current macro is being edited.
     * @return 
     */
    public final boolean inEdit(){
        return inEdit;
    }
    
    /**
     * Set or unset macro in edit.
     * @param edit 
     */
    public final void edit(boolean edit){
        inEdit = edit;
    }
    
    /**
     * Runs the event action(s)
     */
    public final void execute(){
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
    public final class MacroRunActions implements Serializable {
    
        /**
         * Constructor. 
         * @throws org.pidome.server.services.macros.MacroException
         */
        public MacroRunActions() throws MacroException {

        }
        
        /**
         * Returns a list of actions for the macro.
         * @return 
         */
        public final List<Map<String,Object>> getMacroActions(){
            return execs;
        }
        
        /**
         * Runs the actions defined for the current macro.
         */
        public final void runActions(){
            if(execs.isEmpty()){
                LOG.info("There are no actions to run for macro id: {}.", macroId);
            } else {
                boolean runDeviceBatch = false;
                for(int i = 0; i < execs.size(); i++){
                    Map<String,Object> exec = (Map<String,Object>)execs.get(i);
                    switch((String)exec.get("itemtype")){
                        case "device":
                            runDeviceBatch = true;
                            LOG.debug("Adding command to batch: {}", exec);
                            DeviceService.addBatch(((Long)exec.get("deviceid")).intValue(), 
                                                   (String)exec.get("group"), 
                                                   (String)exec.get("command"), 
                                                   exec.get("value"), 
                                                   (String)exec.get("extra"),
                                                   "macro_" + macroId);
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
                        case "remoteplugin":
                            try {
                                RemotesPluginService.getInstance().handleRemoteButton(((Long)exec.get("remoteId")).intValue(), (String)exec.get("remoteButtonId"));
                            } catch (PiDomeRemoteButtonException ex) {
                                LOG.error("Could not press button {} on remote {}",exec.get("remoteButtonId"),exec.get("remoteId"));
                            }
                        break;                
                        case "mediaplugin":
                            try {
                                MediaPlugin.PlayerCommand command = null;
                                switch((String)exec.get("mediaCommandId")){
                                    case "PLAYER_STOP":
                                        command = MediaPlugin.PlayerCommand.STOP;
                                    break;
                                    case "PLAYER_PLAY":
                                        command = MediaPlugin.PlayerCommand.PLAY;
                                    break;
                                    case "PLAYER_PAUSE":
                                        command = MediaPlugin.PlayerCommand.PAUSE;
                                    break;
                                    default:
                                        throw new MediaException("Unsupported run command in macro");
                                }
                                if(command!=null){
                                    MediaPluginService.getInstance().
                                        getPlugin(((Long)exec.get("mediaId")).intValue()).
                                            handlePlayerCommand(command, (Object) null);
                                }
                            } catch (MediaException ex) {
                                LOG.error("Could not run media command {}", exec.get("mediaCommandId"));
                            }
                        break;
                    }
                }
                if(runDeviceBatch==true){
                    DeviceService.runBatch("macro_" + macroId);
                }
            }
        }
        
    }
    
}
