/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panozona.converter.utils;

import com.panozona.converter.task.ImageData;
import com.sun.media.jai.codec.FileSeekableStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import org.xurble.server.ImageInfo;

/**
 *
 * @author Marek
 */
public class ImageDimensionsChecker {

    private ImageDimensionsChecker(){}

    public static ImageData analise(File image) {
        ImageData imageData = new ImageData();
        imageData.path = image.getAbsolutePath();
        if (image.getName().matches("(?i)^.+\\.(jpg|jpeg|gif|bmp|png)$")) {
            try {
                RandomAccessFile raf = new RandomAccessFile(image, "r");
                ImageInfo imageInfo = new ImageInfo();
                imageInfo.setInput(raf);
                if (!imageInfo.check()) {
                    raf.close();
                    System.out.println("Check not passed");
                    return null;
                }
                imageData.width = imageInfo.getWidth();
                imageData.height = imageInfo.getHeight();
                raf.close();
            } catch (FileNotFoundException ex) {
                System.out.println("Could not open file.");
            } catch (IOException ex) {
                System.out.println("Could not close file.");
            } catch (Exception ex) {
                System.out.println("Something is wrong ;].");
                ex.printStackTrace();
            }            
        } else if (image.getName().matches("(?i)^.+\\.(tif|tiff)$")) {
            FileSeekableStream stream = null;
            try {
                stream = new FileSeekableStream(image);
                PlanarImage input = JAI.create("stream", stream);
                imageData.width = input.getWidth();
                imageData.height = input.getHeight();
            } catch (IOException ex) {
                System.out.println("Could not open file " + image);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ex) {
                        System.out.println("Could not close file stream.");
                    }
                }
            }
        }
        return imageData;
    }
}
