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

import org.pidome.client.system.scenes.components.mainstage.displays.components.DragDropMovePane;

/**
 *
 * @author John
 */
public class DraggableIcon extends DragDropMovePane {
    
    DraggableIconInterface source;
    
    boolean inDrag = false;
    
    public DraggableIcon(DraggableIconInterface source){
        this.source = source;
    }

    @Override
    public void dragDropDone() {
        this.source.iconRemoved();
    }
    
}
