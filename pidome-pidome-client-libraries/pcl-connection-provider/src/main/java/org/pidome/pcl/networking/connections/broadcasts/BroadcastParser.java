/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.networking.connections.broadcasts;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.networking.connections.server.ServerConnection;
import org.pidome.pcl.utilities.parser.jsonrpc.PidomeJSONRPC;

/**
 * Parser for the broadcast message from the server.
 * @author John
 */
public class BroadcastParser {
    
    static {
        Logger.getLogger(BroadcastParser.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * The host found;
     */
    private String  host;
    /**
     * The port found.
     */
    private int     port = 0;
    
    /**
     * If broadcasted message points to an SSL location or not.
     */
    private boolean ssl = false;
    
    /**
     * Constructor.
     * Parses given message.
     * @param broadcastMessage The catched broadcast message.
     * @param profile The connection profile used. This detects the correct endpoint to be used.
     * @throws BroadcastParserException When the message can not be parsed.
     */
    public BroadcastParser(String broadcastMessage, ServerConnection.Profile profile) throws BroadcastParserException {
        try {
            Map<String,Object> msgParts = new PidomeJSONRPC(broadcastMessage).getParsedObject();
            
            String string = "{SRV=192.168.1.6, UP=8080, DP=11000, UA=SRV, SSL=true, DA=SRV}";
            
            host = (String)msgParts.get("SRV");
            ssl  = (boolean)msgParts.get("SSL");
            
            switch(profile){
                case FIXED:
                    if(msgParts.containsKey("DA") && !msgParts.get("DA").equals("SRV")){
                        host = (String)msgParts.get("DA");
                    }
                    if(msgParts.containsKey("DP")){
                        port = (int)msgParts.get("DP");
                    } else {
                        throw new BroadcastParserException("Missing port for display client");
                    }
                break;
                case MOBILE:
                    if(msgParts.containsKey("UA") && !msgParts.get("UA").equals("SRV")){
                        host = (String)msgParts.get("UA");
                    }
                    if(msgParts.containsKey("UP")){
                        port = (int)msgParts.get("UP");
                    } else {
                        throw new BroadcastParserException("Missing port for mobile client");
                    }
                break;
            }
        } catch (Exception ex){
            throw new BroadcastParserException("Could not parse: " + ex.getMessage());
        }
    }
    
    /**
     * Returns found host.
     * @return The host string.
     * @throws BroadcastParserException When there is no host found.
     */
    public final String getHost() throws BroadcastParserException {
        if(host!=null){
            return host;
        } else {
            throw new BroadcastParserException("Invalid host: " + host);
        }
    }
    
    /**
     * Returns found port.
     * @return returns the port.
     * @throws BroadcastParserException When there is no port.
     */
    public final int getPort() throws BroadcastParserException {
        return port;
    }
    
    /**
     * Returns if the connection is an SSL connection or not.
     * @return true if the catched broadcast defines an SSL port.
     * @throws BroadcastParserException When there no data available.
     */
    public final boolean isSSL() throws BroadcastParserException {
        return ssl;
    }
    
}
