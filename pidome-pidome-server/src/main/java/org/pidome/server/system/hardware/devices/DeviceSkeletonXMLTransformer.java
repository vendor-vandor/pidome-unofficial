/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.hardware.devices;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.logging.log4j.LogManager;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException;
import org.pidome.server.connector.tools.XMLTools;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author John
 */
public class DeviceSkeletonXMLTransformer {
 
    private String XMLString;
    private Map<String,Object> newObject = new HashMap<>();
    
    private String deviceName = "Unknown device";
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(DeviceSkeletonXMLTransformer.class);
    
    protected DeviceSkeletonXMLTransformer(String XMLString){
        this.XMLString = XMLString;
    }
    
    protected void transform() throws DeviceSkeletonException {
        InputSource xmlSource;
        XPathFactory xpathFactory;
        XPath xPath;
        try {
            xmlSource = new InputSource(new StringReader(XMLString));
            xpathFactory = XPathFactory.newInstance();
            xPath = xpathFactory.newXPath();
            newObject.put("controlset", createDeviceControlStruct((Node)xPath.evaluate("/device/commandset", xmlSource, XPathConstants.NODE)));
            
            xmlSource = new InputSource(new StringReader(XMLString));
            xpathFactory = XPathFactory.newInstance();
            xPath = xpathFactory.newXPath();
            newObject.put("options", createDeviceOptionsStruct((NodeList)xPath.evaluate("/device/options/*", xmlSource, XPathConstants.NODESET)));

            xmlSource = new InputSource(new StringReader(XMLString));
            xpathFactory = XPathFactory.newInstance();
            xPath = xpathFactory.newXPath();
            newObject.put("address", getDeviceAddressStruct((Node)xPath.evaluate("/device/address", xmlSource, XPathConstants.NODE)));

            newObject.put("description", "From composer");
            try {
                newObject.put("name", getDeviceName(XMLString));
            } catch (UnsupportedDeviceException ex) {
                newObject.put("name", deviceName);
                Logger.getLogger(DeviceSkeletonXMLTransformer.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (XPathExpressionException ex) {
            LOG.error("Device XML problem: {}", ex.getMessage(), ex);
            throw new DeviceSkeletonException("Device XML problem: " + ex.getMessage());
        }
    }
    
    protected Map<String,Object> get() throws PidomeJSONRPCException {
        Map<String,Object> deviceSet = new HashMap<>();
        deviceSet.put("device", newObject);
        LOG.debug("New parsed object set from XML to JSON: {}", deviceSet);
        return deviceSet;
    }
    
    private Map<String,List<Map<String,Object>>> createDeviceControlStruct(Node commandSetNode) throws DeviceSkeletonException {
        Map<String,List<Map<String,Object>>> newStruct = new HashMap<>();
        List<Map<String,Object>> groupsList = new ArrayList<>();
        try {
            if (commandSetNode.hasChildNodes()) {
                NodeList groupList = commandSetNode.getChildNodes();
                for (int i = 0; i < groupList.getLength(); i++) {
                    if (groupList.item(i).getNodeName().equals("group")) {
                        if (groupList.item(i).hasChildNodes()) {
                            try {
                                Map<String,Object> groupDetails = new HashMap<>();
                                groupDetails.putAll(XMLTools.getNodeAttributes(groupList.item(i)));
                                groupDetails.put("controls", constrolsFromXmlComposer((NodeList)groupList.item(i).getChildNodes()));
                                groupsList.add(groupDetails);
                            } catch (Exception ex){
                                LOG.error("Incorrect group set in XML created: " + ex.getMessage(), ex);
                            }
                        }
                    }
                }
            } else {
                LOG.debug("No command group sets available in : {}", commandSetNode);
            }
        } catch (Exception ex) {
            LOG.error("This should not happen", ex);
            throw new DeviceSkeletonException(ex);
        }
        newStruct.put("groups", groupsList);
        return newStruct;
    }
    
    private String getDeviceName(String xml) throws UnsupportedDeviceException {
        try {
            InputSource xmlSource = new InputSource(new StringReader(xml));
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xPath = xpathFactory.newXPath();
            try {
                Node node = (Node) xPath.evaluate("/device/name", xmlSource, XPathConstants.NODE);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    LOG.debug("Set canonical device name: {}", node.getTextContent());
                    return node.getTextContent();
                } else {
                    LOG.warn("Device xml has no name defined");
                    throw new UnsupportedDeviceException("Xml is not well formed, missing name");
                }
            } catch (XPathExpressionException ex) {
                LOG.error("This should absolutely not happen", ex);
                throw new UnsupportedDeviceException("Could not find name in xml");
            } catch (NullPointerException ex){
                LOG.error("This should not happen", ex);
                throw new UnsupportedDeviceException("Nasty nullpointer exception");
            }
        } catch (Exception ex){
            LOG.error("Problem with xml searching");
            throw new UnsupportedDeviceException("got exception '"+ex.getMessage()+"'");
        }
    }
    
    private List<Map<String,Object>> constrolsFromXmlComposer(NodeList controlNodes){
        List<Map<String,Object>> nodesList = new ArrayList<>();
        for(int i=0; i< controlNodes.getLength();i++){
            try {
                switch (controlNodes.item(i).getNodeName()) {
                    case "#text": break; //// Just in case there are whitespaces
                    case "data":
                        nodesList.add(createDataComponentSet(XMLTools.getNodeAttributes(controlNodes.item(i))));
                        break;
                    case "button":
                        nodesList.add(createButtonComponentSet(XMLTools.getNodeAttributes(controlNodes.item(i))));
                        break;
                    case "toggle":
                        nodesList.add(createToggleButtonComponentSet(XMLTools.getNodeAttributes(controlNodes.item(i)), controlNodes.item(i).getChildNodes()));
                        break;
                    case "select":
                        nodesList.add(createSelectComponentSet(XMLTools.getNodeAttributes(controlNodes.item(i)), controlNodes.item(i).getChildNodes()));
                        break;
                    case "slider":
                        nodesList.add(createSliderComponentSet(XMLTools.getNodeAttributes(controlNodes.item(i))));
                        break;
                    case "colorpicker":
                        nodesList.add(createColorPickerComponentSet(XMLTools.getNodeAttributes(controlNodes.item(i)), controlNodes.item(i).getChildNodes()));
                        break;
                    default:
                        throw new DeviceControlException("Unsupported control: " + controlNodes.item(i).getNodeName());
                }
            } catch (DeviceControlException ex) {
                Logger.getLogger(DeviceSkeletonXMLTransformer.class.getName()).log(Level.SEVERE, "Could not transform to map set", ex);
            }
        }
        return nodesList;
    }
    
    /**
     * creates a data control.
     * @param dataMap 
     */
    private static Map<String,Object> createDataComponentSet(Map<String,String> dataMap) throws DeviceControlException {
        HashMap<String,Object> newMap = new HashMap<>();
        newMap.putAll(defaultTransform(dataMap));
        if(dataMap.containsKey("timeout")){
            try {
                newMap.put("timeout", Integer.parseInt(dataMap.get("timeout")));
            } catch (Exception ex){
                newMap.put("timeout", 0);
            }
        }
        if(newMap.containsKey("datatype")){
            switch((String)newMap.get("datatype")){
                case "float":
                    if(dataMap.containsKey("minvalue")){
                        try {
                            newMap.put("minvalue", Float.parseFloat(dataMap.get("minvalue")));
                        } catch (Exception ex){
                            newMap.put("minvalue", 0f);
                        }
                    }
                    if(dataMap.containsKey("maxvalue")){
                        try {
                            newMap.put("maxvalue", Float.parseFloat(dataMap.get("maxvalue")));
                        } catch (Exception ex){
                            newMap.put("maxvalue", 0f);
                        }
                    }
                    if(dataMap.containsKey("warnvalue")){
                        try {
                            newMap.put("warnvalue", Float.parseFloat(dataMap.get("warnvalue")));
                        } catch (Exception ex){
                            newMap.put("warnvalue", 0f);
                        }
                    }
                    if(dataMap.containsKey("highvalue")){
                        try {
                            newMap.put("highvalue", Float.parseFloat(dataMap.get("highvalue")));
                        } catch (Exception ex){
                            newMap.put("highvalue", 0f);
                        }
                    }
                break;
                default:
                    if(dataMap.containsKey("minvalue")){
                        try {
                            newMap.put("minvalue", Integer.parseInt(dataMap.get("minvalue")));
                        } catch (Exception ex){
                            newMap.put("minvalue", 0f);
                        }
                    }
                    if(dataMap.containsKey("maxvalue")){
                        try {
                            newMap.put("maxvalue", Integer.parseInt(dataMap.get("maxvalue")));
                        } catch (Exception ex){
                            newMap.put("maxvalue", 0f);
                        }
                    }
                    if(dataMap.containsKey("warnvalue")){
                        try {
                            newMap.put("warnvalue", Integer.parseInt(dataMap.get("warnvalue")));
                        } catch (Exception ex){
                            newMap.put("warnvalue", 0f);
                        }
                    }
                    if(dataMap.containsKey("highvalue")){
                        try {
                            newMap.put("highvalue", Integer.parseInt(dataMap.get("highvalue")));
                        } catch (Exception ex){
                            newMap.put("highvalue", 0f);
                        }
                    }
                break;
            }
        }
        newMap.put("type", "data");
        return newMap;
    }
    
    /**
     * creates a slider control.
     * @param dataMap 
     */
    private static Map<String,Object> createSliderComponentSet(Map<String,String> dataMap) throws DeviceControlException {
        HashMap<String,Object> newMap = new HashMap<>();
        newMap.putAll(defaultTransform(dataMap));
        newMap.put("type", "slider");
        Map<String,Object> paramsMap = new HashMap<>();
        if(newMap.containsKey("datatype")){
            switch((String)newMap.get("datatype")){
                case "float":
                    paramsMap.put("min", Float.parseFloat(dataMap.get("min")));
                    paramsMap.put("max", Float.parseFloat(dataMap.get("max")));
                break;
                default:
                    paramsMap.put("min", Integer.parseInt(dataMap.get("min")));
                    paramsMap.put("max", Integer.parseInt(dataMap.get("max")));
                break;
            }
        }
        newMap.remove("min");
        newMap.remove("max");
        newMap.put("parameters", paramsMap);
        return newMap;
    }
    
    /**
     * creates a button control.
     * @param dataMap 
     */
    private static Map<String,Object> createButtonComponentSet(Map<String,String> dataMap) throws DeviceControlException {
        HashMap<String,Object> newMap = new HashMap<>();
        newMap.putAll(defaultTransform(dataMap));
        newMap.put("type", "button");
        return newMap;
    }
    
    /**
     * Creates a toggle button.
     * @param dataMap
     * @param childs 
     */
    private static Map<String,Object> createToggleButtonComponentSet(Map<String,String> dataMap, NodeList childs) throws DeviceControlException {
        HashMap<String,Object> newMap = new HashMap<>();
        newMap.putAll(defaultTransform(dataMap));
        newMap.put("type", "toggle");
        Map<String,Object> trueFalseMap = new HashMap<>();
        for (int j = 0; j < childs.getLength(); j++) {
            switch (childs.item(j).getNodeName()) {
                case "#text": break;
                case "on":
                case "off":
                    trueFalseMap.put(childs.item(j).getNodeName(), XMLTools.getNodeAttributes(childs.item(j)));
                break;
                default:
                    throw new DeviceControlException("Unsupported child tag '("+childs.item(j).getNodeName()+")' in toggle button.");
            }
        }
        newMap.put("parameters", trueFalseMap);
        return newMap;
    }
    
    /**
     * Creates a toggle button.
     * @param dataMap
     * @param childs 
     */
    private static Map<String,Object> createSelectComponentSet(Map<String,String> dataMap, NodeList childs) throws DeviceControlException {
        HashMap<String,Object> newMap = new HashMap<>();
        newMap.putAll(defaultTransform(dataMap));
        newMap.put("type", "select");
        List<Map<String,Object>> optionsList = new ArrayList<>();
        for (int j = 0; j < childs.getLength(); j++) {
            switch (childs.item(j).getNodeName()) {
                case "#text": break; //// Just in case there are whitespaces
                case "option":
                    optionsList.add(createOption(XMLTools.getNodeAttributes(childs.item(j))));
                break;
                default:
                    throw new DeviceControlException("Unsupported child tag '"+childs.item(j).getNodeName()+"' in select control.");
            }
        }
        newMap.put("parameters", optionsList);
        return newMap;
    }
    
    /**
     * Creates the option.
     * @param attributes 
     */
    private static Map<String,Object> createOption(Map<String,String> attributes) throws DeviceControlException {
        if(!attributes.containsKey("value") || attributes.get("value").isEmpty() || 
            !attributes.containsKey("label") || attributes.get("label").isEmpty() || attributes.get("label").length()>20){
            throw new DeviceControlException("Check your select tag. Incorrect option setup");
        }
        Map<String,Object> newAttributes = new HashMap<>();
        newAttributes.put("label", attributes.get("label"));
        newAttributes.put("value", attributes.get("value"));
        return newAttributes;
    }
    
    /**
     * Creates the color picker control.
     * @param dataMap
     * @param childs
     * @throws DeviceControlException 
     */
    private static Map<String,Object> createColorPickerComponentSet(Map<String,String> dataMap, NodeList childs) throws DeviceControlException {
        HashMap<String,Object> newMap = new HashMap<>();
        newMap.putAll(defaultTransform(dataMap));
        newMap.put("type", "colorpicker");
        List<Map<String,String>> buttonsList = new ArrayList<>();
        for (int j = 0; j < childs.getLength(); j++) {
            switch (childs.item(j).getNodeName()) {
                case "#text": break; //// Just in case there are whitespaces
                case "button":
                    buttonsList.add(XMLTools.getNodeAttributes(childs.item(j)));
                break;
                default:
                    throw new DeviceControlException("Unsupported child tag '"+childs.item(j).getNodeName()+"' in select control.");
            }
        }
        newMap.put("parameters", buttonsList);
        return newMap;
    }
    
    private static Map<String,Object> defaultTransform(Map<String,String> data){
        HashMap<String,Object> newMap = new HashMap<>();
        newMap.putAll(data);
        if(data.containsKey("hidden")){
            newMap.put("hidden", Boolean.valueOf(data.get("hidden")));
        }
        if(data.containsKey("shortcut")){
            try {
                newMap.put("shortcut", Integer.valueOf(data.get("shortcut")));
            } catch (NumberFormatException ex){
                newMap.remove("shortcut");
            }
        }
        if(data.containsKey("retention")){
            newMap.put("retention", Boolean.valueOf(data.get("retention")));
        }
        if(data.containsKey("readonly")){
            newMap.put("readonly", Boolean.valueOf(data.get("readonly")));
        }
        return newMap;
    }
    
    private List<Map<String,Object>> createDeviceOptionsStruct(NodeList options){
        List<Map<String,Object>> newOptions = new ArrayList<>();
        if(options!=null && options.getLength()>0){
            for(int i = 0; i < options.getLength();i++){
                Element node = (Element)options.item(i);
                if (node!=null && node.getNodeType() == Node.ELEMENT_NODE) {
                    if(node.getNodeName().equals("#text")) { 
                        break; 
                    }
                    Map<String,Object> newSet = new HashMap<>();
                    newSet.putAll(XMLTools.getNodeAttributes(node));
                    if(newSet.containsKey("order")){
                        newSet.put("order", Integer.parseInt((String)newSet.get("order")));
                    }
                    newSet.put("type", node.getTagName());
                    if(node.getTagName().equals("select") && node.hasChildNodes()){
                        List<Map<String,String>> sublist = new ArrayList<>();
                        for(int j=0;j<node.getChildNodes().getLength();j++){
                            if (node.getChildNodes().item(j)!=null && node.getChildNodes().item(j).getNodeType() == Node.ELEMENT_NODE) {
                                if(node.getChildNodes().item(j).getNodeName().equals("#text")) { 
                                    break; 
                                }
                                Map<String,String> childs = new HashMap<>();
                                childs.putAll(XMLTools.getNodeAttributes(node.getChildNodes().item(j)));
                                sublist.add(childs);
                            }
                        }
                        newSet.put("selectset", sublist);
                    }
                    newOptions.add(newSet);
                }
            }
        }
        return newOptions;
    }
    
    private Map<String,Object> getDeviceAddressStruct(Node address){
        Map<String,Object> addressData = new HashMap<>();
        if(address!=null && address.hasChildNodes()){
            for(int i = 0; i< address.getChildNodes().getLength();i++){
                Node node = address.getChildNodes().item(i);
                switch (node.getNodeName()) {
                    case "description":
                        addressData.put("description", node.getTextContent());
                        break;
                    case "input":
                        addressData.put("input", XMLTools.getNodeAttributes(node));
                        break;
                }
            }
        }
        return addressData;
    }
    
}