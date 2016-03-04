/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.menubars;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public final class FixedMenuBar extends MenuBarBase {

    public FixedMenuBar(){
        super();
    }
    
    @Override
    protected final void build() {
        ///Set started in the beginning.
    }
    
    @Override
    protected final void resume(PCCSystem system){
        Text closeApp      = GlyphsDude.createIcon(MaterialIcon.POWER_SETTINGS_NEW, "1.8em");
        closeApp.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            ScenesHandler.exit();
        });
        this.addToButtonBox(closeApp);
        if((System.getProperty("com.sun.javafx.isEmbedded")==null) || (System.getProperty("com.sun.javafx.isEmbedded")!=null && System.getProperty("com.sun.javafx.isEmbedded").equals("false"))){
            Text fullscreenapp = GlyphsDude.createIcon(MaterialIcon.SETTINGS_OVERSCAN, "1.8em");
            fullscreenapp.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
                ScenesHandler.toggleFullScreen();
            });
            this.addToButtonBox(fullscreenapp);
        }
        Text snapshot     = GlyphsDude.createIcon(MaterialDesignIcon.IMAGE_AREA_CLOSE, "1.8em");
        snapshot.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            ScenesHandler.snapShot();
        });
        this.addToButtonBox(snapshot);
    }
    
    @Override
    protected final void resumeDestroy() {
        /// Set started at the end.
        setStarted(false);
    }
}