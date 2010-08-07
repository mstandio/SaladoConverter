package com.panozona.converter.utils;

import com.panozona.converter.SaladoConverter;
import com.panozona.converter.settings.AggregatedSettings;
import com.panozona.converter.settings.DZTSettings;
import com.panozona.converter.settings.ECSettings;
import com.panozona.converter.table.TaskData;
import com.panozona.converter.table.TaskOperation;
import com.panozona.converter.table.TaskTableModel;

import org.jdesktop.application.Task;

/**
 *
 * @author Marek
 */
public class TasksExecutor extends Task<Void, Void> {

    private AggregatedSettings aggstngs;
    private TaskTableModel taskTableModel;
    private ComponentInvoker componentInvoker;

    public TasksExecutor(TaskTableModel taskTableModel, AggregatedSettings aggstngs) {
        super(SaladoConverter.getApplication());
        super.setUserCanCancel(true);
        this.taskTableModel = taskTableModel;
        this.aggstngs = aggstngs;
        componentInvoker = new ComponentInvoker();
    }

    @Override
    protected Void doInBackground() throws Exception {
        int numTasksToRun = 0;

        TaskData taskData;

        for (int i = 0; i < taskTableModel.getRowCount(); i++) {
            taskData = taskTableModel.getValueAt(i, 0);
            if(taskData.checkBoxEnabled && taskData.checkBoxSelected){
                numTasksToRun++;
            }
        }        
        
        for (int j = 0; j < taskTableModel.getRowCount(); j++) {
            
            setProgress(j, 0, numTasksToRun);

            taskData = taskTableModel.getValueAt(j, 0);
            for (TaskOperation operation : taskData.operations) {
                if (!super.isCancelled()) {
                    taskData.taskState = TaskData.STATE_PROCESSING;
                    taskTableModel.fireTableDataChanged();
                    try {
                        if (operation.operationType.equals(TaskOperation.OPERATION_DZT)) {
                            componentInvoker.run(aggstngs.dzt.getJarDir(), DZTSettings.JAR_CLASSNAME, operation.args);
                        } else if (operation.operationType.equals(TaskOperation.OPERATION_EC)) {
                            componentInvoker.run(aggstngs.ec.getJarDir(), ECSettings.JAR_CLASSNAME, operation.args);
                        }
                    } catch (InfoException ex) {
                        System.out.println("ERROR: "+ex.getMessage());
                        taskData.taskState = TaskData.STATE_ERROR;
                        taskTableModel.fireTableDataChanged();
                    }
                } else {
                    taskData.taskState = TaskData.STATE_CANCELED;
                    System.out.println("CANCELLED");
                    taskTableModel.fireTableDataChanged();
                    return null;
                }
            }
            if(taskData.operations.size() > 0){
                System.out.println("DONE");
                taskData.taskState = TaskData.STATE_DONE;
                taskTableModel.fireTableDataChanged();
            }
        }
        return null;
    }
}
