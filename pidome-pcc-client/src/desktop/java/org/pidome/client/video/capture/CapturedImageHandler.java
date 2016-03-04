/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.video.capture;

import org.opencv.core.Mat;

/**
 *
 * @author John
 */
public interface CapturedImageHandler {
    public void imageCaptured(Mat lastFrame);
}
