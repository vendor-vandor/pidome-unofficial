/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.interfaces.web.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WebConfigurationOptionSet {

    String description = "";
    String title       = "";
    
    List<WebOption> options = new ArrayList();

    /**
     * Sets the option group description.
     *
     * @param description
     */
    public WebConfigurationOptionSet(String title) {
        this.title = title;
    }

    /**
     * Sets the plugin options description.
     * @param description
     * @return 
     */
    public final void setConfigurationSetDescription(String description){
        this.description = description;
    }
    
    /**
     * Returns the plugins options title.
     * @return 
     */
    public final String getConfigurationSetTitle(){
        return this.title;
    }
    
    /**
     * Returns the options description.
     * @return 
     */
    public final String getConfigurationSetDescription(){
        return this.description;
    }
    
    /**
     * Adds an option to the set.
     *
     * @param option
     */
    public final void addOption(WebOption option) {
        options.add(option);
    }

    /**
     * Returns a list of options.
     * @return 
     */
    public final List<WebOption> getOptions(){
        return Collections.unmodifiableList(this.options);
    }
    
}
