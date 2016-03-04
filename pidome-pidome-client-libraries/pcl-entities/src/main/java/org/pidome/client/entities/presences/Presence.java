/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.presences;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.utilities.properties.ReadOnlyStringPropertyBindingBean;
import org.pidome.pcl.utilities.properties.StringPropertyBindingBean;


/**
 * A presence.
 * @author John
 */
public final class Presence {
    
    static {
        Logger.getLogger(Presence.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * The presence id.
     */
    private int presenceId;
    
    /**
     * The presence name.
     */
    private StringPropertyBindingBean presenceName = new StringPropertyBindingBean("");
    
    /**
     * Presence description.
     */
    private StringPropertyBindingBean description = new StringPropertyBindingBean("");
    
    /**
     * Last activation of this presence.
     */
    private String lastActivated;
    
    /**
     * Holding if it currently is the current presence.
     */
    boolean isCurrent = false;
    
    /**
     * Constructs a presence, sets id and initial name.
     * @param presenceId The id of the presence.
     * @param name The name of the current presence.
     */
    protected Presence(int presenceId, String name){
        this.presenceId = presenceId;
        this.presenceName.setValue(name);
    }
    
    /**
     * An user can update the presence name.
     * @param name The name of the current presence.
     */
    protected final void setName(String name){
        this.presenceName.setValue(name);
    }
    
    /**
     * Sets the description.
     * @param description The description of the current presence.
     */
    protected final void setDescription(String description){
        this.description.setValue(description);
    }
    
    /**
     * Sets the last activated time and date.
     * @param lastActivated last presence activated time.
     */
    protected final void setLastActivated(String lastActivated){
        this.lastActivated = lastActivated;
    }
    
    /**
     * Sets a parameter if this presence is the current presence.
     * @param isCurrent true if the current presence is the active one.
     * @deprecated No longer used
     */
    protected final void setCurrent(boolean isCurrent){
        this.isCurrent = isCurrent;
    }
    
    /**
     * Returns the presence id.
     * @return the presence id.
     */
    public final int getPresenceId(){
        return this.presenceId;
    }
    
    /**
     * Returns the name of this presence.
     * @return bindable presence name.
     */
    public final ReadOnlyStringPropertyBindingBean getName(){
        return this.presenceName.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Returns the description. 
     * @return bindable presence description.
     */
    public final ReadOnlyStringPropertyBindingBean getDescription(){
        return this.description.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Returns the last activated time and date. 
     * @return Last activated time.
     */
    public final String getLastActivated(){
        return this.lastActivated;
    }
    
    /**
     * Gets the parameter if this presence is the current presence. 
     * @return if the current presence is the active one.
     * @deprecated Not reliable anymore use the current presence property
     */
    public final boolean isCurrent(){
        return this.isCurrent;
    }
    
}