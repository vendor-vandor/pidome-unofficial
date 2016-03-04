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

import javafx.scene.Node;
import javafx.scene.control.Label;
import org.pidome.client.system.scenes.components.mainstage.ApplicationsBar;
import org.pidome.client.system.scenes.components.mainstage.displays.components.TabbedContent;
import org.pidome.client.system.scenes.windows.TitledWindow;

/**
 *
 * @author John
 */
public class ApplicationsBarSettings extends TitledWindow {

    TabbedContent tabs = new TabbedContent();
    
    ApplicationsBarTimeDisplay time;
    
    ApplicationsBar appBar;
    
    public ApplicationsBarSettings(ApplicationsBar appBar) {
        super("applicationsbarsettings", "Applications bar settings");
        tabs.setMinSize(800, 400);
        this.appBar = appBar;
    }

    @Override
    protected void setupContent() {
        tabs.addTabChangedListener((String oldTab, String newTab) -> {
            tabs.setTabContent(newTab,setTabContent(newTab),setTabContentTitle(newTab));
        });
        tabs.addTab("presence", "Presence");
        tabs.addTab("widgets", "Widgets");
        setContent(tabs);
    }

    final String setTabContentTitle(String tabId){
        switch(tabId){
            case "presence":
                return "Set your presence";
            case "widgets":
                return "Set widget icons";
            default:
                return "ERROR";
        }
    }
    
    final Node setTabContent(String tabId){
        switch(tabId){
            case "presence":
                return new ApplicationsBarSettingsUserPresence();
            case "widgets":
                return new Label("Not yet");
            default:
                return new Label("ERROR");
        }
    }
    
    @Override
    protected void removeContent() {
        
    }
    
}
