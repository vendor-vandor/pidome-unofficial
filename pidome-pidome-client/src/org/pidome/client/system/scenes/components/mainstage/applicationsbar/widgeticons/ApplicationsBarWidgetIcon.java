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

package org.pidome.client.system.scenes.components.mainstage.applicationsbar.widgeticons;

import java.util.ArrayList;
import javafx.geometry.Pos;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.scenes.components.mainstage.applicationsbar.ApplicationsBarWidgetIcons;
import org.pidome.client.system.scenes.components.mainstage.applicationsbar.ApplicationsBarWidgetIconsException;
import org.pidome.client.system.scenes.components.mainstage.desktop.DesktopIconDeletableInterface;
import org.pidome.client.system.scenes.components.mainstage.displays.components.DragDropMovePane;

/**
 *
 * @author John
 */
public final class ApplicationsBarWidgetIcon extends DragDropMovePane implements DesktopIconDeletableInterface {
    
    int iconPosition;
    
    String widgetPath;
    ArrayList<String> varList;
    
    DraggableApplicationbarWidgetIcon source;
    ApplicationsBarWidgetIcons container;
    
    public ApplicationsBarWidgetIcon(ApplicationsBarWidgetIcons container, DraggableApplicationbarWidgetIcon source){
        this.source = source;
        this.container = container;
        getChildren().add(this.source);
        this.setAlignment(Pos.CENTER);
        setMinWidth(52*DisplayConfig.getWidthRatio());
        setMaxSize(52*DisplayConfig.getWidthRatio(),42*DisplayConfig.getHeightRatio());
        setPrefWidth(52*DisplayConfig.getWidthRatio());
    }
    
    public final void setExtendDetails(String path, ArrayList<String> varList){
        this.widgetPath = path;
        this.varList = varList;
    }
    
    public final String getExtendDetailPath(){
        return this.widgetPath;
    }
    
    public final ArrayList<String> getExtendVarDetails(){
        return this.varList;
    }
    
    public final void setPosition(int position){
        this.iconPosition = position;
    }
    
    public final int getPosition(){
        return this.iconPosition;
    }
    
    @Override
    public final void dragDropDone(){
        try {
            this.container.removeIcon(getExtendDetailPath(), getExtendVarDetails());
        } catch (ApplicationsBarWidgetIconsException ex) {
            ////
        }
        done();
    }
    
    public final void done(){
        getChildren().remove(this.source);
        this.source.destroy();
    }
    
}
