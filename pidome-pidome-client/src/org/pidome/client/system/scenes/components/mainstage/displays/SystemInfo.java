/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays;

import java.util.Map;
import java.util.Map.Entry;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.pidome.client.config.AppProperties;
import org.pidome.client.config.AppPropertiesException;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.domotics.Domotics;
import org.pidome.client.system.domotics.components.Server;
import org.pidome.client.system.scenes.windows.TitledWindowBase;

/**
 *
 * @author John Sirach
 */
public class SystemInfo extends TitledWindowBase {
    
    VBox contentPane = new VBox();
    
    public SystemInfo(Object... params){
        super("sysinfo", "System info");
    }
    
    @Override
    protected void setupContent() {
        Label infoText = new Label("This is the development version of the client, hopefully in sync with the server. Be aware: Problems do arise!\n"
                                 + "Besides this, the interface of this client is not final, a lot of things can still change and hopefully for the better.\n"
                                 + "If you got any suggestions, please let us know on http://pidome.org\n");
        infoText.setWrapText(true);
        infoText.setMinHeight(70);
        infoText.setMaxWidth(650);
        VBox.setVgrow(infoText, Priority.ALWAYS);
        contentPane.getChildren().add(infoText);
        contentPane.getChildren().add(new Label("Server info:"));
        Map<String, String> data = ((Server) Domotics.getServer()).getServerVersionInfo();
        data.keySet().stream().map((key) -> {
            HBox infoLine = new HBox(3 *DisplayConfig.getHeightRatio());
            Label nameLabel = new Label(key);
            nameLabel.setPrefWidth(150);
            nameLabel.setMaxWidth(Region.USE_PREF_SIZE);
            nameLabel.setMinWidth(Region.USE_PREF_SIZE);
            nameLabel.setWrapText(true);
            Label valueLabel = new Label(": " + data.get(key));
            valueLabel.setPrefWidth(500);
            valueLabel.setMaxWidth(Region.USE_PREF_SIZE);
            valueLabel.setMinWidth(Region.USE_PREF_SIZE);
            valueLabel.setWrapText(true);
            infoLine.getChildren().addAll(nameLabel, valueLabel);
            return infoLine;
        }).forEach((infoLine) -> {
            contentPane.getChildren().add(infoLine);
        });
        contentPane.getChildren().add(new Label("Client info:"));
        try {
            for (Entry<Object,Object> entry : AppProperties.getPropertiesNVP("system")) {
                HBox infoLine = new HBox(3 *DisplayConfig.getHeightRatio());
                Label nameLabel = new Label((String)entry.getKey());
                nameLabel.setPrefWidth(150);
                nameLabel.setMaxWidth(Region.USE_PREF_SIZE);
                nameLabel.setMinWidth(Region.USE_PREF_SIZE);
                nameLabel.setWrapText(true);
                Label valueLabel = new Label(": " + (String)entry.getValue());
                valueLabel.setPrefWidth(500);
                valueLabel.setMaxWidth(Region.USE_PREF_SIZE);
                valueLabel.setMinWidth(Region.USE_PREF_SIZE);
                valueLabel.setWrapText(true);
                infoLine.getChildren().addAll(nameLabel, valueLabel);
                contentPane.getChildren().add(infoLine);
            }
        } catch (AppPropertiesException ex) {
            contentPane.getChildren().add(new Label("Client info could not be displayed"));
        }
        assignContent(contentPane);
    }

    @Override
    protected void removeContent() {
        contentPane.getChildren().clear();
    }
    
    
}
