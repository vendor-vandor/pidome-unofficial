/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.presences;

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
 * Class collection for known presences.
 * @author John
 */
public final class PresenceService extends Entity implements PCCConnectionNameSpaceRPCListener {

    static {
        Logger.getLogger(PresenceService.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Connection interface.
     */
    private PCCConnectionInterface connection;
    
    /**
     * List of known presences.
     */
    private final ObservableArrayListBean<Presence> presenceList = new ObservableArrayListBean<>();
    
    /**
     * A read only wrapper for the presence list.
     */
    private final ReadOnlyObservableArrayListBean<Presence> readOnlyPresenceList = new ReadOnlyObservableArrayListBean<>(presenceList);
    
    /**
     * Object property holding the current presence.
     */
    private final ObjectPropertyBindingBean<Presence> currentPresence = new ObjectPropertyBindingBean<>();
    
    /**
     * Creates the presence service.
     * @param connection The server connection.
     */
    public PresenceService(PCCConnectionInterface connection){
        this.connection = connection;
    }
    
    @Override
    public void unloadContent() throws EntityNotAvailableException {
        presenceList.clear();
        currentPresence.setValue(new Presence(0, ""));
    }
    
    /**
     * Returns the current presence wrapped in a property.
     * @return Bindable bean with the current presence.
     */
    public final ReadOnlyObjectPropertyBindingBean<Presence> getCurrentPresenceProperty(){
        return currentPresence.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Returns a observable read only list of users.
     * When the user list is empty the system will try to a request to fetch the users.
     * @return Returns a bindable list of users present.
     * @throws org.pidome.client.entities.presences.PresenceServiceException When the list fails.
     */
    public final ReadOnlyObservableArrayListBean<Presence> getPresenceList() throws PresenceServiceException {
        return readOnlyPresenceList;
    }
    
    /**
     * Initializes a connection listener for the presence service.
     */
    @Override
    protected void initilialize() {
        this.connection.addPCCConnectionNameSpaceListener("PresenceService", this);
    }

    /**
     * removes a connection listener for the presence service.
     */
    @Override
    protected void release() {
        this.connection.removePCCConnectionNameSpaceListener("PresenceService", this);
    }

    /**
     * Loads the initial presence list.
     * @throws UserServiceException 
     */
    private void loadInitialPresenceList() throws PresenceServiceException {
        if(presenceList.isEmpty()){
            try {
                handleRPCCommandByResult(this.connection.getJsonHTTPRPC("PresenceService.getPresences", null, "PresenceService.getPresences"));
            } catch (PCCEntityDataHandlerException ex) {
                throw new PresenceServiceException("Problem getting presences", ex);
            }
        }
    }
    
    /**
     * Preloads the user presences.
     * @throws EntityNotAvailableException When the whole presence entity is unavailable.
     */
    @Override
    public void preload() throws EntityNotAvailableException {
        if(!loaded){
            loaded = true;
            try {
                loadInitialPresenceList();
            } catch (PresenceServiceException ex) {
                throw new EntityNotAvailableException("Could not preload user list", ex);
            }
        }
    }

    @Override
    public void reload() throws EntityNotAvailableException {
        loaded = false;
        presenceList.clear();
        preload();
    }
    
    /**
     * Sets a new global presence.
     * @param presenceId Sets the new global presence.
     */
    public final void setPresence(int presenceId){
        try {
            Map<String,Object> setPresenceParams = new HashMap<>();
            setPresenceParams.put("id", presenceId);
            this.connection.getJsonHTTPRPC("PresenceService.activateGlobalPresence", setPresenceParams, "PresenceService.activateGlobalPresence");
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(PresenceService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Handles a broadcasted command from the presence service.
     * @param rpcDataHandler PCCEntityDataHandler presence broadcast data.
     */
    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        switch(rpcDataHandler.getMethod()){
            case "addPresence":
                Map<String,Object> addData = rpcDataHandler.getParameters();
                Presence presence = new Presence(((Number)addData.get("id")).intValue(), (String)addData.get("name"));
                presence.setDescription((String)addData.get("description"));
                presence.setLastActivated((String)addData.get("lastactivated"));
                presence.setCurrent(false);
                presenceList.add(presence);
            break;
            case "updatePresence":
                Map<String,Object> editData = rpcDataHandler.getParameters();
                for(Presence editPresence:presenceList){
                    if(editPresence.getPresenceId() == ((Number)editData.get("id")).intValue()){
                        editPresence.setName((String)editData.get("name"));
                        editPresence.setDescription((String)editData.get("description"));
                        break;
                    }
                }
            break;
            case "deletePresence":
                int arrPos = -1;
                Map<String,Object> deleteData = rpcDataHandler.getParameters();
                for(Presence deletePresence:presenceList){
                    if(deletePresence.getPresenceId() == ((Number)deleteData.get("id")).intValue()){
                        arrPos = presenceList.indexOf(deletePresence);
                        break;
                    }
                }
                if(arrPos!=-1){
                    presenceList.remove(arrPos);
                }
            break;
            case "activateGlobalPresence":
                Map<String,Object> setData = rpcDataHandler.getParameters();
                for(Presence setPresence:presenceList){
                    if(setPresence.getPresenceId() == currentPresence.getValue().getPresenceId()){
                        setPresence.setCurrent(false);
                    }
                    if(setPresence.getPresenceId() == ((Number)setData.get("id")).intValue()){
                        setPresence.setCurrent(true);
                        this.currentPresence.setValue(setPresence);
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
                List<Presence> presences = new ArrayList<>();
                if(data!=null){
                    for( Map<String,Object> presenceData: data){
                        Presence presence = new Presence(((Number)presenceData.get("id")).intValue(), (String)presenceData.get("name"));
                        presence.setDescription((String)presenceData.get("description"));
                        presence.setLastActivated((String)presenceData.get("lastactivated"));
                        presence.setCurrent((boolean)presenceData.get("active"));
                        presences.add(presence);
                        if(presence.isCurrent){
                            currentPresence.setValue(presence);
                        }
                    }
                }
                presenceList.addAll(presences);
            } catch (Exception ex){
                Logger.getLogger(PresenceService.class.getName()).log(Level.SEVERE, "Problem creating presence list", ex);
            }
        };
        run.run();
    }
    
    
}