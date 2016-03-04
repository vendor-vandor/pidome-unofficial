/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.graphdata;

import java.util.ArrayList;

/**
 *
 * @author John
 */
public interface RoundRobinPluginDataInterface {
    
    /**
     * Stores current value.
     * @param dataGroup
     * @param dataName
     * @param data 
     */
    public void store(String dataGroup, String dataName, double data);
    
    /**
     * Returns the requested field.
     * @param dataGroup
     * @param dataName
     * @return
     * @throws Exception 
     */
    public RoundRobinDataGraphItem getStorageField(String dataGroup, String dataName) throws Exception;
    
    /**
     * Returns the totals as from today from 00:00.
     * @param group
     * @param dataName
     * @return 
     */
    public double getTodayTotal(String group, String dataName);
    
    /**
     * Register fields to be stored.
     * @param dataTypes 
     */
    public void registerDataTypes(ArrayList<RoundRobinDataGraphItem> dataTypes);
    
}
