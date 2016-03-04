/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.plugins.freeform.plugwise.plugwiseSmile;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.interfaces.web.configuration.WebConfiguration;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationOptionSet;
import org.pidome.server.connector.interfaces.web.configuration.WebOption;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.graphdata.RoundRobinDataGraphItem;
import org.pidome.server.connector.plugins.utilitydata.UtilityData;
import org.pidome.server.connector.plugins.utilitydata.UtilityDataException;
import org.pidome.server.connector.plugins.utilitydata.UtilityDataPower;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author John
 */
public class PlugwiseSmile extends UtilityData {

    static Logger LOG = LogManager.getLogger(PlugwiseSmile.class);
    
    private String remoteHost      = "";
    private String remoteLocation  = "";
    
    private long   updateFrequency = 60;
    
    ScheduledExecutorService dataUpdater;
    
    /**
     * Constructor.
     */
    public PlugwiseSmile(){
        createConfig();
    }
    
    /**
     * Creates the config shown in the web interface.
     */
    private void createConfig(){
        
        WebConfiguration conf = new WebConfiguration();
        
        WebConfigurationOptionSet optionSetSmileLocation = new WebConfigurationOptionSet("Plugwise Smile basic settings");
        optionSetSmileLocation.setConfigurationSetDescription("Please enter the location of the Plugwise smile device on your network.");
        
        optionSetSmileLocation.addOption(new WebOption("SMILEIP", "Ip address/Hostname", "Enter the hostname or the ip address",WebOption.WebOptionConfigurationFieldType.STRING));
        WebOption smileXMLLoc = new WebOption("XMLLOC", "XML location on the smile", "Enter the path to the XML data on the smile", WebOption.WebOptionConfigurationFieldType.STRING);
        smileXMLLoc.setDefaultValue("/smartmeter/modules");
        optionSetSmileLocation.addOption(smileXMLLoc);
        
        conf.addOptionSet(optionSetSmileLocation);
        
        WebConfigurationOptionSet optionSetSmileUpdates = new WebConfigurationOptionSet("Plugwise Smile update options");
        
        Map<String,String> updateOptions = new HashMap<>();
        updateOptions.put("UPDATE_10", "10 seconds");
        updateOptions.put("UPDATE_20", "20 seconds");
        updateOptions.put("UPDATE_30", "30 seconds");
        updateOptions.put("UPDATE_40", "40 seconds");
        updateOptions.put("UPDATE_50", "50 seconds");
        updateOptions.put("UPDATE_60", "60 seconds");
        
        WebOption updateOption = new WebOption("UPDATE_FREQ", "Set update frequency", "Set how often there should be updated.", WebOption.WebOptionConfigurationFieldType.SELECT,updateOptions);
        updateOption.setDefaultValue("UPDATE_60");
        optionSetSmileUpdates.addOption(updateOption);
        
        conf.addOptionSet(optionSetSmileUpdates);
        
        setConfiguration(conf);
        
    }
    
    /**
     * Sets configuration values.
     * @param configuration
     * @throws WebConfigurationException 
     */
    @Override
    public final void setConfigurationValues(Map<String, String> configuration) throws WebConfigurationException {
        if(configuration.containsKey("SMILEIP")){
            try {
                if(configuration.get("SMILEIP").contains(":")){
                    InetAddress.getByName(configuration.get("SMILEIP").split(":")[0]);
                } else {
                    InetAddress.getByName(configuration.get("SMILEIP"));
                }
            } catch (UnknownHostException ex) {
                throw new WebConfigurationException("Invalid remote host supplied: " + ex.getMessage());
            }
            remoteHost = configuration.get("SMILEIP");
        } else {
            throw new WebConfigurationException("No remote host given");
        }
        if(configuration.containsKey("XMLLOC")){
            remoteLocation = configuration.get("XMLLOC");
        } else {
            throw new WebConfigurationException("No xml location given");
        }
        if(configuration.containsKey("UPDATE_FREQ")){
            switch(configuration.get("UPDATE_FREQ")){
                case "UPDATE_10":
                    updateFrequency = 10;
                break;
                case "UPDATE_20":
                    updateFrequency = 20;
                break;
                case "UPDATE_30":
                    updateFrequency = 30;
                break;
                case "UPDATE_40":
                    updateFrequency = 40;
                break;
                case "UPDATE_50":
                    updateFrequency = 50;
                break;
                case "UPDATE_60":
                    updateFrequency = 60;
                break;
                default:
                    throw new WebConfigurationException("Invalid frequency update supplied");
            }
        } else {
            throw new WebConfigurationException("No update frequency given");
        }
        this.addPowerMeasurement("UTILITY", new UtilityDataPower("Watt","KW/h",1.0,Measurement.POWER_ABSOLUTE));
        try {
            this.getPowerMeasurement("UTILITY").setTodayKwh(getTodayGraphTotal("UTILITY", "KWH"));
        } catch (UtilityDataException ex) {
            //// There is not today data.
        }
        ArrayList<RoundRobinDataGraphItem> dataTypes = new ArrayList();
        dataTypes.add(new RoundRobinDataGraphItem("UTILITY", "WATT", RoundRobinDataGraphItem.FieldType.AVERAGE));
        dataTypes.add(new RoundRobinDataGraphItem("UTILITY", "KWH", RoundRobinDataGraphItem.FieldType.ABSOLUTE));
        
        registerGraphDataTypes(dataTypes);
    }
    
    /**
     * Starts the plugin.
     * @throws PluginException 
     */
    @Override
    public void startPlugin() throws PluginException {
        stopPlugin();
        if(dataUpdater==null){
            dataUpdater = Executors.newSingleThreadScheduledExecutor();
            dataUpdater.scheduleAtFixedRate(() -> {
                try {
                    parseSmileData();
                } catch (Exception ex) {
                    try {
                        LOG.error("Can not check data (Plugin being halted): {}", ex.getMessage(), ex);
                        stopPluginMethod(true);
                    } catch (Exception exc){}
                }
            }, 0, updateFrequency, TimeUnit.SECONDS);
        }
        this.setRunning(true);
    }
    
    /**
     * Requests and parses the smile data.
     * @throws Exception 
     */
    private void parseSmileData() throws Exception {
        Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new StringBuilder("http://").append(this.remoteHost).append(this.remoteLocation).toString());
        XPath xPath =  XPathFactory.newInstance().newXPath();
        /// Real time current usages:
        XPathExpression expr = xPath.compile("/modules/module/point_logs/point_log/type[text()='electricity_consumed']/../period/measurement");
        Node result = (Node)expr.evaluate(xmlDocument, XPathConstants.NODE);
        try {
            this.getPowerMeasurement("UTILITY").setCurrentValue(Double.valueOf(result.getTextContent()));
            storeGraphData("UTILITY","KWH", this.getPowerMeasurement("UTILITY").getKwh().getCurrentValue());
            storeGraphData("UTILITY","WATT", this.getPowerMeasurement("UTILITY").getCurrentValue());
            broadcastResultValue(Type.POWER, "UTILITY");
        } catch (Exception ex){
            LOG.error("Could not set current wattage usage: {}", ex.getMessage(), ex);
        }
    }
    
    /**
     * Shuts down nice or not.
     * When nice it let's the declared methods in the executor finish nicely.
     * @param nice 
     */
    private void stopPluginMethod(boolean nice){
        if(dataUpdater!=null){
            if(nice==false){
                dataUpdater.shutdownNow();
            } else {
                dataUpdater.shutdown();
            }
            dataUpdater = null;
            this.setRunning(false);
        }
    }
    
    /**
     * Stops the plugin.
     * @throws PluginException 
     */
    @Override
    public void stopPlugin() throws PluginException {
        stopPluginMethod(false);
    }
    
    /**
     * Prepares the plugin with data for being displayed on pages.
     * Not used yet.
     */
    @Override
    public void prepareWebPresentation() {
        throw new UnsupportedOperationException("Not used."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Called when devices are bound and need to be deleted.
     * Not used.
     */
    @Override
    public void prepareDelete() {
        /// Not used
    }

    @Override
    public boolean hasGraphData() {
        return true;
    }
    
}