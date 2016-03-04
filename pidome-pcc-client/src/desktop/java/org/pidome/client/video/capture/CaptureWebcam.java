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
public final class CaptureWebcam extends CaptureControllerVideoSource {

    public CaptureWebcam(String resource, double width, double height) throws UnsatisfiedLinkError {
        super(resource, width, height);
    }
    
}