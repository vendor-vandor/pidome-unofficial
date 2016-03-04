/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.tools;

import javafx.stage.Screen;
import org.pidome.client.services.ServiceConnector;

/**
 *
 * @author John
 */
public class DisplayTools {
    
    private static ServiceConnector.DisplayType dispType = ServiceConnector.DisplayType.DUNNO;
    
    public static double getHeightScaleRatio(double origheight){
        return origheight * ((1.0/1080.0) * Screen.getPrimary().getBounds().getHeight());
    }
    
    public static void setUserDisplayType(ServiceConnector.DisplayType disp){
        dispType = disp;
    }
    
    public static ServiceConnector.DisplayType getUserDisplayType(){
        return dispType;
    }
    
}