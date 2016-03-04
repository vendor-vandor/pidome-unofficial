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

package org.pidome.server.system.presence;

/**
 * Holds a presence.
 * If correctly used it represents a user away, home, sleeping etc...
 * @author John
 */
public final class Presence {
    
    String name;
    String description;
    int id;
    boolean isFixed = true;
    String lastActivated;
    int macroId = 0;
    
    /**
     * Constructor.
     * @param id
     * @param name
     * @param description 
     * @param macroId 
     * @param fixed 
     */
    protected Presence(int id, String name, String description, int macroId, boolean fixed){
        this.id = id;
        this.name = name;
        this.description = description;
        this.isFixed = fixed;
        this.macroId = macroId;
    }
    
    /**
     * Returns if a presence is a fixed presence.
     * @return 
     */
    public final boolean getIsFixed(){
        return this.isFixed;
    }
    
    /**
     * Returns the id.
     * @return 
     */
    public final int getId(){
        return this.id;
    }
    
    /**
     * Returns the name.
     * @return 
     */
    public final String getName(){
        return this.name;
    }
    
    /**
     * Returns the description.
     * @return 
     */
    public final String getDescription(){
        return this.description;
    }
    
    /**
     * Updates the presence name.
     * @param name 
     */
    protected final void updateName(String name){
        this.name = name;
    }
    
    /**
     * Updates the description.
     * @param description 
     */
    protected final void updateDescription(String description){
        this.description = description;
    }
    
    /**
     * Updates the macro id.
     * @param macroId 
     */
    protected final void updateMacro(int macroId){
        this.macroId = macroId;
    }
    
    /**
     * Returns the last activated time.
     * @return 
     */
    public String getLastActivated(){
        return this.lastActivated;
    }
    
    /**
     * Sets the last activation time.
     * @param datetime 
     */
    protected void setLastActivated(String datetime){
        this.lastActivated = datetime;
    }
    
    /**
     * Returns if this presence holds a macro.
     * @return 
     */
    public final boolean hasMacro(){
        return (macroId>0);
    }
    
    /**
     * Returns a macro id if available, otherwise throws exception.
     * @return 
     * @throws org.pidome.server.system.presence.PresenceException 
     */
    public final int getMacroId() throws PresenceException {
        if(hasMacro()){
            return macroId;
        } else {
            throw new PresenceException("There is no macro configured");
        }
    }
    
}
