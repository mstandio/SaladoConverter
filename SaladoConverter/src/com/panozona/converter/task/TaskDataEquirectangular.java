package com.panozona.converter.task;

import com.panozona.converter.settings.AggregatedSettings;
import com.panozona.converter.utils.Messages;

/**
 * @author Marek Standio
 */
public class TaskDataEquirectangular extends TaskData{

    private int fov = 360;
    private int offset = 0;
    
    public TaskDataEquirectangular(Panorama panorama){
        super(panorama);        
    }
    
    public boolean requiresFilling(){
        return!(panorama.getImages().get(0).width == 2 * panorama.getImages().get(0).height && fov == 360);
    }

    public void setFov(String value) throws IllegalArgumentException {
        if (value != null) {
            try {
                if (Integer.parseInt(value) > 0 && Integer.parseInt(value) <= 360) {
                    fov = Integer.parseInt(value);
                } else {
                    throw new IllegalArgumentException();
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException(Messages.TSK_FOV_SIZE_ERROR);
            }
        }
    }

    public void setFov(int value) throws IllegalArgumentException {
        if (value > 0 && value <= 360) {
            fov = value;
        } else {
            throw new IllegalArgumentException(Messages.TSK_FOV_SIZE_ERROR);
        }
    }

    public int getFov() {
        return fov;
    }

    public void setOffset(String value) throws IllegalArgumentException {
        if (value != null) {
            try {
                offset = Integer.parseInt(value);
            } catch (Exception ex) {
                throw new IllegalArgumentException(Messages.TSK_OFFSET_ERROR);
            }
        }
    }

    public void setOffset(int value) throws IllegalArgumentException {
        offset = value;
    }       

    public int getOffset() {
        return offset;
    }
    
    private int getWidth(){
        if (fov == 360){
            return panorama.getImages().get(0).width;
        }else{
            return (int) Math.floor(360d * (double) panorama.getImages().get(0).width / (double) fov);
        }
    }

    @Override
    public final int getOriginalCubeSize() {
        return (int) ((Math.tan(Math.PI / 4D)
                    * getWidth() / (2D * Math.PI)) * 2)
                    + AggregatedSettings.getInstance().ec.getWallOverlap() * 2;
    }

    @Override
    public final String getPathDescription() {
        return panorama.getImages().get(0).path;
    }
}
