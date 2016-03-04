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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John
 */
public class GraphDataHistoryHelperSum extends GraphDataHistoryHelper {

    static Logger LOG = LogManager.getLogger(GraphDataHistoryHelperSum.class);

    double fiveMinuteData  = 0.0;
    double halfHourData    = 0.0;
    double curFourHourData = 0.0;
    double curYearData     = 0.0;
    
    DateFormat testFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm, w (EEEE)");
    
    @Override
    void setPreData(String type, int time, double value){
        String dataHourNormal = dateFormatter("day",time);
        String curWeekDay = dateFormatter("week",time);
        String curMonthDay = dateFormatter("month",time);
        String curYearDay = dateFormatter("year",time);
        switch (type) {
            case "year":
                if(yearData.containsKey(curYearDay)){
                    yearData.put(curYearDay, yearData.get(curYearDay)+value);
                } else {
                    yearData.put(curYearDay, value);
                }
            break;
            case "month":
                if(monthData.containsKey(curMonthDay)){
                    monthData.put(curMonthDay, monthData.get(curMonthDay)+value);
                } else {
                    monthData.put(curMonthDay, value);
                }
            break;
            case "week":
                if(weekData.containsKey(curWeekDay)){
                    weekData.put(curWeekDay, weekData.get(curWeekDay)+value);
                } else {
                    weekData.put(curWeekDay, value);
                }
            break;
            case "day":
                if(dayData.containsKey(dataHourNormal)){
                    dayData.put(dataHourNormal, dayData.get(dataHourNormal)+value);
                } else {
                    dayData.put(dataHourNormal, value);
                }
            break;
            case "hour":
                hourData.put(dateFormatter(type,time), value);
            break;
        }
    }
    
    @Override
    public final void handleData(final double data) {
        LOG.debug("I got data: {}", data);
        Date curDate = new Date();
        int curHourMinute = (int) (snapToMinuteCount(curDate).getTime() / 1000);
        boundDataHistory("hour");
        hourData.put(dateFormatter("day",curHourMinute),data);

    }
    
}
