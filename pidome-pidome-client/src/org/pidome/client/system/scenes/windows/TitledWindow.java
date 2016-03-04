/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.windows;

import javafx.scene.layout.Region;

/**
 *
 * @author John Sirach
 */
public abstract class TitledWindow extends TitledWindowBase {

    public TitledWindow(String windowId, String windowName) {
        super(windowId, windowName);
    }

    public TitledWindow(WindowComponent parent, String windowId, String windowName) {
        super(parent, windowId, windowName);
    }
    
    public TitledWindow(String windowId, String windowName, double width, double height) {
        super(windowId, windowName);
        setSize(width, height);
    }
    
    public TitledWindow(WindowComponent parent, String windowId, String windowName, double width, double height) {
        super(parent, windowId, windowName);
        setSize(width, height);
    }
    
    public final void setToolbarText(String text){
        setBottomLabel(text);
    }
    
    public final void setContent(Region region){
        assignContent(region);
    }
    
    @Override
    protected abstract void setupContent();
    @Override
    protected abstract void removeContent();
    
}
