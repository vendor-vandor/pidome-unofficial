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

package org.pidome.server.system.categories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.system.db.DB;

/**
 *
 * @author John Sirach
 */
public final class BaseCategories {
    
    private static final Map<String,Map<String,Object>> categories = new HashMap<>();
    private static final Map<String,Map<String,Object>> subCategories = new HashMap<>();
    
    private static List<Map<String,Object>> fullList = null;
    
    static Logger LOG = LogManager.getLogger(BaseCategories.class);
    
    /**
     * Constructor.
     * @throws CategoriesException 
     */
    BaseCategories() throws CategoriesException {
        reload();
    }
    
    /**
     * Reloads categories and sub categories.
     * @throws CategoriesException 
     */
    public static void reload() throws CategoriesException {
        reloadCategories();
        reloadSubcategories();
    }
    
    /**
     * Reloads only categories.
     * @throws CategoriesException 
     */
    public static void reloadCategories() throws CategoriesException {
        LOG.debug("Reload categories");
        fullList = null;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
            categories.clear();
            try (Statement statementCategories = fileDBConnection.createStatement();
                    ResultSet rsCategories = statementCategories.executeQuery("SELECT id,name,description,constant,fixed "
                            + "FROM device_categories")) {
                while (rsCategories.next()) {
                    Map<String, Object> setCategory = new HashMap<>();
                    setCategory.put("id", rsCategories.getInt("id"));
                    setCategory.put("name", rsCategories.getString("name"));
                    setCategory.put("description", rsCategories.getString("description"));
                    setCategory.put("constant", rsCategories.getString("constant"));
                    setCategory.put("fixed", rsCategories.getBoolean("fixed"));
                    categories.put(rsCategories.getString("id"), setCategory);
                    LOG.debug("Loaded category: " + rsCategories.getString("id") + ":" + rsCategories.getString("name"));
                }
            } catch (SQLException ex) {
                LOG.error("Could not load categories: {}", ex.getMessage());
                throw new CategoriesException("Could not load categories: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("Could not load categories: {}", ex.getMessage());
            throw new CategoriesException("Could not load categories: "+ ex.getMessage());
        }
    }
    
    /**
     * Reloads sub categories.
     * @throws CategoriesException 
     */
    public static void reloadSubcategories() throws CategoriesException {
        LOG.debug("Reload sub categories");
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
            subCategories.clear();
            try (Statement statementCategories = fileDBConnection.createStatement();
                 ResultSet rsCategories = statementCategories.executeQuery("SELECT id,name,description,cat_id,fixed FROM device_subcategories")){
                while (rsCategories.next()) {
                    Map<String, Object> setCategory = new HashMap<>();
                    setCategory.put("id", rsCategories.getInt("id"));
                    setCategory.put("name", rsCategories.getString("name"));
                    setCategory.put("description", rsCategories.getString("description"));
                    setCategory.put("cat_id", rsCategories.getInt("cat_id"));
                    setCategory.put("fixed", rsCategories.getBoolean("fixed"));
                    subCategories.put(rsCategories.getString("id"), setCategory);
                    LOG.debug("Loaded sub category: " +rsCategories.getString("id") +":"+ rsCategories.getString("name"));
                }
            } catch (SQLException ex) {
                LOG.error("Could not load sub categories: {}", ex.getMessage());
                throw new CategoriesException("Could not load sub categories: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("Could not load sub categories: {}", ex.getMessage());
            throw new CategoriesException("Could not load sub categories: "+ ex.getMessage());
        }
    }
    
    /**
     * Return the full categorylist including sub categories.
     * @return List<Map<String,String>> as list of <idsubcat, category - subcategory>
     * @throws org.pidome.server.system.categories.CategoriesException
     */
    public static List<Map<String,Object>> getFullCategoryList() throws CategoriesException {
        if(categories.isEmpty() || subCategories.isEmpty()){
            reload();
        }
        if(fullList == null){
            fullList = new ArrayList();
            for(String catId: categories.keySet()){
                for(String subCatId: subCategories.keySet()){
                    if(subCategories.get(subCatId).get("cat_id").equals(categories.get(catId).get("id"))){
                        Map<String,Object> item = new HashMap<>();
                        item.put("id", subCategories.get(subCatId).get("id"));
                        item.put("name", categories.get(catId).get("name") + " - " + subCategories.get(subCatId).get("name"));
                        item.put("constant", categories.get(catId).get("constant"));
                        item.put("fixed", subCategories.get(subCatId).get("fixed"));
                        item.put("category", categories.get(catId).get("id"));
                        fullList.add(item);
                        LOG.debug("Added item: {}", item);
                    }
                }
            }
        }
        return fullList;
    }
    
    /**
     * Returns a full blown sub category including the main category
     * @param subCatIdRequestded
     * @return 
     */
    public static Map<String,Object> getFullSubCategory(int subCatIdRequestded) throws CategoriesException {
        if(categories.isEmpty() || subCategories.isEmpty()){
            reload();
        }
        Map<String,Object> item = new HashMap<>();
        for(String catId: categories.keySet()){
            for(String subCatId: subCategories.keySet()){
                if(Integer.parseInt(subCatId) == subCatIdRequestded){
                    item.put("id",       subCategories.get(subCatId).get("id"));
                    item.put("name",     categories.get(catId).get("name") + " - " + subCategories.get(subCatId).get("name"));
                    item.put("constant", categories.get(catId).get("constant"));
                    item.put("fixed",    subCategories.get(subCatId).get("fixed"));
                    item.put("category", categories.get(catId).get("id"));
                }
            }
        }
        return item;
    }
    
    /**
     * Returns the categories list.
     * @return
     * @throws CategoriesException 
     */
    public static List<Map<String,Object>> getCategoryList() throws CategoriesException {
        if(categories.isEmpty() || subCategories.isEmpty()){
            reload();
        }
        List<Map<String,Object>> returnList = new ArrayList();
        for(String catId: categories.keySet()){
            Map<String,Object> item = new HashMap<>();
            item.put("id", categories.get(catId).get("id"));
            item.put("name", categories.get(catId).get("name"));
            item.put("fixed", categories.get(catId).get("fixed"));
            item.put("description", categories.get(catId).get("description"));
            item.put("constant", categories.get(catId).get("constant"));
            returnList.add(item);
        }
        return returnList;
    }
    
    /**
     * Returns a single category.
     * @param categoryId
     * @return
     * @throws CategoriesException 
     */
    public static Map<String,Object> getCategory(int categoryId) throws CategoriesException {
        if(categories.isEmpty() || subCategories.isEmpty()){
            reload();
        }
        for(String j: categories.keySet()){
            if (categories.get(j).get("id").equals(categoryId)) {
                return categories.get(j);
            }
        }
        throw new CategoriesException("Category with id: "+categoryId+" not found");
    }
    
    /**
     * Saves a category.
     * @param name
     * @param description
     * @param constant
     * @return
     * @throws CategoriesException 
     */
    public static boolean saveCategory(String name, String description, String constant) throws CategoriesException {
        if(categories.isEmpty() || subCategories.isEmpty()){
            reload();
        }
        LOG.debug("Saving category {}", name);
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            PreparedStatement prep;
            prep = fileDBConnection.prepareStatement("insert into 'device_categories' ('name', 'description', 'constant', 'fixed','created','modified') values (?,?,?,0,datetime('now'),datetime('now'))",Statement.RETURN_GENERATED_KEYS);
            prep.setString(1, name);
            prep.setString(2, description);
            prep.setString(3, constant);
            prep.execute();
            final int auto_id;
            try (ResultSet rs = prep.getGeneratedKeys()) {
                if (rs.next()) {
                    auto_id = rs.getInt(1);
                } else {
                    auto_id = 0;
                }
            }
            prep.close();
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", auto_id);
                }
            };
            ClientMessenger.send("CategoryService","addCategory", 0, sendObject);
            saveSubCategory(auto_id, "Common", "Common for " + name, true);
            reload();
            return true;
        } catch (SQLException ex) {
            LOG.error("could not save category: {} ", ex.getMessage());
            throw new CategoriesException("could not save category: "+ ex.getMessage());
        }
    }
    
    public static boolean editCategory(final int id, String name, String description, String constant) throws CategoriesException {
        if(categories.isEmpty() || subCategories.isEmpty()){
            reload();
        }
        LOG.debug("Saving category {}, {}", id,name);
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            PreparedStatement prep;
            prep = fileDBConnection.prepareStatement("update 'device_categories' set 'name'=?, 'description'=?, 'constant'=?,'modified'=datetime('now') where id=? and fixed=0");
            prep.setString(1, name);
            prep.setString(2, description);
            prep.setString(3, constant);
            prep.setInt(4, id);
            prep.executeUpdate();
            prep.close();
            reload();
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", id);
                }
            };
            ClientMessenger.send("CategoryService","editCategory", 0, sendObject);
            return true;
        } catch (SQLException ex) {
            LOG.error("could not save category: {} ", ex.getMessage());
            throw new CategoriesException("could not edit category: "+ ex.getMessage());
        }
    }
    
    /**
     * Deletes a category
     * @param id
     * @return
     * @throws CategoriesException 
     */
    public static boolean deleteCategory(final int id) throws CategoriesException {
        LOG.debug("Deleting category {}, {}", id);
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            PreparedStatement prep;
            prep = fileDBConnection.prepareStatement("delete FROM 'device_categories' where id=? and fixed=0");
            prep.setInt(1, id);
            prep.executeUpdate();
            prep.close();
            reload();
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", id);
                }
            };
            ClientMessenger.send("CategoryService","deleteCategory", 0, sendObject);
            return true;
        } catch (SQLException ex) {
            LOG.error("could not delete category: {} ", ex.getMessage());
            throw new CategoriesException("could not delete category: "+ ex.getMessage());
        }
    }
    
    /**
     * Saves a sub category.
     * @param catId
     * @param name
     * @param description
     * @return
     * @throws CategoriesException 
     */
    public static boolean saveSubCategory(int catId, String name, String description) throws CategoriesException {
        return saveSubCategory(catId, name, description, false);
    }
    
    /**
     * Saves a sub category.
     * @param catId
     * @param name
     * @param description
     * @param fixed
     * @return
     * @throws CategoriesException 
     */
    static boolean saveSubCategory(int catId, String name, String description, boolean fixed) throws CategoriesException {
        if(categories.isEmpty() || subCategories.isEmpty()){
            reload();
        }
        LOG.debug("Saving sub category {} in catId: {}", name,catId);
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            PreparedStatement prep;
            prep = fileDBConnection.prepareStatement("insert into 'device_subcategories' ('name', 'description', 'cat_id', 'fixed', 'created', 'modified') values (?,?,?,?,datetime('now'),datetime('now'))",Statement.RETURN_GENERATED_KEYS);
            prep.setString(1, name);
            prep.setString(2, description);
            prep.setInt(3, catId);
            prep.setBoolean(4, fixed);
            prep.execute();
            final int auto_id;
            try (ResultSet rs = prep.getGeneratedKeys()) {
                if (rs.next()) {
                    auto_id = rs.getInt(1);
                } else {
                    auto_id = 0;
                }
            }
            prep.close();
            reload();
            fileDBConnection.close();
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", auto_id);
                }
            };
            ClientMessenger.send("CategoryService","addSubCategory", 0, sendObject);
            return true;
        } catch (SQLException ex) {
            LOG.error("could not save subcategory: {}", ex.getMessage());
            throw new CategoriesException("could not save subcategory: "+ ex.getMessage());
        }
    }
    
    public static boolean editSubCategory(final int id, int catId, String name, String description) throws CategoriesException {
        if(categories.isEmpty() || subCategories.isEmpty()){
            reload();
        }
        LOG.debug("Saving sub category {}, {}", id,name);
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            PreparedStatement prep;
            prep = fileDBConnection.prepareStatement("update 'device_subcategories' set 'name'=?, 'description'=?, 'cat_id'=?, 'modified'=datetime('now') where id=? and fixed=0");
            prep.setString(1, name);
            prep.setString(2, description);
            prep.setInt(3, catId);
            prep.setInt(4, id);
            prep.executeUpdate();
            prep.close();
            reload();
            fileDBConnection.close();
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", id);
                    put("name", name);
                    put("description", description);
                    put("category", catId);
                }
            };
            ClientMessenger.send("CategoryService","editSubCategory", 0, sendObject);
            return true;
        } catch (SQLException ex) {
            LOG.error("could not save subcategory: {}", ex.getMessage());
            throw new CategoriesException("could not save subcategory: "+ ex.getMessage());
        }
    }
    
    /**
     * Deletes a sub category.
     * @param id
     * @return
     * @throws CategoriesException 
     */
    public static boolean deleteSubCategory(final int id) throws CategoriesException {
        LOG.debug("Deleting sub category {}, {}", id);
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            PreparedStatement prep;
            prep = fileDBConnection.prepareStatement("delete FROM 'device_subcategories' where id=? and fixed=0");
            prep.setInt(1, id);
            prep.executeUpdate();
            prep.close();
            reload();
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", id);
                }
            };
            ClientMessenger.send("CategoryService","deleteSubCategory", 0, sendObject);
            return true;
        } catch (SQLException ex) {
            LOG.error("could not delete category: {} ", ex.getMessage());
            throw new CategoriesException("could not delete category: "+ ex.getMessage());
        }
    }
    
    /**
     * Returns the full sub category list.
     * @return
     * @throws CategoriesException 
     */
    public static Map<String,Object> getSubCategoryList() throws CategoriesException {
        if(categories.isEmpty() || subCategories.isEmpty()){
            reload();
        }
        Map<String,Object> items = new HashMap<>();
        for(String catId: subCategories.keySet()){
            items.put(catId, subCategories.get(catId).get("name"));
        }
        return items;
    }
    
    /**
     * Returns a sub category list based on category.
     * @param catId
     * @return
     * @throws CategoriesException 
     */
    public static List<Map<String,Object>> getSubCategoryList(int catId) throws CategoriesException {
        if(categories.isEmpty() || subCategories.isEmpty()){
            reload();
        }
        List<Map<String,Object>> returnList = new ArrayList();
        for (String subCatId : subCategories.keySet()) {
            if (subCategories.get(subCatId).get("id").equals(catId)) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", subCategories.get(subCatId).get("id"));
                item.put("name", subCategories.get(subCatId).get("name"));
                returnList.add(item);
            }
        }
        return returnList;
    }
    
    /**
     * Returns a sub category.
     * @param subCatId
     * @return
     * @throws CategoriesException 
     */
    public static Map<String,Object> getSubCategory(Integer subCatId) throws CategoriesException {
        if(categories.isEmpty() || subCategories.isEmpty()){
            reload();
        }
        if(subCategories.containsKey(subCatId.toString())) {
            return subCategories.get(subCatId.toString());
        } else {
            throw new CategoriesException("Sub category id: "+subCatId+" does not exist");
        }
    }
    
}
