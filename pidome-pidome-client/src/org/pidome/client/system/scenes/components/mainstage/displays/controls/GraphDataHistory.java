/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays.controls;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.ObservableMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.ClientSystem;
import org.pidome.client.system.network.connectors.HTTPConnector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author John Sirach
 */
public final class GraphDataHistory {
    
    static Logger LOG = LogManager.getLogger(GraphDataHistory.class);

    public enum ResourceType {
        DEVICE,UTILITY
    }
    
    //ExecutorService getService = Executors.newSingleThreadExecutor();
    
    String itemSet;
    String itemGroup;
    int itemId;
    
    ResourceType type;
    
    GraphDataHistoryHelper dataHelper;
    boolean active = false;
    
    public GraphDataHistory(ResourceType type, int itemId, final String itemGroup, final String itemSet){
        this.type = type;
        this.itemId = itemId;
        this.itemSet = itemSet;
        this.itemGroup=itemGroup;
        switch(this.type){
            case UTILITY:
                if(itemSet.equals("WATT")){
                    dataHelper = new GraphDataHistoryHelperAvg();
                } else {
                    dataHelper = new GraphDataHistoryHelperSum();
                }
                break;
            default:
                dataHelper = new GraphDataHistoryHelperAvg();
            break;
        }
    }
    
    public final void refreshData(){
        clearData();
        Runnable getData = () -> {
            List<String> dataList = new ArrayList();
            dataList.add("year");
            dataList.add("month");
            dataList.add("week");
            dataList.add("day");
            dataList.add("hour");
            Map<String,Object> serverData = ClientSystem.getServerData();
            HTTPConnector connector = new HTTPConnector();
            connector.setServerData((String)serverData.get("HTTPADDRESS"), (int)serverData.get("HTTPPORT"), ClientSystem.isHTTPSSL());
            try {
                connector.setHTTPMethod(HTTPConnector.POST);
                switch(this.type){
                    case UTILITY:
                        connector.setUrl("/xmlapi/utilityhistory.xml?id="+String.valueOf(this.itemId)+"&dataGroup="+this.itemGroup+"&dataItem=" + this.itemSet);
                        break;
                    default:
                        connector.setUrl("/xmlapi/devicehistdata.xml?id="+String.valueOf(this.itemId)+"&dataGroup="+this.itemGroup+"&dataItem=" + this.itemSet);
                    break;
                }
                connector.setHTTPMethod("GET");
                String xmlData = connector.getData();
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder;
                builder = factory.newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(xmlData)));
                
                for (String key : dataList) {
                    XPathFactory xPathfactory = XPathFactory.newInstance();
                    XPath xpath = xPathfactory.newXPath();
                    try {
                        NodeList nodeList = (NodeList)xpath.compile("/data/history/" + key + "/data").evaluate(document, XPathConstants.NODESET);
                        for(int i = 0; i < nodeList.getLength(); i++){
                            Node node = nodeList.item(i);
                            if(node.getNodeName().equals("data")){
                                try {
                                    dataHelper.setPreData(key, Integer.parseInt(node.getAttributes().getNamedItem("time").getNodeValue()), Double.parseDouble(node.getTextContent()));
                                } catch (Exception ex){
                                    LOG.error("Having trouble adding data '{}' with timestamp {} to '{}' history of data item '{}' for item type: '{}'",node.getTextContent(),node.getAttributes().getNamedItem("time").getNodeValue(),key,itemSet,itemId);
                                }
                            }
                        }
                    } catch (XPathExpressionException ex) {
                        LOG.error("Could not get xml predata nodelist for '{}': {}", key,ex.getMessage());
                    }
                }
            } catch (MalformedURLException ex) {
                LOG.error("Could not set graph history data, URL problem: {}", ex.getMessage());
            } catch (IOException ex) {
                LOG.error("Could not set graph history data, IO Exception: {}", ex.getMessage());
            } catch (ParserConfigurationException | SAXException ex) {
                LOG.error("Could not set graph history data, Could not read xml: {}", ex.getMessage());
            }
            LOG.debug("Added serie: {}, {}",this.itemId, this.itemSet);
        };
        //getService.execute(getData);
        getData.run();
    }
    
    public final void clearData(){
        dataHelper.clearData();
    }
    
    public final Map<String, Double> getInitialSet(String timeLine){
        return dataHelper.getInitialSet(timeLine);
    }
    
    public final ObservableMap<String, Double> getDataConnection(String timeLine){
        return dataHelper.getDataConnection(timeLine);
    }
    
    public final void handleData(final Double data) {
        dataHelper.handleData(data);
    }
    
}