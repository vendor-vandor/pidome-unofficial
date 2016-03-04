/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.shareddata;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

/**
 *
 * @author John
 */
public class SharedDataStatusSetter {
    
    public static void setNewUserStatus(int id, String name){
        SharedUserStatusService.setNewStatus(id, name);
    }

    public static void setNewDayPartStatus(int id, String name){
        SharedDayPartService.setNewStatus(id, name);
    }
    
    public static void setNewPresenceStatus(int id, String name){
        SharedPresenceService.setNewStatus(id, name);
    }
    
    public static void setNewTimeStatus(GregorianCalendar cal){
        SharedServerTimeService.setNewStatus(cal);
    }

    public static void setSunRiseSet(long rise, long set){
        SharedServerTimeService.setSunRiseSet(rise, set);
    }
    
    public static void setNewLatLonStatus(String lat, String lon){
        SharedServerTimeService.setNewLatLonStatus(lat, lon);
    }
    
    public static void setNewLocationSet(List<Map<String,Object>> locationSet){
        SharedLocationService.setNewLocationCollection(locationSet);
    }
    
}
