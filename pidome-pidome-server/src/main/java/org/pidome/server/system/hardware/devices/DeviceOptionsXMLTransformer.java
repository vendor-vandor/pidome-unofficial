/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.hardware.devices;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.logging.log4j.LogManager;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author John
 */
public class DeviceOptionsXMLTransformer {
    
    private String XMLString;
    private Map<String,Object> newObject = new HashMap<>();
    
    private String deviceName = "Unknown device";
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(DeviceOptionsXMLTransformer.class);
    
    protected DeviceOptionsXMLTransformer(String XMLString){
        this.XMLString = XMLString;
    }
    
    /**
     * Sets the options.
     * This version is for backwards compatibility. When settings are saved they will be JSONObjects.
     */
    protected void transform() {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xPath = xpathFactory.newXPath();
        InputSource xmlSource = new InputSource(new StringReader(XMLString));
        try {
            NodeList nodes = (NodeList) xPath.evaluate("/settings/setting", xmlSource, XPathConstants.NODESET);
            int nodesAmount = nodes.getLength();
            if (nodesAmount > 0) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element node = (Element) nodes.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("setting")) {
                        Element element = (Element) node;
                        if (element.hasAttribute("identification") && element.hasAttribute("value")) {
                            newObject.put(element.getAttribute("identification"), element.getAttribute("value"));
                        }
                    }
                }
            } else {
                LOG.debug("There are no options to set");
            }
        } catch (XPathExpressionException ex) {
            LOG.error("Could not create options set: {}", ex.getMessage());
        }
        LOG.debug("Created device options from XML transform: {}", newObject);
    }
    protected Map<String,Object> get() throws PidomeJSONRPCException {
        return newObject;
    }
    
}