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

package org.pidome.client.system.scenes.components.mainstage.displays.visualfloor;

import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.scenes.windows.TitledWindow;

/**
 *
 * @author John
 */
public final class VisualFloors extends TitledWindow  {

    FloorsViewManagement floorWindow;
    
    HBox floorControl = new HBox();
    
    FloorsControl floorsControl;
    
    double maxWidth  = 1400 * DisplayConfig.getWidthRatio();
    double maxHeight = 800 * DisplayConfig.getHeightRatio();
    
    double maxViewPortWidth = 1200 * DisplayConfig.getWidthRatio();
    double maxViewPortHeight = 800 * DisplayConfig.getHeightRatio();
    
    public VisualFloors(Object... numbs) throws Exception {
        this();
    }
    
    public VisualFloors() {
        super("floorsmap", "House plan");
    }

    @Override
    protected void setupContent() {
        if(Platform.isSupported(ConditionalFeature.SCENE3D)){
            floorWindow = new FloorsViewPort3D(maxViewPortWidth-2, maxViewPortHeight-2);
            ((FloorsViewPort3D)floorWindow).setExternalFloorActiveListener(floorsControl);
        } else {
            floorWindow = new FloorsViewPort2D(maxViewPortWidth-2, maxViewPortHeight-2);
        }
        StackPane floorWindowHolder = new StackPane();
        floorWindowHolder.setMinSize(maxViewPortWidth, maxViewPortHeight);
        floorWindowHolder.setMaxSize(maxViewPortWidth, maxViewPortHeight);
        floorWindowHolder.setPrefSize(maxViewPortWidth, maxViewPortHeight);
        floorWindowHolder.getChildren().add(floorWindow);
        floorsControl = new FloorsControl(floorWindow);
        if(Platform.isSupported(ConditionalFeature.SCENE3D)){            
            ((FloorsViewPort3D)floorWindow).setExternalFloorActiveListener(floorsControl);
        }
        floorsControl.setMinWidth(maxWidth - maxViewPortWidth);
        floorsControl.setMaxSize(maxWidth - maxViewPortWidth, maxHeight);
        floorsControl.setPrefWidth(maxWidth - maxViewPortWidth);
        floorsControl.setAlignment(Pos.TOP_LEFT);
        floorControl.getChildren().addAll(floorsControl, floorWindowHolder);
        setContent(floorControl);
        floorsControl.build();
        floorWindow.build();
    }

    @Override
    protected void removeContent() {
        floorWindow.destroy();
    }
    
}
