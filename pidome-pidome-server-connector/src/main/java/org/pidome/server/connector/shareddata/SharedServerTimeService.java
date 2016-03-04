/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.shareddata;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import org.pidome.server.connector.permissions.SharedDataPermission;

/**
 * Will be updated every minute.
 * @author John
 */
public class SharedServerTimeService {
    
    private static Date currentDate;
    private static GregorianCalendar cal;
    private final static SimpleDateFormat largeDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final static SimpleDateFormat miltitaryFormat = new SimpleDateFormat("HH:mm:ss");
    private final static SimpleDateFormat defaultFormat   = new SimpleDateFormat("hh:mm:ss a");
    
    private static List<SharedServerTimeServiceListener> _listeners = new ArrayList<>();
    
    private static String latitude = "";
    private static String longitude = "";
    
    private static long sunrise = 0L;
    private static long sunset = 0L;
    
    private static TimeZone currentTimeZone = TimeZone.getDefault();
    
    /**
     * Returns the used gregorian calendar.
     * @return 
     */
    public static GregorianCalendar getCalendar(){
        return cal;
    }
    
    /**
     * Returns current status name.
     * @return 
     */
    public static Date getCurrentStatusName(){
        return currentDate;
    }
    
    /**
     * Sets the sunset and sunrise times.
     * @param rise
     * @param set 
     */
    protected static void setSunRiseSet(long rise, long set){
        sunrise = rise;
        sunset  = set;
    }
    
    /**
     * Gets the sunrise time
     * @return 
     */
    public static long getSunset(){
        return sunset;
    }
    
    /**
     * Gets the sunset time
     * @return 
     */
    public static long getSunrise(){
        return sunrise;
    }
    
    /**
     * Set's new status.
     * @param lat
     * @param lon 
     */
    protected static void setNewLatLonStatus(String lat, String lon){
        latitude  = lat;
        longitude = lon;
    }
    
    /**
     * get latitude.
     * @return 
     */
    public static String getLatitude(){
        return latitude;
    }
    
    /**
     * Returns longitude data.
     */
    public static String getLongitude(){
        return longitude;
    }
    
    /**
     * Adds a listener.
     * @param listener 
     */
    public static void addListener(SharedServerTimeServiceListener listener){
        if(!_listeners.contains(listener)){
            _listeners.add(listener);
        }
    }
    
    /**
     * Removes a listener.
     * @param listener 
     */
    public static void removeListener(SharedServerTimeServiceListener listener){
        if(_listeners.contains(listener)){
            _listeners.remove(listener);
        }
    }
    
    /**
     * Returns the calendar date.
     * Best to call this after an update notification has been send out.
     * @return 
     */
    public static Date getCalendarDate(){
        return cal.getTime();
    }
    
    /**
     * Returns current Date.
     * @return 
     */
    public static Date getCurrentDate(){
        return currentDate;
    }

    /**
     * Returns a datetime in string as 00-00-0000 00:00:00
     * @return 
     */
    public static String getDateTime(){
        return largeDateFormat.format(cal.getTime());
    }
    
    /**
     * Returns a datetime in string as 00-00-0000 00:00:00 for the given calendar including offset.
     * @param cal
     * @return 
     */
    public static String getDateTimeConverted(GregorianCalendar cal){
        ///cal.setTime(new Date(getEpochSeconds(cal)*1000));
        return largeDateFormat.format(cal.getTime());
    }
    
    /**
     * Returns 24 hours military time
     * @return 
     */
    public static String getMilitaryTime(){
        return miltitaryFormat.format(cal.getTime());
    }
    
    /**
     * Returns time in not military format.
     * @return 
     */
    public static String getDefaultTime(){
        return defaultFormat.format(cal.getTime());
    }
    
    /**
     * Returns epoch modified to time-zone.
     * @return 
     */
    public static int getEpochSeconds(){
        return Math.round((cal.getTimeInMillis()+TimeZone.getDefault().getOffset(cal.getTimeInMillis()))/1000);
    }
    
    /**
     * Returns epoch modified to time-zone for the given calendar.
     * @param cal
     * @return 
     */
    public static int getEpochSeconds(GregorianCalendar cal){
        return Math.round((cal.getTimeInMillis()+TimeZone.getDefault().getOffset(cal.getTimeInMillis()))/1000);
    }
    
    /**
     * Returns current set time zone.
     * @return 
     */
    public static TimeZone getCurrentTimeZone(){
        return currentTimeZone;
    }
    
    /**
     * Sets the new status. 
     * @param newCal
     */
    protected static void setNewStatus(GregorianCalendar newCal){
        SecurityManager manager = System.getSecurityManager();
        if (manager != null){
            manager.checkPermission(new SharedDataPermission("SharedServerTimeService", "update"));
        }
        cal = newCal;
        currentDate = newCal.getTime();
        cal.setTime(currentDate);
        largeDateFormat.setCalendar(cal);
        miltitaryFormat.setCalendar(cal);
        defaultFormat.setCalendar(cal);
        currentTimeZone = cal.getTimeZone();
        for(SharedServerTimeServiceListener listener:_listeners){
            listener.handleNewTimeServiceMinute();
        }
    }
    
}
