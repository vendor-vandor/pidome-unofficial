/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.misc.utils;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.logging.log4j.LogManager;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.pidome.server.connector.shareddata.SharedDataStatusSetter;
import org.pidome.server.connector.tools.properties.ObjectPropertyBindingBean;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;

/**
 * Utility class for some date and time functions
 * @author John Sirach
 */
public final class TimeUtils {
    
    private static final ObjectPropertyBindingBean currentTime         = new ObjectPropertyBindingBean();
    private static final ObjectPropertyBindingBean currentTimeAsInt    = new ObjectPropertyBindingBean();
    private static final ObjectPropertyBindingBean currentShortDayName = new ObjectPropertyBindingBean();
    private static final ObjectPropertyBindingBean weekDayType         = new ObjectPropertyBindingBean();
    private static final ObjectPropertyBindingBean sunrise             = new ObjectPropertyBindingBean();
    private static final ObjectPropertyBindingBean sunset              = new ObjectPropertyBindingBean();
    
    private static Long sunriseTime = 0L;
    private static Long sunsetTime  = 0L;
    
    private static boolean sunIsSet = false;
    
    private static String internalCurDate = "";
    private static final Map<String, String> eventTimeHelper = new HashMap<>();
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("HH:mm");
    private static final DateTimeFormatter fullDateTimeFormatter = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm");
    private static final DateTimeFormatter shortDateStringRepresentation = DateTimeFormat.forPattern("E, d MMM yyyy");
    private static final DateTimeFormatter shortDayOfWeekName = DateTimeFormat.forPattern("E");
    private static final DateTimeFormatter dayOfWeekName = DateTimeFormat.forPattern("EEEE");
    private static final DateTimeFormatter monthOfYearName = DateTimeFormat.forPattern("MMMM");
    
    private static final GregorianCalendar cal = new GregorianCalendar(TimeZone.getDefault());
    
    private static String currentTimeZone  = "Europe/Amsterdam";
    private static String currentLatitude  = "51.950000";
    private static String currentLongitude = "4.449999";
    
    private static Thread timeUpdater;
    
    private static DateTime dt;
    
    private static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(TimeUtils.class);
    
    private static List<MinuteListener> minuteListeners = new ArrayList();
    
    private static TimeUtils self;
    
    public TimeUtils (){
        setDateTime();
        self = this;
    }
    
    /**
     * Returns the current military time property.
     * @return 
     */
    public static final ObjectPropertyBindingBean getCurrentTimeProperty(){
        return currentTime;
    }
    
    /**
     * Returns the current military time as int property.
     * @return 
     */
    public static final ObjectPropertyBindingBean getCurrentTimeAsIntProperty(){
        return currentTimeAsInt;
    }
    
    /**
     * Returns the current military time property.
     * @return 
     */
    public static final ObjectPropertyBindingBean getCurrentSunriseProperty(){
        return sunrise;
    }
    
    /**
     * Returns the current military time property.
     * @return 
     */
    public static final ObjectPropertyBindingBean getCurrentSunsetProperty(){
        return sunset;
    }
    
    /**
     * Returns the current short day name.
     * @return 
     */
    public static final ObjectPropertyBindingBean getCurrentShortDayNameProperty(){
        return currentShortDayName;
    }
    
    /**
     * Returns weekday type property.
     * @return 
     */
    public static final ObjectPropertyBindingBean getWeekDayTypeProperty(){
        return weekDayType;
    }
    
    public static TimeUtils getInstance(){
        return self;
    }
    
    /**
     * Date time string
     * @return 
     */
    public static String getCurrentTimeDate(){
        return dt.toString(fullDateTimeFormatter);
    }
    
    /**
     * Returns the current time in military time.
     * @return 
     */
    public static String getCurrentMilitaryTime(){
        return dt.toString(dateTimeFormatter);
    }
    
    /**
     * Returns current time in long.
     * @return 
     */
    public final long getTime(){
        return dt.getMillis();
    }    
    
    /**
     * Returns the internally class used date.
     * @return String
     */
    public String getDDMMYYYYdate(){
        return internalCurDate;
    }
    
    /**
     * A short date representation.
     * @return 
     */
    public final String getShortDateTextRepresentation(){
        return dt.toString(shortDateStringRepresentation);
    }
    
    /**
     * Get current day of month.
     * @return 
     */
    public final int getDayOfMonth(){
        return dt.getDayOfMonth();
    }

    /**
     * Get current week day name.
     * @return 
     */
    public final String getDayName(){
        return dt.toString(dayOfWeekName);
    }
    
    /**
     * Get the current month number
     * @return 
     */
    public final int getMonth(){
        return dt.getMonthOfYear();
    }
    
    /**
     * Returns the current month's name.
     * @return 
     */
    public final String getMonthName(){
        return dt.toString(monthOfYearName);
    }
    
    /**
     * Returns the four digit year.
     * @return 
     */
    public final int getYear(){
        return dt.getYear();
    }
    
    /**
     * Returns the current day name.
     * @return 
     */
    public String getCurrentDayName(){
        return dt.dayOfWeek().getAsShortText().toUpperCase();
    }
    
    /**
     * Returns time in military format
     * @return String
     */
    public String get24HoursTime(){
        return compose24Hours(dt.getHourOfDay(), dt.getMinuteOfHour());
    }
    
    /**
     * Returns time in military format with seconds
     * @return String
     */
    public String getFull24HoursTime(){
        return compose24Hours(dt.getHourOfDay(), dt.getMinuteOfHour()) + ":" + MiscImpl.setZeroLeading(String.valueOf(dt.getSecondOfMinute()), 2);
    }
    
    /**
     * Returns date.
     * @return String YYY-MM-DD
     */
    public String getYYYYMMDDDate(){
        return dt.getYear() + "-" + MiscImpl.setZeroLeading(String.valueOf(dt.getMonthOfYear()),2) + "-" + MiscImpl.setZeroLeading(String.valueOf(dt.getDayOfMonth()),2);
    }
    
    /**
     * Creates military time from hours and minutes from strings.
     * @param sHour
     * @param sMinute
     * @return String military time
     */
    public static String compose24Hours(String sHour, String sMinute){
        return MiscImpl.setZeroLeading(sHour, 2) + ":" + MiscImpl.setZeroLeading(sMinute, 2);
    }
    
    /**
     * Creates military time from hours and minutes from ints.
     * @param sHour
     * @param sMinute
     * @return String military time.
     */
    public static String compose24Hours(int sHour, int sMinute){
        return MiscImpl.setZeroLeading(String.valueOf(sHour), 2) + ":" + MiscImpl.setZeroLeading(String.valueOf(sMinute), 2);
    }
    
    /**
     * Creates dd-mm-YYYY
     * @param day
     * @param month
     * @param year
     * @return String as formatted dd-mm-yyyy
     */
    public static String composeDDMMYYYYDate(int day, int month, int year){
        return MiscImpl.setZeroLeading(String.valueOf(day),2)+"-"+MiscImpl.setZeroLeading(String.valueOf(month),2)+"-"+MiscImpl.setZeroLeading(String.valueOf(year),2);
    }
    
    /**
     * The amount of seconds in the current minute.
     * @return int
     */
    public int getSecondOfMinute(){
        return dt.getSecondOfMinute();
    } 
    
    /**
     * Returns the current day of the week.
     * @return 
     */
    public int getDayOfWeek(){
        return dt.getDayOfWeek();
    }
    
    /**
     * Sets localized time settings.
     */
    private static void setNewLocalizedValues() {
        eventTimeHelper.clear();
        try {
            currentTimeZone = SystemConfig.getProperty("system", "server.timezone");
            currentLatitude = SystemConfig.getProperty("system", "server.latitude");
            currentLongitude= SystemConfig.getProperty("system", "server.longitude");
        } catch (ConfigPropertiesException ex) {
            LOG.debug("Could not set timezone data");
        }
        LOG.info("Setting timezone data: {}, {}, {}", currentTimeZone, currentLatitude, currentLongitude);
        TimeZone.setDefault(TimeZone.getTimeZone(currentTimeZone));
        cal.setTimeZone(TimeZone.getDefault());
        SharedDataStatusSetter.setNewLatLonStatus(currentLatitude, currentLongitude);
        Location location = new Location(currentLatitude, currentLongitude);
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, currentTimeZone);
        String officialSunrise = calculator.getOfficialSunriseForDate(cal);
        String officialSunset = calculator.getOfficialSunsetForDate(cal);

        sunsetTime = calculator.getOfficialSunsetCalendarForDate(cal).getTime().getTime();
        sunriseTime = calculator.getOfficialSunriseCalendarForDate(cal).getTime().getTime();

        SharedDataStatusSetter.setSunRiseSet(sunriseTime, sunsetTime);
        
        eventTimeHelper.put("SUNRISE", officialSunrise);
        eventTimeHelper.put("SUNSET", officialSunset);
        
        sunrise.setValue(officialSunrise);
        sunset.setValue(officialSunset);
        
        currentShortDayName.setValue(dt.toString(shortDayOfWeekName).toUpperCase());
        
        switch((String)currentShortDayName.getValue()){
            case "SAT":
            case "SUN":
                weekDayType.setValue("WEEKEND");
            break;
            default:
                weekDayType.setValue("WEEKDAY");
            break;
        }
    }
    
    /**
     * Sets the new time zone settings
     * @param timeZone
     * @param latitude
     * @param longitude 
     */
    public static void setNewLocalizedTimeZoneData(String timeZone, String latitude, String longitude){
        SystemConfig.setProperty("system", "server.timezone",timeZone);
        SystemConfig.setProperty("system", "server.latitude",latitude);
        SystemConfig.setProperty("system", "server.longitude",longitude);
        try {
            SystemConfig.store("system", null);
        } catch (IOException ex) {
            LOG.error("Could not store new timezone settings");
        }
        currentTimeZone = timeZone;
        currentLatitude = latitude;
        currentLongitude = longitude;
        setNewLocalizedValues();
        setDateTime();
        notifyTimeUpdate();
    }
    
    /**
     * Returns the current timezone
     * @return 
     */
    public static String getCurrentTimeZone(){
        return currentTimeZone;
    }
    
    /**
     * Returns the current latitude
     * @return 
     */
    public static float getCurrentLatitude(){
        return Float.parseFloat(currentLatitude);
    }

    /**
     * Returns the current longitude
     * @return 
     */
    public static float getCurrentLongitude(){
        return Float.parseFloat(currentLongitude);
    }
    
    /**
     * Sunrise
     * @return string sunrise military 
     */
    public static String getSunrise(){
        if(eventTimeHelper.containsKey("SUNRISE")){
            return eventTimeHelper.get("SUNRISE");
        } else {
            return "00:00";
        }
    }
    
    /**
     * Returns sunset military
     * @return 
     */
    public static String getSunset(){
        if(eventTimeHelper.containsKey("SUNSET")){
            return eventTimeHelper.get("SUNSET");
        } else {
            return "00:00";
        }
    }
    
    /**
     * Calculates time difference base on minutes or hours.
     * Given a military time string with -10m or +10h it will return the time corresponding to this calculation
     * @param origTime The original time military format
     * @param differ This only takes whole military time notation including + or - signs: +00:10,+10:00,-01:26,-01:00
     * @return 
     */
    public static String calcTimeDiff(String origTime, String differ){
        LocalTime time = dateTimeFormatter.parseLocalTime(origTime);
        LocalTime differTime = dateTimeFormatter.parseLocalTime(differ.substring(1));
        LocalTime differStartTime = dateTimeFormatter.parseLocalTime("00:00");
        Long duration = new Duration(differStartTime.getMillisOfDay(), differTime.getMillisOfDay()).getStandardMinutes();
        if (differ.startsWith("-")) {
            return time.minusMinutes(duration.intValue()).toString(DateTimeFormat.forPattern("HH:mm"));
          } else if (differ.startsWith("+")) {
            return time.plusMinutes(duration.intValue()).toString(DateTimeFormat.forPattern("HH:mm"));
        } 
        return origTime;
    }
    
    /**
     * Sets new date and time
     */
    private static void setDateTime(){
        dt = new DateTime(DateTimeZone.forTimeZone(TimeZone.getDefault()));
        String realCurDate = composeDDMMYYYYDate(dt.getDayOfMonth(),dt.getMonthOfYear(),dt.getYear());
        if(!internalCurDate.equals(realCurDate)){
            TimeUtils.setNewLocalizedValues();
            internalCurDate = realCurDate;
        }
    }
    
    /**
     * Add listeners for changed time.
     * @param l 
     */
    public static synchronized void addMinuteListener( MinuteListener l ) {
        if(!minuteListeners.contains(l)) minuteListeners.add( l );
    }
    
    /**
     * Removes listeners for changed time.
     * @param l 
     */
    public static synchronized void removeMinuteListener( MinuteListener l ) {
        if(minuteListeners.contains(l)) minuteListeners.remove( l );
    }
    
    /**
     * Thread for dispatching every minute.
     */
    public final synchronized void startTimeThread(){
        if(timeUpdater==null){
            timeUpdater = new Thread(){
                @Override
                public final void run(){
                    while(true){
                        setDateTime();
                        Integer nextrun = 60 - getSecondOfMinute();
                        Runnable notifier = () -> {
                            cal.setTime(new Date());
                            notifyTimeUpdate();
                        };
                        notifier.run();
                        try {
                            Thread.sleep(nextrun * 1000L);
                        } catch (InterruptedException e) {

                        }
                    }
                }
            };
            timeUpdater.setName("THREAD:TimeMinuteUpdater");
            timeUpdater.start();
        }
    }
 
    private static void notifyTimeUpdate() {
        currentTime.setValue(getCurrentMilitaryTime());
        currentTimeAsInt.setValue(Integer.parseInt(getCurrentMilitaryTime().replace(":", "")));
        try {
            Iterator listeners = minuteListeners.iterator();
            while (listeners.hasNext()) {
                ((MinuteListener) listeners.next()).handleMinuteUpdate(self);
            }
        } catch(Exception ex) {  
            /// in case other code fails
        }
        SharedDataStatusSetter.setNewTimeStatus(cal);
    }

}