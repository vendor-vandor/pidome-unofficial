/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.menus;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import org.pidome.client.PiDomeClient;
import static org.pidome.client.menus.AppMainMenu.defaultIconHeight;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.scenes.macros.MacrosPopUpLargeScreen;
import org.pidome.client.scenes.presences.PresencesPopUpLargeScreen;
import org.pidome.client.scenes.scenes.ScenesPopUpLargeScreen;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.services.ServiceConnector.DisplayType;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class AppFixedMenu extends AppMainMenu {

    public AppFixedMenu(PCCSystem system,ServiceConnector serviceConnector) {
        super(system, serviceConnector);
    }

    @Override
    protected void appendBuild() {
        addBottomContainerItem(GlyphsDude.createIcon(MaterialDesignIcon.PLAY, String.valueOf(uImageSize-10)), () -> {
            if(this.serviceConnector.userDisplayType()==DisplayType.TINY){
                PiDomeClient.switchScene(ScenesHandler.ScenePane.MACROS_EVENTS);
            } else {
                MacrosPopUpLargeScreen popup = new MacrosPopUpLargeScreen(this.system, this.serviceConnector);
                popup.setButtons();
                popup.build();
                popup.show(true);
            }
        });
        addBottomContainerItem(GlyphsDude.createIcon(FontAwesomeIcon.LIGHTBULB_ALT, String.valueOf(uImageSize-10)), () -> {
            if(this.serviceConnector.userDisplayType()==DisplayType.TINY){
                PiDomeClient.switchScene(ScenesHandler.ScenePane.SCENES);
            } else {
                ScenesPopUpLargeScreen popup = new ScenesPopUpLargeScreen(this.system, this.serviceConnector);
                popup.setButtons();
                popup.build();
                popup.show(true);
            }
        });
        addBottomContainerItem(GlyphsDude.createIcon(MaterialIcon.GROUP, String.valueOf(uImageSize-10)), () -> {
            if(this.serviceConnector.userDisplayType()==DisplayType.TINY){
                PiDomeClient.switchScene(ScenesHandler.ScenePane.PRESENCES);
            } else {
                PresencesPopUpLargeScreen popup = new PresencesPopUpLargeScreen(this.system, this.serviceConnector);
                popup.setButtons();
                popup.build();
                popup.show(true);
            }
        });
        addMainBlockItem(new SceneBigMenuItem(GlyphsDude.createIcon(MaterialDesignIcon.CUBE_UNFOLDED, String.valueOf(defaultIconHeight)), ScenesHandler.ScenePane.UTILITIES, "Utilities"));
    }

    @Override
    protected void bindPresence() {
        /// Bind to what? we are fixed :)
    }

    @Override
    public void opened() {
        this.setName(this.system.getLocalSettings().getStringSetting("user.login", "Display client"));
    }
    
}