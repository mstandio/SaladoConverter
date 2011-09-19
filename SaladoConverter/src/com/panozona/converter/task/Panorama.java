package com.panozona.converter.task;

import java.util.ArrayList;

/** 
 * @author Marek Standio
 */
public class Panorama {
    
    private ArrayList<Image> images;
    
    public Panorama(Image singleImage) {
        images = new ArrayList<Image>();
        images.add(singleImage);
    }
    
    public Panorama(ArrayList<Image> cubeImages) {
        if (cubeImages.size() == 6) {
            images = cubeImages;
        }        
    }
    
    public ArrayList<Image> getImages() {
        return images;
    }
}
