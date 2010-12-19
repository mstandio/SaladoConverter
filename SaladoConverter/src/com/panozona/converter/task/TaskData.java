package com.panozona.converter.task;

import com.panozona.converter.settings.TSKSettings;
import java.util.ArrayList;

/**
 *
 * @author Marek
 */
public class TaskData {

    public static final String STATE_READY = "ready";
    public static final String STATE_PROCESSING = "processing";
    public static final String STATE_DONE = "done";
    public static final String STATE_ERROR = "error";
    public static final String STATE_CANCELED = "canceled";
    public Boolean checkBoxSelected;
    public Boolean checkBoxEnabled;
    public String taskState;
    public ArrayList<TaskOperation> operations;
    public TSKSettings taskSettings;

    public TaskData(TaskImages taskImages) {
        checkBoxSelected = true;
        checkBoxEnabled = false;
        taskState = STATE_ERROR;
        taskSettings = new TSKSettings(taskImages);
        operations = new ArrayList<TaskOperation>();
    }
    
    public TSKSettings getTaskSettings(){
        return taskSettings;
    }
}