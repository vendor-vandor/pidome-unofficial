/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.userstatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.Entity;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.users.UserServiceException;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.client.system.PCCConnectionNameSpaceRPCListener;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.utilities.properties.ObjectPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ObservableArrayListBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObjectPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 * Class collection for known user statuses.
 * This class is not used to set or get a single user status. The user status service
 * is used to set the global status. So when the status for example is away all
 * the users are set to away. This service should only be used as a read only
 * class unless you have total control of every user as then this can be used
 * as a convenience class.
 * @author John
 */
public final class UserStatusService extends Entity implements PCCConnectionNameSpaceRPCListener {

    static {
        Logger.getLogger(UserStatusService.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Connection interface.
     */
    private PCCConnectionInterface connection;
    
    /**
     * List of known user statuses.
     */
    private final ObservableArrayListBean<UserStatus> userStatusList = new ObservableArrayListBean<>();
    
    /**
     * A read only wrapper for the user status list.
     */
    private final ReadOnlyObservableArrayListBean<UserStatus> readOnlyUserStatusList = new ReadOnlyObservableArrayListBean<>(userStatusList);
    
    /**
     * Object property holding the current users status.
     */
    private final ObjectPropertyBindingBean<UserStatus> currentUserStatus = new ObjectPropertyBindingBean<>();
    
    /**
     * Creates the user status service.
     * @param connection The server connection.
     */
    public UserStatusService(PCCConnectionInterface connection){
        this.connection = connection;
    }
    
    @Override
    public void unloadContent() throws EntityNotAvailableException {
        userStatusList.clear();
    }
    
    /**
     * Returns a observable read only list of known user statuses.
     * @return Returns a bindable list of statuses known.
     * @throws org.pidome.client.entities.userstatus.UserStatusServiceException When the user status list is not available.
     */
    public final ReadOnlyObservableArrayListBean<UserStatus> getUserStatusList() throws UserStatusServiceException {
        return readOnlyUserStatusList;
    }
    
    /**
     * Returns the current user status wrapped in a property.
     * @return Bindable bean with the current presence.
     */
    public final ReadOnlyObjectPropertyBindingBean<UserStatus> getCurrentPresenceProperty(){
        return currentUserStatus.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Initializes a connection listener for the user status service.
     */
    @Override
    protected void initilialize() {
        this.connection.addPCCConnectionNameSpaceListener("UserStatusService", this);
    }

    /**
     * removes a connection listener for the user status service.
     */
    @Override
    protected void release() {
        this.connection.removePCCConnectionNameSpaceListener("UserStatusService", this);
    }

    /**
     * Loads the initial user status list.
     * @throws UserServiceException 
     */
    private void loadInitialUserStatusList() throws UserStatusServiceException {
        if(userStatusList.isEmpty()){
            try {
                handleRPCCommandByResult(this.connection.getJsonHTTPRPC("UserStatusService.getUserStatuses", null, "UserStatusService.getUserStatuses"));
            } catch (PCCEntityDataHandlerException ex) {
                throw new UserStatusServiceException("Problem getting user statuses", ex);
            }
        }
    }
    
    /**
     * Preloads the user statuses.
     * @throws EntityNotAvailableException When the whole user status entity is unavailable.
     */
    @Override
    public void preload() throws EntityNotAvailableException {
        if(!loaded){
            loaded = true;
            try {
                loadInitialUserStatusList();
            } catch (UserStatusServiceException ex) {
                throw new EntityNotAvailableException("Could not preload user status list", ex);
            }
        }
    }

    @Override
    public void reload() throws EntityNotAvailableException {
        loaded = false;
        currentUserStatus.setValue(new UserStatus(0));
        userStatusList.clear();
        preload();
    }
    
    /**
     * Sets a new global user status.
     * Remember, this sets the status of ALL the users.
     * @param statusId Sets the new user status.
     */
    public final void setUserStatus(int statusId){
        try {
            Map<String,Object> setStatusParams = new HashMap<>();
            setStatusParams.put("id", statusId);
            this.connection.getJsonHTTPRPC("UserStatusService.setUserStatus", setStatusParams, "UserStatusService.setUserStatus");
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(UserStatusService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Handles a broadcasted command from the presence service.
     * @param rpcDataHandler PCCEntityDataHandler presence broadcast data.
     */
    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        switch(rpcDataHandler.getMethod()){
            case "addUserStatus":
                Map<String,Object> addData = rpcDataHandler.getParameters();
                UserStatus userStatus = new UserStatus(((Number)addData.get("id")).intValue());
                userStatus.setDescription((String)addData.get("description"));
                userStatus.setName((String)addData.get("name"));
                userStatusList.add(userStatus);
            break;
            case "updateUserStatus":
                Map<String,Object> editData = rpcDataHandler.getParameters();
                for(UserStatus editUserStatus:userStatusList){
                    if(editUserStatus.getUserStatusId()== ((Number)editData.get("id")).intValue()){
                        editUserStatus.setName((String)editData.get("name"));
                        editUserStatus.setDescription((String)editData.get("description"));
                        break;
                    }
                }
            break;
            case "deleteUserStatus":
                int arrPos = -1;
                Map<String,Object> deleteData = rpcDataHandler.getParameters();
                for(UserStatus deleteMacro:userStatusList){
                    if(deleteMacro.getUserStatusId()== ((Number)deleteData.get("id")).intValue()){
                        arrPos = userStatusList.indexOf(deleteMacro);
                        break;
                    }
                }
                if(arrPos!=-1){
                    userStatusList.remove(arrPos);
                }
            break;
            case "setUserStatus":
                Map<String,Object> setData = rpcDataHandler.getParameters();
                for(UserStatus newStatus:userStatusList){
                    if(newStatus.getUserStatusId()== ((Number)setData.get("id")).intValue()){
                        this.currentUserStatus.setValue(newStatus);
                        break;
                    }
                }
            break;
        }
    }

    /**
     * Handles a command by it's result.
     * @param rpcDataHandler PCCEntityDataHandler presence result data.
     */
    @Override
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler) {
        ArrayList<Map<String,Object>> data = (ArrayList<Map<String,Object>>)rpcDataHandler.getResult().get("data");
        Runnable run = () -> {
            try {
                UserStatus current = null;
                List<UserStatus> statuses = new ArrayList<>();
                for( Map<String,Object> statusData: data){
                    UserStatus status = new UserStatus(((Number)statusData.get("id")).intValue());
                    status.setDescription((String)statusData.get("description"));
                    status.setName((String)statusData.get("name"));
                    statuses.add(status);
                    if((boolean)statusData.get("active")==true){
                        current = status;
                    }
                }
                userStatusList.addAll(statuses);
                if(current!=null){
                    this.currentUserStatus.setValue(current);
                }
            } catch (Exception ex){
                Logger.getLogger(UserStatusService.class.getName()).log(Level.SEVERE, "Problem creating user status list", ex);
            }
        };
        run.run();
    }
    
    
}