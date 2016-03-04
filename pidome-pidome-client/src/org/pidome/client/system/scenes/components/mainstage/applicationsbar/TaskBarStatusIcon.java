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

package org.pidome.client.system.scenes.components.mainstage.applicationsbar;

import java.util.HashMap;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;

/**
 *
 * @author John
 */
public class TaskBarStatusIcon {
        
    HashMap<String, Image> iconViews = new HashMap<>();

    String iconName;
    ImageView imgContainer = new ImageView();

    public TaskBarStatusIcon(String icon){
        iconName = icon;
        Image offImage = new ImageLoader("notificationbar/" + iconName + "-off.png",22,22).getImage();
        Image onImage = new ImageLoader("notificationbar/" + iconName + "-on.png",22,22).getImage();

        iconViews.put("off", offImage);
        iconViews.put("on", onImage);

        imgContainer.setImage(iconViews.get("off"));
        imgContainer.setPreserveRatio(true);
        imgContainer.setFitWidth(22 * DisplayConfig.getWidthRatio());

    }

    public final void swapStatus(String status){
        imgContainer.setImage(iconViews.get(status));
    }

    public final ImageView getImage(){
        return imgContainer;
    }

}
    
