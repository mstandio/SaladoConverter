package com.panozona.converter;

import com.panozona.converter.task.TaskData;

public class Optimizer {

    private Optimizer() {
    }

    static void optimize(TaskData taskData) {
        taskData.setNewCubeSize(optimizeCubeSize(taskData.getPanorama().getCubeSize()));
        //taskData.setNewTileSize(optimizeCubeSize());
    }

    private static int optimizeCubeSize(int size) {
        int result = size;
        return result;
    }

    private static int optimizeTileSize(int size) {
        int result = size;
        return result;
    }
}
