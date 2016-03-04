/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.macros;

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
 * Class collection for known macros.
 * @author John
 */
public final class MacroService extends Entity implements PCCConnectionNameSpaceRPCListener {

    static {
        Logger.getLogger(MacroService.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Connection interface.
     */
    private PCCConnectionInterface connection;
    
    /**
     * List of known macros.
     */
    private final ObservableArrayListBean<Macro> macroList = new ObservableArrayListBean<>();
    
    /**
     * A read only wrapper for the macro list.
     */
    private final ReadOnlyObservableArrayListBean<Macro> readOnlyMacroList = new ReadOnlyObservableArrayListBean<>(macroList);
    
    /**
     * Creates the macro service.
     * @param connection The server connection.
     */
    public MacroService(PCCConnectionInterface connection){
        this.connection = connection;
    }
    
    @Override
    public void unloadContent() throws EntityNotAvailableException {
        macroList.clear();
    }
    
    /**
     * Returns a observable read only list of macros.
     * @return Returns a bindable list of macros.
     * @throws org.pidome.client.entities.macros.MacroServiceException When the macro service is not available.
     */
    public final ReadOnlyObservableArrayListBean<Macro> getMacroList() throws MacroServiceException {
        return readOnlyMacroList;
    }
    
    /**
     * Initializes a connection listener for the macro service.
     */
    @Override
    protected void initilialize() {
        this.connection.addPCCConnectionNameSpaceListener("MacroService", this);
    }

    /**
     * removes a connection listener for the macro service.
     */
    @Override
    protected void release() {
        this.connection.removePCCConnectionNameSpaceListener("MacroService", this);
    }

    /**
     * Loads the initial macro list.
     * @throws MacroServiceException When not available
     */
    private void loadInitialMacroList() throws MacroServiceException {
        if(macroList.isEmpty()){
            try {
                handleRPCCommandByResult(this.connection.getJsonHTTPRPC("MacroService.getMacros", null, "MacroService.getMacros"));
            } catch (PCCEntityDataHandlerException ex) {
                throw new MacroServiceException("Problem getting macros", ex);
            }
        }
    }
    
    /**
     * Preloads the macros.
     * @throws EntityNotAvailableException When the whole macro entity is unavailable.
     */
    @Override
    public void preload() throws EntityNotAvailableException {
        if(!loaded){
            loaded = true;
            try {
                loadInitialMacroList();
            } catch (MacroServiceException ex) {
                throw new EntityNotAvailableException("Could not preload macro list", ex);
            }
        }
    }

    /**
     * Reloads the macro's
     * @throws EntityNotAvailableException 
     */
    @Override
    public void reload() throws EntityNotAvailableException {
        loaded = false;
        macroList.clear();
        preload();
    }
    
    /**
     * Runs a macro.
     * @param macroId The id of the macro to run.
     * @deprecated Please use the runMacro from the macro object.
     */
    public final void runMacro(int macroId){
        try {
            Map<String,Object> setMacroParams = new HashMap<>();
            setMacroParams.put("id", macroId);
            this.connection.getJsonHTTPRPC("MacroService.runMacro", setMacroParams, "MacroService.runMacro");
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(MacroService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Returns a macro.
     * @param macroId the id of the macro.
     * @return A macro
     * @throws MacroServiceException When a macro can not be found.
     */
    public final Macro getMacro(int macroId) throws MacroServiceException {
        for(Macro macro:macroList){
            if(macro.getMacroId()==macroId){
                return macro;
            }
        }
        return loadSingleMacro(macroId);
    }
    
    /**
     * Loads and returns a single macro.
     * This function is internal only and called when a specific macro does not exist.
     * This is a blocking function!
     * @param macroId
     * @return a macro
     * @throws MacroServiceException When the macro can not be loaded.
     */
    private Macro loadSingleMacro(int macroId) throws MacroServiceException {
        try {
            Map<String,Object> macroData = new HashMap<>();
            macroData.put("id", macroId);
            Macro macro = createMacro((Map<String,Object>)this.connection.getJsonHTTPRPC("MacroService.getMacro", macroData, "MacroService.getMacro").getResult().get("data"));
            return macro;
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(MacroService.class.getName()).log(Level.SEVERE, "Macro with id "+macroId+" not found");
            throw new MacroServiceException("Macro with id " + macroId + " not found");
        }
    }
    
    /**
     * Creates a macro.
     * @param addData macro data.
     * @return A constructed macro.
     */
    private Macro createMacro(Map<String,Object> addData){
        Macro macro = new Macro(this.connection, ((Number)addData.get("id")).intValue());
        macro.setDescription((String)addData.get("description"));
        macro.setName((String)addData.get("name"));
        macro.setFavorite((boolean)addData.get("favorite"));
        macroList.add(macro);
        return macro;
    }
    
    /**
     * Handles a broadcasted command from the presence service.
     * @param rpcDataHandler PCCEntityDataHandler presence broadcast data.
     */
    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        switch(rpcDataHandler.getMethod()){
            case "addMacro":
                Map<String,Object> addData = rpcDataHandler.getParameters();
                createMacro(addData);
            break;
            case "updateMacro":
                Map<String,Object> editData = rpcDataHandler.getParameters();
                for(Macro editMacro:macroList){
                    if(editMacro.getMacroId()== ((Number)editData.get("id")).intValue()){
                        editMacro.setName((String)editData.get("name"));
                        editMacro.setDescription((String)editData.get("description"));
                        editMacro.setFavorite((boolean)editData.get("favorite"));
                        break;
                    }
                }
            break;
            case "deleteMacro":
                int arrPos = -1;
                Map<String,Object> deleteData = rpcDataHandler.getParameters();
                for(Macro deleteMacro:macroList){
                    if(deleteMacro.getMacroId()== ((Number)deleteData.get("id")).intValue()){
                        arrPos = macroList.indexOf(deleteMacro);
                        break;
                    }
                }
                if(arrPos!=-1){
                    macroList.remove(arrPos);
                }
            break;
            case "runMacro":
                Map<String,Object> setData = rpcDataHandler.getParameters();
                for(Macro runMacro:macroList){
                    if(runMacro.getMacroId()== ((Number)setData.get("id")).intValue()){
                        runMacro.running();
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
                List<Macro> macros = new ArrayList<>();
                for( Map<String,Object> macroData: data){
                    Macro macro = new Macro(this.connection, ((Number)macroData.get("id")).intValue());
                    macro.setDescription((String)macroData.get("description"));
                    macro.setName((String)macroData.get("name"));
                    macro.setFavorite((boolean)macroData.get("favorite"));
                    macros.add(macro);
                }
                macroList.addAll(macros);
            } catch (Exception ex){
                Logger.getLogger(MacroService.class.getName()).log(Level.SEVERE, "Problem creating macro list", ex);
            }
        };
        run.run();
    }

}