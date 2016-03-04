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

package org.pidome.server.services.scenes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCUtils;
import org.pidome.server.services.ServiceInterface;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.system.db.DB;

/**
 *
 * @author John
 */
public final class ServerScenes implements ServiceInterface {

    static Logger LOG = LogManager.getLogger(ServerScenes.class);
    
    public enum SceneType {
        /**
         * A program makes sure devices are not modified by external factors.
         */
        PROGRAM,
        /**
         * A one shot scene just runs the scene settings and returns.
         */
        ONE_SHOT
    }
    
    List<ServerScene> scenes = new ArrayList<>();
    
    /**
     * Are we running or not.
     */
    private boolean isAlive = false;
    
    private static ServerScenes me;
    
    private ServerScenes(){}
    
    public static ServerScenes getInstance(){
        if(me==null){
            me = new ServerScenes();
        }
        return me;
    }
    
    @Override
    public void interrupt() {
        isAlive = false;
    }

    /**
     * Loads available scenes.
     * @throws ServerScenesException 
     */
    private void loadScenes() throws ServerScenesException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
            try (Statement statementScenes = fileDBConnection.createStatement();
                    ResultSet rsScenes = statementScenes.executeQuery("SELECT id,name,description,dependencies FROM scenes")) {
                while (rsScenes.next()) {
                    ServerScene scene = new ServerScene(rsScenes.getInt("id"), rsScenes.getString("name"));
                    scene.setDescription(rsScenes.getString("description"));
                    JSONParser jsonParser = new JSONParser();
                    try {
                        scene.setDependencies((ArrayList)jsonParser.parse(rsScenes.getString("dependencies")));
                    } catch (ParseException ex) {
                        LOG.error("Could not create dependency set for macro '{}': {}", rsScenes.getString("name"), ex.getMessage(), ex);
                    }
                    scenes.add(scene);
                }
            } catch (SQLException ex) {
                throw new ServerScenesException("Could not load scenes: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            throw new ServerScenesException("Could not load scenes: "+ ex.getMessage());
        }
    }
    
    public final List<ServerScene> getScenes(){
        return this.scenes;
    }
    
    /**
     * Saves a new scene.
     * @param name
     * @param description
     * @param dependencies
     * @throws ServerScenesException 
     */
    public final boolean saveScene(final String name, final String description, final ArrayList dependencies) throws ServerScenesException {
        try {
            LOG.debug("Saving scene '{}'", name);
            String executes = PidomeJSONRPCUtils.getParamCollection(dependencies);
            try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
                PreparedStatement prep;
                prep = fileDBConnection.prepareStatement("insert into 'scenes' ('name', 'description', 'dependencies','created','modified') values (?,?,?,datetime('now'),datetime('now'))",Statement.RETURN_GENERATED_KEYS);
                prep.setString(1, name);
                prep.setString(2, description);
                prep.setString(3, executes);
                prep.execute();
                final int auto_id;
                try (ResultSet rs = prep.getGeneratedKeys()) {
                    if (rs.next()) {
                        auto_id = rs.getInt(1);
                    } else {
                        auto_id = 0;
                    }
                }
                prep.close();
                Map<String, Object> sendObject = new HashMap<String, Object>() {
                    {
                        put("id", auto_id);
                        put("name", name);
                    }
                };
                ServerScene scene = new ServerScene(auto_id, name);
                scene.setDescription(description);
                scene.setDependencies(dependencies);
                scenes.add(scene);
                ClientMessenger.send("SceneService","addScene", 0, sendObject);
            } catch (SQLException ex) {
                LOG.error("could not save scene: {} ", ex.getMessage(), ex);
                throw new ServerScenesException("could not save scene: "+ ex.getMessage());
            }
            LOG.info("Added scene: {}", name);
            return true;
        } catch (PidomeJSONRPCException ex) {
            LOG.error("could not save scene: {} ", ex.getMessage(), ex);
            throw new ServerScenesException("could not save scene: "+ ex.getMessage());
        }
    }
    
    public final boolean editScene(final int id, final String name, final String description, final ArrayList dependencies) throws ServerScenesException {
        try {
            LOG.debug("Updating scene {}, {}", id,name);
            String executes = PidomeJSONRPCUtils.getParamCollection(dependencies);
            try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
                PreparedStatement prep;
                prep = fileDBConnection.prepareStatement("update 'scenes' set 'name'=?, 'description'=?, 'dependencies'=?,'modified'=datetime('now') where id=?");
                prep.setString(1, name);
                prep.setString(2, description);
                prep.setString(3, executes);
                prep.setInt(4, id);
                prep.executeUpdate();
                prep.close();
                Map<String, Object> sendObject = new HashMap<String, Object>() {
                    {
                        put("id", id);
                        put("name", name);
                    }
                };
                ClientMessenger.send("SceneService","editScene", 0, sendObject);
                updateSceneDetails(id, name, description,dependencies);
            } catch (SQLException ex) {
                LOG.error("could not save category: {} ", ex.getMessage());
                throw new ServerScenesException("could not edit category: "+ ex.getMessage());
            }
            return true;
        } catch (PidomeJSONRPCException ex) {
            LOG.error("could not update scene: {} ", ex.getMessage(), ex);
            throw new ServerScenesException("could not update scene: "+ ex.getMessage());
        }
    }
    
    /**
     * Deletes a scene and deactivates it if active.
     * @param sceneId
     * @return 
     */
    public final boolean deleteScene(int sceneId){
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM 'scenes' WHERE id=?")) {
            prep.setInt(1, sceneId);
            prep.executeUpdate();
        } catch (SQLException ex) {
            LOG.error("Could not delete scene {}", sceneId);
            return false;
        }
        try {
            unsetScene(sceneId);
        } catch (ServerScenesException ex) {
            LOG.warn("Id not found in existing list: {}: {}", sceneId, ex.getMessage(), ex);
        }
        Iterator<ServerScene> sceneSet = scenes.iterator();
        ServerScene delete = null;
        while(sceneSet.hasNext()){
            ServerScene scene = sceneSet.next();
            if(scene.getSceneId() == sceneId){
                delete = scene;
            }
        }
        if(delete!=null){
            scenes.remove(delete);
        }
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("id", sceneId);
            }
        };
        ClientMessenger.send("SceneService","deleteScene", 0, sendObject);
        LOG.info("Deleted scene: {}", sceneId);
        return true;
    }
    
    /**
     * Updates the details of the given sceneId, checks if it is alive and does the needed swapping if so.
     * @param sceneId
     * @param name
     * @param description
     * @param newDependencySet 
     */
    private void updateSceneDetails(int sceneId, String name, String description, ArrayList newDependencySet){
        Iterator<ServerScene> sceneSet = scenes.iterator();
        while(sceneSet.hasNext()){
            ServerScene scene = sceneSet.next();
            if(scene.getSceneId() == sceneId){
                scene.setSceneName(name);
                scene.setDescription(description);
                scene.setDependencies(newDependencySet);
                LOG.info("Updated scene: {}", name);
                swapScenesNiceIfActive(scene);
            }
        }
    }
    
    /**
     * Swaps scenes a nice way if the given scene id is the current scene.
     * The nice way is to take into account of old devices are included in the newly configured scene. Instead of
     * turning of the old configuration before activating a new configuration they overlap the swap so stored last known commands
     * are not being executed if they are included in the new configuration. This function also checks if there currently are
     * scenes active which could possibly have overlapped locations.
     * @param scene
     * @return 
     */
    private boolean swapScenesNiceIfActive(ServerScene scene){
        if(scene.isActive()){
            Runnable run = () -> {
                // Get all current scene inclusions.
                // Do not run the last known commands of the old scene if a device is known in both the old and the new scene.
                List<DeviceSceneInclusion> newInclusions = scene.getDependencies();
                List<DeviceSceneInclusion> toRemove = new ArrayList<>();
                for(ServerScene oldScene:getOverlappingScenesCollection(scene)){
                    for(DeviceSceneInclusion oldOne:oldScene.getDependencies()){
                        boolean found = false;
                        for(DeviceSceneInclusion newOne:newInclusions){
                            if(newOne.getDeviceId() == oldOne.getDeviceId() && 
                               newOne.getGroupId().equals(oldOne.getGroupId()) && 
                               newOne.getControlId().equals(oldOne.getControlId())){
                                found = true;
                                LOG.debug("Device id {} found in new scene, not unsetting", oldOne.getDeviceId());
                            }
                        }
                        if(found == false){
                            LOG.debug("Device id {} not found in new scene, must be unset", oldOne.getDeviceId());
                            toRemove.add(oldOne);
                        }
                    }
                    if(oldScene!=scene){
                        LOG.info("(Edit action) De-activating scene '{}' because of location overlap with {}", oldScene.getSceneName(), scene.getSceneName());
                        oldScene.setActive(false);
                        try {
                            Map<String, Object> sendObject = new HashMap<String, Object>() {
                                {
                                    put("id", oldScene.getSceneId());
                                }
                            };
                            ClientMessenger.send("ScenesService","deActivateScene", 0, sendObject);
                        } catch (Exception ex){
                            //// Could not send out scene de-activation.
                        }
                    }
                }
                newInclusions.stream().forEach((setInclusion) -> {
                    setInclusion.runInclusionActivate();
                });
                toRemove.stream().forEach((unsetInclusion) -> {
                    unsetInclusion.runInclusionDeActivate();
                });
            };
            run.run();
        }
        return true;
    }
    
    /**
     * Activates a scene which does not have overlapping active other scene locations.
     * @param scene
     * @return 
     */
    private boolean activateScene(ServerScene scene){
        for(DeviceSceneInclusion setInclusion:scene.getDependencies()){
            setInclusion.runInclusionActivate();
        }
        scene.setActive(true);
        try {
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", scene.getSceneId());
                }
            };
            ClientMessenger.send("ScenesService","activateScene", 0, sendObject);
        } catch (Exception ex){
            //// Could not send out scene de-activation.
        }
        return true;
    }
    
    /**
     * Swaps scenes a nice way if there are overlapping locations.
     * The nice way is to take into account of old devices are included in the newly selected scene. Instead of
     * turning of the old selected scene before activating a new one they overlap. This way the stored last known commands
     * are not being executed if they are included in the new configuration.
     * @param sceneId
     * @param oldInclusions
     * @return 
     */
    private boolean swapScenesNiceSameLocationSets(final ServerScene newScene, ArrayList<ServerScene> oldScenes){
        Runnable run = () -> {
            List<DeviceSceneInclusion> newInclusions = newScene.getDependencies();
            List<DeviceSceneInclusion> toRemove = new ArrayList<>();
            for(ServerScene oldScene:oldScenes){
                LOG.info("De-activating scene '{}' because of location overlap with {}", oldScene.getSceneName(), newScene.getSceneName());
                for(DeviceSceneInclusion oldOne:oldScene.getDependencies()){
                    boolean found = false;
                    for(DeviceSceneInclusion newOne:newInclusions){
                        if(newOne.getDeviceId() == oldOne.getDeviceId() &&
                           newOne.getGroupId().equals(oldOne.getGroupId()) &&
                           newOne.getControlId().equals(oldOne.getControlId())){
                            found = true;
                            LOG.debug("Device id {} found in new scene configuration, not unsetting", oldOne.getDeviceId());
                        }
                    }
                    if(found == false){
                        LOG.debug("DeviceInclusion with device id {} not found in new scene, added to original state list for unsetting", oldOne.getDeviceId());
                        toRemove.add(oldOne);
                    }
                }
                oldScene.setActive(false);
                try {
                    Map<String, Object> sendObject = new HashMap<String, Object>() {
                        {
                            put("id", oldScene.getSceneId());
                        }
                    };
                    ClientMessenger.send("ScenesService","deActivateScene", 0, sendObject);
                } catch (Exception ex){
                    //// Could not send out scene de-activation.
                }
            }
            newInclusions.stream().forEach((setInclusion) -> {
                setInclusion.runInclusionActivate();
            });
            toRemove.stream().forEach((unsetInclusion) -> {
                unsetInclusion.runInclusionDeActivate();
            });
            newScene.setActive(true);
            try {
                Map<String, Object> sendObject = new HashMap<String, Object>() {
                    {
                        put("id", newScene.getSceneId());
                    }
                };
                ClientMessenger.send("ScenesService","activateScene", 0, sendObject);
            } catch (Exception ex){
                //// Could not send out scene de-activation.
            }
        };
        run.run();
        return true;
    }
    
    public final boolean unsetScene(int sceneId) throws ServerScenesException {
        ServerScene oldScene = this.getScene(sceneId);
        LOG.info("De-activating scene '{}'", oldScene.getSceneName());
        if(oldScene.isActive()){
            oldScene.setActive(false);
            for(DeviceSceneInclusion inclu:oldScene.getDependencies()){
                inclu.runInclusionDeActivate();
            }
            try {
                Map<String, Object> sendObject = new HashMap<String, Object>() {
                    {
                        put("id", oldScene.getSceneId());
                    }
                };
                ClientMessenger.send("ScenesService","deActivateScene", 0, sendObject);
            } catch (Exception ex){
                //// Could not send out scene de-activation.
            }
        }
        return true;
    }
    
    /**
     * Sets the given scene id.
     * A scene can be activated multiple times but does not need to be de-activated multiple times.
     * @param id
     * @return
     * @throws ServerScenesException 
     */
    public final boolean setScene(int id) throws ServerScenesException {
        ServerScene newScene = this.getScene(id);
        LOG.info("Activating scene: {}", newScene.getSceneName());
        ArrayList<ServerScene> overlappingOld = getOverlappingScenesCollection(newScene);
        if(!overlappingOld.isEmpty()){
            swapScenesNiceSameLocationSets(newScene, overlappingOld);
        } else {
            return activateScene(newScene);
        }
        return true;
    }
    
    /**
     * Returns a list of current active scenes which have a location overlap with the given scene.
     * @param newScene The scene given to check overlaps with.
     * @return 
     */
    private ArrayList<ServerScene> getOverlappingScenesCollection(ServerScene newScene){
        ArrayList<ServerScene> deactivateScenes = new ArrayList<>();
        for(ServerScene currentScene:this.scenes){
            if(currentScene.isActive()){
                for(int oldLocationId:currentScene.getLocationIdsFromDependencies()){
                    if(newScene.getLocationIdsFromDependencies().contains(oldLocationId)){
                        deactivateScenes.add(currentScene);
                        break;
                    }
                }
            }
        }
        return deactivateScenes;
    }
    
    public final ServerScene getScene(int sceneId) throws ServerScenesException {
        Iterator<ServerScene> sceneSet = scenes.iterator();
        while(sceneSet.hasNext()){
            ServerScene scene = sceneSet.next();
            if(scene.getSceneId()==sceneId){
                return scene;
            }
        }
        throw new ServerScenesException("Scene id "+sceneId+" not found");
    }
    
    /**
     * Starts the service.
     */
    @Override
    public void start() {
        isAlive = true;
        try {
            loadScenes();
        } catch (ServerScenesException ex) {
            LOG.error("Could not load scenes: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Checks if the service is alive.
     * @return 
     */
    @Override
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * Returns the service name.
     * @return 
     */
    @Override
    public String getServiceName() {
        return "ServerScenes";
    }
    
}