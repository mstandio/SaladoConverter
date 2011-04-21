package com.panozona.converter;

import com.panozona.converter.settings.AggregatedSettings;
import com.panozona.converter.task.TaskData;
import java.util.HashMap;

public class Optimizer {

    private Optimizer() {
    }

    static void optimize(TaskData taskData) {
        taskData.setNewCubeSize(optimizeCubeSize(taskData.getPanorama().getCubeSize()));
        taskData.setNewTileSize(optimizeTileSize(taskData.getNewCubeSize()));
    }

    private static int optimizeCubeSize(int size) {
        int resize = (int) ((float) AggregatedSettings.getInstance().opt.getResizePercent() / 100f * (float) size);
        HashMap<Integer, Integer> optResults = new HashMap<Integer, Integer>();
        for (int newSize = (size - resize); newSize <= (size + resize); newSize++) {
            for (int division = 512; division >= 2; division /= 2) {
                if (newSize % division == 0) {
                    if (optResults.get(division) == null
                            || Math.abs(size - newSize) < Math.abs(size - optResults.get(division).intValue())) {
                        optResults.put(division, newSize);
                    }
                }
            }
        }
        Integer bestKey = 0;
        for (Integer key : optResults.keySet()) {
            if (key.intValue() > bestKey.intValue()) {
                bestKey = key;
            }
        }
        if (optResults.get(bestKey) != null) {
            return optResults.get(bestKey).intValue();
        } else {
            return size;
        }
    }

    private static int optimizeTileSize(int newCubeSize) {
        for (int i = AggregatedSettings.getInstance().opt.getMinTileSize(); i <= AggregatedSettings.getInstance().opt.getMaxTileSize(); i++) {
            if (newCubeSize % i == 0) {
                return i;
            }
        }
        return AggregatedSettings.getInstance().dzt.getTileSize();
    }
}
