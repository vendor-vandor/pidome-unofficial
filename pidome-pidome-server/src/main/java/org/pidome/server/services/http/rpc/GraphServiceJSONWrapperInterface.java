/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.http.rpc;

import java.util.List;

/**
 *
 * @author John
 */
public interface GraphServiceJSONWrapperInterface {
 
    /**
     * Returns device usages graph data.
     * @param deviceId id of the device.
     * @param groupId id of the data group.
     * @param controlId id of the data control.
     * @param range Array of ranges as string.
     * @param calcTypeParam The type of calculation.
     * @return mapped listing of data.
     */
    public Object getDeviceGraph(Number deviceId, String groupId, String controlId, List<String> range, String calcTypeParam);
    
    /**
     * Returns utility usages graph data.
     * @param pluginId id of the plugin.
     * @param groupId id of the data group.
     * @param controlId id of the data control.
     * @param range Array of ranges as string.
     * @param calcTypeParam The type of calculation.
     * @return mapped listing of data.
     */
    public Object getUtilityGraph(Number pluginId, String groupId, String controlId, List<String> range, String calcTypeParam);
    
    public Object getPluginGraph();
    
}