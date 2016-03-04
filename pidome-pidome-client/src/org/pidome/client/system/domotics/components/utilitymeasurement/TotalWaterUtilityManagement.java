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

package org.pidome.client.system.domotics.components.utilitymeasurement;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 *
 * @author John
 */
public class TotalWaterUtilityManagement {

    DoubleProperty todayWater   = new SimpleDoubleProperty();
    
    double todayThreshold = 1.5D;
    
    /**
     * updates current water values.
     * @param water
     * @param todayWater 
     */
    protected final void updateWater(double water, double todayWater){
        this.todayWater.set(todayWater);
    }
    
    /**
     * Returns the today water usage property
     * @return 
     */
    public final DoubleProperty getTodayWaterProperty(){
        return this.todayWater;
    }

    /**
     * Sets the threshold.
     * @param threshold 
     */
    protected final void setThreshold(double threshold){
        todayThreshold = threshold;
    }
    
    /**
     * Returns the threshold for today.
     * @return 
     */
    public final double getThreshold(){
        return todayThreshold;
    }
    
}
