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

/**
 *
 * @author John Sirach
 */
public abstract class MathImpl {

    /**
     * Returns the mapped range from lower bound to higher bound.
     * Calculation result 60 = map(6, 0, 10, 0, 100);
     * @param curValue The known value between minCurValue and maxCurvalue
     * @param minCurValue The minimum value of the lower bound range range
     * @param maxCurValue The maximum value of the lower bound range
     * @param minValue The minimum value of the higher bound range
     * @param maxValue The maximum value of the higher bound range
     * @return The calculated value between minValue and maxValue
     */
    public static double map(double curValue, double minCurValue, double maxCurValue, double minValue, double maxValue){
        return (curValue-minCurValue)/(maxCurValue-minCurValue) * (maxValue-minValue) + minValue;
    }
    
    /**
     * Returns the inverse mapped range from lower bound to higher bound.
     * Calculation result 40 = map(6, 0, 10, 0, 100);
     * @param curValue The known value between minCurValue and maxCurvalue
     * @param minCurValue The minimum value of the lower bound range range
     * @param maxCurValue The maximum value of the lower bound range
     * @param minValue The minimum value of the higher bound range
     * @param maxValue The maximum value of the higher bound range
     * @return The calculated value between minValue and maxValue
     */
    public static double mapInverse(double curValue, double minCurValue, double maxCurValue, double minValue, double maxValue){
        return maxValue - ((curValue-minCurValue)/(maxCurValue-minCurValue) * (maxValue-minValue) + minValue) + minValue;
    }

}
