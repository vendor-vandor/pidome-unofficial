/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.utilities.math;

/**
 *
 * @author John
 */
public class MathUtilities {

    /**
     * The Java 8 copy.
     * @param x Number to divide
     * @param y Number dividing with
     * @return The result rounded down.
     */
    public static int floorDiv(int x, int y) {
        int r = x / y;
        // if the signs are different and modulo not zero, round down
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }

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
     * Returns the mapped range from lower bound to higher bound.
     * Calculation result 60 = map(6, 0, 10, 0, 100);
     * @param curValue The known value between minCurValue and maxCurvalue
     * @param minCurValue The minimum value of the lower bound range range
     * @param maxCurValue The maximum value of the lower bound range
     * @param minValue The minimum value of the higher bound range
     * @param maxValue The maximum value of the higher bound range
     * @return The calculated value between minValue and maxValue
     */
    public static float map(float curValue, float minCurValue, float maxCurValue, float minValue, float maxValue){
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