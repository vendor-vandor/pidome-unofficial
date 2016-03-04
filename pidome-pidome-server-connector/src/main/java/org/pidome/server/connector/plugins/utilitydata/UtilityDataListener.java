/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.utilitydata;

import java.util.Map;
import org.pidome.server.connector.plugins.utilitydata.UtilityDataInterface.Type;

/**
 *
 * @author John
 */
public interface UtilityDataListener {
    
    public void handleUtilityData(final UtilityData plugin, final Type type, final Map<String,Map<String,Object>> value);
    
}
