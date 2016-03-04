/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.media;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import static javafx.scene.layout.Region.USE_PREF_SIZE;
import javafx.stage.Screen;
import org.pidome.client.entities.plugins.media.MediaPlugin;
import org.pidome.client.scenes.panes.popups.PopUp;

/**
 *
 * @author John
 */
public class CurrentPlaylistPopup extends PopUp {

    CurrentPlaylist playlist = new CurrentPlaylist();
    
    protected CurrentPlaylistPopup(MediaPlugin plugin){
        super(FontAwesomeIcon.FILM, plugin.getName().getValueSafe());
        playlist.build(plugin);
        playlist.noHBar();
        playlist.noHover();
        this.setContent(playlist);
        playlist.setPrefSize(Screen.getPrimary().getBounds().getWidth()*0.8, Screen.getPrimary().getBounds().getHeight()*0.6);
        playlist.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        playlist.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
    }

    @Override
    public void unload() {
        playlist.destroy();
    }
    
}