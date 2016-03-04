/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.panes.popups;

import de.jensd.fx.glyphs.GlyphIcons;

/**
 *
 * @author John
 */
public class SimpleDialog extends PopUp {

    public SimpleDialog(GlyphIcons icon, String title) {
        super(icon, title);
    }

    @Override
    public void unload() {
        /// Not needed.
    }
    
}
