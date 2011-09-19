package com.panozona.converter.utils;

import com.panozona.converter.SaladoConverter;
import com.panozona.converter.settings.AggregatedSettings;
import com.panozona.converter.settings.DZTSettings;
import com.panozona.converter.settings.ECSettings;
import com.panozona.converter.task.TaskData;
import com.panozona.converter.task.Operation;
import com.panozona.converter.maintable.TaskTableModel;
import com.panozona.converter.settings.ERFSettings;
import com.panozona.converter.settings.RESSettings;
import com.panozona.converter.settings.SBMSettings;
import com.panozona.converter.settings.ZYTSettings;
import com.panozona.converter.task.TaskDataStates;
import org.jdesktop.application.Task;
import java.io.File;

/** 
 * @author Marek Standio
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
            if (!taskData.operations.get(k).type.equals(Operation.TYPE_DEL)) {
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
        Operation taskOperation;
        File file;
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
                    taskData.state = TaskDataStates.PROCESSING;
                    taskTableModel.fireTableDataChanged();
                    try {
                        if (taskOperation.type.equals(Operation.TYPE_DZT)) {
                            componentInvoker.run(aggstngs.dzt.getJarDir(), DZTSettings.JAR_CLASSNAME, taskOperation.args);
                            numOperationsDone++;
                        }else if (taskOperation.type.equals(Operation.TYPE_ZYT)) {
                            componentInvoker.run(aggstngs.zyt.getJarDir(), ZYTSettings.JAR_CLASSNAME, taskOperation.args);
                            numOperationsDone++;
                        }else if (taskOperation.type.equals(Operation.TYPE_SB)) {
                            componentInvoker.run(aggstngs.sbm.getJarDir(), SBMSettings.JAR_CLASSNAME, taskOperation.args);
                            numOperationsDone++;
                        } else if (taskOperation.type.equals(Operation.TYPE_EC)) {
                            componentInvoker.run(aggstngs.ec.getJarDir(), ECSettings.JAR_CLASSNAME, taskOperation.args);
                            numOperationsDone++;
                        } else if (taskOperation.type.equals(Operation.TYPE_RES)) {
                            componentInvoker.run(aggstngs.res.getJarDir(), RESSettings.JAR_CLASSNAME, taskOperation.args);
                            numOperationsDone++;
                        } else if (taskOperation.type.equals(Operation.TYPE_ERF)) {
                            componentInvoker.run(aggstngs.erf.getJarDir(), ERFSettings.JAR_CLASSNAME, taskOperation.args);
                            numOperationsDone++;
                        } else if (taskOperation.type.equals(Operation.TYPE_DEL)) {
                            file = new File(taskOperation.args[0]);
                            if (file.isDirectory()) {
                                for (File f: file.listFiles()) {
                                    f.delete();
                                }
                            }
                            file.delete();
                            setProgress(numOperationsDone, 0, numOperationsToRun);
                        }
                        setProgress(numOperationsDone, 0, numOperationsToRun);
                    } catch (IllegalStateException ex) {
                        numOperationsDone += numberOperations(taskData) - m;
                        setProgress(numOperationsDone, 0, numOperationsToRun);
                        System.out.println("ERROR: " + ex.getMessage());
                        taskData.state = TaskDataStates.ERROR;
                        taskTableModel.fireTableDataChanged();
                    }
                } else {
                    numOperationsDone += numberOperations(taskData);
                    setProgress(numOperationsDone, 0, numOperationsToRun);
                    taskData.state = TaskDataStates.CANCELED;
                    System.out.println("CANCELLED");
                    taskTableModel.fireTableDataChanged();
                    return null;
                }
            }
            if (taskData.operations.size() > 0) {
                System.out.println("DONE");
                taskData.checkBoxSelected = false;
                taskData.state = TaskDataStates.DONE;
                taskTableModel.fireTableDataChanged();
            }
        }
        return null;
    }
}
