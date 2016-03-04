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

package org.pidome.server.system.userstatus;

/**
 *
 * @author John
 */
public final class UserStatus {
    
    String name;
    String description;
    int id;
    boolean isFixed = true;
    String lastActivated;
    
    /**
     * Constructor.
     * @param id
     * @param name
     * @param description 
     * @param fixed 
     */
    protected UserStatus(int id, String name, String description, boolean fixed){
        this.id = id;
        this.name = name;
        this.description = description;
        this.isFixed = fixed;
    }
    
    /**
     * Returns if a status is a fixed status.
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
     * Updates the status name.
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
    
}

