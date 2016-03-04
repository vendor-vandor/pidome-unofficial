/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.video.capture.faces.recognition;

/**
 *
 * @author John
 */
public class FaceRect {
    
    private double x      = 0.0;
    private double y      = 0.0;
    private double width  = 0.0;
    private double height = 0.0;
    
    protected FaceRect(){}
    
    protected final void setRect(double x, double y, double width, double height){
        this.x=x;
        this.y=y;
        this.width=width;
        this.height=height;
    }
    public final boolean hasRect(){
        return x!=0.0 && y!=0.0 && width!=0.0 && height!=0.0;
    }
    public final double getX(){
        return x;
    }
    public final double getY(){
        return y;
    }
    public final double getWidth(){
        return width;
    }
    public final double getHeight(){
        return height;
    }
    
}
