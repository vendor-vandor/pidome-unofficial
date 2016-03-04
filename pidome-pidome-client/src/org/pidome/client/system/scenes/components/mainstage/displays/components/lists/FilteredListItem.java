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

package org.pidome.client.system.scenes.components.mainstage.displays.components.lists;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John
 */
public final class FilteredListItem extends StackPane {
    
    String itemId;
    String itemName;
    
    Map<String,String> categories = new HashMap();
    
    static Logger LOG = LogManager.getLogger(FilteredListItem.class);
    
    public FilteredListItem(String itemId, String itemName, String mainCategoryId, String mainCategoryValue){
        this.itemId = itemId;
        this.itemName = itemName;
        categories.put(mainCategoryId, mainCategoryValue);
        getStyleClass().add("row");
    }
    
    public final String getItemId(){
        return itemId;
    }
    
    public final void setContent(Node node){
        getChildren().add(node);
    }
    
    public final String getName(){
        return itemName;
    }
    
    protected final Map<String,String> getCategories(){
        return categories;
    }
    
    public final void addCategory(String mainCategoryId, String mainCategoryValue){
        categories.putIfAbsent(mainCategoryId, mainCategoryValue);
    }
    
    protected final boolean hasCategory(String mainCategoryId){
        return categories.containsKey(mainCategoryId);
    }
    
}
