/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.system;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.utilities.properties.ReadOnlyStringPropertyBindingBean;
import org.pidome.pcl.utilities.properties.StringPropertyBindingBean;

/**
 * Server time class can be used to show the server time.
 * @author John
 */
public final class ServerTime {
    
    static {
        Logger.getLogger(ServerTime.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Bind-able current server time.
     */
    private final StringPropertyBindingBean currentServerTime = new StringPropertyBindingBean();
    /**
     * Bind-able current server date.
     */
    private final StringPropertyBindingBean currentServerDate = new StringPropertyBindingBean();
    /**
     * Bind-able current server time.
     */
    private final StringPropertyBindingBean currentSunrise = new StringPropertyBindingBean();
    /**
     * Bind-able current server date.
     */
    private final StringPropertyBindingBean currentSunset = new StringPropertyBindingBean();
    /**
     * Bind-able concatenated time and date.
     */
    private final StringPropertyBindingBean currentServerConcatenatedDate = new StringPropertyBindingBean();
    /**
     * Bind-able concatenated time and date.
     */
    private final StringPropertyBindingBean currentServerConcatenatedDateShort = new StringPropertyBindingBean();
    
    /**
     * Return a long date time formatter.
     * @return 
     */
    public static final SimpleDateFormat getLongDateFormat(){
        return new SimpleDateFormat("EEE, d MMM yyyy");
    }
    
    /**
     * Return a long date formatter.
     * @return 
     */
    public static final SimpleDateFormat getLongDateTimeFormat(){
        return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
    }
    
    /**
     * Return a long date formatter.
     * @return 
     */
    public static final SimpleDateFormat getTimeFormat(){
        return new SimpleDateFormat("HH:mm:ss");
    }
    
    /**
     * Server time object.
     */
    protected ServerTime(){}
    
    /**
     * Handles server time updates.
     * @param timeParams Map containing the current server time.
     */
    protected final void handleTimeUpdate(Map<String,Object> timeParams){
        if(timeParams.containsKey("time")){
            currentServerTime.setValue((String)timeParams.get("time"));
        }
        if(timeParams.containsKey("date")){
            currentServerDate.setValue((String)timeParams.get("date"));
        }
        if(timeParams.containsKey("sunrise")){
            currentSunrise.setValue((String)timeParams.get("sunrise"));
        }
        if(timeParams.containsKey("sunset")){
            currentSunset.setValue((String)timeParams.get("sunset"));
        }
        if(timeParams.containsKey("dayname") && timeParams.containsKey("monthname") && timeParams.containsKey("day")  && timeParams.containsKey("year")){
            currentServerConcatenatedDate.setValue(new StringBuilder((String)timeParams.get("dayname")).append(", ").
                                                              append((String)timeParams.get("monthname")).append(" ").
                                                              append((Number)timeParams.get("day")).append(", ").
                                                              append((Number)timeParams.get("year")).toString());
        }
        if(timeParams.containsKey("shorttext")){
            currentServerConcatenatedDateShort.setValue((String)timeParams.get("shorttext"));
        }
    }
    
    /**
     * Removes all listeners.
     */
    protected final void releaseListeners(){
        /// Not used.
    }
    
    /**
     * Returns the current system time.
     * @return "[HH:MM]" (Militairy).
     */
    public final ReadOnlyStringPropertyBindingBean getCurrentTimeProperty(){
        return currentServerTime.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Returns the current sunrise time.
     * @return "[HH:MM]" (Militairy).
     */
    public final ReadOnlyStringPropertyBindingBean getCurrentSunriseProperty(){
        return currentSunrise.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Returns the current sunset time.
     * @return "[HH:MM]" (Militairy).
     */
    public final ReadOnlyStringPropertyBindingBean getCurrentSunsetProperty(){
        return currentSunset.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Returns the current system time.
     * @return "dd-mm-yyyy"
     */
    public final ReadOnlyStringPropertyBindingBean getCurrentDateProperty(){
        return currentServerDate.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Returns a concatenated current date and time string.
     * @return "[daynamelong], [monthname] [dayofmonth], [year]"
     */
    public final ReadOnlyStringPropertyBindingBean getCurrentConcatenatedDatePeroperty(){
        return currentServerConcatenatedDate.getReadOnlyBooleanPropertyBindingBean();
    }
    /**
     * Returns a concatenated current date and time string.
     * @return "[daynameshort], [dayofmonth] [monthnameshort], [year]"
     */
    public final ReadOnlyStringPropertyBindingBean getCurrentConcatenatedDatePeropertyShort(){
        return currentServerConcatenatedDateShort.getReadOnlyBooleanPropertyBindingBean();
    }
}