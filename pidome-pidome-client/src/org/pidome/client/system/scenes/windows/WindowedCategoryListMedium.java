/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.windows;

import javafx.scene.Node;
import org.pidome.client.system.scenes.components.mainstage.displays.components.lists.FilteredListItem;
import org.pidome.client.system.scenes.components.mainstage.displays.components.lists.FilteredList;

/**
 *
 * @author John Sirach
 */
public abstract class WindowedCategoryListMedium extends TitledWindowBase {

    FilteredList listedContent = new FilteredList(null);
    
    double listWidth;
    
    String statusBarTitle;
    
    public WindowedCategoryListMedium(String windowId, String windowName) {
        super(windowId, windowName);
        setSize(650, 361);
        getStyleClass().add("listedwindow");
        listWidth = this.getContentWidth();
        assignContent(listedContent);
    }
    
    public final void setStatusBarTitle(String title){
        statusBarTitle = title;
    }
    
    public final void addItem(String category, String id, Node node){
        if(node!=null){
            FilteredListItem item = new FilteredListItem(id, id, category, category);
            item.setContent(node);
            listedContent.addItem(item);
            updateStatusBarTitle(listedContent.getItemsAmount());
        }
    }
    
    public final void removeItem(String category, String id){
        listedContent.removeItem(id);
        updateStatusBarTitle(listedContent.getItemsAmount());
    }

    public final Node getItem(String category, String id){
        return listedContent.getItem(id);
    }
    
    public final void updateItemCategory(String oldCat, String newCat, String itemId){
        //listedContent.updateItemCategory(oldCat, newCat, itemId);
        updateStatusBarTitle(listedContent.getItemsAmount());
    }
    
    public final void highlightRow(String category, String itemId){
        listedContent.highlightRow(itemId);
    }
    
    final void updateStatusBarTitle(int amount){
        setBottomLabel(amount + " " + statusBarTitle);
    }
    
}
