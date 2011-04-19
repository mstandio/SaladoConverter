package com.panozona.converter.task;

import com.panozona.converter.settings.AggregatedSettings;
import com.panozona.converter.utils.Messages;
import java.io.File;
import java.util.ArrayList;

/**
 * @author Marek
 */
public class TaskData {

    public static final String STATE_READY = "ready"; // TODO: enum, perhaps?
    public static final String STATE_PROCESSING = "processing";
    public static final String STATE_DONE = "done";
    public static final String STATE_ERROR = "error";
    public static final String STATE_CANCELED = "canceled";
    public String state = STATE_READY;
    public Boolean checkBoxSelected = true;
    public Boolean checkBoxEnabled = false;
    public boolean autosize;
    private Panorama panorama;
    private int newTileSize;
    private int newCubeSize;
    public ArrayList<Operation> operations = new ArrayList<Operation>();

    public TaskData(Panorama panorama) {
        this.panorama = panorama;
        newCubeSize = panorama.getCubeSize();
        newTileSize = AggregatedSettings.getInstance().dzt.getTileSize();
    }

    public Panorama getPanorama() {
        return panorama;
    }

    public void setNewTileSize(int value) throws IllegalArgumentException {
        if (value > 0) {
            newTileSize = value;
        } else {
            throw new IllegalArgumentException(Messages.TSK_TILE_SIZE_ERROR);
        }
    }

    public int getNewTileSize() {
        return newTileSize;
    }

    public void setNewCubeSize(int value) throws IllegalArgumentException {
        if (value > 0) {
            newCubeSize = value;
        } else {
            throw new IllegalArgumentException(Messages.TSK_CUBE_SIZE_ERROR);
        }
    }

    public int getNewCubeSize() {
        return newCubeSize;
    }

    public boolean cubeSizeChanged() {
        return newCubeSize != panorama.getCubeSize();
    }

    public String getCubeSizeDescription() {
        if (panorama.getCubeSize() != newCubeSize) {
            return panorama.getCubeSize() + " to " + newCubeSize;
        } else {
            return Integer.toString(newCubeSize);
        }
    }

    public String getTileSizeDescription() {
        return Integer.toString(newTileSize);
    }

    public String getPathDescription() {
        if (panorama.getPanoramaType().equals(PanoramaTypes.equirectangular)) {
            return panorama.getImages().get(0).path;
        } else {
            return panorama.getImages().get(0).path.substring(0, panorama.getImages().get(0).path.lastIndexOf(File.separator));
        }
    }
}
