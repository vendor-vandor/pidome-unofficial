/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.parsers;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John Sirach
 */
public class Parser {

    BroadcastParser broadcast = new BroadcastParser();
    
    static Logger LOG = LogManager.getLogger(Parser.class);
    
    public Parser(){
        
    }
    
    public final Map<String,Object> parseInitBroadcast(String broadcastString){
        broadcast.parseInitData(broadcastString);
        return broadcast.getInitData();
    }
    
    public final ServerProtocolParser parseServerStreamData (String serverData) throws ParseException {
        return new ServerProtocolParser(serverData);
    }
    
}
