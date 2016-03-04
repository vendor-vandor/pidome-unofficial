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

package org.pidome.client.system.time;

import java.util.Map;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.domotics.DomComponents;
import org.pidome.client.system.domotics.DomResourceException;
import org.pidome.client.system.domotics.Domotics;
import org.pidome.client.system.domotics.DomoticsEvent;
import org.pidome.client.system.domotics.DomoticsEventListener;
import org.pidome.client.system.domotics.components.dayparts.DayParts;
import org.pidome.client.system.domotics.components.dayparts.DayPartsEvent;
import org.pidome.client.system.domotics.components.dayparts.DayPartsEventListener;
import org.pidome.client.system.scenes.components.mainstage.ApplicationsBar;

/**
 *
 * @author John
 */
public final class ClientTime implements DomoticsEventListener,DayPartsEventListener,ClientDataConnectionListener {

    static StringProperty dayName     = new SimpleStringProperty("");
    static StringProperty monthName   = new SimpleStringProperty("");
    static StringProperty time24HName = new SimpleStringProperty("");
    
    static StringProperty longDateString = new SimpleStringProperty("");
    static StringProperty shortDateString = new SimpleStringProperty("");
    
    static IntegerProperty dayNumber  = new SimpleIntegerProperty(0);
    static IntegerProperty yearNumber = new SimpleIntegerProperty(0);
    static IntegerProperty monthNumber = new SimpleIntegerProperty(0);
    
    static String dayPartName = "";
    
    static Logger LOG = LogManager.getLogger(ApplicationsBar.class);
    
    public ClientTime(){
        Domotics.addDomoticsListener(this);
        DayParts.addDayPartsEventListener(this);
        ClientData.addClientDataConnectionListener(this);
    }
    
    @Override
    public void handleClientDataConnectionEvent(final ClientDataConnectionEvent event) {
        switch (event.getEventType()) {
            case ClientDataConnectionEvent.SYSRECEIVED:
                Map<String, Object> workData = (Map<String, Object>) event.getData();
                switch (event.getMethod()) {
                    case "time":
                        dayName.setValue((String)workData.get("dayname"));
                        monthName.setValue((String) workData.get("monthname"));
                        time24HName.setValue((String) workData.get("time"));
                        
                        dayNumber.setValue(((Long) workData.get("day")).intValue());
                        yearNumber.setValue(((Long) workData.get("year")).intValue());
                        monthNumber.setValue(((Long) workData.get("month")).intValue());
                        
                        setLongDateString();
                        setShortDateString();
                        
                    break;
                }
            break;
        }
    }
    
    final void setLongDateString(){
        longDateString.setValue(dayName.getValueSafe() + dayPartName + ", " + monthName.getValueSafe() + " " + dayNumber.get() + ", " + yearNumber.get());
    }
    
    final void setShortDateString(){
        shortDateString.setValue(monthName.getValueSafe() + " " + dayNumber.getValue() + ", " + yearNumber.getValue());
    }
    
    @Override
    public void handleDayPartsEvent(DayPartsEvent event) {
        switch(event.getEventType()){
            case DayPartsEvent.DAYPARTCHANGED:
                dayPartName = " " + event.getDayPartName();
                setLongDateString();
            break;
            case DayPartsEvent.DAYPARTUPDATED:
                if(DayParts.getCurrent()==event.getDayPartId()){
                    dayPartName = " " + event.getDayPartName();
                    setLongDateString();
                }
            break;
            case DayPartsEvent.DAYPARTREMOVED:
                if(DayParts.getCurrent()==event.getDayPartId()){
                    dayPartName = "";
                    setLongDateString();
                }
            break;
        }
    }
    
    @Override
    public void handleDomoticsEvent(DomoticsEvent event) {
        switch (event.getEventType()) {
            case DomoticsEvent.INITDATARECEIVED:
                Runnable getTime = () -> {
                    try {
                        Map<String,Object> workData = (Map<String,Object>)DomComponents.getInstance().getJSONData("SystemService.getCurrentTime", null).getResult().get("data");
                        dayName.setValue((String)workData.get("dayname"));
                        monthName.setValue((String) workData.get("monthname"));
                        time24HName.setValue((String) workData.get("time"));
                        
                        dayNumber.setValue(((Long) workData.get("day")).intValue());
                        yearNumber.setValue(((Long) workData.get("year")).intValue());
                        monthNumber.setValue(((Long) workData.get("month")).intValue());
                        
                        setLongDateString();
                        setShortDateString();
                    } catch (DomResourceException ex) {
                        LOG.error("Could not set initial time, wait for update.{}", ex.getMessage());
                    }
                };
                getTime.run();
            break;
        }
    }
    
    public static ReadOnlyStringProperty getLongDateString(){
        return (ReadOnlyStringProperty)longDateString;
    }
    
    public static ReadOnlyStringProperty getDayNameProperty(){
        return (ReadOnlyStringProperty)dayName;
    }

    public static ReadOnlyStringProperty getMonthNameProperty(){
        return (ReadOnlyStringProperty)monthName;
    }
    
    public static ReadOnlyStringProperty get24HourNameProperty(){
        return (ReadOnlyStringProperty)time24HName;
    }
    
    public static ReadOnlyIntegerProperty getDayNumberProperty(){
        return (ReadOnlyIntegerProperty)dayNumber;
    }
    
    public static ReadOnlyIntegerProperty getYearNumberProperty(){
        return (ReadOnlyIntegerProperty)yearNumber;
    }
    
    public static ReadOnlyIntegerProperty getMonthNumberProperty(){
        return (ReadOnlyIntegerProperty)monthNumber;
    }
    
    public static StringProperty getShortDateStringProperty(){
        return shortDateString;
    }
    
}
