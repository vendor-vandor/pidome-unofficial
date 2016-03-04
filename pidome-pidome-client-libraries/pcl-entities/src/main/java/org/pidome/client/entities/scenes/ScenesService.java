/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.scenes;

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
import org.pidome.pcl.utilities.properties.ObservableArrayListBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 *
 * @author John
 */
public final class ScenesService extends Entity implements PCCConnectionNameSpaceRPCListener {

    static {
        Logger.getLogger(ScenesService.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Connection interface.
     */
    private PCCConnectionInterface connection;
    
    /**
     * List of known scenes.
     */
    private final ObservableArrayListBean<Scene> scenesList = new ObservableArrayListBean<>();
    
    /**
     * A read only wrapper for the scenes list.
     */
    private final ReadOnlyObservableArrayListBean<Scene> readOnlyScenesList = new ReadOnlyObservableArrayListBean<>(scenesList);
    
    /**
     * Creates the scenes service.
     * @param connection The server connection.
     */
    public ScenesService(PCCConnectionInterface connection){
        this.connection = connection;
    }
    
    /**
     * Clears the scenes data.
     * @throws EntityNotAvailableException When the scene service is unavailable.
     */
    @Override
    public void unloadContent() throws EntityNotAvailableException {
        scenesList.clear();
    }
    
    /**
     * Returns a observable read only list of scenes.
     * @return Returns a bindable list of scenes.
     * @throws org.pidome.client.entities.scenes.ScenesServiceException When the list is not available.
     */
    public final ReadOnlyObservableArrayListBean<Scene> getScenesList() throws ScenesServiceException {
        return readOnlyScenesList;
    }
    
    /**
     * Initializes a connection listener for the scenes service.
     */
    @Override
    protected void initilialize() {
        this.connection.addPCCConnectionNameSpaceListener("ScenesService", this);
    }

    /**
     * removes a connection listener for the scenes service.
     */
    @Override
    protected void release() {
        this.connection.removePCCConnectionNameSpaceListener("ScenesService", this);
    }

    /**
     * Preloads the scenes service data.
     * @throws EntityNotAvailableException When the scenes service can not be pre-loaded.
     */
    @Override
    public void preload() throws EntityNotAvailableException {
        if(!loaded){
            loaded = true;
            try {
                loadInitialScenesList();
            } catch (ScenesServiceException ex) {
                throw new EntityNotAvailableException("Could not preload scenes list", ex);
            }
        }
    }

    /**
     * Loads the initial macro list.
     * @throws UserServiceException 
     */
    private void loadInitialScenesList() throws ScenesServiceException {
        if(scenesList.isEmpty()){
            try {
                handleRPCCommandByResult(this.connection.getJsonHTTPRPC("ScenesService.getScenes", null, "ScenesService.getScenes"));
            } catch (PCCEntityDataHandlerException ex) {
                throw new ScenesServiceException("Problem getting scenes", ex);
            }
        }
    }
    
    /**
     * Reloads the scene service data.
     * @throws EntityNotAvailableException When the scenes service can not be reloaded.
     */
    @Override
    public void reload() throws EntityNotAvailableException {
        loaded = false;
        scenesList.clear();
        preload();
    }

    /**
     * Activates a scene.
     * @param scene The scene to activate.
     * @deprecated Please use the version within the Scene object
     */
    public final void activateScene(Scene scene){
        try {
            Map<String,Object> setSceneParams = new HashMap<>();
            setSceneParams.put("id", scene.getSceneId());
            this.connection.getJsonHTTPRPC("ScenesService.activateScene", setSceneParams, "ScenesService.activateScene");
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(ScenesService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * De-activates a scene.
     * @param scene The scene to de-activate.
     * @deprecated Please use the version within the Scene object
     */
    public final void deActivateScene(Scene scene){
        try {
            Map<String,Object> setSceneParams = new HashMap<>();
            setSceneParams.put("id", scene.getSceneId());
            this.connection.getJsonHTTPRPC("ScenesService.deActivateScene", setSceneParams, "ScenesService.deActivateScene");
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(ScenesService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Returns a scene.
     * @param sceneId the id of the scene.
     * @return A scene
     * @throws ScenesServiceException When a scene can not be found.
     */
    public final Scene getScene(int sceneId) throws ScenesServiceException {
        for(Scene scene:scenesList){
            if(scene.getSceneId()==sceneId){
                return scene;
            }
        }
        return loadSingleScene(sceneId);
    }
    
    /**
     * Loads and returns a single scene.
     * This function is internal only and called when a specific scene does not exist.
     * This is a blocking function!
     * @param sceneId
     * @return a scene
     * @throws ScenesServiceException When the scene can not be loaded.
     */
    private Scene loadSingleScene(int sceneId) throws ScenesServiceException {
        try {
            Map<String,Object> sceneData = new HashMap<>();
            sceneData.put("id", sceneId);
            Scene scene = createScene((Map<String,Object>)this.connection.getJsonHTTPRPC("ScenesService.getScene", sceneData, "ScenesService.getScene").getResult().get("data"));
            scenesList.add(scene);
            return scene;
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(ScenesService.class.getName()).log(Level.SEVERE, "Scene with id "+sceneId+" not found");
            throw new ScenesServiceException("Scene with id " + sceneId + " not found");
        }
    }
    
    /**
     * Creates a single scene.
     * @param addData
     * @return 
     */
    private Scene createScene(Map<String,Object> addData){
        Scene scene = new Scene(((Number)addData.get("id")).intValue(), this.connection);
        scene.setName((String)addData.get("name"));
        scene.setSceneActive((boolean)addData.get("active"));
        return scene;
    }
    
    /**
     * Handles scenes service broadcasts from the server.
     * @param rpcDataHandler RPC handler with scenes data.
     */
    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        switch(rpcDataHandler.getMethod()){
            case "addScene":
                Map<String,Object> addData = rpcDataHandler.getParameters();
                Scene scene = new Scene(((Number)addData.get("id")).intValue(), this.connection);
                scene.setName((String)addData.get("name"));
                scenesList.add(scene);
            break;
            case "editScene":
                Map<String,Object> editData = rpcDataHandler.getParameters();
                for(Scene editScene:scenesList){
                    if(editScene.getSceneId()== ((Number)editData.get("id")).intValue()){
                        editScene.setName((String)editData.get("name"));
                        break;
                    }
                }
            break;
            case "deleteScene":
                int arrPos = -1;
                Map<String,Object> deleteData = rpcDataHandler.getParameters();
                for(Scene deleteScene:scenesList){
                    if(deleteScene.getSceneId()== ((Number)deleteData.get("id")).intValue()){
                        arrPos = scenesList.indexOf(deleteScene);
                        break;
                    }
                }
                if(arrPos!=-1){
                    scenesList.remove(arrPos);
                }
            break;
            case "activateScene":
                Map<String,Object> setData = rpcDataHandler.getParameters();
                for(Scene runScene:scenesList){
                    if(runScene.getSceneId()== ((Number)setData.get("id")).intValue()){
                        runScene.setSceneActive(true);
                        break;
                    }
                }
            break;
            case "deActivateScene":
                Map<String,Object> unsetData = rpcDataHandler.getParameters();
                for(Scene runScene:scenesList){
                    if(runScene.getSceneId()== ((Number)unsetData.get("id")).intValue()){
                        runScene.setSceneActive(false);
                        break;
                    }
                }
            break;
        }
    }

    /**
     * Handles resulting commands from requests done by the service.
     * @param rpcDataHandler RPC handler with scenes data.
     */
    @Override
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler) {
        ArrayList<Map<String,Object>> data = (ArrayList<Map<String,Object>>)rpcDataHandler.getResult().get("data");
        Runnable run = () -> {
            try {
                List<Scene> scenes = new ArrayList<>();
                for( Map<String,Object> macroData: data){
                    Scene scene = new Scene(((Number)macroData.get("id")).intValue(), this.connection);
                    scene.setName((String)macroData.get("name"));
                    scene.setSceneActive((boolean)macroData.get("active"));
                    scenes.add(scene);
                }
                scenesList.addAll(scenes);
            } catch (Exception ex){
                Logger.getLogger(ScenesService.class.getName()).log(Level.SEVERE, "Problem creating scenes list", ex);
            }
        };
        run.run();
    }
    
}