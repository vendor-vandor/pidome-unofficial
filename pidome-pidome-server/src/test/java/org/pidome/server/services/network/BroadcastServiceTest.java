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
package org.pidome.server.services.network;

import java.io.IOException;
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
public class BroadcastServiceTest {
    
    static BroadcastService instance;
    
    public BroadcastServiceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        try {
            instance = new BroadcastService();
        } catch (IOException ex) {
            fail("Could not instantiate a broadcast service: " + ex.getMessage());
        }
    }
    
    @AfterClass
    public static void tearDownClass() {
        instance = null;
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getIpAddress method, of class BroadcastService.
     */
    @Test
    public void testGetIpAddress() {
        String expResult = "";
        String result = instance.getIpAddress();
        assertEquals(expResult, result);
    }

}