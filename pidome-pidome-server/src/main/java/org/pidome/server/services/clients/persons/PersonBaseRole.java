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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John
 */
public class PersonBaseRole {
    
    static Logger LOG = LogManager.getLogger(PersonBaseRole.class);
    
    public enum BaseRole {
        ADMIN, USER, DISPLAY;
    }

    private BaseRole baseRole = BaseRole.USER;
    private final Set<String> nameSpaces;
    private final Set<Integer> locations;
    private final Map<String,Object> base;
    
    String nonClientIpAddress = "";
    boolean setNonClientPresent = false;
    boolean setNonClientAway    = false;
    boolean setGlobalPresence   = false;
    
    private boolean hasGpsSettings = false;
    private float   gpsDistance    = 0.0f;
    private int     gpsMacro       = 0;
    
    private boolean persMessage    = false;
    private String  persSubject    = "Arrival";
    private String  persContent    = "Your arrival is being prepared";
    
    public PersonBaseRole (Map<String,Object> roleSet){
        this.base = roleSet;
        this.nameSpaces = new HashSet<>();
        this.locations  = new HashSet<>();
        if(roleSet.containsKey("role")){
            switch((String)roleSet.get("role")){
                case "admin":
                    this.baseRole = BaseRole.ADMIN;
                break;
                default:
                if(roleSet.containsKey("locations")){
                    for(Long i:(List<Long>)roleSet.get("locations")){
                        locations.add(i.intValue());
                    }
                }
                break;
            }
            //// Non connected clients options.
            if(roleSet.containsKey("nonclientpresence")){
                nonClientIpAddress = (String)roleSet.get("nonclientpresence");
            }
            if(roleSet.containsKey("nonclientpresent")){
                setNonClientPresent = (boolean)roleSet.get("nonclientpresent");
            }
            if(roleSet.containsKey("nonclientaway")){
                setNonClientAway = (boolean)roleSet.get("nonclientaway");
            }
            if(roleSet.containsKey("setglobalpresence")){
                setGlobalPresence = (boolean)roleSet.get("setglobalpresence");
            }
            if(roleSet.containsKey("gpsoptions")){
                setGPSOptions((Map<String,Object>)roleSet.get("gpsoptions"));
            }
        } else {
            nameSpaces.add("");
            locations.add(-1);
        }
    }

    /**
     * Sets the GPS options.
     * @param options 
     */
    private void setGPSOptions(Map<String,Object> options){
        try {
            if(options.containsKey("gpsprepare") && (boolean)options.get("gpsprepare")==true){
                if(options.containsKey("gpspreparedistance")){
                    gpsDistance = ((Number)options.get("gpspreparedistance")).floatValue();
                }
                if(options.containsKey("gpspreparemacro")){
                    gpsMacro   = ((Number)options.get("gpspreparemacro")).intValue();
                }
                if(options.containsKey("gpspreparepersmessage")){
                    persMessage = (boolean)options.get("gpspreparepersmessage");
                }
                if(options.containsKey("gpspreparepersmessagesubject") && !((String)options.get("gpspreparepersmessagesubject")).isEmpty()){
                    persSubject = (String)options.get("gpspreparepersmessagesubject");
                }
                if(options.containsKey("gpspreparepersmessagemessage") && !((String)options.get("gpspreparepersmessagemessage")).isEmpty()){
                    persContent = (String)options.get("gpspreparepersmessagemessage");
                }
                hasGpsSettings = true;
            }
        } catch (Exception ex){
            LOG.warn("No GPS options or incorrect setup: {}, known values: {}", ex.getMessage(),options);
        }
    }
    
    /**
     * Return if GPS options have been set.
     * @return 
     */
    protected final boolean hasGPSOptions(){
        return this.hasGpsSettings;
    }
    
    /**
     * Returns if there is a personalized message.
     * @return 
     */
    protected final boolean hasPersonalizedGPSRangeMessage(){
        return this.persMessage;
    }
    
    /**
     * Returns the personalized GPS subject.
     * @return 
     */
    protected final String getPersonalizedGPSRangeMessageSubject(){
        return this.persSubject;
    }
    
    /**
     * Returns the personalized GPS message content.
     * @return 
     */
    protected final String getPersonalizedGPSRangeMessageContent(){
        return this.persContent;
    }
    
    /**
     * Return the distance threshold.
     * @return 
     */
    protected final float getGPSDistanceThreshold(){
        return this.gpsDistance * 1000;
    }
    
    /**
     * Return the macro to run when in the set range.
     * @return 
     */
    protected final int getGPSRangeMacro(){
        return this.gpsMacro;
    }
    
    /**
     * Returns true if this person can set the global presence.
     * This function will always return false when a ping like presence is used.
     * this means that AND non client ip must be empty, non client presence settings both must be false!
     * @return 
     */
    protected final boolean canSetGlobalPresence(){
        return setGlobalPresence;
    }
    
    /**
     * Returns the non client's ip address.
     * @return 
     */
    protected final String getNonClientIpAddress(){
        return nonClientIpAddress;
    }
    
    /**
     * Returns if a non connected client can set presence.
     * @return 
     */
    protected final boolean getSetNonClientPresent(){
        return this.setNonClientPresent;
    }
    
    /**
     * Returns if non connected client can set away
     * @return 
     */
    protected final boolean getSetNonClientAway(){
        return this.setNonClientAway;
    }
    
    /**
     * Returns the base role map set.
     * @return 
     */
    public final Map<String,Object> getPlainDefinition(){
        return this.base;
    }
    
    /**
     * Returns the role.
     * @return 
     */
    public final BaseRole role(){
        return baseRole;
    }
    
    /**
     * Checks if an user has access to a specific namespace.
     * @param nameSpace
     * @return 
     */
    public final boolean hasNameSpaceAccess(String nameSpace){
        switch(baseRole){
            case ADMIN:
                return true;
            default:
                return hasNameSpace(nameSpace);
        }
    }
    
    /**
     * Checks if an user has access to a location.
     * @param locationId
     * @return 
     */
    public final boolean hasLocationAccess(int locationId){
        switch(baseRole){
            case ADMIN:
                return true;
            default:
                return hasLocation(locationId);
        }
    }
    
    /**
     * Checks namespace.
     * @param nameSpace
     * @return 
     */
    private boolean hasNameSpace(String nameSpace){
        return (!nameSpaces.isEmpty())?nameSpaces.contains(nameSpace):true;
    }
    
    /**
     * Checks location.
     * @param locationId
     * @return 
     */
    private boolean hasLocation(int locationId){
        return (locationId==0)?true:((!locations.isEmpty())?locations.contains(locationId):true);
    }
    
}
