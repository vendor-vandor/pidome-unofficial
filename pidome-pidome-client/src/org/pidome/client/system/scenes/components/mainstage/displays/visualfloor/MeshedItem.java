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

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 *
 * @author John
 */
abstract class MeshedItem extends MeshView implements VisualDeviceInterface {
    
    double x = 0;
    double y = 0;
    
    PhongMaterial texturedMaterial;
    boolean playing = false;
    
    Image image;
    
    protected MeshedItem(TriangleMesh mesh, Image image, double x, double y){
        super(mesh);
        this.image = image;
        this.x = x;
        this.y = y;
        setMaterial();
    }
    
    protected final Image getImage(){
        return image;
    }
    
    /**
     * Add pulse and click handlers
     */
    final void addHandlers(){
        addPulseHandler();
    }
    
    final void removeHandlers(){
        removePulseHandler();
    }
    
    @Override
    public final void destroy(){
        removeHandlers();
    }
    
    @Override
    public double getPosX() {
        return this.x;
    }

    @Override
    public double getPosY() {
        return this.y;
    }
    
    private void setMaterial(){
        texturedMaterial = new PhongMaterial();
        texturedMaterial.setDiffuseMap(image);
        texturedMaterial.setSpecularColor(Color.TRANSPARENT);
        setScaleX(-1); //// Texture is mirrorred (? still having troubles with the triangle meshes), reverse.
        materialProperty().setValue(texturedMaterial);
    }
    
    public void pulse(final int length){
        if(texturedMaterial!=null && !playing){
            playing = true;
            Thread runIt = new Thread(){
                @Override
                public final void run(){
                    try {
                        Platform.runLater(() -> { texturedMaterial.setSelfIlluminationMap(image); });
                        Thread.sleep(length);
                    } catch (InterruptedException ex) {
                        Platform.runLater(() -> { texturedMaterial.setSelfIlluminationMap(null); playing = false; });
                    }
                    Platform.runLater(() -> { texturedMaterial.setSelfIlluminationMap(null); playing = false; });
                }
            };
            runIt.start();
        }
    }
    
    abstract void addPulseHandler();
    abstract void removePulseHandler();
    
    @Override
    public final void setAnimate(boolean animate){
        if(animate){addPulseHandler();} else {removePulseHandler();}
    }
    
}
