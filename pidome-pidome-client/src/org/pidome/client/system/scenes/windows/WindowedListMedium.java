/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.windows;

import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 *
 * @author John Sirach
 */
public class WindowedListMedium extends TitledWindowBase {

    VBox listedContent = new VBox();
    
    Map<String,Node> content = new HashMap<>();
    ObservableMap<String,Node> contentList = FXCollections.observableMap(content);
    
    String statusBarTitle;
    
    double listWidth;
    
    public WindowedListMedium(String windowId, String windowName) {
        super(windowId, windowName);
        setSize(650, 361);
        getStyleClass().add("listedwindow");
        contentList.addListener((MapChangeListener.Change<? extends String,? extends Node> change) -> {
            if (change.wasAdded()) {
                if (!listedContent.getChildren().contains(change.getValueAdded())) {
                    Node node = change.getValueAdded();
                    if (listedContent.getChildren().size() % 2 != 0) {
                        node.getStyleClass().add("rowodd");
                    } else {
                        node.getStyleClass().add("roweven");
                    }
                    listedContent.getChildren().add(node);
                }
            } else if (change.wasRemoved()) {
                if (listedContent.getChildren().contains(change.getValueRemoved())) {
                    listedContent.getChildren().remove(change.getValueRemoved());
                }
            }
            updateStatusBarTitle(listedContent.getChildren().size());
        });
        listWidth = this.getContentWidth();
        assignContent(listedContent);
    }
    
    
    public final void setStatusBarTitle(String title){
        statusBarTitle = title;
    }
    
    public final void addItem(String id, Node node){
        addItem(id, node, false);
    }
    
    public final void addItem(String id, Node node, boolean active){
        if(node!=null){
            if(active==true){
                node.getStyleClass().add("rowhiglight");
            }
            contentList.put(id, node);
        }
    }
    
    public final void removeItem(String id){
        if(contentList.containsKey(id)){
            contentList.remove(id);
        }
    }

    public final Node getItem(String id){
        if(contentList.containsKey(id)){
            return contentList.get(id);
        }
        return null;
    }

    public final void emptyList(){
        contentList.clear();
    }
    
    public final void highLight(final String id){
        if(content.containsKey(id)){
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        highlightRow(id);
                        Thread.sleep(1000);
                        deHighlightRow(id);
                    } catch (InterruptedException ex) {
                        //// no probs dude
                        deHighlightRow(id);
                    }
                }
            };
            thread.start();
        }
    }
    
    final void updateStatusBarTitle(int amount){
        setBottomLabel(amount + " " + statusBarTitle);
    }
    
    public final void highlightRow(final String id){
        Platform.runLater(() -> {
            content.get(id).getStyleClass().add("rowhiglight");
        });
    }
    
    public final void deHighlightRow(final String id){
        Platform.runLater(() -> {
            content.get(id).getStyleClass().remove("rowhiglight");
        });
    }
    
    @Override
    protected void setupContent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void removeContent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
