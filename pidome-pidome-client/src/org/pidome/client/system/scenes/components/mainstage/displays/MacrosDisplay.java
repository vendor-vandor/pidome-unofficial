/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays;

import static java.util.Collections.emptyList;
import java.util.HashMap;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.components.macros.Macro;
import org.pidome.client.system.domotics.components.macros.MacroStateEvent;
import org.pidome.client.system.domotics.components.macros.MacroStateEventListener;
import org.pidome.client.system.domotics.components.macros.Macros;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;
import org.pidome.client.system.scenes.components.mainstage.desktop.NewDesktopShortcut;
import org.pidome.client.system.scenes.components.mainstage.displays.components.lists.FilteredListItem;
import org.pidome.client.system.scenes.components.mainstage.displays.components.lists.FilteredList;
import org.pidome.client.system.scenes.windows.TitledWindow;

/**
 *
 * @author John Sirach
 */
public class MacrosDisplay extends TitledWindow implements MacroStateEventListener {

    static Logger LOG = LogManager.getLogger(MacrosDisplay.class);
    
    FilteredList list = new FilteredList(null);
    
    String currentActive = "";
    
    public MacrosDisplay(Object... numbs) throws Exception {
        this();
    }
    
    public MacrosDisplay(){
        super("Macros", "Macros", 650, 361);
        getStyleClass().add("macrosdisplay");
        setToolbarText("macros");
        list.setListSize(650, 361);
        list.build();
    }
    
    final void createInitialMacros(){
        Map<Integer,Macro> macros = Macros.getMacros();
        for(Macro macro: macros.values()){
            list.addItem(createMacroListItem(macro.getId(), macro.getMacroInfoMap()));
            setToolbarText(list.getItemsAmount() + " macros");
        }
    }
    
    final FilteredListItem createMacroListItem(int macroId, Map<String,Object> macro){
        FilteredListItem item = new FilteredListItem(String.valueOf(macroId), (String)macro.get("name"), "macro", "default");
        item.setContent(createMacroBar(macroId, macro));
        return item;
    }
    
    HBox createMacroBar(final int MacroId, final Map<String,Object> macroInfo){
        final HBox macroBox = new HBox();
        macroBox.setMinWidth(getContentWidth());
        
        final ImageView defaultIconView = new ImageView(new ImageLoader("macros/play-button.png", 33,33).getImage());
        
        final Button play = new Button();
        play.setGraphic(defaultIconView);
        play.setUserData(MacroId);
        
        play.setOnAction((ActionEvent t) -> {
            try {
                ClientData.sendData(Macros.getMacroCommand((int)play.getUserData()));
            } catch (DomComponentsException ex) {
                LOG.error("Could not send data");
            }
        });
        macroBox.getChildren().add(play);
        VBox macrodetails = new VBox();
        
        Label nameLabel = new Label((String)macroInfo.get("name"));
        nameLabel.getStyleClass().add("name");
        VBox.setMargin(nameLabel, new Insets(0,
                                             0,
                                             0,
                                             5*DisplayConfig.getWidthRatio()));
        Label descLabel = new Label((String)macroInfo.get("description"));
        VBox.setMargin(descLabel, new Insets(0,
                                             0,
                                             0,
                                             5*DisplayConfig.getWidthRatio()));
        descLabel.getStyleClass().add("description");

        macrodetails.getChildren().add(nameLabel);
        macrodetails.getChildren().add(descLabel);
        macroBox.getChildren().add(macrodetails);
        HBox.setMargin(macroBox, new Insets(0,
                                            0,
                                            5*DisplayConfig.getHeightRatio(),
                                            0));
        Map<String,Object> shortcutOptions = new HashMap<>();
        shortcutOptions.put("id", MacroId);
        shortcutOptions.put("favorite", true);
        NewDesktopShortcut creator = new NewDesktopShortcut(nameLabel);
        creator.setServerCall("MacroService.setFavorite", shortcutOptions);
        return macroBox;
    }
 
    @Override
    public void handleMacroStateEvent(MacroStateEvent event) {
        Map<String,Object> eventData = (Map<String,Object>)event.getData();
        switch(event.getEventType()){
            case MacroStateEvent.MACROADDED:
                LOG.debug("Add macro: {} - {}",(String)eventData.get("id"),eventData);
                createMacroListItem((int)eventData.get("id"), eventData);
                setToolbarText(list.getItemsAmount() + " macros");
            break;
            case MacroStateEvent.MACROREMOVED:
                LOG.debug("Removed macro: {} - {}",(String)eventData.get("id"),eventData);
                list.removeItem(String.valueOf(eventData.get("id")));
                setToolbarText(list.getItemsAmount() + " macros");
            break;
            case MacroStateEvent.MACROACTIVE:
                LOG.debug("macro active: {} - {}",eventData.get("id"),eventData);
                list.highLight(currentActive);
            break;
        }
    }
    
    @Override
    public final void setupContent() {
        createInitialMacros();
        setContent(list);
        Macros.addMacroListener(this);
    }

    @Override
    public void removeContent() {
        list.destroy();
        Macros.removeMacroListener(this);
    }
    
}
