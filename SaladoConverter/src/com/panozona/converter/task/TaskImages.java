package com.panozona.converter.task;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author Marek
 */
public class TaskImages {    

    public enum panoType {
        cubic, equirectangular
    };
    private ArrayList<ImageData> imagesData;
    private panoType pType;
    private int cubeSize;

    public TaskImages(ImageData equirectImage) {
        imagesData = new ArrayList<ImageData>();
        imagesData.add(equirectImage);
        pType = panoType.equirectangular;
        cubeSize = (int) ((Math.tan(Math.PI / 4D)
                * equirectImage.width / (2D * Math.PI)) * 2);
    }

    public TaskImages(ArrayList<ImageData> cubeImages) {
        imagesData = cubeImages;
        pType = panoType.cubic;
        cubeSize = cubeImages.get(0).width;
    }

    public ArrayList<ImageData> getImagesData(){
        return imagesData;
    }

    public panoType getPanoType() {
        return pType;
    }

    public String getTaskPathDescription() {
        if (pType.equals(panoType.equirectangular)) {
            return imagesData.get(0).path;
        } else {
            return imagesData.get(0).path.substring(0, imagesData.get(0).path.lastIndexOf(File.separator));
        }
    }
    
    public int getCubeSize() {
        return cubeSize;
    }
}
