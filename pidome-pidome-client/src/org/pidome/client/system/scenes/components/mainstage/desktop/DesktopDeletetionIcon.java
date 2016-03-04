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

package org.pidome.client.system.scenes.components.mainstage.desktop;

import javafx.geometry.Pos;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;

/**
 *
 * @author John
 */
public class DesktopDeletetionIcon extends StackPane {
    
    Bloom bloom = new Bloom();
    
    public DesktopDeletetionIcon(){
        bloom.setThreshold(0.4);
    }
    
    final Region createIcon(){
        VBox plane = new VBox();
        plane.setAlignment(Pos.TOP_CENTER);
        ImageView image = new ImageView(new ImageLoader("desktop/delete-icon.png", 48, 63).getImage());
        image.setScaleX(DisplayConfig.getWidthRatio());
        image.setScaleY(DisplayConfig.getHeightRatio());
        plane.getChildren().add(image);
        Text iconText = new Text("Delete");
        iconText.setWrappingWidth(100*DisplayConfig.getWidthRatio());
        iconText.setTextAlignment(TextAlignment.CENTER);
        iconText.setStyle("-fx-fill: #d2d2d2;");
        if(DisplayConfig.getRunMode().equals(DisplayConfig.RUNMODE_DEFAULT)){
            DropShadow ds = new DropShadow();
            ds.setRadius(6);
            ds.setBlurType(BlurType.ONE_PASS_BOX);
            ds.setSpread(1.0);
            ds.setColor(Color.web("#000000"));
            iconText.setEffect(ds);
        }
        plane.getChildren().add(iconText);
        getChildren().add(plane);
        image.setTranslateX((getWidth()/2) - (image.getFitWidth()/2));
        
        image.setOnDragDropped((DragEvent event) -> {
            if (event.getGestureSource() instanceof DesktopIconDeletableInterface) {
                event.setDropCompleted(true);
            }
            event.consume();
        });
        image.setOnDragOver((DragEvent event) -> {
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        });
        image.setOnDragEntered((DragEvent event) -> {
            if (event.getGestureSource() instanceof DesktopIconDeletableInterface) {
                event.acceptTransferModes(TransferMode.MOVE);
                image.setEffect(bloom);
            } else {
                
            }
            event.consume();
        });
        image.setOnDragExited((DragEvent event) -> {
            image.setEffect(null);
            event.consume();
        });
        return this;
    }
    
}
