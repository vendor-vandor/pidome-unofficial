/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.tools;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author John
 */
public class MathImplTest {
    
    public MathImplTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of map method, of class MathImpl.
     */
    @Test
    public void testMap() {
        /*
        System.out.println("map");
        double curValue = 0.0;
        double minCurValue = 0.0;
        double maxCurValue = 0.0;
        double minValue = 0.0;
        double maxValue = 0.0;
        double expResult = 0.0;
        double result = MathImpl.map(curValue, minCurValue, maxCurValue, minValue, maxValue);
        assertEquals(expResult, result, 0.0);
        */
    }

    /**
     * Test of mapInverse method, of class MathImpl.
     */
    @Test
    public void testMapInverse() {
        /*
        System.out.println("mapInverse");
        double curValue = 0.0;
        double minCurValue = 0.0;
        double maxCurValue = 0.0;
        double minValue = 0.0;
        double maxValue = 0.0;
        double expResult = 0.0;
        double result = MathImpl.mapInverse(curValue, minCurValue, maxCurValue, minValue, maxValue);
        assertEquals(expResult, result, 0.0);
        */
    }

    /**
     * Test of GeoDistance method, of class GeoLocation.
     */
    @Test
    public void testGeoDistance() {
        System.out.println("geoDistance");
        float latitute1 = 52.37F;
        float longitude1 = 4.89F;
        float latitude2 = 51.92F;
        float longitude2 = 4.47F;
        float expResult = 57664F; ///  roughly 57.7Km.
        float result = Math.round(MathImpl.GeoDistance(latitute1, longitude1, latitude2, longitude2));
        assertEquals(expResult, result, 0.0);
    }
    
}
