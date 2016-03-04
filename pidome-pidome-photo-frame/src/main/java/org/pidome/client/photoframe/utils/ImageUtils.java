/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.photoframe.utils;

import org.pidome.client.photoframe.screens.photoscreen.actors.IncompatibleImageException;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

/**
 *
 * @author John
 */
public class ImageUtils {
    
    public static boolean getJPEGOutputStream(ImageInputStream in) throws IOException,IncompatibleImageException {
        String jpegMetaFormat = "javax_imageio_jpeg_image_1.0";
        java.util.Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("jpeg");

        ImageReader reader = null;
        while(readers.hasNext()) {
            ImageReader tmp = readers.next();
            if(jpegMetaFormat.equals(tmp.getOriginatingProvider().getNativeImageMetadataFormatName())) {
                reader = tmp;
                break;
            }
        }
        if(reader == null) {
            throw new IncompatibleImageException("No registered jpeg reader with \"javax_imageio_jpeg_image_1.0\" metadata format.");
        }

        IIOMetadata meta;
        
        reader.setInput(in,true,false);
        meta = reader.getImageMetadata(0);
        reader.dispose();

        IIOMetadataNode root = (IIOMetadataNode) meta.getAsTree(jpegMetaFormat);
        IIOMetadataNode markerSeq = (IIOMetadataNode)root.getElementsByTagName("markerSequence").item(0);

        return markerSeq.getElementsByTagName("sos").getLength() > 1;
    }
    
}
