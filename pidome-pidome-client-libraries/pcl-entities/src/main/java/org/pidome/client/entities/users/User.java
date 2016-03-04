/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.users;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.utilities.properties.BooleanPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ReadOnlyBooleanPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ReadOnlyStringPropertyBindingBean;
import org.pidome.pcl.utilities.properties.StringPropertyBindingBean;


/**
 * Holding a single user information.
 * @author John
 */
public final class User {
    
    static {
        Logger.getLogger(User.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * The user id.
     */
    private final int userId;
    /**
     * The login name.
     */
    private final String login;
    /**
     * Users first name.
     */
    private final StringPropertyBindingBean firstName = new StringPropertyBindingBean("");    
    /**
     * USer's last name.
     */
    private final StringPropertyBindingBean lastName = new StringPropertyBindingBean("");
    /**
     * USers last login date and time.
     */
    private final StringPropertyBindingBean lastlogin = new StringPropertyBindingBean("00-00-0000 00:00");
    /**
     * User's current presence.
     */
    private final BooleanPropertyBindingBean present = new BooleanPropertyBindingBean(false);
    
    /**
     * The user's current presence id.
     * Can be mapped against the presence service list.
     */
    private int presenceId = 1;
    
    /**
     * Constructor.
     * @param userId The user id as known on the server.
     * @param loginName The login name as known on the server.
     */
    protected User(int userId, String loginName){
        this.userId = userId;
        this.login  = loginName;
    }

    /**
     * Returns the user id.
     * @return The user id.
     */
    public final int getUserId(){
        return this.userId;
    }
    
    /**
     * Returns the username.
     * @return The user name
     */
    public final String getUserName(){
        return this.login;
    }
    
    /**
     * Returns the last login property.
     * @return Last login date.
     */
    public final ReadOnlyStringPropertyBindingBean getLastLogin(){
        return this.lastlogin.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Returns the first name.
     * @return The first name.
     */
    public final ReadOnlyStringPropertyBindingBean getFirstName(){
        return this.firstName.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Returns the last name.
     * @return The last name.
     */
    public final ReadOnlyStringPropertyBindingBean getLastName(){
        return this.lastName.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Returns the presence.
     * @return a read only bean with live presence updates.
     */
    public final ReadOnlyBooleanPropertyBindingBean getPresent(){
        return this.present.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Sets the first name.
     * @param name The person first name
     */
    protected final void setFirstName(String name){
        this.firstName.setValue(name);
    }
    
    /**
     * Sets the last name.
     * @param name The person last name.
     */
    protected final void setLastName(String name){
        this.lastName.setValue(name);
    }
    
    /**
     * Set's the last login date.
     * @param loginDate The last login date as String.
     */
    protected final void setLastLogin(String loginDate){
        this.lastlogin.setValue(loginDate);
    }
    
    /**
     * Sets if the user is present or not.
     * @param present if the user is present or not.
     */
    protected final void setPresent(boolean present){
        this.present.setValue(present);
    }
 
    /**
     * Sets the user's current presence id.
     * This can be mapped to the PresenceService list.
     * @param presenceId The id of the presence to set.
     */
    protected final void setCurrentPresenceId(int presenceId){
        this.presenceId = presenceId;
    }
    
    /**
     * Returns the current presence id.
     * @return The current presence id which can be matched against the presence service list.
     */
    public final int getCurrentPresenceId(){
        return this.presenceId;
    }
    
}