package com.panozona.converter.task;

import com.panozona.converter.settings.AggregatedSettings;
import java.util.ArrayList;

/** 
 * @author Marek
 */
public class Panorama {

    private ArrayList<Image> images;
    private PanoramaTypes panoramaType;

    public Panorama(Image equirectImage) {
        images = new ArrayList<Image>();
        images.add(equirectImage);
        panoramaType = PanoramaTypes.equirectangular;

    }

    public Panorama(ArrayList<Image> cubeImages) {
        images = cubeImages;
        panoramaType = PanoramaTypes.cubic;
    }

    public ArrayList<Image> getImages() {
        return images;
    }

    public PanoramaTypes getPanoramaType() {
        return panoramaType;
    }

    public int getCubeSize() {
        if (panoramaType.equals(PanoramaTypes.cubic)) {
            return images.get(0).width;
        } else {
            return (int) ((Math.tan(Math.PI / 4D)
                    * images.get(0).width / (2D * Math.PI)) * 2)
                    + AggregatedSettings.getInstance().ec.getWallOverlap() * 2;
        }
    }
}
