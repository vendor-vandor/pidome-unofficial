/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.config;

/**
 *
 * @author John Sirach
 */
public final class AppResources {

    static String basePath = "resources/";
    
    static String cssPath = "css/";
    static String imagePath  = "images/";
    static String fontsPath = "fonts/";
    
    public static String getCss(String cssName){
        return AppResources.class.getResource("/org/pidome/client/app/" + basePath + cssPath + cssName).toExternalForm();
    }
    
    public static String getImage(String imageName){
        return "file:" + getImagePath(imageName);
    }
    
    public static String getImagePath(String imageName){
        return basePath + imagePath + imageName;
    }
    
    public static String getFont(String fontName){
        return AppResources.class.getResource("/org/pidome/client/app/" + basePath + fontsPath + fontName).toExternalForm();
    }
    
}
