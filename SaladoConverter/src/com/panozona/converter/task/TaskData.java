package com.panozona.converter.task;

import com.panozona.converter.settings.AggregatedSettings;
import com.panozona.converter.utils.Messages;
import java.util.ArrayList;

/**
 * @author Marek Standio
 */
public abstract class TaskData {

    public TaskDataStates state = TaskDataStates.READY;
    public Boolean checkBoxSelected = true;
    public Boolean checkBoxEnabled = false;
    public Boolean showCubeSize = true;
    public Boolean showTizeSize = true;
    protected Panorama panorama;
    private int newTileSize;
    private int newCubeSize;
    public boolean optimalSize = true;
    public boolean surpressOptimalSize = false;
    private int newOptimalTileSize;
    private int newOptimalCubeSize;
    public ArrayList<Operation> operations = new ArrayList<Operation>();

    public TaskData(Panorama panorama) {
        this.panorama = panorama;
        newTileSize = AggregatedSettings.getInstance().zyt.getTileSize(); //TODO: change to dzt
    }

    public Panorama getPanorama() {
        return panorama;
    }

    public void setNewTileSize(String value) throws IllegalArgumentException {
        if (value != null) {
            try {
                if (Integer.parseInt(value) > 0) {
                    newTileSize = Integer.parseInt(value);
                } else {
                    throw new IllegalArgumentException();
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException(Messages.TSK_TILE_SIZE_ERROR);
            }
        }
    }

    public void setNewTileSize(int value) throws IllegalArgumentException {
        if (value > 0) {
            newTileSize = value;
        } else {
            throw new IllegalArgumentException(Messages.TSK_TILE_SIZE_ERROR);
        }
    }

    public void setNewOptimalTileSize(int value) {
        newOptimalTileSize = value;
    }

    public int getNewTileSize() {
        if (!surpressOptimalSize && optimalSize) {
            return newOptimalTileSize;
        } else {
            return newTileSize;
        }

    }

    public void setNewCubeSize(String value) throws IllegalArgumentException {
        if (value != null) {
            try {
                if (Integer.parseInt(value) > 0) {
                    newCubeSize = Integer.parseInt(value);
                } else {
                    throw new IllegalArgumentException();
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException(Messages.TSK_CUBE_SIZE_ERROR);
            }
        }
    }

    public void setNewCubeSize(int value) throws IllegalArgumentException {
        if (value > 0) {
            newCubeSize = value;
        } else {
            throw new IllegalArgumentException(Messages.TSK_CUBE_SIZE_ERROR);
        }
    }

    public void setNewOptimalCubeSize(int value) {
        newOptimalCubeSize = value;
    }

    public int getNewCubeSize() {
        if (!surpressOptimalSize && optimalSize) {
            return newOptimalCubeSize;
        } else {
            return newCubeSize;
        }

    }

    public boolean cubeSizeChanged() {
        return getNewCubeSize() != getOriginalCubeSize();
    }

    public abstract int getOriginalCubeSize();

    public String getCubeSizeDescription() {
        if (showCubeSize) {
            if (getOriginalCubeSize() != getNewCubeSize()) {
                return getOriginalCubeSize() + " to " + getNewCubeSize();
            } else {
                return Integer.toString(getNewCubeSize());
            }
        } else {
            return "-";
        }
    }

    public String getTileSizeDescription() {
        if (showTizeSize) {
            return Integer.toString(getNewTileSize());
        } else {
            return "-";
        }
    }

    public abstract String getPathDescription();
}
