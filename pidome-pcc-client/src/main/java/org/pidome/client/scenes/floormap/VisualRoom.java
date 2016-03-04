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

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 *
 * @author John
 */
public class VisualRoom {
    
    int roomId = 0;
    String roomName = "";
    SimpleDoubleProperty x = new SimpleDoubleProperty(0.0);
    SimpleDoubleProperty y = new SimpleDoubleProperty(0.0);
    SimpleDoubleProperty w = new SimpleDoubleProperty(0.0);
    SimpleDoubleProperty h = new SimpleDoubleProperty(0.0);
    
    public VisualRoom(int id, String name, double x, double y, double width, double height){
        this.roomId = id;
        this.roomName = name;
        if(width<0){
            this.x.set(x + width);
            this.w.set(Math.abs(width));
        } else {
            this.x.set(x);
            this.w.set(width);
        }
        if(height<0){
            this.y.set(y + height);
            this.h.set(Math.abs(height));
        } else {
            this.y.set(y);
            this.h.set(height);
        }
    }
    
    protected final String getRoomName(){
        return roomName;
    }
    
    protected final int getRoomId(){
        return this.roomId;
    }
    
    protected final ReadOnlyDoubleProperty getXProperty(){
        return (ReadOnlyDoubleProperty)this.x;
    }
    
    protected final ReadOnlyDoubleProperty getYProperty(){
        return (ReadOnlyDoubleProperty)this.y;
    }
    
    protected final ReadOnlyDoubleProperty getWidthProperty(){
        return (ReadOnlyDoubleProperty)this.w;
    }
    
    protected final ReadOnlyDoubleProperty getHeightProperty(){
        return (ReadOnlyDoubleProperty)this.h;
    }
    
}
