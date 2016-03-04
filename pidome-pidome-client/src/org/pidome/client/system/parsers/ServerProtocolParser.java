/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.parsers;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.rpc.PidomeJSONRPC;
import org.pidome.client.system.rpc.PidomeJSONRPCException;

/**
 *
 * @author John Sirach
 */
public final class ServerProtocolParser {
    
    //// Server message namespace types
    public static final String DEVICE     = "DeviceService";
    public static final String LOC        = "LocationService";
    public static final String CAT        = "CategoryService";
    public static final String MACRO      = "MacroService";
    public static final String SYSTEM     = "SystemService";
    public static final String CLIENT     = "ClientService";
    public static final String MEDIA      = "MediaService";
    public static final String PLUGIN     = "PluginService";
    public static final String DAYPART    = "DayPartService";
    public static final String PRESENCE   = "PresenceService";
    public static final String USERSTATUS = "UserStatusService";
    public static final String UTILITYMEASURE = "UtilityMeasurementService";
    public static final String NOTIFICATION = "NotificationService";
    
    /// System response constants
    public static final String msg_OK = "200";
    public static final String msg_AUTH_FAILED = "401";
    public static final String msg_INVALID_DEVICE = "500";
    public static final String msg_INVALID = "500";
    public static final String msg_SEND_FAILED = "502";
    public static final String msg_BAD_REQUEST = "400";
    public static final String msg_NOT_FOUND = "404";
    public static final String msg_NOT_ALLOWED = "405";
    
    PidomeJSONRPC data;    
    String dataType;
    
    public Boolean ok;
    
    static Logger LOG = LogManager.getLogger(ServerProtocolParser.class);
    
    public ServerProtocolParser(String serverData) throws ParseException {
        try {
            data = new PidomeJSONRPC(serverData);
        } catch (PidomeJSONRPCException ex) {
            throw new ParseException(ex.getMessage());
        }
    }
    
    public final String getType(){
        return dataType;
    }
    
    public final String getNameSpace(){
        return data.getNameSpace();
    }
    
    public final String getMethod(){
        return data.getMethod();
    }
    
    public final Object getId(){
        return data.getId();
    }
    
    public final Map<String,Object> getParameters(){
        return data.getParameters();
    }
    
    public final Map<String,Object> getResult(){
        return data.getResult();
    }
    
}
