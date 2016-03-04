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
package org.pidome.server.system.hardware;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.pidome.server.connector.tools.PlatformException;
import org.pidome.server.connector.tools.Platforms;

/**
 *
 * @author John
 */
public class HardwareTest {
    
    static Hardware instance;
    
    public HardwareTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        instance = new Hardware();
        instance.init();
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
     * Test of init method, of class Hardware.
     */
    @Test
    public void testInit() {
        instance.init();
    }

    /**
     * Test of start method, of class Hardware.
     */
    @Test
    public void testStart() {
        instance.start();
    }

    /**
     * Test of discover method, of class Hardware.
     */
    @Test
    public void testDiscover() {
        instance.discover();
    }

}