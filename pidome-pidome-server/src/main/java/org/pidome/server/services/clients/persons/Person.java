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
package org.pidome.server.services.clients.persons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.pidome.misc.utils.TimeUtils;
import org.pidome.server.services.clients.remoteclient.RemoteClient;
import org.pidome.server.services.macros.MacroService;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.system.audit.Notifications;
import org.pidome.server.system.presence.PresenceException;
import org.pidome.server.system.presence.PresenceService;

/**
 *
 * @author John
 */
public final class Person extends PersonBase {
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(Person.class);
    
    String firstName;
    String lastName;
    
    int personId;
    
    int presence = 2;
    boolean officialPresence = false;
    
    String nonBoundIpAddress = "";
    
    ScheduledExecutorService scheduler;
    
    private int failedRetries = 0;
    private final int maxFails = 10;
    
    private float curDistance = 0.0f;
    private float setDistance = 0.0f;
    
    private float lastLat = 0.0f;
    private float lastLon = 0.0f;
    private float lastAcc = 0.0f;
    
    private boolean GPSConfirmedOutofBound = false;
    private boolean isOutsideGPSBoundary   = false;    
    private boolean GPSMacroHasRun         = false;
    
    
    /**
     * A person.
     * @param personId
     * @param lastLogin
     * @param clientName
     * @param firstName
     * @param lastName 
     */
    protected Person(int personId, String lastLogin, String clientName, String firstName, String lastName){
        super(personId,clientName,lastLogin);
        this.personId  = personId;
        this.firstName = firstName;
        this.lastName  = lastName;
    }

    /**
     * Returns first name.
     * @return 
     */
    public final String getFirstName(){
        return this.firstName;
    }
    
    /**
     * Returns last name.
     * @return 
     */
    public final String getLastName(){
        return this.lastName;
    }
    
    /**
     * Returns true if this person can not be deleted.
     * @return 
     */
    public final boolean getReadOnly(){
        return readOnly;
    }
    
    /**
     * Returns if a person is present.
     * @return 
     */
    public final boolean getIfPresent(){
        return this.presence==1;
    }
    
    /**
     * Returns the ip address used to determine if an user is present.
     * @return 
     */
    public final String getNonClientIpAddress(){
        return nonBoundIpAddress;
    }
    
    /**
     * Returns the current set presence.
     * @return 
     */
    public final int getPresence(){
        return this.presence;
    }
    
    /**
     * Sets the firstname.
     * @param name 
     */
    protected final void setFirstName(String name){
        this.firstName = name;
    }
    
    /**
     * Set's the last name.
     * @param name 
     */
    protected final void setLastName(String name){
        this.lastName = name;
    }
    
    /**
     * Sets read only to true or false.
     * @param set 
     */
    protected final void setReadOnly(boolean set){
        this.readOnly = set;
    }
    
    /**
     * Sets an ip address for a non login client.
     * When the set ip address is present, this person is marked present.
     * @param address 
     */
    private void setNonClientIpAddress(String address){
        if(scheduler!=null){
            scheduler.shutdownNow();
            scheduler = null;
        }
        if(officialPresence==false && !address.isEmpty()){
            nonBoundIpAddress = address;
            if(scheduler!=null){
                scheduler.shutdownNow();
                scheduler = null;
            }
            if(scheduler==null){
                try {
                    InetAddress.getByName(address);
                    scheduler = Executors.newSingleThreadScheduledExecutor();
                    scheduler.scheduleAtFixedRate(() -> {
                        try {
                            if(isReachable(address)){
                                setPingPresence();
                            } else {
                                unsetPingPresence();
                            }
                        } catch (Exception ex) {
                            try {
                                LOG.error("Can not check presence: {}", ex.getMessage(), ex);
                                scheduler.shutdown();
                            } catch (Exception exc){}
                        }
                    }, 1, 1, TimeUnit.MINUTES);
                } catch (UnknownHostException ex) {
                    LOG.error("Unable to check if given ip is present: {}", ex.getMessage());
                }
            }
        }
    }

    private static boolean isReachable(String host) throws IOException {
        if (!InetAddress.getByName(host).isReachable(5000)){
            try {
                StringBuilder output = new StringBuilder();
                String[] cmdLine = {"/bin/ping", "-c","1","-w","5","-q",host};
                ProcessBuilder processBuilder = new ProcessBuilder(cmdLine);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();
                process.waitFor(6, TimeUnit.SECONDS);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    Pattern re = Pattern.compile("^.*,\\s(\\d+)%\\spacket\\sloss.*$$",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                    Matcher m = re.matcher(output.toString());
                    if(m.find()){
                        return !m.group(1).trim().equals("100");
                    }
                }
                return false;
            } catch (InterruptedException e) {
                throw new IOException("Interrupted: " + e.getMessage());
            }
        } else {
            return true;
        }
    }
    
    /**
     * Returns if a person may set the global presence.
     * A global presence can be set when this person is the last to leave and
     * the first to enter.
     * @see org.pidome.server.services.persons.PersonBaseRole.canSetGlobalPresence
     * @return 
     */
    public final boolean canSetGlobalPresence(){
        return getRole().canSetGlobalPresence();
    }
    
    /**
     * Sets the current presence.
     * @param presenceId 
     */
    public final void setPresence(int presenceId){
        this.presence = presenceId;
        //// In case of GPS macro usage which should only run once when in reach per away set, reset to false when home.
        if(this.presence==1){
            GPSMacroHasRun = false;
        }
        try {
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", personId);
                    put("presence", presenceId);
                    put("name", PresenceService.getPresence(presenceId).getName());
                }
            };
            ClientMessenger.send("UserService","setUserPresence", 0, sendObject);
        } catch (Exception ex){
            //// Could not send out presence.
        }
        this.presence = presenceId;
    }
    
    /**
     * Sets presence by ping
     */
    private void setPingPresence(){
        failedRetries = 0;
        if(this.presence != 1 && getRole().getSetNonClientPresent()){
            LOG.info("Setting {} to present", this.getFirstName());
            try {
                PresenceService.setPresence(getId(), 1);
                PersonsManagement.updateLoginDateTime(this);
            } catch (PersonsManagementException ex){
                LOG.error("Could not set last presence time for {}: ", this.getFirstName(), ex.getMessage());
            } catch (PresenceException ex) {
                LOG.error("Could not set presence home for {}: ", this.getFirstName(), ex.getMessage());
            }
        }
    }
    
    /**
     * Unsets presence by ping.
     */
    private void unsetPingPresence(){
        if(failedRetries!=maxFails){
            failedRetries++;
        } else {
            if(this.presence == 1 && getRole().getSetNonClientAway()){
                LOG.info("Setting {} to non present", this.getFirstName());
                try {
                    PresenceService.setPresence(getId(), 2);
                } catch (PresenceException ex) {
                    LOG.error("Could not set presence away for {}: ", this.getFirstName(), ex.getMessage());
                }
            }
        }
    }

    /**
     * Returns the person's current distance as reported by a device with GPS enabled.
     * @return 
     */
    public final float getCurrentDistance(){
        if(this.presence==1){
            return 0f;
        } else {
            return this.curDistance;
        }
    }
    
    /**
     * Returns the person's current distance as reported by a device with GPS enabled.
     * @return 
     */
    public final Map<String,Float> getLastLatLon(){
        Map<String,Float> lastLoc = new HashMap<>();
        lastLoc.put("lastLat", (lastLat==0?TimeUtils.getCurrentLatitude():lastLat));
        lastLoc.put("lastLon", (lastLon==0?TimeUtils.getCurrentLongitude():lastLon));
        lastLoc.put("lastAcc", lastAcc);
        return lastLoc;
    }
    
    /**
     * Checks the current distance, and if in range run the macro if the timeout has passed.
     * When a person is out of range it starts the timeout and tries to run the 
     * macro.
     * @param reportingClient The client reporting this.
     * @param curDistance the distance to check.
     * @param latitude The last known latitude.
     * @param longitude The last known longitude.
     * @param accuracy The last known accuracy
     */
    public void setDistance(RemoteClient reportingClient, float curDistance, float latitude, float longitude, float accuracy){
        this.curDistance = curDistance;
        this.lastLat = latitude;
        this.lastLon = longitude;
        this.lastAcc = accuracy;
        LOG.debug("GPS enabled: {}",this.getRole().hasGPSOptions());
        if(this.getRole().hasGPSOptions()){
            for(RemoteClient client:this.getRemoteClients()){
                if(reportingClient == client && client.getHasGPSEnabled()){
                    this.isOutsideGPSBoundary = this.getRole().getGPSDistanceThreshold() < this.curDistance;
                    if(!this.getIfPresent() && !this.isOutsideGPSBoundary && this.GPSConfirmedOutofBound && !GPSMacroHasRun){
                        LOG.info("User {} is out of the GPS boundary of {} KM: {}, at {} KM. Initiated by device: {}", this.getLoginName(), this.getRole().getGPSDistanceThreshold()/1000, this.isOutsideGPSBoundary, curDistance/1000, client.getClientName());
                        runInRangeMacro();
                    } else if(!this.getIfPresent() && this.isOutsideGPSBoundary){
                        LOG.info("User {} is out of the GPS boundary of {} KM: {}, at {} KM. Initiated by device: {}", this.getLoginName(), this.getRole().getGPSDistanceThreshold()/1000, this.isOutsideGPSBoundary, curDistance/1000, client.getClientName());
                        this.GPSConfirmedOutofBound = true;
                    }
                    Map<String, Object> sendObject = new HashMap<String, Object>() {
                        {
                            put("id", personId);
                            put("distance", curDistance);
                            put("lat", latitude);
                            put("lon", longitude);
                            put("acc", accuracy);
                        }
                    };
                    ClientMessenger.send("UserService", "updateLocation", 0, sendObject);
                    break;
                }
            }
        }
    }
    
    /**
     * Returns if this person is out of GPS bounds.
     * @return 
     */
    protected boolean isOutSideGPSBoundary(){
        return this.GPSConfirmedOutofBound;
    }
    
    /**
     * Runs the macro set when in range and out of range timeout has passed.
     */
    private void runInRangeMacro(){
        LOG.trace("in runmacro");
        if(!PersonsManagement.getInstance().allPersonsPresenceCheck() && PersonsManagement.getInstance().allPersonsOutOfBounds()){
            this.GPSConfirmedOutofBound = false;
            this.GPSMacroHasRun = true;
            LOG.info("Nobody home and everyone outside GPS threshold except {}, lets do stuff", this.getLoginName());
            MacroService.runMacro(this.getRole().getGPSRangeMacro());
            if(this.getRole().hasPersonalizedGPSRangeMessage()){
                Notifications.sendPersonalizedMessage(this.getId(), Notifications.NotificationType.INFO, this.getRole().getPersonalizedGPSRangeMessageSubject(), this.getRole().getPersonalizedGPSRangeMessageContent());
            }
        } else {
            LOG.info("Someone home or inside GPS threshold, do nothing");
        }
    }
    
    /**
     * Executed when a persons role has been created.
     */
    @Override
    public void roleCreated() {
        String ip = getRole().getNonClientIpAddress();
        if (!ip.isEmpty()) setNonClientIpAddress(ip);
    }
    
    /**
     * Returns the only possible type base for a person.
     * @return 
     */
    @Override
    public final RemoteClient.Type getType(){
        return RemoteClient.Type.WEBSOCKET;
    }
    
}
