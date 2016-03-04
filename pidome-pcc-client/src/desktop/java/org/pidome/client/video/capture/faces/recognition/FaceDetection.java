/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.video.capture.faces.recognition;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

/**
 *
 * @author John
 */
public class FaceDetection {
    
    private double absoluteFaceSize = 0;
    private final CascadeClassifier faceCascade;
    private boolean drawRectangle = false;
    
    private final int rectThickness = 2;
    
    public FaceDetection(){
        this.faceCascade = new CascadeClassifier();
        this.faceCascade.load("lib/opencv/haarcascade_frontalface_alt.xml");
    }
    
    public final void drawRectangle(boolean draw){
        this.drawRectangle = draw;
    }
    
    /**
     * Perform face detection.
     * When drawRectangle is set to true a green rectangle will be placed on the image around a detected face.
     * @param frame the current frame
     * @return The location and size of the detected face.
     */
    public final FaceRect detectFace(Mat frame) {
        // init
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);

        // compute minimum face size (20% of the frame height)
        if (absoluteFaceSize == 0) {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0) {
                absoluteFaceSize = Math.round(height * 0.2f);
            }
        }
        // detect faces
        this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2,
                Objdetect.CASCADE_SCALE_IMAGE
                | Objdetect.CASCADE_DO_ROUGH_SEARCH
                | Objdetect.CASCADE_FIND_BIGGEST_OBJECT, new Size(
                        absoluteFaceSize, absoluteFaceSize), new Size());

        // each rectangle in faces is a face
        Rect[] facesArray = faces.toArray();
        FaceRect faceRect = new FaceRect();
        if (facesArray.length == 1) {
            Point loc = facesArray[0].tl();
            Size size = facesArray[0].size();
            faceRect.setRect(loc.x+rectThickness, loc.y+rectThickness, size.width-rectThickness, size.height-rectThickness);
            if(drawRectangle) {
                Core.rectangle(frame, loc, facesArray[0].br(), new Scalar(0, 255, 0, 255), rectThickness);
            }
        }
        return faceRect;
    }
    
}
