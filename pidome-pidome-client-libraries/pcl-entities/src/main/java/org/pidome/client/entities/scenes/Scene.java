/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.scenes;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.utilities.properties.BooleanPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ReadOnlyBooleanPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ReadOnlyStringPropertyBindingBean;
import org.pidome.pcl.utilities.properties.StringPropertyBindingBean;

/**
 *
 * @author John
 */
public final class Scene {
 
    /**
     * Connection interface.
     */
    private final PCCConnectionInterface connection;
    
    /**
     * The scene id.
     */
    private int id = 0;
    
    /**
     * The scene's name in a bindable bean.
     */
    private final StringPropertyBindingBean name = new StringPropertyBindingBean();
    
    /**
     * Bindable boolean holding if the scene is active or not.
     */
    private final BooleanPropertyBindingBean active = new BooleanPropertyBindingBean(false);
    
    /**
     * Construct a scene.
     * @param sceneId The id of the scene.
     * @param connection The server connection interface.
     */
    protected Scene(int sceneId, PCCConnectionInterface connection){
        this.id = sceneId;
        this.connection = connection;
    }
    
    /**
     * Returns the id of the scene.
     * @return scene id.
     */
    public final int getSceneId(){
        return this.id;
    }
    
    /**
     * Set's a scene name.
     * @param sceneName The name of the scene.
     */
    protected final void setName(String sceneName){
        this.name.setValue(sceneName);
    }
    
    /**
     * Returns the bindable scene's name.
     * A listener can be added to listen for name changes.
     * @return A boundable string property of the scene name.
     */
    public final ReadOnlyStringPropertyBindingBean getSceneName(){
        return this.name.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Set's a scene active or not.
     * @param active boolean if the scene is active or not.
     */
    protected final void setSceneActive(boolean active){
        this.active.setValue(active);
    }
    
    /**
     * Reads a readonly bean for scene active determination.
     * A listener can be bind to this bean to listen for current scene status.
     * @return a boundable boolean property if the scene is active or not
     */
    public final ReadOnlyBooleanPropertyBindingBean getSceneActive(){
        return this.active.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * De-activates this scene.
     */
    public final void deActivateScene(){
        try {
            Map<String,Object> setSceneParams = new HashMap<>();
            setSceneParams.put("id", id);
            this.connection.getJsonHTTPRPC("ScenesService.deActivateScene", setSceneParams, "ScenesService.deActivateScene");
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(Scene.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Activates this scene.
     */
    public final void activateScene(){
        try {
            Map<String,Object> setSceneParams = new HashMap<>();
            setSceneParams.put("id", id);
            this.connection.getJsonHTTPRPC("ScenesService.activateScene", setSceneParams, "ScenesService.activateScene");
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(Scene.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}