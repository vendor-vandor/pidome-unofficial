/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.video.capture;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

/**
 *
 * @author John
 */
public abstract class CaptureControllerVideoSource implements Closeable {
    
    /**
     * Video capture.
     */
    private VideoCapture capture;
    /**
     * The source where captures are taken from.
     */
    private Object captureSource;
    /**
     * Capture width.
     */
    private double captureWidth;
    /**
     * Capture height.
     */
    private double captureHeight;
    /**
     * Capture executor when an capture threshold is chosen.
     */
    private ScheduledExecutorService grabExecutor;
    /**
     * Capture threshold.
     */
    private long threshold = 0;
    /**
     * This is set to true when the app is ready to get the next image.
     */
    private boolean ready = true;
    
    /**
     * Just in case someone does not call the init.
     */
    private boolean initDone = false;
    
    /**
     * Loads from string resource (local file access).
     * @param resource String resource
     * @param captureWidth capture width
     * @param captureHeight capture height
     * @throws UnsatisfiedLinkError 
     */
    protected CaptureControllerVideoSource(String resource, double captureWidth, double captureHeight) throws UnsatisfiedLinkError {
        loader(resource, captureWidth, captureHeight);
    }
    
    /**
     * Loads from string resource (Webcam access).
     * @param resource Integer resource
     * @param captureWidth capture width
     * @param captureHeight capture height
     * @throws UnsatisfiedLinkError 
     */
    protected CaptureControllerVideoSource(Integer resource, double captureWidth, double captureHeight) throws UnsatisfiedLinkError {
        loader(resource, captureWidth, captureHeight);
    }
    
    /**
     * Initializes the source and loads the native library.
     * @param resource An in for a webcam, or string for a file resource.
     * @param captureWidth
     * @param captureHeight
     * @throws UnsatisfiedLinkError 
     */
    private void loader(Object resource, double captureWidth, double captureHeight) throws UnsatisfiedLinkError {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        this.captureSource = resource;
        this.captureWidth = captureWidth;
        this.captureHeight = captureHeight;
        this.capture = new VideoCapture();
    }
    
    /**
     * Call this to open a stream.
     */
    public final void init(long ms) throws CaptureInitException{
        if(grabExecutor==null){
            this.threshold = ms;
            grabExecutor = Executors.newSingleThreadScheduledExecutor();
            if(captureSource instanceof String){
                this.capture.open((String)captureSource);
            } else if (captureSource instanceof Integer){
                this.capture.open((Integer)captureSource);
            }
            this.capture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, this.captureWidth);
            this.capture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, this.captureHeight);
        } else {
            throw new CaptureInitException("We are already initialized, Call close first and re-initialize the object");
        }
    }
    
    /**
     * Starts the frame grabbing.
     * If you do not call init the default threshold of 500 milliseconds is used.
     * @param caller
     */
    protected final void startGrabbing(CapturedImageHandler caller) throws CaptureInitException {
        if(!initDone){
            init(500);
        }
        Runnable grabber = () -> {
            try {
                if(ready){
                    ready = false;
                    caller.imageCaptured(grabFrame());
                }
            } catch (IOException ex) {
                Logger.getLogger(CaptureControllerVideoSource.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        if(grabExecutor!=null){
            grabExecutor.scheduleWithFixedDelay(grabber, this.threshold, this.threshold, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * Use this to indicate you are ready to receive the next frame.
     * @param ready Set to true when you are ready to receive captures again.
     */
    protected final void ready(boolean ready){
        this.ready = ready;
    }
    
    /**
     * Get a frame from the opened video stream (if any)
     * @return the {@link Mat} captured
     */
    private Mat grabFrame() throws IOException {
        Mat frame = new Mat();
        if (this.capture.isOpened()) {
            this.capture.read(frame);
            if (!frame.empty()) {
                return frame;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    
    /**
     * Stops any executors and closes the capture stream.
     * @throws IOException 
     */
    @Override
    public void close() throws IOException {
        if(grabExecutor!=null){
            grabExecutor.shutdownNow();
            grabExecutor = null;
        }
        capture.release();
    }
    
}