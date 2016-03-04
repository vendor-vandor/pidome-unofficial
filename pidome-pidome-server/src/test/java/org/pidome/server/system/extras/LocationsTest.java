/*
 * Copyright 2013 John.
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
package org.pidome.server.system.extras;

import org.pidome.server.system.location.BaseLocations;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.pidome.server.system.db.DB;
import org.pidome.server.system.location.LocationServiceException;

/**
 *
 * @author John
 */
public class LocationsTest {
    
    public LocationsTest() {
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
     * Test of getBaseLocations method, of class BaseLocations.
     */
    @Test
    public void testGetLocations() throws LocationServiceException {
        assertNotNull(BaseLocations.getLocations());
    }

    /**
     * Test of getLocation method, of class BaseLocations.
     */
    @Test
    public void testGetLocation() throws LocationServiceException {
        assertNotNull(BaseLocations.getLocation(1));
    }

    /**
     * Test of saveLocation method, of class BaseLocations.
     */
    @Test
    public void testEditLocation() throws LocationServiceException {
        int locationId = 1;
        String name = "test location";
        int floor = 1;
        boolean expResult = true;
        boolean result = BaseLocations.editLocation(locationId, name, floor);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of saveLocation method, of class BaseLocations.
     */
    @Test
    public void testAddLocation() throws LocationServiceException {
        String name = "test location";
        boolean expResult = true;
        int floor = 1;
        boolean result = BaseLocations.saveLocation(name, floor);
        assertEquals(expResult, result);
    }
    
}