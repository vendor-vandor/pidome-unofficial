/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.parsers;

import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The parser for broadcast messages
 * @author John Sirach
 */
public class BroadcastParser {

    static Logger LOG = LogManager.getLogger(BroadcastParser.class);
    
    Map<String,Object> serverData = new HashMap<>();
    
    public Boolean parseInitData(String data){
        serverData.put("STREAMSSL", false);
        try {
            String[] aData = data.split("-");
            for(int i=0;i<aData.length;i++){
                String checkData[] = aData[i].split(":");
                LOG.debug("Working: {}", checkData);
                switch(checkData[0].trim()){
                    case "DOMCONTROL":
                        serverData.put("DOMCONTROL", checkData[1]);
                        serverData.put("TELNETADDRESS", checkData[1]);
                    break;
                    case "SCREEN":
                        if(serverData.containsKey("STREAMSSL") && (boolean)serverData.get("STREAMSSL")==false){
                            serverData.put("TELNETPORT", Integer.parseInt(checkData[2]));
                        }
                    break;
                    case "SCREENSSL":
                        serverData.put("STREAMSSL", true);
                        serverData.put("TELNETPORT", Integer.parseInt(checkData[2]));
                    break;
                }
            }
        } catch (Exception ex){
            return false;
        }
        LOG.debug("Final broadcast data: {}", serverData);
        return serverData.size()==4;
    }
    
    public final Map<String,Object> getInitData(){
        return serverData;
    }
    
}