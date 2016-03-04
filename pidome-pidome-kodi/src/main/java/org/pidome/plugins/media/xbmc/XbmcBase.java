/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.plugins.media.xbmc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.pidome.server.connector.plugins.NonFatalPluginException;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.media.Media;
import org.pidome.server.connector.plugins.media.MediaException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfiguration;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationOptionSet;
import org.pidome.server.connector.interfaces.web.configuration.WebOption;
import org.pidome.server.connector.interfaces.web.configuration.WebOption.WebOptionConfigurationFieldType;

/**
 *
 * @author John Sirach
 */
public abstract class XbmcBase extends Media implements XbmcConnectionListener {

    Map<String,String> configuration = new HashMap<>();
    final XbmcConnection connection = new XbmcConnection();
    
    String checkConnectionMessage = null;
    
    /**
     * Base class for XBMC constructor.
     */
    public XbmcBase(){
        WebConfiguration conf = new WebConfiguration();
        WebConfigurationOptionSet optionSet = new WebConfigurationOptionSet("XBMC connect configurations");
        optionSet.addOption(new WebOption("IPADDRESS", "Ip address", "Ip address", WebOptionConfigurationFieldType.IP_ADDRESS));
        
        WebOption telnet = new WebOption("TELNETPORT", "Telnet Port", "Port for telnet connection",WebOptionConfigurationFieldType.INT);
        telnet.setDefaultValue("9090");
        optionSet.addOption(telnet);
        
        WebOption httpport = new WebOption("HTTPPORT", "HTTP Port", "Port for http connection",WebOptionConfigurationFieldType.INT);
        httpport.setDefaultValue("8080");
        optionSet.addOption(httpport);
        
        optionSet.addOption(new WebOption("USER", "Username", "Fill in if the connection requires a username",WebOptionConfigurationFieldType.STRING));
        optionSet.addOption(new WebOption("PASS", "Password", "Fill in if the connection requires a username and password ",WebOptionConfigurationFieldType.PASSWORD));
        
        WebOption recon = new WebOption("RECONNECT", "Connection timeout", "This timeout is used for checking if xbmc is alive and how long it takes to reconnect in seconds.",WebOptionConfigurationFieldType.INT);
        recon.setDefaultValue("60");
        optionSet.addOption(recon);
        
        conf.addOptionSet(optionSet);
        setConfiguration(conf);
    }
    
    /**
     * Starts the connection monitor.
     * @param message 
     */
    public final void startConnectionCheck(String message){
        checkConnectionMessage = message;
        connection.lastMessageThread(checkConnectionMessage);
    }
    
    /**
     * Sets configuration values.
     * @param configuration
     * @throws WebConfigurationException 
     */
    @Override
    public void setConfigurationValues(Map<String, String> configuration) throws WebConfigurationException {
        this.configuration = configuration;
        try {
            if(this.configuration.get("RECONNECT").equals("")){
                this.configuration.put("RECONNECT","60");
            }
            connection.setConfiguration(this.configuration.get("IPADDRESS"), Integer.parseInt(this.configuration.get("TELNETPORT")), Integer.parseInt(this.configuration.get("HTTPPORT")),Integer.parseInt(this.configuration.get("RECONNECT")),this.configuration.get("USER"),this.configuration.get("PASS"), XbmcConnection.XbmcVersion.VERSION_12);
        } catch (Exception ex) {
            throw new WebConfigurationException(ex.getMessage());
        }
    }
    
    /**
     * Starts the plugin.
     * @throws PluginException 
     * @throws org.pidome.pluginconnector.NonFatalPluginException 
     */
    @Override
    public void startPlugin() throws PluginException,NonFatalPluginException {
        try {
            if(connection!=null){
                connection.addListener(this);
                connection.open();
                synchronized(connection){
                    try {
                        connection.wait();
                        if(connection.isInReconnect()) throw new NonFatalPluginException("Reconnect running");
                    } catch (InterruptedException ex) {
                        
                    }
                }
            } else {
                throw new PluginException("Connection not initialized");
            }
        } catch (IOException ex) {
            throw new PluginException("Could not start XBMC connection: " + ex.getMessage());
        }
    }
    
    /**
     * Stops the plugin.
     * @throws PluginException 
     */
    @Override
    public void stopPlugin() throws PluginException {
        if(connection!=null){
            connection.removeListener(this);
            connection.close();
        }
    }
    
    /**
     * Returns the connection.
     * @return
     * @throws MediaException 
     */
    public final XbmcConnection getConnection() throws MediaException {
        if(this.connection!=null){
            return this.connection;
        } else {
            throw new MediaException("Connection not available");
        }
    }
    
    @Override
    public boolean hasGraphData() {
        return false;
    }
    
}
