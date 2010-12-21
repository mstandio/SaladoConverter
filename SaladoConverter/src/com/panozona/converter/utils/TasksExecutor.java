package com.panozona.converter.utils;

import com.panozona.converter.SaladoConverter;
import com.panozona.converter.settings.AggregatedSettings;
import com.panozona.converter.settings.DZTSettings;
import com.panozona.converter.settings.ECSettings;
import com.panozona.converter.task.TaskData;
import com.panozona.converter.task.TaskOperation;
import com.panozona.converter.maintable.TaskTableModel;
import com.panozona.converter.settings.RESSettings;
import org.jdesktop.application.Task;
import java.io.File;

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

    private int numberOperations(TaskData taskData) {
        int result = 0;
        for (int k = 0; k < taskData.operations.size(); k++) {
            if (!taskData.operations.get(k).operationType.equals(TaskOperation.OPERATION_DEL)) {
                result++;
            }
        }
        return result;
    }

    @Override
    protected Void doInBackground() throws Exception {
        int numOperationsToRun = 0;
        int numOperationsDone = 0;
        TaskData taskData;
        TaskOperation taskOperation;

        for (int i = 0; i < taskTableModel.getRowCount(); i++) {
            taskData = taskTableModel.rows.get(i);
            if (taskData.checkBoxEnabled && taskData.checkBoxSelected) {
                numOperationsToRun += numberOperations(taskData);
            }
        }

        for (int j = 0; j < taskTableModel.getRowCount(); j++) {
            taskData = taskTableModel.rows.get(j);
            for (int m = 0; m < taskData.operations.size(); m++) {
                taskOperation = taskData.operations.get(m);
                if (!super.isCancelled()) {
                    taskData.taskState = TaskData.STATE_PROCESSING;
                    taskTableModel.fireTableDataChanged();
                    try {
                        if (taskOperation.operationType.equals(TaskOperation.OPERATION_DZT)) {
                            componentInvoker.run(aggstngs.dzt.getJarDir(), DZTSettings.JAR_CLASSNAME, taskOperation.args);
                            numOperationsDone++;
                        } else if (taskOperation.operationType.equals(TaskOperation.OPERATION_EC)) {                            
                            componentInvoker.run(aggstngs.ec.getJarDir(), ECSettings.JAR_CLASSNAME, taskOperation.args);
                            numOperationsDone++;
                        } else if (taskOperation.operationType.equals(TaskOperation.OPERATION_RES)) {                            
                            componentInvoker.run(aggstngs.res.getJarDir(), RESSettings.JAR_CLASSNAME, taskOperation.args);
                            numOperationsDone++;
                        } else if (taskOperation.operationType.equals(TaskOperation.OPERATION_DEL)) {
                            new File(taskOperation.args[0]).delete();
                        }
                        setProgress(numOperationsDone, 0, numOperationsToRun);
                    } catch (InfoException ex) {
                        numOperationsDone += numberOperations(taskData) - m;
                        setProgress(numOperationsDone, 0, numOperationsToRun);
                        System.out.println("ERROR: " + ex.getMessage());
                        taskData.taskState = TaskData.STATE_ERROR;
                        taskTableModel.fireTableDataChanged();
                    }
                } else {
                    numOperationsDone += numberOperations(taskData);
                    setProgress(numOperationsDone, 0, numOperationsToRun);
                    taskData.taskState = TaskData.STATE_CANCELED;
                    System.out.println("CANCELLED");
                    taskTableModel.fireTableDataChanged();
                    return null;
                }
            }
            if (taskData.operations.size() > 0) {
                System.out.println("DONE");
                taskData.taskState = TaskData.STATE_DONE;
                taskData.checkBoxSelected = false;
                taskTableModel.fireTableDataChanged();
            }
        }
        return null;
    }
}
