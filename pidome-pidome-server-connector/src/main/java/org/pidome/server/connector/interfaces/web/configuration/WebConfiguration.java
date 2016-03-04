/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.interfaces.web.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Media configuration.
 * This contains groups of options.
 * @author John Sirach
 */
public final class WebConfiguration {

    List<WebConfigurationOptionSet> options = new ArrayList();
    
    public final void addOptionSet(WebConfigurationOptionSet optionSet){
        options.add(optionSet);
    }    
    
    public final List<WebConfigurationOptionSet> getOptions (){
        return Collections.unmodifiableList(options);
    }
    
}
    
