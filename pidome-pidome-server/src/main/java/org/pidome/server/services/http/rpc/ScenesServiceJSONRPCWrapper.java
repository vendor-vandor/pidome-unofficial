/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.http.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.pidome.server.services.scenes.ServerScene;
import org.pidome.server.services.scenes.ServerScenes;
import org.pidome.server.services.scenes.ServerScenesException;

/**
 *
 * @author John
 */
public final class ScenesServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements ScenesServiceJSONRPCWrapperInterface {

    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getScenes", null);
                put("getScene", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("saveScene", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("name", "");}});
                        put(1,new HashMap<String,Object>(){{put("description", "");}});
                        put(2,new HashMap<String,Object>(){{put("dependencies", new ArrayList<>());}});
                    }
                });
                put("editScene", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("name", "");}});
                        put(2,new HashMap<String,Object>(){{put("description", "");}});
                        put(3,new HashMap<String,Object>(){{put("dependencies", new ArrayList<>());}});
                    }
                });
                put("deleteScene", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("activateScene", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("deActivateScene", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
            }
        };
        return mapping;
    }

    @Override
    public Object getScenes() {
        List<Map<String,Object>> scenes = new ArrayList<>();
        List<ServerScene> list = ServerScenes.getInstance().getScenes();
        for(ServerScene scene:list){
            Map<String,Object> sceneMap = new HashMap<>();
            sceneMap.put("id", scene.getSceneId());
            sceneMap.put("name", scene.getSceneName());
            sceneMap.put("description", scene.getDescription());
            sceneMap.put("active", scene.isActive());
            sceneMap.put("locations", scene.getStringLocationFromDependencies());
            scenes.add(sceneMap);
        }
        return scenes;
    }

    @Override
    public Object getScene(Number id) throws ServerScenesException {
        ServerScene scene = ServerScenes.getInstance().getScene(id.intValue());
        Map<String,Object> returnScene = new HashMap<>();
        returnScene.put("id", scene.getSceneId());
        returnScene.put("name", scene.getSceneName());
        returnScene.put("description", scene.getDescription());
        returnScene.put("active", scene.isActive());
        returnScene.put("dependencies", scene.getPlainDependenciesArrayList());
        return returnScene;
    }

    @Override
    public Object saveScene(String name, String description, ArrayList dependencies) throws ServerScenesException {
        ServerScenes.getInstance().saveScene(name, description, dependencies);
        return true;
    }

    @Override
    public Object editScene(Number id, String name, String description, ArrayList dependencies) throws ServerScenesException {
        ServerScenes.getInstance().editScene(id.intValue(), name, description, dependencies);
        return true;
    }

    @Override
    public Object deleteScene(Number id) throws ServerScenesException {
        ServerScenes.getInstance().deleteScene(id.intValue());
        return true;
    }

    @Override
    public Object activateScene(Number id) throws ServerScenesException {
        ServerScenes.getInstance().setScene(id.intValue());
        return true;
    }

    @Override
    public Object deActivateScene(Number id) throws ServerScenesException {
        ServerScenes.getInstance().unsetScene(id.intValue());
        return true;
    }
    
}