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

package org.pidome.server.services.http.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.pidome.server.services.macros.Macro;
import org.pidome.server.services.macros.MacroException;
import org.pidome.server.services.macros.MacroService;

/**
 *
 * @author John
 */
public class MacroServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements MacroServiceJSONRPCWrapperInterface {

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("runMacro", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getMacros", null);
                put("getFavoriteMacros", null);
                put("getMacro", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("deleteMacro", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("saveMacro", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("name", "");}});
                        put(1,new HashMap<String,Object>(){{put("description", "");}});
                        put(2,new HashMap<String,Object>(){{put("favorite", new Boolean(false));}});
                        put(3,new HashMap<String,Object>(){{put("executions", new ArrayList());}});
                    }
                });
                put("updateMacro", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("name", 0L);}});
                        put(2,new HashMap<String,Object>(){{put("description", "");}});
                        put(3,new HashMap<String,Object>(){{put("favorite", new Boolean(false));}});
                        put(4,new HashMap<String,Object>(){{put("executions", new ArrayList());}});
                    }
                });
                put("setFavorite", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("favorite", new Boolean(true));}});
                    }
                });
            }
        };
        return mapping;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object runMacro(Long macroId) throws MacroException {
        return MacroService.runMacro(macroId.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getMacros() {
        Map<Integer, Macro> macroList = MacroService.getMacrosList();
        ArrayList<Map<String,Object>> newList = new ArrayList();
        for(Integer key:macroList.keySet()){
            Map<String,Object> MacroDetail = new HashMap<>();
            MacroDetail.put("id", key);
            MacroDetail.put("name", macroList.get(key).getMacroName());
            MacroDetail.put("description", macroList.get(key).getDescription());
            MacroDetail.put("favorite", macroList.get(key).getIsFavorite());
            MacroDetail.put("executions", macroList.get(key).getActionsCount());
            newList.add(MacroDetail);
        }
        return newList;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getFavoriteMacros() {
        Map<Integer, Macro> macroList = MacroService.getMacrosList();
        ArrayList<Map<String,Object>> newList = new ArrayList();
        for(Integer key:macroList.keySet()){
            if(macroList.get(key).getIsFavorite()){
                Map<String,Object> MacroDetail = new HashMap<>();
                MacroDetail.put("id", key);
                MacroDetail.put("name", macroList.get(key).getMacroName());
                MacroDetail.put("description", macroList.get(key).getDescription());
                MacroDetail.put("executions", macroList.get(key).getActionsCount());
                newList.add(MacroDetail);
            }
        }
        return newList;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getMacro(Long id) throws MacroException {
        Macro macro = MacroService.getMacro(id.intValue());
        Map<String,Object> returnMacro = new HashMap<>();
        returnMacro.put("id", macro.getMacroId());
        returnMacro.put("name", macro.getMacroName());
        returnMacro.put("description", macro.getDescription());
        returnMacro.put("favorite", macro.getIsFavorite());
        returnMacro.put("executions", macro.getActionsList());
        return returnMacro;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object deleteMacro(Long macroId) throws MacroException {
        return MacroService.deleteMacro(macroId.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object saveMacro(String name, String description, Boolean favorite, ArrayList exec) throws MacroException {
        return MacroService.saveMacro(name, description, favorite, exec);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object updateMacro(Long macroId, String name, String description, Boolean favorite, ArrayList exec) throws MacroException {
        return MacroService.saveMacro(macroId.intValue(), name, description, favorite, exec);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object setFavorite(Long macroId, Boolean favorite) throws MacroException {
        return MacroService.setFavorite(macroId.intValue(), favorite);
    }

}
