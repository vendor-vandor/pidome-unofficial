/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.client.data;

import java.util.Map;

/**
 *
 * @author John Sirach
 */
public final class ClientDataConnectionEvent {

    String name;
    String key;
    String message;
    
    String method;
    
    int errorCode = 0;
    
    Object data;
    
    String eventType;
    
    public final static String CONNECTED     = "CONNECTED";
    public final static String DISCONNECTED  = "DISCONNECTED";
    public final static String LOGGEDIN      = "LOGGEDIN";
    public final static String DATARECEIVED  = "DATARECEIVED";
    public final static String LOGINFAILURE  = "LOGINFAILURE";
    public final static String NOSERVERFOUND = "NOSERVERFOUND";
    public final static String CONNECTERROR  = "CONNECTERROR";
    
    public final static String MCRRECEIVED  = "MCRRECEIVED";
    public final static String DEVRECEIVED  = "DEVRECEIVED";
    public final static String CATRECEIVED  = "CATRECEIVED";
    public final static String LOCRECEIVED  = "LOCRECEIVED";
    public final static String INITRECEIVED = "INITRECEIVED";
    public final static String SYSRECEIVED  = "SYSRECEIVED";
    public final static String MEDIARECEIVED  = "MEDIARECEIVED";
    public final static String PLUGINRECEIVED  = "PLUGINRECEIVED";
    public final static String CLIENTRECEIVED  = "CLIENTRECEIVED";
    public final static String DAYPARTRECEIVED  = "DAYPARTRECEIVED";
    public final static String USERPRESENCERECEIVED  = "USERPRESENCERECEIVED";
    public final static String USERSTATUSRECEIVED  = "USERSTATUSRECEIVED";
    public final static String UTILITYMEASURERECEIVED = "UTILITYMEASURERECEIVED";
    public final static String NOTIFICATIONRECEIVED = "NOTIFICATIONRECEIVED";
    
    public ClientDataConnectionEvent(String setEventType){
        eventType = setEventType;
    }
    
    public final int getLoginFailureReason(){
        return errorCode;
    }
    
    public final String getLoginFailureReasonMessage(){
        return message;
    }
    
    public final String getLoginName(){
        return name;
    }
    
    public final String getEventType(){
        return eventType;
    }
    
    public final void setClientData(String clientName, int errorCode, String message){
        this.name = clientName;
        this.errorCode = errorCode;
        this.message = message;
    }
    
    public final String[] getClientData(){
        return new String[] {name, key, message};
    }
    
    public final void setData(Object datas){
        data = datas;
    }
    
    public final void setMethod(String method){
        this.method = method;
    }
    
    public final String getMethod(){
        return method;
    }
    
    public final Object getData(){
        return data;
    }
    
    public final Map<String,Object> getParams(){
        return (Map<String,Object>)data;
    }
    
}
