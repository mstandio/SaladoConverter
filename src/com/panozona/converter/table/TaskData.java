package com.panozona.converter.table;

import java.io.File;
import java.util.Vector;

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
    public Vector<String> taskPaths; // single equirectangular or six cube walls
    public Vector<TaskOperation> operations;

    public TaskData() {
        checkBoxSelected = true;
        checkBoxEnabled = false;
        taskState = STATE_ERROR;
        taskPaths = new Vector<String>();
        operations = new Vector<TaskOperation>();
    }

    public String getTaskPaths(){
        if(taskPaths.size() == 1){
            return taskPaths.get(0);
        }else if(taskPaths.size() == 6){            
            return taskPaths.get(0).substring(0,taskPaths.get(0).lastIndexOf(File.separator));
        }else{
            return "[invalid]";
        }
    }
}