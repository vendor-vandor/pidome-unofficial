/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.interfaces.web.configuration.WebConfiguration;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.plugins.plugindata.PluginDataException;
import org.pidome.server.connector.plugins.plugindata.PluginDataSet;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentationGroup;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentationGroups;
import org.pidome.server.connector.plugins.graphdata.RoundRobinDataGraphItem;
import org.pidome.server.connector.plugins.graphdata.RoundRobinPluginDataInterface;

/**
 *
 * @author John Sirach
 */
public abstract class PluginBase {
    
    int pluginId = 0;
    String baseName = "";
    String pluginName = "";
    String pluginDescription = "";
    String pluginPath = "";
    String pluginLocation = "";
    int installedPluginId = 0;
    int pluginLocationId = 1;
    /**
     * Favorite setting.
     */
    boolean isFavorite = false;
    
    boolean running = false;
    
    static Map<String,String> pluginXML = new HashMap<>();
    
    static Logger LOG = LogManager.getLogger(PluginBase.class);
    
    private final WebPresentationGroups presentation = new WebPresentationGroups();
    
    /**
     * Data fields.
     */
    private RoundRobinPluginDataInterface serverGraphLink;
    
    /**
     * Holds the configuration set by the plugins constructor.
     */
    WebConfiguration pluginConfiguration;
    
    /**
     * Holds the data set for the plugin.
     */
    PluginDataSet pluginDataSet = new PluginDataSet();
    
    String customPluginData = "";
    
    public final void registerGraphDataTypes(ArrayList<RoundRobinDataGraphItem> dataTypes){
        serverGraphLink.registerDataTypes(dataTypes);
    }
    
    /**
     * Links to the graph storage.
     * @param link 
     */
    public final void setGraphReceiver(RoundRobinPluginDataInterface link){
        serverGraphLink = link;
    }
    
    /**
     * Returns if there is a link to a graph receiver.
     * @return 
     */
    public final boolean hasGraphReceiver(){
        return serverGraphLink!=null;
    }
    
    /**
     * Removes a link.
     */
    public final void removeGraphReceiver(){
        serverGraphLink=null;
    }
    
    /**
     * Returns the current receiver.
     * @return 
     */
    public final RoundRobinPluginDataInterface getGraphReceiver(){
        return this.serverGraphLink;
    }
    
    /**
     * Returns the total from the graph based on today.
     * @param field
     * @return 
     */
    public final double getTodayGraphTotal(String group, String dataName){
        return this.serverGraphLink.getTodayTotal(group, dataName);
    }
    
    /**
     * Stores a value for a graph.
     * @param group
     * @param name
     * @param value 
     */
    public final void storeGraphData(String group, String name, double value){
        if(serverGraphLink!=null) {
            this.serverGraphLink.store(group, name, value);
        }
    }
    
    /**
     * Sets id
     * @param id 
     */
    public final void setPluginId(int id){
        if(pluginId==0) pluginId = id;
    }

    /**
     * Sets the plugin installed id.
     * @param installedId 
     */
    public final void setInstalledId(int installedId){
        if(this.installedPluginId==0)this.installedPluginId = installedId;
    }

    /**
     * Returns the plugin's installed id.
     * @return 
     */
    public final int getInstalledId(){
        return this.installedPluginId;
    }
    
    /**
     * Return if favorite.
     * @return 
     */
    public final boolean getIsFavorite(){
        return this.isFavorite;
    }
    
    /**
     * Sets favorite status.
     * @param favorite 
     */
    public final void setIsFavorite(boolean favorite){
        this.isFavorite = favorite;
    }
    
    /**
     * Sets the plugin base name.
     * @param baseName 
     */
    public final void setBaseName(String baseName){
        if(this.baseName.isEmpty())this.baseName = baseName;
    }
    
    /**
     * Sets name
     * @param name 
     */
    public final void setPluginName(String name){
        pluginName = name;
    }
    
    /**
     * Returns the name.
     * @return 
     */
    public String getName() {
        return this.baseName;
    }
    
    /**
     * Returns the name.
     * @return 
     */
    public String getFriendlyName() {
        return this.pluginName;
    }
    
    /**
     * Sets description. 
     * @param description
     */
    public final void setPluginDescription(String description){
        pluginDescription = description;
    }
    
    /**
     * Sets class path.
     * @param path 
     */
    public final void setPluginPath(String path){
        if(this.pluginPath.isEmpty())pluginPath = path;
    }
    
    /**
     * Sets location name.
     * @param location 
     */
    public final void setPluginLocation(String location){
        pluginLocation = location;
    }
 
    /**
     * The location id.
     * @param location 
     */
    public final void setPluginLocationId(int location){
        pluginLocationId = location;
    }
    
    /**
     * Sets running to true.
     * @param running 
     */
    public final void setRunning(boolean running){
        this.running = running;
    }
    
    /**
     * Returns if running.
     * @return 
     */
    public final boolean getRunning(){
        return this.running;
    }
    
    /**
     * gets the plugin id 
     * @return 
     */
    public final int getPluginId(){
        return pluginId;
    }

    /**
     * gets the base name. 
     * @return 
     */
    public final String getBaseName(){
        return this.baseName;
    }
    
    /**
     * gets the plugin name 
     * @return 
     */
    public final String getPluginName(){
        return pluginName;
    }
    
    /**
     * Gets description.
     * @return 
     */
    public final String getPluginDescription(){
        return this.pluginDescription;
    }
    
    /**
     * gets the plugin path 
     * @return 
     */
    public final String getPluginPath(){
        return pluginPath;
    }
    
    /**
     * gets the plugin location. 
     * @return 
     */
    public final String getPluginLocation(){
        return pluginLocation;
    }    
 
    /**
     * Retreives the location id.
     * @return 
     */
    public final int getPluginLocationId(){
        return pluginLocationId;
    }
    
    /**
     * Sets the configuration options.
     * @param configuration
     */
    public final void setConfiguration(WebConfiguration configuration) {
        pluginConfiguration = configuration;
    };    
    
    /**
     * Must return true or false.
     * Return true to use graph data.
     * @return 
     */
    public abstract boolean hasGraphData();
    
    /**
     * Publishes configuration settings to the plugin.
     * @param configuration
     * @throws org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException 
     */
    public abstract void setConfigurationValues(Map<String,String> configuration) throws WebConfigurationException;
    
    /**
     * Asks for configuration options.
     * These can be displayed in the web interface.
     * @return
     * @throws org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException 
     */
    public final WebConfiguration getConfiguration() throws WebConfigurationException {
        if(pluginConfiguration==null){
            throw new WebConfigurationException("There is no configuration set");
        } else {
            return pluginConfiguration;
        }
    }
    
    /**
     * Returns the plugin's data set.
     * These can be displayed in the web interface.
     * @return 
     * @throws org.pidome.server.connector.plugins.plugindata.PluginDataException 
     */
    public final PluginDataSet getDataSet() throws PluginDataException {
        if(pluginDataSet==null){
            throw new PluginDataException("There is no data set");
        } else {
            return pluginDataSet;
        }
    }
    
    /**
     * Returns string data as literal as it has been set.
     * This function is optimal for storing xml or json based data. This data
     * is stored in the database.
     * @return
     * @throws PluginDataException 
     */
    public final String getCustomData() throws PluginDataException {
        if(customPluginData!=null && customPluginData.equals("")){
            throw new PluginDataException("There is no data set");
        } else {
            return customPluginData;
        }
    }
    
    /**
     * Sets the custom data.
     * This data is set while the plugin loads.
     * @param data
     */
    public final void setCustomData(String data) {
        customPluginData = data;
    }
    
    /**
     * Publishes data values to the plugin.
     * @param dataSet 
     * @throws org.pidome.server.connector.plugins.plugindata.PluginDataException 
     */
    public void setDataValuesByProperties(Properties dataSet) throws PluginDataException {
        pluginDataSet.composeByProperties(dataSet);
    }
    
    
    
    /**
     * Method to start the plugin. 
     * @throws org.pidome.server.connector.plugins.PluginException
     */
    public abstract void startPlugin() throws PluginException;
    
    /**
     * Method to stop the plugin. 
     * @throws org.pidome.server.connector.plugins.PluginException
     */
    public abstract void stopPlugin() throws PluginException;
    
    /**
     * To be implemented by an plugin that it should take preparations for deletion.
     * This is for example used by the devices plugin to delete the devices
     * made by the plugin.
     */
    public abstract void prepareDelete();
    
    /**
     * Returns the specific plugin xml.
     * @return 
     * @throws java.io.IOException 
     */
    public final String getXml() throws IOException {
        String className = this.getClass().getName();
        className = (className.substring(0,className.lastIndexOf(".")) + "." + className.substring(className.lastIndexOf(".")+1)).replace(".", "/");
        if(pluginXML.containsKey(className)){
            return pluginXML.get(className);
        } else {
            String xmlContent = loadXmlForPlugin(className);
            pluginXML.put(className, xmlContent);
            return xmlContent;
        }
    }
 
    /**
     * Loads the actual xml file.
     * @param pluginName
     * @return
     * @throws IOException 
     */
    final String loadXmlForPlugin(String pluginName) throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(pluginName + ".xml");
        if(in!=null){
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            String returnData = "";
            while((line = reader.readLine()) != null) {
                returnData += line;
            }
            return returnData;
        } else {
            LOG.error("Could not open '{}.xml'",pluginName);
            throw new IOException("Could not open '" + pluginName + ".xml'");
        }
    }
    
    public final String getClasspathString() {
        StringBuffer classpath = new StringBuffer();
        ClassLoader applicationClassLoader = this.getClass().getClassLoader();
        URL[] urls = ((URLClassLoader)applicationClassLoader).getURLs();
         for(int i=0; i < urls.length; i++) {
             classpath.append(urls[i].getFile()).append("\r\n");
         }    
         return classpath.toString();
     }
    
    /**
     * Returns true if there is a web presentation present.
     * @return 
     */
    public final boolean hasPresentation(){
        return !presentation.getList().isEmpty();
    }
    
    /**
     * Sets a web presentation;
     * @param pres 
     */
    public final void addWebPresentationGroup(WebPresentationGroup pres){
        this.presentation.getList().add(pres);
    }
    
    /**
     * Returns the web presentation.
     * @return 
     */
    public final List<WebPresentationGroup> getWebPresentationGroups(){
        return this.presentation.getList();
    }
    
    /**
     * Returns a file location for custom files.
     * @param fileName
     * @return 
     * @throws org.pidome.server.connector.plugins.IllegalFileLocationException 
     */
    public final File getFile(String fileName) throws IllegalFileLocationException, IOException {
        if(getBaseName().isEmpty() || getPluginId()==0){
            throw new IllegalFileLocationException("Can not construct path yet, plugin needs to be initialized first");
        } else if (fileName.contains("../") || fileName.contains("./")){
            throw new IllegalFileLocationException("Illegal path given");
        }
        File locationPath = new File(new StringBuilder("config/plugins/").append(getBaseName()).append("/").append(getPluginId()).toString());
        if(!locationPath.exists()){
            locationPath.mkdirs();
        }
        File newFile = new File(new StringBuilder("config/plugins/").append(getBaseName()).append("/").append(getPluginId()).append("/").append(fileName).toString());
        if(!newFile.exists()){
            newFile.createNewFile();
        }
        return newFile;
    }
    
}
