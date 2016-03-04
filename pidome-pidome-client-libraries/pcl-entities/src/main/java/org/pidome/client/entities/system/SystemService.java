/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.system;

import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.Entity;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.system.PCCConnectionException;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.client.system.PCCConnectionNameSpaceRPCListener;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.utilities.parser.jsonrpc.PidomeJSONRPC;
import org.pidome.pcl.utilities.parser.jsonrpc.PidomeJSONRPCException;
import org.pidome.pcl.networking.connections.server.ServerConnection.Profile;

/**
 * The SystemService provider.
 * @author John
 */
public final class SystemService extends Entity implements PCCConnectionNameSpaceRPCListener {
    
    static {
        Logger.getLogger(SystemService.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Connection interface.
     */
    private PCCConnectionInterface connection;
    
    /**
     * Server time object.
     */
    private final ServerTime serverTime = new ServerTime();
    
    /**
     * Constructor.
     * @param connection The server connection.
     */
    public SystemService(PCCConnectionInterface connection){
        this.connection = connection;
    }
    
    @Override
    public void unloadContent() throws EntityNotAvailableException {
        /// no needed
    }
    
    /**
     * Returns the server time object including it's properties.
     * @return The server time object.
     */
    public final ServerTime getServerTime(){
        return serverTime;
    }
    
    /**
     * Initializes.
     */
    @Override
    protected void initilialize() {
        this.connection.addPCCConnectionNameSpaceListener("SystemService", this);
    }
    
    /**
     * Releases any information.
     */
    @Override
    protected void release() {
        this.connection.removePCCConnectionNameSpaceListener("SystemService", this);
        this.connection = null;
    }

    /**
     * Handles an RPC method assigned to this service.
     * @param rpcDataHandler PCCEntityDataHandler.
     */
    @Override
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler) {
        try{
            switch((String)rpcDataHandler.getId()){
                case "SystemService.getCurrentTime":
                    serverTime.handleTimeUpdate((Map<String,Object>)rpcDataHandler.getResult().get("data"));
                break;
                case "SystemService.getLocaleSettings":
                    if(this.connection.getConnectionProfile()==Profile.FIXED){
                        Map<String,String> dataSet = (Map<String,String>)rpcDataHandler.getResult().get("data");
                        Locale.setDefault(Locale.forLanguageTag(dataSet.get("locale")));
                        TimeZone.setDefault(TimeZone.getTimeZone(dataSet.get("timezone")));
                    }
                break;
            }
        } catch (Exception ex){
            Logger.getLogger(SystemService.class.getName()).log(Level.SEVERE, "Got problem", ex);
        }
    }

    /**
     * Handle a RPC broadcast.
     * @param rpcDataHandler PCCEntityDataHandler.
     */
    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        try{
            switch(rpcDataHandler.getMethod()){
                case "time":
                    try {
                        serverTime.handleTimeUpdate((Map<String,Object>)rpcDataHandler.getParameters());
                    } catch (Exception ex){
                        Logger.getLogger(SystemService.class.getName()).log(Level.SEVERE, "Could not handle time update", ex);
                    }
                break;
            }
        } catch (NullPointerException ex){
            Logger.getLogger(SystemService.class.getName()).log(Level.FINE, "Got problem", ex);
        }
    }

    @Override
    public final void preload() throws EntityNotAvailableException {
        if(!loaded){
            loaded = true;
            try {
                this.connection.sendData(PidomeJSONRPC.createExecMethod("SystemService.getLocaleSettings", "SystemService.getLocaleSettings", null));
                this.connection.sendData(PidomeJSONRPC.createExecMethod("SystemService.getCurrentTime", "SystemService.getCurrentTime", null));
            } catch (PCCConnectionException | PidomeJSONRPCException ex) {
                Logger.getLogger(SystemService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void reload() throws EntityNotAvailableException {
        loaded = false;
        preload();
    }
    
    private static Locale getLocaleFromString(String localeString){
        if (localeString == null){
            return null;
        }
        localeString = localeString.trim();
        if (localeString.toLowerCase().equals("default")){
            return Locale.getDefault();
        }

        // Extract language
        int languageIndex = localeString.indexOf('_');
        String language = null;
        if (languageIndex == -1){
            // No further "_" so is "{language}" only
            return new Locale(localeString, "");
        } else {
            language = localeString.substring(0, languageIndex);
        }

        // Extract country
        int countryIndex = localeString.indexOf('_', languageIndex + 1);
        String country = null;
        if (countryIndex == -1) {
            // No further "_" so is "{language}_{country}"
            country = localeString.substring(languageIndex+1);
            return new Locale(language, country);
        } else {
            // Assume all remaining is the variant so is "{language}_{country}_{variant}"
            country = localeString.substring(languageIndex+1, countryIndex);
            String variant = localeString.substring(countryIndex+1);
            return new Locale(language, country, variant);
        }
    }
    
}