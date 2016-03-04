/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.fxutils;

/**
 *
 * @author John
 */
public class FXResources {
    
    static String basePath = "/resources/";
    
    static String cssPath    = "css/";
    static String imagePath  = "images/";
    static String fontsPath  = "fonts/";
    
    public static String getCss(String cssName){
        return FXResources.class.getResource(basePath + cssPath + cssName).toExternalForm();
    }
    
    public static String getImage(String imageName){
        return "file:" + getImagePath(imageName);
    }
    
    public static String getImagePath(String imageName){
        return basePath + imagePath + imageName;
    }
    
    public static String getFont(String fontName){
        return FXResources.class.getResource(basePath + fontsPath + fontName).toExternalForm();
    }   
}
