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

package org.pidome.client.system.domotics.components.remotes;

import java.util.Map;
import javafx.scene.paint.Color;
import org.pidome.client.utils.ColorImpl;

/**
 *
 * @author John
 */
public class PiDomeRemoteButton {
    
    private String id;
    private int pos;
    
    private String cat;
    private String buttonDesc;
    private String label;
    
    private String type;
    private String buttonFunc;
    private Color buttonColor;
    
    private Category buttonTypeCat;
    
    private String sendSignal;
    
    public enum Category {
        DEFAULT,COLOR,PREDEF;
    }
    
    public PiDomeRemoteButton(Map<String,Object> buttonData){
        this.id = (String)buttonData.get("id");
        this.pos = ((Long)buttonData.get("pos")).intValue();
        this.cat = (String)buttonData.get("cat");
        this.buttonDesc = (String)buttonData.get("sdesc");
        this.label = (String)buttonData.get("label");
        switch((String)buttonData.get("type")){
            case "btn_col":
                buttonTypeCat = Category.COLOR;
            break;
            case "btn_def":
                buttonTypeCat = Category.DEFAULT;
            break;
            default:
                buttonTypeCat = Category.PREDEF;
            break;
        }
        this.type = (String)buttonData.get("type");
        this.buttonFunc = (String)buttonData.get("func");
        this.buttonColor = ColorImpl.hexToRgbColor((String)buttonData.get("color"));
        this.sendSignal = (String)buttonData.get("signal");
    }
    
    public final String getId(){
        return this.id;
    }
    
    public final int getPos(){
        return this.pos;
    }
    
    public final String getCategory(){
        return this.cat;
    }
    
    public final String getDescription(){
        return this.buttonDesc;
    }
    
    public final String getLabel(){
        return this.label;
    }
    
    public final String getType(){
        return this.type;
    }
    
    public final Category getButtonTypeCategory(){
        return this.buttonTypeCat;
    }
    
    public final String getFunction(){
        return this.buttonFunc;
    }
    
    public final Color getColor(){
        return this.buttonColor;
    }
    
    public final String getSignal(){
        return this.sendSignal;
    }
    
}
