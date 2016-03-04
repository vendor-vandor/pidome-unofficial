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
package org.pidome.server.system.config;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author John
 */
public class SystemConfigTest {
    
    public SystemConfigTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws ConfigException {
        SystemConfig.initialize();
    }

    /**
     * Test of getItem method, of class SystemConfig.
     */
    @Test
    public void testGetItemLegit() throws Exception {
        assertNotNull(SystemConfig.getProperty("system", "server.releasename"));
    }

    @Test (expected=ConfigPropertiesException.class)
    public void testGetItemNotLegit() throws Exception {
        SystemConfig.getProperty("system", "server.nonexisting");
    }
    
}