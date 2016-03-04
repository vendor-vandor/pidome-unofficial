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

package org.pidome.client.scenes.floormap;

import java.io.IOException;
import javafx.scene.image.Image;

/**
 *
 * @author John
 */
public class MeshedMovement extends MeshedItem {
    
    VisualDevice visualDevice;
    
    protected MeshedMovement(Image img, double x, double y) throws IOException {
        super(VisualFloorUtils.createSimpleOnePlanedMesh((float)img.getWidth(), (float)img.getHeight()), img, x, y);
        addPulseHandler();
    }
    
    @Override
    final void addPulseHandler(){
        
    }
    
    @Override
    final void removePulseHandler(){
        
    }
    
}