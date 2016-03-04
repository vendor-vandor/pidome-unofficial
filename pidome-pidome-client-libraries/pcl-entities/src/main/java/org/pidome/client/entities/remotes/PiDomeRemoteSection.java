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
public class PiDomeRemoteSection {
    
    private ArrayList<PiDomeRemoteRow> rowList = new ArrayList<PiDomeRemoteRow>();
    
    private int sectionId;
    private String sectionName;
    
    public PiDomeRemoteSection(Map<String,Object> sectionData)    {
        Map<String,Object> data = (Map<String,Object>)sectionData.get("section");
        sectionId   = ((Long)data.get("id")).intValue();
        sectionName = (String)data.get("name");
        for(Map<String,Object>rowData:(ArrayList<Map<String,Object>>)data.get("rows")){
            rowList.add(new PiDomeRemoteRow(rowData));
        }
    }
    
    /**
     * Get the section id;
     * @return int id of the section.
     */
    public final int getId(){
        return this.sectionId;
    }
    
    /**
     * Get's the section names.
     * @return The section's name.
     */
    public final String getName(){
        return this.sectionName;
    }
    
    /**
     * Returns the rows in this section;
     * @return int with row amount
     */
    public final ArrayList<PiDomeRemoteRow> getRows(){
        return this.rowList;
    }
    
}
