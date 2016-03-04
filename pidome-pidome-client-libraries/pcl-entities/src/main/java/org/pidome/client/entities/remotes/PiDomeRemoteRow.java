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

package org.pidome.client.entities.remotes;

import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author John
 */
public class PiDomeRemoteRow {
    
    private ArrayList<PiDomeRemoteButton> buttonList = new ArrayList<PiDomeRemoteButton>();
    
    private int rowId;
    private int cells;
    
    public PiDomeRemoteRow(Map<String,Object> rowData){
        Map<String,Object> data = (Map<String,Object>)rowData.get("row");
        rowId = ((Long)data.get("id")).intValue();
        cells = ((Long)data.get("cells")).intValue();
        for(Map<String,Object>button:(ArrayList<Map<String,Object>>)data.get("buttons")){
            buttonList.add(new PiDomeRemoteButton(button));
        }
    }
    
    public final int getId(){
        return this.rowId;
    }
    
    public final int getCellsAmount(){
        return this.cells;
    }
    
    public final ArrayList<PiDomeRemoteButton> getButtons(){
        return this.buttonList;
    }
    
}
