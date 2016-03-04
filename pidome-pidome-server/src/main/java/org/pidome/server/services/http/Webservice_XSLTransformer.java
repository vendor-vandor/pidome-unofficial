/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.server.services.http;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceException;
import org.pidome.server.system.network.http.HttpServer;
import org.pidome.server.system.hardware.devices.DeviceInterface;

/**
 * General XSL transforming.
 * @author John Sirach
 * @deprecated 
 */
public class Webservice_XSLTransformer {
    
    private static String baseLoc = null;
    static Logger LOG = LogManager.getLogger(Webservice_XSLTransformer.class);
    
    TransformerFactory tFactory;
    Source xslDoc;
    Transformer transformer;
    
    private String xslFile = null;
    
    /**
     * Sets location of the xsl transformers.
     * @param outputType 
     */
    public Webservice_XSLTransformer(){
        if(baseLoc==null){
            baseLoc = HttpServer.getDocumentRoot() + "/transformers/";
        }
        tFactory = TransformerFactory.newInstance();
    }
    
    /**
     * Sets the render file name.
     * These are a fixed selection of xsl sheets.
     * @param renderType
     * @throws UnsupportedOperationException 
     */
    public void setRenderFile(String renderType) throws UnsupportedOperationException {
        switch(renderType){
            case "media":
                xslFile = "media.xsl";
            break;
            case "device":
                xslFile = "device.xsl";
            break;
            case "deviceSettings":
                xslFile = "device_settings.xsl";
            break;
            case "package":
                xslFile = "packages.xsl";
            break;
            case "settings":
                xslFile = "settings.xsl";
            break;
            case "deviceActions":
                xslFile = "deviceActions.xsl";
            break;
            case "triggerActions":
                xslFile = "triggerActions.xsl";
            break;
            default:
                LOG.error("Unsupported xsl rendertype: " + renderType);
                throw new UnsupportedOperationException("Unsupported xsl rendertype: " + renderType);
            
        }
        xslDoc = new StreamSource(baseLoc + xslFile);
        try {
            Templates template = tFactory.newTemplates(xslDoc);
            transformer = template.newTransformer();
        } catch (TransformerConfigurationException ex) {
            LOG.error("Device renderfile error: " + ex.getMessage(), ex);
            throw new UnsupportedOperationException("xslDoc device renderfile error: " + ex.getMessageAndLocation());
        }
    }
    
    /**
     * Sets the xsl params content.
     * @param name
     * @param parameter 
     */
    public void setRenderParameter(String name, Object parameter){
        LOG.debug("Setting parameters: "+ name +", param: " + parameter);
        if(parameter==null) parameter = "";
        transformer.setParameter(name, parameter);
    }
    
    /**
     * Renders the xsl with the given device.
     * @param device
     * @return 
     */
    public String render(DeviceInterface device){
        try {
            StringReader xmlDoc = new StringReader(device.getDeviceXml());
            StringWriter returnOutput = new StringWriter();
            
            transformer.transform(new StreamSource(xmlDoc),new StreamResult(returnOutput));
            
            return returnOutput.toString();
            
        } catch(TransformerException ex) {
            LOG.error("Device render error: " + ex.getMessageAndLocation(), ex);
            throw new UnsupportedOperationException("xslDoc device render error: " + ex.getMessageAndLocation());
        } catch (UnsupportedDeviceException ex) {
            LOG.error("Device xml error: {}", ex.getMessage());
            throw new UnsupportedOperationException("Device xml error: " + ex.getMessage());
        }
    }
    
    /**
     * Feeds the xsl with any xml content.
     * @param xml
     * @return 
     */
    public String render(String xml){
        try {
            StringWriter returnOutput = new StringWriter();
            transformer.transform(new StreamSource(new StringReader(xml)),new StreamResult(returnOutput));
            return returnOutput.toString();
        } catch(TransformerException ex) {
            LOG.error("Xml render error: " + ex.getMessageAndLocation(), ex);
            throw new UnsupportedOperationException("xslDoc xml render error: " + ex.getMessageAndLocation());
        }
    }
    
}
