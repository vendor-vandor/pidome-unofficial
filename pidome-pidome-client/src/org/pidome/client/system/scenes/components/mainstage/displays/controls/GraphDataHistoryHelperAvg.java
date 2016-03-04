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

import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John
 */
public class GraphDataHistoryHelperAvg extends GraphDataHistoryHelper {
    
    static Logger LOG = LogManager.getLogger(GraphDataHistoryHelperAvg.class);

    int dayCounter   = 1;
    double totalDay  = 0.0d;
    String lastDay   = "";
    
    int weekCounter  = 1;
    double totalWeek = 0.0d;
    String lastWeek  = "";
    
    int monthCounter = 1;
    double totalMonth= 0.0d;
    String lastMonth = "";
    
    int yearCounter  = 1;
    double totalYear = 0.0d;
    String lastYear  = "";
    
    @Override
    void setPreData(String type, int time, double value){
        String dataHourNormal = dateFormatter("day",time);
        lastDay = dataHourNormal;
        String curWeekDay = dateFormatter("week",time);
        lastWeek = curWeekDay;
        String curMonthDay = dateFormatter("month",time);
        lastMonth = curMonthDay;
        String curYearDay = dateFormatter("year",time);
        lastYear = curYearDay;
        switch (type) {
            case "year":
                yearData.put(curYearDay, value);
            break;
            case "month":
                monthData.put(curMonthDay, value);
            break;
            case "week":
                weekData.put(curWeekDay, value);
            break;
            case "day":
                dayData.put(dataHourNormal, value);
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
