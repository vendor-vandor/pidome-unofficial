/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes;

import org.pidome.client.config.DisplayConfig;

/**
 *
 * @author John Sirach
 */
public class ComponentDimensions {

    double width = 0;
    double height= 0;
    
    public final double heightRatio = DisplayConfig.getHeightRatio();
    public final double widthRatio = DisplayConfig.getWidthRatio();
    
    public ComponentDimensions(){}
    
    public final void setDimensions(double width, double height){
        this.width = width  * widthRatio;
        this.height= height * heightRatio;
    }
    
    public final double getWidth(){
        return width;
    }
    
    public final double getHeight(){
        return height;
    }
    
}
