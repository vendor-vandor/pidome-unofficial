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

package org.pidome.server.services.http.rpc;

import java.util.List;
import java.util.Map;
import org.pidome.server.system.categories.CategoriesException;

/**
 *
 * @author John
 */
public interface CategoryServiceJSONRPCWrapperInterface {
    
    /**
     * Retrieves the full category list where categories and sub categories are combined and the id is the link id for devices.
     * @return
     * @throws CategoriesException
     */
    public List<Map<String,Object>> getFullCategoryList() throws CategoriesException;
    
    /**
     * Retrieves only the categories.
     * @return
     * @throws CategoriesException 
     */
    public List<Map<String,Object>> getCategoryList() throws CategoriesException;
    
    /**
     * Get a single category.
     * @param categoryId
     * @return
     * @throws CategoriesException 
     */
    public Map<String,Object> getCategory(Long categoryId) throws CategoriesException;
    
    /**
     * Get the list of sub categories.
     * @param id
     * @return
     * @throws CategoriesException 
     */
    public List<Map<String,Object>> getSubCategoryList(Long id) throws CategoriesException;
    
    /**
     * Get a single sub category.
     * @param id
     * @return
     * @throws CategoriesException 
     */
    public Map<String,Object> getSubCategory(Long id) throws CategoriesException;
    
    /**
     * Returns a full blown sub category including main category.
     * @param subCatId
     * @return
     * @throws CategoriesException 
     */
    public Map<String,Object> getFullSubcategory(Long subCatId) throws CategoriesException;
    
    /**
     * Adds a category.
     * @param name
     * @param description
     * @param constant
     * @return
     * @throws CategoriesException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean addCategory(String name, String description, String constant) throws CategoriesException;
    
    /**
     * Edits a category.
     * @param id
     * @param name
     * @param description
     * @param constant
     * @return
     * @throws CategoriesException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean editCategory(Long id, String name, String description, String constant) throws CategoriesException;
    
    /**
     * Deletes a category.
     * @param id
     * @return
     * @throws CategoriesException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean deleteCategory(Long id) throws CategoriesException;
            
            
    /**
     * Adds a sub category.
     * @param catId
     * @param name
     * @param description
     * @return
     * @throws CategoriesException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean addSubCategory(Long catId, String name, String description) throws CategoriesException;
    
    /**
     * Edits a sub category.
     * @param id
     * @param catId
     * @param name
     * @param description
     * @return
     * @throws CategoriesException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean editSubCategory(Long id, Long catId, String name, String description) throws CategoriesException;
    
    /**
     * Deletes a sub category.
     * @param id
     * @return
     * @throws CategoriesException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean deleteSubCategory(Long id) throws CategoriesException;
    
}
