package com.panozona.converter.settings;

import com.panozona.converter.task.TaskImages;
import com.panozona.converter.utils.InfoException;

/**
 *
 * @author Marek Standio
 */
public class TSKSettings {

    private TaskImages taskImages;
    private int cubeNewSize;

    public TSKSettings(TaskImages taskImages) {
        this.taskImages = taskImages;
        cubeNewSize = taskImages.getCubeSize();
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
        return Integer.toString(taskImages.getCubeSize());
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
}
