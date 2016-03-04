/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.video.capture;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;

/**
 * Controller for capturing images from a webcam or local streaming dev device.
 * @author John
 */
public final class CaptureController implements Closeable,CapturedImageHandler {
    
    /**
     * The source the captures are taken from.
     */
    private final CaptureControllerVideoSource captureSource;
    /**
     * The last captured frame.
     */
    Mat lastFrame;
    
    /**
     * Constructor.
     * @param captureWidth The capture width
     * @param captureHeight The capture height
     * @param source The source where the capture is being done from.
     */
    public CaptureController(double captureWidth, double captureHeight, CaptureControllerVideoSource source){
        this.captureSource = source;
    } 
    
    /**
     * Call this to open a stream.
     * Set the amount of milliseconds between each capture. Please keep it real.
     * @param thresholdMs Amount of milliseconds for capturing.
     * @throws org.pidome.client.video.capture.CaptureInitException
     */
    public final void init(long thresholdMs) throws CaptureInitException {
        this.captureSource.init(thresholdMs);
    }
    
    /**
     * Starts the capture process.
     * @param handler
     * @throws org.pidome.client.video.capture.CaptureInitException
     */
    public final void open(CapturedImageHandler handler) throws CaptureInitException {
        this.captureSource.startGrabbing(handler);
    }

    @Override
    public void close() throws IOException {
        this.captureSource.close();
    }

    @Override
    public void imageCaptured(Mat frame) {
        if(frame!=null){
            lastFrame = frame;
        }
    }
    
    /**
     * Returns the buffered image
     * @return 
     */
    protected final BufferedImage getBufferedImage() throws IOException {
        return mat2BufferedImage(lastFrame);
    }
    
    /**
     * Enables capturing again.
     */
    public void getNext(){
        this.captureSource.ready(true);
    }
    
    /**
     * Pauses capturing.
     * Pausing does not close the source.
     */
    public void pause(){
        this.captureSource.ready(false);
    }
    
    /**
     * Return a buffered image from an opencv mat.
     * @param frame
     * @return
     * @throws IOException 
     */
    private BufferedImage mat2BufferedImage(Mat frame) throws IOException {
        MatOfByte buffer = new MatOfByte();
        Highgui.imencode(".png", frame, buffer);
        try(InputStream in = new ByteArrayInputStream(buffer.toArray())){
            BufferedImage buffered = ImageIO.read(in);
            return buffered;
        }
    }
    
}