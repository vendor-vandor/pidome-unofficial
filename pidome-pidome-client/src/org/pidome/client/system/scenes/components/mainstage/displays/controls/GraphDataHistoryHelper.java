/*
 * Copyright 2014 John.
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

package org.pidome.client.system.scenes.components.mainstage.displays.controls;

import com.sun.javafx.collections.UnmodifiableObservableMap;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John
 */
public abstract class GraphDataHistoryHelper {
    
    static Logger LOG = LogManager.getLogger(GraphDataHistoryHelper.class);
    
    Map<String, Double> hourMapRaw = new LinkedHashMap<>();
    ObservableMap<String, Double> hourData = FXCollections.observableMap(hourMapRaw);

    Map<String, Double> dayMapRaw = new LinkedHashMap<>();
    ObservableMap<String, Double> dayData = FXCollections.observableMap(dayMapRaw);
    
    Map<String, Double> weekMapRaw = new LinkedHashMap<>();
    ObservableMap<String, Double> weekData = FXCollections.observableMap(weekMapRaw);
    
    Map<String, Double> monthMapRaw = new LinkedHashMap<>();
    ObservableMap<String, Double> monthData = FXCollections.observableMap(monthMapRaw);

    Map<String, Double> yearMapRaw = new LinkedHashMap<>();
    ObservableMap<String, Double> yearData = FXCollections.observableMap(yearMapRaw);
    
    DateFormat hourFormat = new SimpleDateFormat("HH:mm");
    DateFormat dayFormat = new SimpleDateFormat("HH:00 (E)");
    DateFormat weekFormat = new SimpleDateFormat("EEEE (w)");
    DateFormat monthFormat = new SimpleDateFormat("dd-MM");
    DateFormat yearFormat = new SimpleDateFormat("w (Y)");
    
    String currentHour = dayFormat.format(new Date());
    
    DateFormat dayCheckFormat = new SimpleDateFormat("EEEE");
    String currentDay = dayCheckFormat.format(new Date());
    
    public abstract void handleData(final double data);
    
    final String dateFormatter(String type, Integer time){
        switch (type) {
            case "year":
                return yearFormat.format(new Date(time*1000L));
            case "month":
                return monthFormat.format(new Date(time*1000L));
            case "week":
                return weekFormat.format(new Date(time*1000L));
            case "day":
                return dayFormat.format(new Date(time*1000L));
            default:
                return hourFormat.format(new Date(time*1000L));
        }
    }
    
    final void clearData(){
        hourData.clear();
        dayData.clear();
        weekData.clear();
        monthData.clear();
        yearData.clear();
    }
    
    public final ObservableMap<String, Double> getDataConnection(String timeLine){
        switch (timeLine) {
            case "year":
                return yearData;
            case "month":
                return monthData;
            case "week":
                return weekData;
            case "day":
                return dayData;
            default:
                return hourData;
        }
    }
    
    public final Map<String, Double> getInitialSet(String timeLine){
        switch (timeLine) {
            case "year":
                return yearData;
            case "month":
                return monthData;
            case "week":
                return weekData;
            case "day":
                return dayData;
            default:
                return hourData;
        }
    }
    
    final void removeFromSet(String type, Map<String, Double> map){
        int amount;
        switch(type){
            case "day":
                amount = 24;
            break;
            case "week":
                amount = 7;
            break;
            case "month":
                amount = 31;
            break;
            case "year":
                amount = 52;
            break;
            default:
                amount = 60;
            break;
        }
        if(!map.isEmpty() && map.size()>amount){
            while(map.size()>amount){
                String keytoRemove = map.keySet().iterator().next();
                map.remove(keytoRemove);
            }
        }
    }
    
    final void boundDataHistory(String histType) {
        switch (histType) {
            case "day":
                removeFromSet(histType, dayData); /// max a day
            break;
            case "week":
                removeFromSet(histType, weekData); /// Max a week
            break;
            case "month":
                removeFromSet(histType, monthData); /// Max a month
            break;
            case "year":
                removeFromSet(histType, yearData); /// Max a month
            break;
            default:
                removeFromSet(histType, hourData); ///max an hour
            break;
        }
    }
    
    void setPreData(String type, int time, double value){
        boundDataHistory(type);
        switch (type) {
            case "year":
                yearData.put(dateFormatter("year",time), value);
            break;
            case "month":
                monthData.put(dateFormatter("month",time), value);
            break;
            case "week":
                weekData.put(dateFormatter("week",time), value);
            break;
            case "day":
                dayData.put(dateFormatter("day",time), value);
            break;
            case "hour":
                hourData.put(dateFormatter(type,time), value);
            break;
        }
    }
    
    final Date snapToMinuteCount(Date origDate) {
        return new Date((long) Math.floor((origDate.getTime() / (1000.0 * 60 * 1))) * (1000 * 60 * 1));
    }

    final Date snapToHourCount(Date origDate) {
        return new Date((long) Math.floor((origDate.getTime() / (1000.0 * 60 * 60))) * (1000 * 60 * 60));
    }
    
}
