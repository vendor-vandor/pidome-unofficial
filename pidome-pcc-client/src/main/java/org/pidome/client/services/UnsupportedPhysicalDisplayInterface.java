/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.StackPane;

/**
 *
 * @author John
 */
public class UnsupportedPhysicalDisplayInterface extends PhysicalDisplayInterface {

    public UnsupportedPhysicalDisplayInterface(ServiceConnector connector) {
        super(connector);
    }

    @Override
    public List<Support> getAvailableSupportTypes() {
        return new ArrayList<PhysicalDisplayInterface.Support>() {
            {
                add(PhysicalDisplayInterface.Support.NONE);
            }
        };
    }

    @Override
    public void setBrightness(int value) {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getBrightness() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDisplayOn(boolean value) {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean getDisplayOn() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void init() {
        /// Do nothing;
    }

    @Override
    public boolean brightnessInitialized() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public StackPane getTouchOverlay() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateBlankTimer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
