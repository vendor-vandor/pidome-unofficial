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

import org.pidome.server.system.categories.BaseCategories;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.pidome.server.system.categories.CategoriesException;

/**
 *
 * @author John
 */
public class DeviceCategoriesTest {
    
    public DeviceCategoriesTest() {
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
     * Test of reload method, of class BaseCategories.
     */
    @Test
    public void testReload() throws CategoriesException {
        BaseCategories.reload();
    }

    /**
     * Test of reloadCategories method, of class BaseCategories.
     */
    @Test
    public void testReloadCategories() throws CategoriesException {
        BaseCategories.reloadCategories();
    }

    /**
     * Test of reloadSubcategories method, of class BaseCategories.
     */
    @Test
    public void testReloadSubcategories() throws CategoriesException {
        BaseCategories.reloadSubcategories();
    }

    /**
     * Test of getFullCategoryList method, of class BaseCategories.
     */
    @Test
    public void testGetFullCategoryList() throws CategoriesException {
        assertNotNull(BaseCategories.getFullCategoryList());
    }

    /**
     * Test of getCategoryList method, of class BaseCategories.
     */
    @Test
    public void testGetCategoryList() throws CategoriesException {
        assertNotNull(BaseCategories.getCategoryList());
    }

    /**
     * Test of getCategory method, of class BaseCategories.
     */
    @Test
    public void testGetCategory() throws CategoriesException {
        assertNotNull(BaseCategories.getCategory(1));
    }

    /**
     * Test of saveCategory method, of class BaseCategories.
     */
    @Test
    public void testSaveCategory() throws CategoriesException {
        String name = "testname";
        String description = "testdesc";
        String constant = "testConst";
        boolean expResult = true;
        boolean result = BaseCategories.saveCategory(name, description, constant);
        assertEquals(expResult, result);
    }

    /**
     * Test of saveSubCategory method, of class BaseCategories.
     */
    @Test
    public void testSaveSubCategory() throws CategoriesException {
        String name = "testsubname";
        String description = "testsubdesc";
        boolean expResult = true;
        boolean result = BaseCategories.saveSubCategory(0, name, description);
        assertEquals(expResult, result);
    }

    /**
     * Test of getSubCategoryList method, of class BaseCategories.
     */
    @Test
    public void testGetSubCategoryList_0args() throws CategoriesException {
        assertNotNull(BaseCategories.getSubCategoryList());
    }

    /**
     * Test of getSubCategoryList method, of class BaseCategories.
     */
    @Test
    public void testGetSubCategoryList_String() throws CategoriesException {
        assertNotNull(BaseCategories.getSubCategoryList(1));
    }

    /**
     * Test of getSubCategory method, of class BaseCategories.
     */
    @Test
    public void testGetSubCategory() throws CategoriesException {
        assertNotNull(BaseCategories.getSubCategory(1));
    }
}