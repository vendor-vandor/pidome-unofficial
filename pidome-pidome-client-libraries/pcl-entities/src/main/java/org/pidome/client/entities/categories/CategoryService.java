/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.categories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.Entity;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.client.system.PCCConnectionNameSpaceRPCListener;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.utilities.properties.ObservableArrayListBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 *
 * @author John
 */
public class CategoryService extends Entity implements PCCConnectionNameSpaceRPCListener {

    static {
        Logger.getLogger(CategoryService.class.getName()).setLevel(Level.ALL);
    }
    
    private boolean initialized = false;
    
    /**
     * The server connection.
     */
    PCCConnectionInterface connection;
    
    /**
     * List of known categories.
     */
    private final ObservableArrayListBean<Category> categories = new ObservableArrayListBean<>();
    
    /**
     * A read only wrapper for the categories list.
     */
    private final ReadOnlyObservableArrayListBean<Category> readOnlyCategories = new ReadOnlyObservableArrayListBean<>(categories);
    
    /**
     * Constructor
     * @param connection The server connection.
     */
    public CategoryService(PCCConnectionInterface connection){
        this.connection = connection;
    }
    
    /**
     * Initializes a connection listener for the category service.
     */
    @Override
    protected void initilialize() {
        if(!initialized){
            this.connection.addPCCConnectionNameSpaceListener("CategoryService", this);
        }
        initialized = true;
    }

    /**
     * removes a connection listener for the category service.
     */
    @Override
    protected void release() {
        this.connection.removePCCConnectionNameSpaceListener("CategoryService", this);
    }

    /**
     * Loads the initial category list.
     * @throws CategoryServiceException 
     */
    private void loadInitialCategoriesList() throws CategoryServiceException {
        if(categories.isEmpty()){
            try {
                handleRPCCommandByResult(this.connection.getJsonHTTPRPC("CategoryService.getFullCategoryList", null, "CategoryService.getFullCategoryList"));
            } catch (PCCEntityDataHandlerException ex) {
                throw new CategoryServiceException(ex);
            }
        }
    }
    
    /**
     * Preloads the categories.
     * @throws EntityNotAvailableException When the whole macro entity is unavailable.
     */
    @Override
    public void preload() throws EntityNotAvailableException {
        if(!loaded){
            loaded = true;
            try {
                loadInitialCategoriesList();
            } catch (CategoryServiceException ex) {
                throw new EntityNotAvailableException("Could not preload categories list", ex);
            }
        }
    }

    /**
     * Reloads categories.
     * @throws EntityNotAvailableException 
     */
    @Override
    public void reload() throws EntityNotAvailableException {
        loaded = false;
        categories.clear();
        preload();
    }

    /**
     * Returns a single category.
     * Use this only for quick viewing, refer to getting a full list as categories
     * can be deleted where you should be notified of.
     * @param categoryId the id of the category you want.
     * @return A Category.
     * @throws CategoryNotAvailableException When the requested c ategory is not available.
     */
    public final Category getCategory(int categoryId) throws CategoryNotAvailableException {
        for(Category cat:this.categories){
            if (cat.getCategoryId()==categoryId){
                return cat;
            }
        }
        throw new CategoryNotAvailableException();
    }
    
    /**
     * Returns a list of categories.
     * @return a read only observable list of categories.
     */
    public final ReadOnlyObservableArrayListBean<Category> getCategories(){
        return this.readOnlyCategories;
    }
    
    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        switch(rpcDataHandler.getMethod()){
            case "addSubCategory":
                try {
                    Map<String,Object> params = new HashMap<>();
                    params.put("id", rpcDataHandler.getParameters().get("id"));
                    handleRPCCommandByResult(this.connection.getJsonHTTPRPC("CategoryService.getFullSubcategory", params, "CategoryService.getFullSubcategory"));
                } catch (PCCEntityDataHandlerException ex) {
                    Logger.getLogger(CategoryService.class.getName()).log(Level.SEVERE, null, ex);
                }
            break;
            case "deleteSubCategory":
                try {
                    Category cat = getCategory(((Number)rpcDataHandler.getParameters().get("id")).intValue());
                    this.categories.remove(cat);
                } catch (CategoryNotAvailableException ex) {
                    Logger.getLogger(CategoryService.class.getName()).log(Level.SEVERE, null, ex);
                }
            break;
            case "editSubCategory":
                try {
                    getCategory(((Number)rpcDataHandler.getParameters().get("id")).intValue()).update(rpcDataHandler.getParameters());
                } catch (CategoryNotAvailableException ex) {
                    Logger.getLogger(CategoryService.class.getName()).log(Level.SEVERE, null, ex);
                }
            break;
        }
    }

    @Override
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler) {
        switch((String)rpcDataHandler.getId()){
            case "CategoryService.getFullCategoryList":
                List<Map<String,Object>> cats = (List<Map<String,Object>>)rpcDataHandler.getResult().get("data");
                List<Category> toAdd = new ArrayList<>();
                for(Map<String,Object> cat:cats){
                    toAdd.add(new Category(cat));
                }
                this.categories.addAll(toAdd);
            break;
            case "CategoryService.getFullSubcategory":
                this.categories.add(new Category((Map<String,Object>)rpcDataHandler.getResult().get("data")));
            break;
        }
    }

    @Override
    public void unloadContent() throws EntityNotAvailableException {
        this.categories.clear();
    }
    
}