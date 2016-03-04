/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.image.Image;
import org.pidome.client.config.AppResources;
import org.pidome.client.config.DisplayConfig;

/**
 * This specific class makes certain that an image is only loaded once in memory and scales the image as needed by the display config.
 * To make sure this works correct use the unload method when you are done with the image
 * @author John Sirach
 */
public class ImageLoader {

    static Map<String,Integer> instancesNeeded = new HashMap<>();
    static Map<String,Image>   loadedImages    = new HashMap<>();
    static List<String>        preloadedImages = new ArrayList();
    
    String currentImage;

    /**
     * Used for pre loading images.
     * When using this function an image will be loaded into memory, but when the last instance unloads the image
     * the image itself won't be removed from memory.
     * @param imageName
     * @param imageWidth
     * @param imageHeight 
     */
    public static void preload(String imageName, double imageWidth, double imageHeight){
        load(imageName, imageWidth, imageHeight);
        preloadedImages.add(imageName);
    }
    
    /**
     * Loads an image in memory unless pre loaded.
     * @param imageName
     * @param imageWidth
     * @param imageHeight 
     */
    public ImageLoader(String imageName, double imageWidth, double imageHeight){
        currentImage = imageName;
        load(imageName, imageWidth, imageHeight);
        if(!instancesNeeded.containsKey(currentImage)){
            instancesNeeded.put(currentImage,1);
        } else {
            instancesNeeded.put(currentImage,instancesNeeded.get(currentImage)+1);
        }
    }
    
    /**
     * The initial loading mechanism.
     * This loads an image based on the width and height, when quality is low an image will be loaded scaled to the ratio of the display size
     * @param imageName
     * @param imageWidth
     * @param imageHeight 
     */
    static void load(String imageName, double imageWidth, double imageHeight){
        if(!loadedImages.containsKey(imageName)){
            if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){
                loadedImages.put(imageName, new Image(AppResources.getImage(imageName),
                                                         imageWidth * DisplayConfig.getWidthRatio(),
                                                         imageHeight * DisplayConfig.getHeightRatio(),
                                                         true,
                                                         true,
                                                         false));
            } else {
                loadedImages.put(imageName, new Image(AppResources.getImage(imageName),
                                                         imageWidth * DisplayConfig.getWidthRatio(),
                                                         imageHeight * DisplayConfig.getHeightRatio(),
                                                         true,
                                                         false,
                                                         false));
            }
        }
    }
    
    /**
     * Retrieve the image previously used in the constructor
     * @return 
     */
    public final Image getImage(){
        return loadedImages.get(currentImage);
    }
 
    /**
     * Unloads an image from memory if it is the last instance loading it.
     * An image won't be unloaded if it has been used with preload.
     */
    public final void unload(){
        instancesNeeded.put(currentImage,instancesNeeded.get(currentImage)-1);
        if(instancesNeeded.get(currentImage)==0 && !preloadedImages.contains(currentImage)){
            loadedImages.remove(currentImage);
        }
    }
    
}
