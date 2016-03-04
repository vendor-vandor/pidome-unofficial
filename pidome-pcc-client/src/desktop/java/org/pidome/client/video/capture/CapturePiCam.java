/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.video.capture;

/**
 *
 * @author John
 */
public class CapturePiCam extends CaptureControllerVideoSource {
    
    public CapturePiCam(String resource, double captureWidth, double captureHeight) throws UnsatisfiedLinkError {
        super(resource, captureWidth, captureHeight);
    }
    
}