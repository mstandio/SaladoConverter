package com.panozona.converter.settings;

import com.panozona.converter.task.TaskImages;
import com.panozona.converter.utils.InfoException;

/**
 *
 * @author Marek Standio
 */
public class TSKSettings {

    public static enum RadioButtonState {

        DEFAULT, CUSTOM, DIVISION
    }
    private TaskImages taskImages;
    private AggregatedSettings aggstngs;
    private int cubeNewSize;
    private int tileNewSize;
    private RadioButtonState tileSelection;
    private int tileCustom;
    private int tileDivision;
    private final RadioButtonState defaultTileSelection = RadioButtonState.DEFAULT;
    private final int defaultTileDivision = 4;

    public TSKSettings(TaskImages taskImages, AggregatedSettings aggstngs) {
        this.taskImages = taskImages;
        this.aggstngs = aggstngs;
        if (taskImages.getPanoType().equals(TaskImages.panoType.cubic)) {
            cubeNewSize = taskImages.getCubeSize();
        } else {
            cubeNewSize = taskImages.getCubeSize()+ 2 * Integer.parseInt(aggstngs.ec.getOverlap());
        }
        tileSelection = defaultTileSelection;
        tileCustom = Integer.parseInt(aggstngs.dzt.getTileSize());
        tileDivision = defaultTileDivision;
        tileNewSize = Integer.parseInt(aggstngs.dzt.getTileSize());
    }

    public TaskImages getTaskImages() {
        return taskImages;
    }

    public void setCubeNewSize(String value, String errorMsg) throws InfoException {
        if (value != null) {
            try {
                if (Integer.parseInt(value) > 0) {
                    cubeNewSize = Integer.parseInt(value);
                } else {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                throw new InfoException(errorMsg);
            }
        }
    }

    public String getCubeNewSize() {
        return Integer.toString(cubeNewSize);
    }

    public String getDefaultCubeNewSize() {
        if (taskImages.getPanoType().equals(TaskImages.panoType.cubic)) {
            return Integer.toString(taskImages.getCubeSize());
        } else {
            return Integer.toString(taskImages.getCubeSize() + 2 * Integer.parseInt(aggstngs.ec.getOverlap()));
        }
    }

    public boolean CubeNewSizeChanged() {
        return cubeNewSize != taskImages.getCubeSize();
    }

    public String cubeSizeDescription() {
        if (CubeNewSizeChanged()) {
            return getDefaultCubeNewSize() + " to " + getCubeNewSize();
        } else {
            return getDefaultCubeNewSize();
        }
    }

    public void setTileNewSize(String value, String errorMsg) throws InfoException {
        if (value != null) {
            try {
                if (Integer.parseInt(value) > 0) {
                    tileNewSize = Integer.parseInt(value);
                } else {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                throw new InfoException(errorMsg);
            }
        }
    }

    public String getTileNewSize() {
        if (tileSelection.equals(RadioButtonState.DEFAULT)) {
            return aggstngs.dzt.getTileSize();
        } else {
            return Integer.toString(tileNewSize);
        }
    }

    public String getDefaultTileNewSize() {
        return aggstngs.dzt.getTileSize();
    }

    public boolean TileNewSizeChanged() {
        return !getTileNewSize().equals(aggstngs.dzt.getTileSize());
    }

    public String tileSizeDescription() {
        return getTileNewSize();
    }

    public String getTileCustom() {
        return Integer.toString(tileCustom);
    }

    public void setTileCustom(String value, String errorMsg) throws InfoException {
        if (value != null) {
            try {
                if (Integer.parseInt(value) > 0) {
                    tileCustom = Integer.parseInt(value);
                } else {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                throw new InfoException(errorMsg);
            }
        }
    }

    public RadioButtonState getTileSelection() {
        return tileSelection;
    }

    public void setTileSelection(RadioButtonState value) {
        this.tileSelection = value;
    }

    public void setTileDivision(int value) {
        this.tileDivision = value;
    }

    public int getTileDivision() {
        return tileDivision;
    }
}
