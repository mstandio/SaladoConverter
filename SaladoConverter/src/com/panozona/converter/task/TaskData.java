package com.panozona.converter.task;

import com.panozona.converter.Optimizer;
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
    private int newTileSize = 0;
    private int newCubeSize = 0;
    private int optimalTileSize = 0;
    private int optimalCubeSize = 0;
    public ArrayList<Operation> operations = new ArrayList<Operation>();
    public boolean surpressOptimalisation = false;
    private boolean isOptimalisated = true;

    public TaskData(Panorama panorama) {
        this.panorama = panorama;
    }

    public void optimalize() {
        Optimizer.optimize(this);
    }

    public boolean getIsOptimalisated() {
        return isOptimalisated;
    }

    public void setIsOptimalisated(boolean value) {
        isOptimalisated = value;
    }

    public Panorama getPanorama() {
        return panorama;
    }

    public void setNewTileSize(String value) throws IllegalArgumentException {
        if (value != null) {
            if (value.trim().length() == 0) {
                newTileSize = 0;
                return;
            }
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

    public int getNewTileSize() {
        if (isOptimalisated && !surpressOptimalisation) {
            return optimalTileSize;
        }
        if (newTileSize == 0) {
            return getOriginalTileSize();
        }
        return newTileSize;
    }

    public void setNewCubeSize(String value) throws IllegalArgumentException {
        if (value != null) {
            if (value.trim().length() == 0) {
                newCubeSize = 0;
                return;
            }

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

    public int getNewCubeSize() {
        if (isOptimalisated && !surpressOptimalisation) {
            return optimalCubeSize;
        }
        if (newCubeSize == 0) {
            return getOriginalCubeSize();
        }
        return newCubeSize;
    }

    public boolean cubeSizeChanged() {
        return getNewCubeSize() != getOriginalCubeSize();
    }

    public boolean tileSizeChanged() {
        return getNewTileSize() != getOriginalTileSize();
    }

    public abstract int getOriginalCubeSize();

    public int getOriginalTileSize() {
        return AggregatedSettings.getInstance().zyt.getTileSize(); // TODO: change to dzt    
    }

    public String getCubeSizeDescription() {
        if (!checkBoxEnabled) {
            return "";
        }
        if (showCubeSize) {
            if (getOriginalCubeSize() != getNewCubeSize()) {
                return getOriginalCubeSize() + " to " + getNewCubeSize();
            } else {
                return Integer.toString(getOriginalCubeSize());
            }
        } else {
            return "";
        }
    }

    public String getTileSizeDescription() {
        if (!checkBoxEnabled) {
            return "";
        }
        if (showTizeSize) {
            if (tileSizeChanged()) {
                return Integer.toString(getNewTileSize());
            } else {
                return Integer.toString(getOriginalTileSize());
            }

        } else {
            return "";
        }
    }

    public void setOptimalCubeSize(int value) {
        optimalCubeSize = value;
    }

    public void setOptimalTileSize(int value) {
        optimalTileSize = value;
    }

    public abstract String getPathDescription();
}
