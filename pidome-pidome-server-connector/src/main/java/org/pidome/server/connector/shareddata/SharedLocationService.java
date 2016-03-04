/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.shareddata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John
 */
public class SharedLocationService {
    
    static List<Map<String,Object>> locations = new ArrayList();
    
    static Logger LOG = LogManager.getLogger(SharedLocationService.class);
    
    public static Map<String,Object> getLocation(int locationId) throws Exception {
        for (int j = 0, k = locations.size(); j < k; j++) {
            if (locations.get(j).get("id").equals(locationId)) {
                return locations.get(j);
            }
        }
        LOG.debug("Location id " + locationId + " not found");
        Map<String,Object> defaultUnknown = new HashMap<>();
        defaultUnknown.put("floorname", "Unknown floor");
        defaultUnknown.put("name",      "Unknown room");
        return defaultUnknown;
    }
    
    protected static void setNewLocationCollection(List<Map<String,Object>> locationSet){
        locations = locationSet;
    }
    
}
