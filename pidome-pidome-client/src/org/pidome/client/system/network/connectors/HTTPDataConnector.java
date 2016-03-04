/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.network.connectors;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.ClientSystem;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author John Sirach
 */
public class HTTPDataConnector extends HTTPConnector {

    static Logger LOG = LogManager.getLogger(HTTPDataConnector.class);
    
    public HTTPDataConnector(){
        
    }
    
    public final void setHTTPResource(String ip, int port){
        remoteIp = ip;
        remotePort = port;
        ssl = ClientSystem.isHTTPSSL();
    }
    
    public final Document getXml(String URL, String method, Map<String,String>postData) throws XMLConnectorException {
        try {
            setUrl(URL);
            setHTTPMethod(method);
            setPostData(postData);
            return createXmlFromString(getData());
        } catch (MalformedURLException ex) {
            throw new XMLConnectorException(ex.getMessage());
        } catch (IOException ex) {
            throw new XMLConnectorException(ex.getMessage());
        }
    }
    
    public final Document getXml(String URL, String method) throws XMLConnectorException {
        try {
            setUrl(URL);
            setHTTPMethod(method);
            return createXmlFromString(getData());
        } catch (MalformedURLException ex) {
            throw new XMLConnectorException(ex.getMessage());
        } catch (IOException ex) {
            throw new XMLConnectorException(ex.getMessage());
        }
    }
    
    public final Document createXmlFromString(String xmlString) throws XMLConnectorException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
            DocumentBuilder builder;  
            builder = factory.newDocumentBuilder();
            Document document = builder.parse( new InputSource( new StringReader( xmlString ) ) );
            return document;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new XMLConnectorException(ex.getMessage());
        }
    }
    
}
