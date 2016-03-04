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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.pidome.server.system.categories.BaseCategories;
import org.pidome.server.system.categories.CategoriesException;

/**
 *
 * @author John
 */
public class CategoryServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements CategoryServiceJSONRPCWrapperInterface {

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("addCategory", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("name", "");}});
                        put(1,new HashMap<String,Object>(){{put("description", "");}});
                        put(2,new HashMap<String,Object>(){{put("constant", "");}});
                    }
                });
                put("editCategory", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("name", "");}});
                        put(2,new HashMap<String,Object>(){{put("description", "");}});
                        put(3,new HashMap<String,Object>(){{put("constant", "");}});
                    }
                });
                put("addSubCategory", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("category", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("name", "");}});
                        put(2,new HashMap<String,Object>(){{put("description", "");}});
                    }
                });
                put("editSubCategory", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("category", 0L);}});
                        put(2,new HashMap<String,Object>(){{put("name", "");}});
                        put(3,new HashMap<String,Object>(){{put("description", "");}});
                    }
                });
                put("deleteCategory", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getCategory", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("deleteSubCategory", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getSubCategory", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getFullCategoryList", null);
                put("getCategoryList", null);
                put("getSubCategoryList", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getFullSubcategory", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
            }
        };
        return mapping;
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<Map<String, Object>> getFullCategoryList() throws CategoriesException {
        return BaseCategories.getFullCategoryList();
    }
    /**
     * @inheritDoc
     */
    @Override
    public List<Map<String, Object>> getCategoryList() throws CategoriesException {
        return BaseCategories.getCategoryList();
    }
    /**
     * @inheritDoc
     */
    @Override
    public Map<String, Object> getCategory(Long categoryId) throws CategoriesException {
        return BaseCategories.getCategory(categoryId.intValue());
    }
    /**
     * @inheritDoc
     */
    @Override
    public List<Map<String,Object>> getSubCategoryList(Long id) throws CategoriesException {
        return BaseCategories.getSubCategoryList(id.intValue());
    }
    /**
     * @inheritDoc
     */    
    @Override
    public Map<String, Object> getFullSubcategory(Long subCatId) throws CategoriesException {
        return BaseCategories.getFullSubCategory(subCatId.intValue());
    }
    
    /**
     * @inheritDoc
     */    
    @Override
    public Map<String, Object> getSubCategory(Long id) throws CategoriesException {
        return BaseCategories.getSubCategory(id.intValue());
    }
    /**
     * @inheritDoc
     */
    @Override
    public boolean addCategory(String name, String description, String constant) throws CategoriesException {
        return BaseCategories.saveCategory(name, description, constant);
    }
    /**
     * @inheritDoc
     */
    @Override
    public boolean editCategory(Long id, String name, String description, String constant) throws CategoriesException {
        return BaseCategories.editCategory(id.intValue(), name, description, constant);
    }
    /**
     * @inheritDoc
     */
    @Override
    public boolean addSubCategory(Long catId, String name, String description) throws CategoriesException {
        return BaseCategories.saveSubCategory(catId.intValue(), name, description);
    }
    /**
     * @inheritDoc
     */
    @Override
    public boolean editSubCategory(Long id, Long catId, String name, String description) throws CategoriesException {
        return BaseCategories.editSubCategory(id.intValue(), catId.intValue(), name, description);
    }
    /**
     * @inheritDoc
     */
    @Override
    public boolean deleteCategory(Long id) throws CategoriesException {
        return BaseCategories.deleteCategory(id.intValue());
    }
    /**
     * @inheritDoc
     */
    @Override
    public boolean deleteSubCategory(Long id) throws CategoriesException {
        return BaseCategories.deleteSubCategory(id.intValue());
    }

}
