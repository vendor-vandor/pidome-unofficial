/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.photoframe.screens.photoscreen.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import org.pidome.client.photoframe.FrameSettings;



/**
 *
 * @author John
 */
public class PhotosActor {
 
    List<PhotoImage> images = new ArrayList<>();
    
    private static String imgDir = "photos/";
    List<String> fileSet = new ArrayList<>();
    
    String currentFile;
    
    private final ScheduledExecutorService changeExecutor = Executors.newSingleThreadScheduledExecutor();
    
    Stage stage;
    
    private static boolean formatReplace = false;
    
    private float transitionDelay = 5f;
    
    public PhotosActor(Stage stage) {
        this.stage = stage;
        this.formatReplace = FrameSettings.getFormatReplace();
    }
    
    public final void preload(){
        loadFileSet();
        this.transitionDelay = FrameSettings.getTransitionDelay();
        changeExecutor.scheduleWithFixedDelay(switchimg(), 5, ((FrameSettings.getRotationDelay()<10)?10:FrameSettings.getRotationDelay())+FrameSettings.getTransitionDelay(), TimeUnit.SECONDS);
    }
    
    private Runnable switchimg(){
        Runnable run = () -> {
            try {
                byte[] byteResult = readLocalRandomFile();
                Pixmap map = new Pixmap(byteResult, 0, byteResult.length);
                Gdx.app.postRunnable(() -> {
                    TextureRegionDrawable tex = new TextureRegionDrawable(new TextureRegion(new Texture(map)));
                    map.dispose();
                    PhotoImage newImg = new PhotoImage(tex);
                    newImg.setCorrectAspectSize();
                    images.add(0,newImg);
                    stage.addActor(newImg);
                    addTransform(newImg);
                });
            } catch (Exception ex) {
                Logger.getLogger(PhotosActor.class.getName()).log(Level.WARNING, "Problem switching image: " + ex.getMessage(), ex);
                System.out.println("Problem switching image: " + ex.getMessage());
            }
        };
        return run;
    }
    
    public void addTransform(Image img){
        float newImgX = (this.stage.getWidth()-img.getWidth())/2;
        float newImgY = (this.stage.getHeight()-img.getHeight())/2;
        if(!FrameSettings.isErratic()){
            img.toBack();
            img.setPosition(newImgX, newImgY);
            if(images.size()>1){ images.get(1).toBack(); }
            img.addAction(Actions.sequence(Actions.alpha(0),Actions.fadeIn(transitionDelay,Interpolation.linear),Actions.run(() -> {
                removeOldImg();
            })));
        } else {
            switch(new Random().nextInt(5)){
                case 0:
                    img.toBack();
                    img.setPosition(newImgX, newImgY);
                    if(images.size()>1){ images.get(1).toBack(); }
                    img.addAction(Actions.sequence(Actions.alpha(0),Actions.fadeIn(transitionDelay,getRandomFadeInterpolation()),Actions.run(() -> {
                        removeOldImg();
                    })));
                break;
                case 1:
                    img.toBack();
                    if(images.size()>1){ images.get(1).toBack(); }
                    img.setPosition(this.stage.getWidth(), newImgY);
                    img.addAction(Actions.sequence(Actions.moveTo(newImgX, newImgY, transitionDelay,getRandomInterpolation()),Actions.run(() -> {
                        removeOldImg();
                    })));
                break;
                case 2:
                    img.toBack();
                    if(images.size()>1){ images.get(1).toBack(); }
                    img.setScale(0f, 0f);
                    img.setPosition(this.stage.getWidth()/2, this.stage.getHeight()/2);
                    img.addAction(Actions.sequence(Actions.parallel(Actions.scaleTo(1f, 1f, transitionDelay,getRandomInterpolation()), Actions.moveTo(newImgX, newImgY, transitionDelay,getRandomInterpolation())),Actions.run(() -> {
                        removeOldImg();
                    })));
                break;
                case 3:
                    if(images.size()>1){
                        img.toBack();
                        img.setPosition(newImgX, newImgY);
                        img.addAction(Actions.sequence(Actions.alpha(0),Actions.fadeIn(1f),Actions.run(() -> {
                            images.get(1).addAction(Actions.sequence(Actions.parallel(Actions.scaleTo(0f, 0f, transitionDelay,getRandomInterpolation()), Actions.moveTo(this.stage.getWidth()/2, this.stage.getHeight()/2, transitionDelay,getRandomInterpolation())),Actions.run(() -> {
                                removeOldImg();
                            })));
                        })));
                    } else {
                        addTransform(img);
                    }
                break;
                case 4:
                    if(images.size()>1){
                        img.toBack();
                        img.setPosition(newImgX, newImgY);
                        img.addAction(Actions.sequence(Actions.alpha(0),Actions.fadeIn(1f),Actions.run(() -> {
                            images.get(1).addAction(Actions.sequence(Actions.moveTo(-images.get(1).getWidth(), (this.stage.getHeight()/2) - (images.get(1).getHeight()/2), transitionDelay,getRandomInterpolation()),Actions.run(() -> {
                                removeOldImg();
                            })));
                        })));
                    } else {
                        addTransform(img);
                    }
                break;
            }
        }
    }
    
    
    public Interpolation getRandomFadeInterpolation(){
        switch(new Random().nextInt(14)){
            case 0:
                return Interpolation.exp10;
            case 1:
                return Interpolation.exp10In;
            case 2:
                return Interpolation.exp10Out;
            case 3:
                return Interpolation.exp5;
            case 4:
                return Interpolation.exp5In;
            case 5:
                return Interpolation.exp5Out;
            case 6:
                return Interpolation.fade;
            case 7:
                return Interpolation.linear;
            case 8:
                return Interpolation.sine;
            case 9:
                return Interpolation.sineIn;
            case 10:
                return Interpolation.sineOut;
            case 11:
                return Interpolation.bounce;
            case 12:
                return Interpolation.bounceIn;
            case 13:
                return Interpolation.bounceOut;
            default:  
                return Interpolation.linear;
        }
    }
    
    public Interpolation getRandomInterpolation(){
        switch(new Random().nextInt(20)){
            case 0:
                return Interpolation.bounce;
            case 1:
                return Interpolation.bounceIn;
            case 2:
                return Interpolation.bounceOut;
            case 3:
                return Interpolation.circle;
            case 4:
                return Interpolation.circleIn;
            case 5:
                return Interpolation.circleOut;
            case 6:
                return Interpolation.exp10;
            case 7:
                return Interpolation.exp10In;
            case 8:
                return Interpolation.exp10Out;
            case 9:
                return Interpolation.exp5;
            case 10:
                return Interpolation.exp5In;
            case 11:
                return Interpolation.exp5Out;
            case 12:
                return Interpolation.fade;
            case 13:
                return Interpolation.linear;
            case 14:
                return Interpolation.sine;
            case 15:
                return Interpolation.sineIn;
            case 16:
                return Interpolation.sineOut;
            case 17:
                return Interpolation.swing;
            case 18:
                return Interpolation.swingIn;
            case 19:
                return Interpolation.swingOut;
            default:  
                return Interpolation.linear;
        }
    }
    
    private void removeOldImg(){
        if(images.size()>1){
            PhotoImage oldImg = images.remove(1);
            oldImg.addAction(Actions.sequence(Actions.alpha(1),Actions.fadeOut(1f),Actions.delay(1),Actions.run(() -> {
                oldImg.remove();
                oldImg.getActions().clear();
                oldImg.dispose();
            })));
        }
    }
    
    private byte[] readLocalRandomFile() throws IncompatibleImageException {
        if(fileSet.isEmpty()){
            loadFileSet();
        }
        try {
            currentFile = fileSet.get(new Random().nextInt(fileSet.size()));
            fileSet.remove(currentFile);
            File file = new File(currentFile);
            if(file.getName().endsWith("_pidomereplaced.jpg") || file.getName().substring(file.getName().lastIndexOf('.')+1).toLowerCase().equals("png")){
                return getPlainByteArrayOutputStream(file);
            } else {
                return getTransformedByteArrayOutputStream(file);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PhotosActor.class.getName()).log(Level.SEVERE, "Could not find file: " + currentFile, ex);
            throw new IncompatibleImageException("Could not find file: " + currentFile);
        } catch (IOException ex) {
            Logger.getLogger(PhotosActor.class.getName()).log(Level.SEVERE, "Could not read file: " + currentFile, ex);
            throw new IncompatibleImageException("Could not read file: " + currentFile);
        }
    }
    
    private static byte [] getPlainByteArrayOutputStream(File file) throws IOException, IncompatibleImageException {
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            ByteArrayOutputStream out;
            try (InputStream in = new BufferedInputStream(input)) {
                out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n = 0;
                while (-1 != (n = in.read(buf))) {
                    out.write(buf, 0, n);
                }
                input.close();
                out.close();
                return out.toByteArray();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PhotosActor.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        }
    }
    
    private static byte[] getTransformedByteArrayOutputStream(File file) throws IOException, IncompatibleImageException {
        ImageInputStream in = ImageIO.createImageInputStream(file);
        if(in!=null){
            ImageReader reader = null;
            Iterator<ImageReader> iter = ImageIO.getImageReaders(in);
            if (!iter.hasNext()) {
                throw new IncompatibleImageException("Incompatible image found");
            } else {
                reader = iter.next();
            }
            reader.setInput(in);

            try (ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream()) {
                BufferedImage buffer = reader.read(0);
                if(formatReplace){
                    String ext = file.getName().substring(file.getName().lastIndexOf('.'));
                    File newFile = new File(imgDir + file.getName().replace(ext, "_pidomereplaced.jpg"));
                    
                    ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
                    ImageWriteParam param = writer.getDefaultWriteParam();
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(1.0f);
                    param.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
                    
                    ImageOutputStream ios = ImageIO.createImageOutputStream(newFile);
                    
                    writer.setOutput(ios);
                    writer.write(buffer);
                    file.delete();
                    
                    
                    //if(ImageIO.write(buffer, "png", newFile)){
                    //    file.delete();
                    //}
                    /**
                     * Return the written file.
                     */

                    reader.dispose();
                    in.close();
                    byteArrayOut.close();
                    return getPlainByteArrayOutputStream(newFile);
                } else {
                    reader.dispose();
                    in.close();
                    ImageIO.write(buffer, "png", byteArrayOut);
                    byte[] set = byteArrayOut.toByteArray();
                    byteArrayOut.close();
                    return set;
                }
            }
        } else {
            throw new IncompatibleImageException("Unsupported file type");
        }
    }
    
    private void loadFileSet(){
        File[] files = new File(imgDir).listFiles();
        for (File file : files) {
            if (file.isFile() && !file.getName().equals("readme.txt")) {
                fileSet.add(imgDir + file.getName());
            }
        }
    }
    
    
}
