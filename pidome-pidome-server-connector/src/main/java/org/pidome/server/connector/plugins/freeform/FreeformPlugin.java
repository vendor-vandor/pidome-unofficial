/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.freeform;

import java.util.Map;
import org.pidome.server.connector.plugins.PluginBase;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;

/**
 *
 * @author John
 */
public abstract class FreeformPlugin extends PluginBase {

    @Override
    public void setConfigurationValues(Map<String, String> configuration) throws WebConfigurationException {
        throw new UnsupportedOperationException("This plugin does not support options");
    }

    @Override
    public void startPlugin() throws PluginException {
        /// Not really used
    }

    @Override
    public void stopPlugin() throws PluginException {
        //// Not really used either
    }
    
    /**
     * This gives the possibility to execute commands from the plugin's web interface.
     * @param function
     * @param values 
     */
    public void handleCustomWebCommand(String function, Map<String,String> values){
        throw new UnsupportedOperationException("This plugin does not support custom commands");
    }
    
    public abstract void prepareWebPresentation();
    
}
