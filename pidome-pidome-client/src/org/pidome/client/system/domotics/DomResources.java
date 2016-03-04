/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.pidome.client.system.network.connectors.HTTPConnector;
import org.pidome.client.system.network.connectors.JSONConnector;
import org.pidome.client.system.network.connectors.XMLConnectorException;
import org.pidome.client.system.rpc.PidomeJSONRPC;
import org.pidome.client.system.rpc.PidomeJSONRPCException;


/**
 *
 * @author John Sirach
 */
public class DomResources extends DomoticsHttpComponentParser {

    String initXmlLocation;
    
    String jsonIp;
    int jsonPort;
    String jsonEntry;
    
    boolean isSecure;
    
    static Logger LOG = LogManager.getLogger(DomResources.class);
    
    static List _domitcsListeners = new ArrayList();
    
    public DomResources(){
        
    }
    
    public final void setHttpConnectorResources(String ip, int port, String jsonEntry, int xmlPort, String xmlLocation, boolean ssl){
        setHTTPResource(ip, xmlPort);
        this.initXmlLocation = xmlLocation;
        
        this.jsonEntry = jsonEntry;
        this.jsonIp = ip;
        this.jsonPort = port;
        this.isSecure = ssl;
        
        LOG.debug("Resources JSON: {} - {} - {}, XML: {} - {} (ssl used?: {})", ip, port, jsonEntry, xmlPort, xmlLocation, ssl);
    }
    
    public final Document getInitXml() throws DomResourceException {
        try {
            return getXml(initXmlLocation, HTTPConnector.GET);
        } catch (XMLConnectorException ex) {
            throw new DomResourceException("Could not initialize: "+ ex.getMessage());
        }
    }
    
    public final Document getRawXml(String xmlFile) throws DomResourceException {
        try {
            return getXml(xmlFile, HTTPConnector.GET);
        } catch (XMLConnectorException ex) {
            throw new DomResourceException("Could not initialize: "+ ex.getMessage());
        }
    }
    
    public final Document getRawXml(String xmlFile, Map<String,String>postData) throws DomResourceException {
        try {
            return getXml(xmlFile, HTTPConnector.POST, postData);
        } catch (XMLConnectorException ex) {
            throw new DomResourceException("Could not initialize: "+ ex.getMessage());
        }
    }
    
    /**
     * Returns JSON data.
     * @param method
     * @param params
     * @return
     * @throws DomResourceException 
     */
    public final PidomeJSONRPC getJSONData(String method, Map<String,Object> params) throws DomResourceException {
        try {
            JSONConnector jsonData = new JSONConnector(this.jsonIp, this.jsonPort, this.jsonEntry, this.isSecure);
            return jsonData.getJSON(method, params, method);
        } catch (MalformedURLException ex) {
            throw new DomResourceException("Could not initialize json connection: "+ ex.getMessage());
        } catch (PidomeJSONRPCException ex) {
            throw new DomResourceException("Could not create json rpc method: "+ ex.getMessage());
        }
    }
    
    public final Image loadRemoteImage(String image) throws DomResourceException {
        try {
            setHTTPMethod("GET");
            setUrl(image);
            return loadRemoteImage();
        } catch (IOException ex) {
            throw new DomResourceException("Could not remote load image: " + ex.getMessage());
        }
    }
    
    public static synchronized void addDomoticsListener(DomoticsEventListener l) {
        LOG.debug("Added domotics listener: {}", l.getClass().getName());
        _domitcsListeners.add(l);
    }

    public static synchronized void removeDomoticsListener(DomoticsEventListener l) {
        LOG.debug("Removed domotics listener: {}", l.getClass().getName());
        _domitcsListeners.remove(l);
    }

    protected void _fireDomoticsEvent(String EventType){
        LOG.debug("New event: {}", EventType);
        DomoticsEvent event = new DomoticsEvent(EventType);
        Iterator listeners = _domitcsListeners.iterator();
        while (listeners.hasNext()) {
            ((DomoticsEventListener) listeners.next()).handleDomoticsEvent(event);
        }
    }
    
    
}
