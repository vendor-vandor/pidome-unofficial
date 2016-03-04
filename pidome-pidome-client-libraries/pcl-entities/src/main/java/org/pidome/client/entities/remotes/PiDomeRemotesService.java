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

package org.pidome.client.entities.remotes;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.pidome.pcl.utilities.properties.ObservableArrayListBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 *
 * @author John
 */
public final class PiDomeRemotesService extends Entity implements PCCConnectionNameSpaceRPCListener {

    static {
        Logger.getLogger(PiDomeRemotesService.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * List of known presences.
     */
    private final ObservableArrayListBean<PiDomeRemote> remotes = new ObservableArrayListBean<>();
    
    /**
     * A read only wrapper for the presence list.
     */
    private final ReadOnlyObservableArrayListBean<PiDomeRemote> readOnlyRemotes = new ReadOnlyObservableArrayListBean<>(remotes);
    
    /**
     * Connection interface.
     */
    private PCCConnectionInterface connection;
    
    /**
     * Creates the presence service.
     * @param connection The server connection.
     */
    public PiDomeRemotesService(PCCConnectionInterface connection){
        this.connection = connection;
    }
    
    public static boolean emptyList(){
        /// Sometimes failing to load, force refreshes.
        return true;
    }
    
    private void createRemotes(ArrayList<HashMap<String,Object>> remotes){
        for (Map<String,Object> remoteData:remotes){
            createRemote(remoteData);
        };
    }
    
    private void createRemote(Map<String,Object> remote){
        remotes.add(new PiDomeRemote(connection, remote));
    }
    
    public ReadOnlyObservableArrayListBean<PiDomeRemote> getRemotes(){
        return this.readOnlyRemotes;
    }

    @Override
    protected void initilialize() {
        this.connection.addPCCConnectionNameSpaceListener("RemotesService", this);
    }

    @Override
    protected void release() {
        this.connection.removePCCConnectionNameSpaceListener("RemotesService", this);
    }

    @Override
    public void preload() throws EntityNotAvailableException {
        remotes.clear();
        try {
            handleRPCCommandByResult(this.connection.getJsonHTTPRPC("RemotesService.getRemotes", null, "RemotesService.getRemotes"));
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(PiDomeRemotesService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void reload() throws EntityNotAvailableException {
        unloadContent();
        preload();
    }

    @Override
    public void unloadContent() throws EntityNotAvailableException {
        remotes.clear();
    }

    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        /// Not yet.
    }

    @Override
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler) {
        ArrayList<Map<String,Object>> data = (ArrayList<Map<String,Object>>)rpcDataHandler.getResult().get("data");
        Runnable run = () -> {
            try {
                List<PiDomeRemote> remoteSet = new ArrayList<>();
                if(data!=null){
                    for( Map<String,Object> remoteData: data){
                        PiDomeRemote remote = new PiDomeRemote(connection, remoteData);
                        remoteSet.add(remote);
                    }
                }
                remotes.addAll(remoteSet);
            } catch (Exception ex){
                Logger.getLogger(PiDomeRemotesService.class.getName()).log(Level.SEVERE, "Problem creating presence list", ex);
            }
        };
        run.run();
    }
    
}