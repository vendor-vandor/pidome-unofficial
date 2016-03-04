/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.tools;

import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author John
 */
public class XMLTools {
    
    /**
     * Convenience method for getting all the node attributes from an xml tag.
     * @param node
     * @return 
     */
    public static Map<String,String> getNodeAttributes(Node node){
        Map<String,String> nodeAttribMap = new HashMap<>();
        try {
            NamedNodeMap nodeAttr = node.getAttributes();
            for (int i = 0; i < nodeAttr.getLength(); ++i){
                Node attr = nodeAttr.item(i);
                nodeAttribMap.put(attr.getNodeName(),attr.getNodeValue());
            }
        } catch (Exception ex){
            /// There are no attributes
        }
        return nodeAttribMap;
    }
    
}
