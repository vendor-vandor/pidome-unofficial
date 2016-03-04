/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.Entity;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.client.system.PCCConnectionNameSpaceRPCListener;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.networking.connections.server.ServerConnection.Profile;
import org.pidome.pcl.utilities.properties.ObservableArrayListBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 * Class exposing all the known users in the PiDome system.
 * @author John
 */
public final class UserService extends Entity implements PCCConnectionNameSpaceRPCListener {

    static {
        Logger.getLogger(UserService.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Connection interface.
     */
    private PCCConnectionInterface connection;
    
    /**
     * Set of listeners.
     */
    private final HashSet<UserServiceListener> _listeners = new HashSet<>();
    
    /**
     * List of known users.
     */
    private final ObservableArrayListBean<User> userList = new ObservableArrayListBean();
    
    /**
     * A read only wrapper for the users list.
     */
    private final ReadOnlyObservableArrayListBean<User> readOnlyUserList = new ReadOnlyObservableArrayListBean<>(userList);
    
    /**
     * The user self.
     */
    private User me;
    
    /**
     * Constructor.
     * @param connection The server connection.
     */
    public UserService(PCCConnectionInterface connection){
        this.connection = connection;
    }
    
    @Override
    public void unloadContent() throws EntityNotAvailableException {
        userList.clear();
    }
    
    /**
     * Sets the connected user's personal presence.
     * This is only available when the user personal info is available using getMyData.
     * @param presenceId The id of the presence to set.
     * @throws UserServiceException When the personal user data is not known
     */
    public final void setPresence(int presenceId) throws UserServiceException {
        if(me!=null){
            try {
                Map<String,Object> userData = new HashMap<>();
                userData.put("id", me.getUserId());
                userData.put("presenceid", presenceId);
                this.connection.getJsonHTTPRPC("UserService.setUserPresence", userData, "UserService.setUserPresence");
            } catch (PCCEntityDataHandlerException ex) {
                Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            throw new UserServiceException("Personal user data not available");
        }
    }
    
    /**
     * Returns a observable read only list of users.
     * When the user list is empty the system will to a request to fetch the users.
     * @return a read only list of users.
     * @throws org.pidome.client.entities.users.UserServiceException When the userlist fails.
     */
    public final ReadOnlyObservableArrayListBean<User> getUserList() throws UserServiceException {
        return readOnlyUserList;
    }
    
    /**
     * Initializes the users service.
     * It adds a UserService namespace listener to the connection.
     */
    @Override
    protected void initilialize() {
        this.connection.addPCCConnectionNameSpaceListener("UserService", this);
    }

    /**
     * De-initializes the users service.
     * It removes a UserService namespace listener from the connection.
     */
    @Override
    protected void release() {
        this.connection.removePCCConnectionNameSpaceListener("UserService", this);
    }

    /**
     * Handles user service broadcasts.
     * @param rpcDataHandler PCCEntityDataHandler containing user data broadcasts.
     */
    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        switch(rpcDataHandler.getMethod()){
            case "setUserPresence":
                Map<String,Object> userData = rpcDataHandler.getParameters();
                int uid           = ((Number)userData.get("id")).intValue();
                int curPresence   = ((Number)userData.get("presence")).intValue();
                boolean isPresent = curPresence==1;
                for(User user:userList){
                    if(user.getUserId()==uid){
                        user.setCurrentPresenceId(curPresence);
                        user.setPresent(isPresent);
                    }
                }
                if(me!=null && me.getUserId()==uid){
                    me.setCurrentPresenceId(curPresence);
                    me.setPresent(isPresent);
                }
            break;
        }
    }
    
    /**
     * Handles user service request results if send out using the connection socket.
     * This function is compatible for both result broadcasts as data received from an json http request.
     * @param rpcDataHandler PCCEntityDataHandler containing user data results.
     */
    @Override
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler) {
        ArrayList<Map<String,Object>> data = (ArrayList<Map<String,Object>>)rpcDataHandler.getResult().get("data");
        Runnable run = () -> {
            List<User> users = new ArrayList<>();
            if(data!=null){
                for(Map<String,Object> userData: data){
                    User user = new User(((Number)userData.get("id")).intValue(), (String)userData.get("clientname"));
                    user.setFirstName((String)userData.get("firstname"));
                    user.setLastName((String)userData.get("lastname"));
                    user.setLastLogin((String)userData.get("lastlogin"));
                    user.setPresent((boolean)userData.get("present"));
                    user.setCurrentPresenceId(((Number)userData.get("presence")).intValue());
                    users.add(user);
                }
            }
            userList.addAll(users);
        };
        run.run();
    }

    /**
     * Return the personal user data of the connected device.
     * This method is only usable for the mobile profile.
     * @return Returns the user with the personal data.
     * @throws UserServiceException When incorrect connection profile is used or problem with data parsing.
     */
    public final User getMyData() throws UserServiceException {
        try {
            if(this.connection.getConnectionProfile().equals(Profile.MOBILE)){
                if(me==null){
                    PCCEntityDataHandler rpcDataHandler = this.connection.getJsonHTTPRPC("UserService.getMyData", null, "UserService.getMyData");
                    Map<String,Object> data = (Map<String,Object>)rpcDataHandler.getResult().get("data");
                    User user = new User(((Number)data.get("id")).intValue(), (String)data.get("clientname"));
                    user.setFirstName((String)data.get("firstname"));
                    user.setLastName((String)data.get("lastname"));
                    user.setPresent((boolean)data.get("present"));
                    user.setCurrentPresenceId(((Number)data.get("presence")).intValue());
                    me = user;
                }
                return me;
            } else {
                throw new UserServiceException("Developer info: Only available for MOBILE profile");
            }
        } catch (PCCEntityDataHandlerException ex) {
            throw new UserServiceException("Could not get user data", ex);
        }
    }
    
    /**
     * Loads the initial user list.
     * @throws UserServiceException 
     */
    private void loadInitialUserList() throws UserServiceException {
        if(userList.isEmpty()){
            try {
                handleRPCCommandByResult(this.connection.getJsonHTTPRPC("UserService.getUsers", null, "UserService.getUsers"));
            } catch (PCCEntityDataHandlerException ex) {
                throw new UserServiceException("Problem getting users", ex);
            }
        }
    }
    
    /**
     * Pre loads the users.
     * @throws EntityNotAvailableException When the userlist fails to load.
     */
    @Override
    public void preload() throws EntityNotAvailableException {
        if(!loaded){
            loaded = true;
            try {
                loadInitialUserList();
            } catch (UserServiceException ex) {
                throw new EntityNotAvailableException("Could not preload user list", ex);
            }
        }
    }
    
    @Override
    public void reload() throws EntityNotAvailableException {
        loaded = false;
        userList.clear();
        preload();
    }
    
}
