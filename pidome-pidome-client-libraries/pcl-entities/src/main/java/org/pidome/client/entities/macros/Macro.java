/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.macros;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.utilities.properties.BooleanPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ReadOnlyBooleanPropertyBindingBean;

/**
 * A Single macro.
 * @author John
 */
public class Macro {
    
    /**
     * The macro id.
     */
    private final int macroId;
    
    /**
     * The name of the macro.
     */
    private String macroName;
    
    /**
     * The macro description.
     */
    private String description;
    
    /**
     * If a macro is a favorite macro or not.
     */
    private boolean isFavorite = false;
    
    /**
     * A one shot trigger if the macro is running.
     */
    private BooleanPropertyBindingBean running = new BooleanPropertyBindingBean(false);
    
    /**
     * Connection interface.
     */
    private final PCCConnectionInterface connection;
    
    /**
     * Constructor with macro id.
     * @param connection The connection interface.
     * @param macroId the id of the macro.
     */
    protected Macro(PCCConnectionInterface connection, int macroId){
        this.macroId = macroId;
        this.connection = connection;
    }
    
    /**
     * Runs a macro.
     */
    public final void runMacro(){
        try {
            Map<String,Object> setMacroParams = new HashMap<>();
            setMacroParams.put("id", macroId);
            this.connection.getJsonHTTPRPC("MacroService.runMacro", setMacroParams, "MacroService.runMacro");
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(MacroService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /**
     * Returns the macro id.
     * @return the macro id.
     */
    public final int getMacroId(){
        return this.macroId;
    }
    
    /**
     * Marks a macro favorite or not.
     * @param favorite boolean value true if favorite, false if not.
     */
    protected final void setFavorite(boolean favorite){
        this.isFavorite = favorite;
    }
    
    /**
     * Returns if a macro is favorite or not.
     * @return boolean value true if favorite, false if not.
     */
    public final boolean getIsFavorite(){
        return this.isFavorite;
    }
    
    /**
     * Set a macro name.
     * @param macroName name of the macro.
     */
    protected final void setName(String macroName){
        this.macroName = macroName;
    }
    
    /**
     * Returns the name of the macro.
     * @return Macro name.
     */
    public final String getName(){
        return this.macroName;
    }
    
    /**
     * Set a macro description.
     * @param description name of the macro.
     */
    protected final void setDescription(String description){
        this.description = description;
    }
    
    /**
     * Returns the description of the macro.
     * @return Macro description.
     */
    public final String getDescription(){
        return this.description;
    }
    
    /**
     * Toggles the running parameter delivering a one shot to listeners.
     * This one short toggles between true and false.
     */
    protected final void running(){
        running.setValue(Boolean.TRUE);
        running.setValue(Boolean.FALSE);
    }
 
    /**
     * Returns a read only version of the running object property.
     * This property is a switching property which quickly switches true and false.
     * When a true is broadcasted it is a promise the macro has been started on the 
     * server. After a broadcasted true it always broadcasts a false.
     * False is a default state, you should only listen for true fires which are only indicators.
     * @return A bindable boolean property.
     */
    public final ReadOnlyBooleanPropertyBindingBean getRunning(){
        return this.running.getReadOnlyBooleanPropertyBindingBean();
    }
    
}