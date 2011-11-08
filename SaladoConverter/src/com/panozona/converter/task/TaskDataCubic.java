package com.panozona.converter.task;

/**
 * @author Marek Standio
 */
public class TaskDataCubic extends TaskData {

    public TaskDataCubic(Panorama panorama) {
        super(panorama);
    }

    @Override
    public final int getOriginalCubeSize() {
        return panorama.getImages().get(0).width;
    }

    @Override
    public final String getPathDescription() {
        return panorama.getImages().get(0).path.substring(0, panorama.getImages().get(0).path.lastIndexOf("_"));
    }
}
