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

import java.util.ArrayList;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.domotics.DomComponents;
import org.pidome.client.system.domotics.components.DomComponent;

/**
 *
 * @author John
 */
public class PiDomeRemotes implements DomComponent,ClientDataConnectionListener {

    private static ArrayList<PiDomeRemote> remotes = new ArrayList<>();
    private static ObservableList<PiDomeRemote> observableRemotes = FXCollections.observableArrayList(remotes);
    
    DomComponents dom;
    
    public PiDomeRemotes(DomComponents dom, ArrayList<Map<String,Object>> remotes){
        this.dom = dom;
        remotes.stream().forEach((remoteData) -> {
            createRemote(remoteData);
        });
    }
    
    @Override
    public void handleClientDataConnectionEvent(ClientDataConnectionEvent event) {
        ////
    }
    
    private void createRemote(Map<String,Object> remote){
        observableRemotes.add(new PiDomeRemote(dom,remote));
    }
    
    public static ObservableList<PiDomeRemote> getRemotes(){
        return observableRemotes;
    }
    
    public static PiDomeRemote getRemote(int remoteId) throws PiDomeRemotesException {
        for (PiDomeRemote observableRemote : observableRemotes) {
            if (observableRemote.getId() == remoteId) {
                return observableRemote;
            }
        }
        throw new PiDomeRemotesException("Remote id: " + remoteId + "Not found");
    }
    
}