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

package org.pidome.client.system.scenes.components.mainstage.displays;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.domotics.components.plugins.PidomeMediaPluginException;
import org.pidome.client.system.domotics.components.remotes.PiDomeRemote;
import org.pidome.client.system.domotics.components.remotes.PiDomeRemotes;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;
import org.pidome.client.system.scenes.components.mainstage.desktop.NewDesktopShortcut;
import org.pidome.client.system.scenes.components.mainstage.displays.components.lists.FilteredList;
import org.pidome.client.system.scenes.components.mainstage.displays.components.lists.FilteredListItem;
import org.pidome.client.system.scenes.windows.TitledWindow;
import org.pidome.client.system.scenes.windows.WindowManager;

/**
 *
 * @author John
 */
public class RemotesListDisplay extends TitledWindow {

    FilteredList list = new FilteredList(null);
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(RemotesListDisplay.class);
    
    public RemotesListDisplay(Object... numbs) throws Exception {
        this();
    }
    
    public RemotesListDisplay(){
        super("remoteslist", "Remotes list", 650, 361);
        getStyleClass().add("macrosdisplay");
        setToolbarText("Remotes");
        list.setListSize(650, 361);
        list.build();
    }
    
    private void createInitialRemotes(){
        ObservableList<PiDomeRemote> remotes = PiDomeRemotes.getRemotes();
        remotes.stream().forEach((remote) -> {
            list.addItem(createRemoteListItem(remote.getId(), remote.getName(), remote.getDescription()));
        });
        setToolbarText(list.getItemsAmount() + " remotes");
    }
    
    private FilteredListItem createRemoteListItem(int id, String name, String desc){
        FilteredListItem item = new FilteredListItem(String.valueOf(id), name, "remote", "default");
        item.setContent(createRemoteBar(id, name, desc));
        return item;
    }
    
    private HBox createRemoteBar(int id, String name, String desc){
        final HBox remoteBox = new HBox();
        remoteBox.setMinWidth(getContentWidth());
        
        final ImageView defaultIconView = new ImageView(new ImageLoader("plugin_cat/remote-small.png", 33,33).getImage());
        
        remoteBox.getChildren().add(defaultIconView);
        
        VBox remoteDetails = new VBox();
        
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("name");
        VBox.setMargin(nameLabel, new Insets(0,
                                             0,
                                             0,
                                             5*DisplayConfig.getWidthRatio()));
        Label descLabel = new Label(desc);
        VBox.setMargin(descLabel, new Insets(0,
                                             0,
                                             0,
                                             5*DisplayConfig.getWidthRatio()));
        descLabel.getStyleClass().add("description");

        remoteDetails.getChildren().add(nameLabel);
        remoteDetails.getChildren().add(descLabel);
        remoteBox.getChildren().add(remoteDetails);
        HBox.setMargin(remoteBox, new Insets(0,
                                            0,
                                            5*DisplayConfig.getHeightRatio(),
                                            0));
        Map<String,Object> shortcutOptions = new HashMap<>();
        shortcutOptions.put("id", id);
        shortcutOptions.put("favorite", true);
        NewDesktopShortcut creator = new NewDesktopShortcut(nameLabel);
        creator.setServerCall("RemotesService.setFavorite", shortcutOptions);
        remoteBox.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
            try {
                WindowManager.openWindow(new RemoteDisplay(String.valueOf(id)));
            } catch (Exception ex) {
                LOG.error("Could not open remote: {}", id, ex);
            }
        });
        
        return remoteBox;
    }
    
    @Override
    protected void setupContent() {
        createInitialRemotes();
        setContent(list);
    }

    @Override
    protected void removeContent() {
        list.destroy();
    }
    
}
