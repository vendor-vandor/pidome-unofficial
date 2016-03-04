/*
 * Copyright 2014 John.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.client.system.scenes.components.mainstage.displays.visualfloor;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javax.imageio.ImageIO;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.domotics.components.categories.Categories;
import org.pidome.client.system.domotics.components.devices.Device;

/**
 *
 * @author John
 */
public class VisualFloorUtils {
    
    static MeshView getImagePlane(Image planeTexture){
        PhongMaterial texturedMaterial = new PhongMaterial();
        texturedMaterial.setDiffuseMap(planeTexture);
        texturedMaterial.setSpecularColor(Color.TRANSPARENT);

        MeshView imagePlane = new MeshView(createSimpleOnePlanedMesh((float)planeTexture.getWidth(), (float)planeTexture.getHeight()));
        imagePlane.setScaleX(-1); //// Texture is mirrorred (? still having troubles with the triangle meshes), reverse.
        imagePlane.materialProperty().setValue(texturedMaterial);
        
        return imagePlane;
        
    }
    
    protected static TriangleMesh createSimpleOnePlanedMesh(float width, float height){

        float pointValueWidth = width/2;
        float pointValueHeight = height/2;
        
        float[] points = {
            -pointValueWidth, pointValueHeight, 0,
            -pointValueWidth, -pointValueHeight, 0,
            pointValueWidth, pointValueHeight, 0,
            pointValueWidth, -pointValueHeight, 0
        };

        float[] texCoords = {
            1, 1,
            1, 0,
            0, 1,
            0, 0
        };
        
        int[] faces = {
            2, 2, 1, 1, 0, 0,
            2, 2, 3, 3, 1, 1
        };
        
        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().setAll(points);
        mesh.getTexCoords().setAll(texCoords);
        mesh.getFaces().setAll(faces);
        
        return mesh;
    }
    
    protected static ByteArrayInputStream loadSmallDeviceImage(Device device) throws IOException{
        BufferedImage img = ImageIO.read(new File("resources/images/device_cat/" +Categories.getCategoryConstant(device.getCategory())+ "-small.png"));
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        baos.flush();
        return new ByteArrayInputStream(baos.toByteArray());
    }
    
    protected static ByteArrayInputStream loadMovementImage() throws IOException{
        BufferedImage img = ImageIO.read(new File("resources/images/device_cat/MOTION-3dmap.png"));
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        baos.flush();
        return new ByteArrayInputStream(baos.toByteArray());
    }    
    
    protected static ByteArrayInputStream loadDeviceIageResized(Device device, int width, int height) throws IOException{
        BufferedImage img = ImageIO.read(new File("resources/images/device_cat/" +Categories.getCategoryConstant(device.getCategory())+ ".png"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write((RenderedImage) getScaledInstance(img,
                                           width * (int)DisplayConfig.getWidthRatio(),
                                           height * (int)DisplayConfig.getHeightRatio(),
                                           true), "png", out);
        out.flush();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        return in;
    }
    
    /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    protected static BufferedImage getScaledInstance(BufferedImage img,
                                           int targetWidth,
                                           int targetHeight,
                                           boolean higherQuality)
    {
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage)img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }
        
        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }
    
}
