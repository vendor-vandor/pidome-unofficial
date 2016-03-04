/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics;

import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.pidome.client.system.network.connectors.HTTPDataConnector;

/**
 *
 * @author John Sirach
 */
public class DomoticsHttpComponentParser extends HTTPDataConnector {

    public DomoticsHttpComponentParser(){}
    

    /**
     * Convenience function to retrieve all the node attributes contained in an element.
     * @param node
     * @return HashMap of all the attribute name value pairs of a node
     * @todo move to a XML convenience package
     */
    public static Map<String,String> getNodeAttributes(Node node){
        Map<String,String> nodeAttribMap = new HashMap<>();
        NamedNodeMap nodeAttr = node.getAttributes();
        for (int i = 0; i < nodeAttr.getLength(); ++i){
            Node attr = nodeAttr.item(i);
            nodeAttribMap.put(attr.getNodeName(),attr.getNodeValue());
        }
        return nodeAttribMap;
    }
    
}
