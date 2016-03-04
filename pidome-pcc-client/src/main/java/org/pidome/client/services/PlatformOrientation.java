/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services;

/**
 *
 * @author John
 */
public interface PlatformOrientation {
    public enum Orientation {
        LANDSCAPE,PORTRAIT;
    }
    
    public void handleOrientationChanged(Orientation orient);
    
}
