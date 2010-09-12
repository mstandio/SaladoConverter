package com.panozona.converter;

import com.panozona.converter.settings.DZTSettings;
import com.panozona.converter.settings.ECSettings;
import com.panozona.converter.settings.AggregatedSettings;
import com.panozona.converter.settings.GESettings;
import com.panozona.converter.table.TaskData;
import com.panozona.converter.table.TaskOperation;
import com.panozona.converter.table.TaskTableModel;
import com.panozona.converter.utils.InfoException;
import com.panozona.converter.utils.FileFilterAddTask;
import com.panozona.converter.utils.TasksExecutor;
import com.mindprod.common11.ImageInfo;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import org.jdesktop.application.TaskService;

/**
 *
 * @author Marek Standio
 */
public class Controller {

    private Properties prop;
    private TaskTableModel taskTableModel;
    private AggregatedSettings aggstngs;
    private TaskMonitor taskMonitor;
    private static Controller instance;

    private Controller() {
        prop = new Properties();
    }

    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    public void setTaskTableModel(TaskTableModel taskTableModel) {
        this.taskTableModel = taskTableModel;
    }

    public void setAggregatedSettings(AggregatedSettings aggstngs) {
        this.aggstngs = aggstngs;
    }

    public void setTaskMonitor(TaskMonitor taskMonitor) {
        this.taskMonitor = taskMonitor;
    }

    //##########################################################################
    // managing tasks
    public void addTask(File selectedFile) {
        HashMap<String, ArrayList<String>> m = new HashMap<String, ArrayList<String>>();
        addTaskR(selectedFile, m, 0);
        // analyse hashmap and add to tasks every directory containing 6 square images
        Iterator it = m.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            if (((ArrayList<String>) pairs.getValue()).size() == 6) {
                TaskData newTask = new TaskData();
                newTask.taskState = TaskData.STATE_READY;
                for (String path : (ArrayList<String>) pairs.getValue()) {
                    newTask.taskPaths.add(path);
                }
                appendTask(newTask);
            }
        }
    }

    private void addTaskR(File selectedFile, HashMap m, int currentDepth) {
        int[] dim;
        // if it is single image
        if (selectedFile.isFile() && (selectedFile.getName().toLowerCase().endsWith(".jpg") ||
                selectedFile.getName().toLowerCase().endsWith(".jpeg"))) {
            dim = ImageInfo.getImageDimensions(selectedFile.getAbsolutePath());
            // if image is equirectagular
            if (dim[0] == 2 * dim[1]) {
                TaskData newTask = new TaskData();
                newTask.taskPaths.add(selectedFile.getAbsolutePath());
                newTask.taskState = TaskData.STATE_READY;
                appendTask(newTask);
                return;

                // if image is cube wall add it to hashmap,
            } else if (dim[0] == dim[1]) {
                ArrayList<String> contents = (ArrayList<String>) m.get(selectedFile.getParent());
                if (contents == null) {
                    contents = new ArrayList<String>();
                    m.put(selectedFile.getParent(), contents);
                }
                contents.add(selectedFile.getAbsolutePath());
                return;
            }

            // file is directory, so it needs to iterate through its content
        } else {
            File files[] = selectedFile.listFiles((FileFilter) new FileFilterAddTask());
            for (int i = 0; i < files.length; i++) {
                if (aggstngs.ge.getSearchSubDirs().equals("true") && currentDepth < Integer.parseInt(aggstngs.ge.getSearchDepth())) {
                    addTaskR(files[i], m, currentDepth + 1);
                }
            }
        }
    }

    private void appendTask(TaskData newTask) {
        for (int i = 0; i < taskTableModel.getRowCount(); i++) {
            if (taskTableModel.rows.get(i).getTaskPaths().equals(newTask.getTaskPaths())) {
                taskTableModel.rows.get(i).taskState = TaskData.STATE_READY;
                return;
            }
        }
        taskTableModel.addRow(newTask);
    }

    public void removeTasks(TaskData[] tasksToRemove) {
        for (int i = 0; i < tasksToRemove.length; i++) {
            taskTableModel.removeItem(tasksToRemove[i]);
        }
    }

    public void clearTasks() {        
        taskTableModel.rows.clear();
        taskTableModel.fireTableDataChanged();
    }

    public void applyCommand() {
        boolean cubic = aggstngs.ge.getSelectedCommand().equals(GESettings.CUBIC_TO_DEEPZOOM_CUBIC);
        for (int i = 0; i < taskTableModel.getRowCount(); i++) {
            if (taskTableModel.rows.get(i).taskPaths.size() == 1) {
                taskTableModel.rows.get(i).checkBoxEnabled = !cubic;
            } else {
                taskTableModel.rows.get(i).checkBoxEnabled = cubic;
            }
        }
        taskTableModel.fireTableDataChanged();
    }

    //##########################################################################
    // generating / executing operations
    public void generateOperations() {
        TaskData taskData;
        String selection = aggstngs.ge.getSelectedCommand();
        for (int i = 0; i < taskTableModel.getRowCount(); i++) {
            taskData = taskTableModel.rows.get(i);
            taskData.operations.clear();
            if (taskData.checkBoxEnabled && taskData.checkBoxSelected) {
                if (selection.equals(GESettings.CUBIC_TO_DEEPZOOM_CUBIC)) {
                    taskData.operations = generateOpCTDZC(taskData.taskPaths);
                } else if (selection.equals(GESettings.EQUIRECTANGULAR_TO_CUBIC)) {
                    taskData.operations = generateOpETC(taskData.taskPaths);
                } else if (selection.equals(GESettings.EQUIRECTANGULAR_TO_DEEPZOOM_CUBIC)) {
                    taskData.operations = generateOpETDZC(taskData.taskPaths);
                }
            }
        }
    }

    //Cubic to DeepZoom cubic
    private Vector<TaskOperation> generateOpCTDZC(Vector<String> taskPaths) {
        Vector<TaskOperation> operations = new Vector<TaskOperation>();        
           String parentFolderName;
        for (String path : taskPaths) {
            String[] tmp = path.split(Pattern.quote(File.separator));
            parentFolderName = tmp[tmp.length-2]; // huh
            operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(path, aggstngs.ge.getOutputDir()+File.separator+"dz_"+parentFolderName,false)));
        }
        return operations;
    }

    //Equirectangular to cubic
    private Vector<TaskOperation> generateOpETC(Vector<String> taskPaths) {
        Vector<TaskOperation> operations = new Vector<TaskOperation>();
        for (String path : taskPaths) {
            operations.add(new TaskOperation(TaskOperation.OPERATION_EC, generateArgsEC(path, aggstngs.ge.getOutputDir(),false)));
        }
        return operations;
    }

    //Equirectangular to DeepZoom cubic
    private Vector<TaskOperation> generateOpETDZC(Vector<String> taskPaths) {
        Vector<TaskOperation> operations = new Vector<TaskOperation>();        

        String nameWithoutExtension;
        String generatedFile;
        String outputDir;

        for (String path : taskPaths) {
            nameWithoutExtension = path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf('.'));

            generatedFile = aggstngs.ge.getTmpDir() +                    
                    File.separator +
                    nameWithoutExtension;
            
            outputDir=aggstngs.ge.getOutputDir()+File.separator+"dz_"+nameWithoutExtension;

            operations.add(new TaskOperation(TaskOperation.OPERATION_EC, generateArgsEC(path, aggstngs.ge.getTmpDir(),true)));
            operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(generatedFile + "_b.jpg", outputDir, true)));
            operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(generatedFile + "_d.jpg", outputDir, true)));
            operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(generatedFile + "_f.jpg", outputDir, true)));
            operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(generatedFile + "_l.jpg", outputDir, true)));
            operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(generatedFile + "_r.jpg", outputDir, true)));
            operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(generatedFile + "_u.jpg", outputDir, true)));
        }
        return operations;
    }   

    private String[] generateArgsDZT(String input, String output, boolean delFiles) {
        String argsDZT[] = {
            "-verbose",
            "-simpleoutput",
            "-overlap", aggstngs.dzt.getOverlap(),
            "-quality", aggstngs.dzt.getQuality(),
            "-tilesize", aggstngs.dzt.getTileSize(),
            "-outputdir", output,
            (delFiles ? "-delsrc" : "-verbose"),
            input
        };       

        return argsDZT;
    }

    private String[] generateArgsEC(String input, String output, boolean simpleOutput) {
        String argsEC[] = {
            "-verbose",
            "-overlap", aggstngs.ec.getOverlap(),
            "-quality", aggstngs.ec.getQuality(),
            "-interpolation", aggstngs.ec.getInterpolation(),
            "-outputdir", output,
            (simpleOutput ? "-simpleoutput":"-verbose"),
            input
        };
        return argsEC;
    }

    // TODO: is that right?
    public void executeTasks() {
        TasksExecutor executor = new TasksExecutor(taskTableModel, aggstngs);
        ApplicationContext applicationContext = SaladoConverter.getApplication().getContext();
        TaskService taskService = applicationContext.getTaskService();
        taskService.execute(executor);
        taskMonitor.setForegroundTask(executor);
    }

    public void cancelRunningTasks() {
        ApplicationContext applicationContext = SaladoConverter.getApplication().getContext();
        TaskService taskService = applicationContext.getTaskService();
        List<Task> tasks = taskService.getTasks();
        if (tasks.size() > 0) {
            tasks.get(0).cancel(true);
        }
    }

    //##########################################################################
    // reading / writing settings
    // TODO: save tasks data prompt for saving before exit show read/write errors
    public void readSettingsFromFile() {
        try {
            prop.load(new FileInputStream(aggstngs.getCurrentDirectory() + File.separator + AggregatedSettings.FILE_PROPERTIES));
            try {
                aggstngs.dzt.setOverlap(prop.getProperty(DZTSettings.VALUE_OVERLAP));
                aggstngs.dzt.setTileSize(prop.getProperty(DZTSettings.VALUE_TILE_SIZE));
                aggstngs.dzt.setQuality(prop.getProperty(DZTSettings.VALUE_QUALITY));
                aggstngs.dzt.setJarDir(prop.getProperty(DZTSettings.VALUE_JAR_DIR));

                aggstngs.ec.setOverlap(prop.getProperty(ECSettings.VALUE_OVERLAP));
                aggstngs.ec.setInterpolation(prop.getProperty(ECSettings.VALUE_INTERPOLATION));
                aggstngs.ec.setQuality(prop.getProperty(ECSettings.VALUE_QUALITY));
                aggstngs.ec.setJarDir(prop.getProperty(ECSettings.VALUE_JAR_DIR));

                aggstngs.ge.setSearchSubDirs(prop.getProperty(GESettings.VALUE_SEARCH_SUBDIR));
                aggstngs.ge.setSearchDepth(prop.getProperty(GESettings.VALUE_SEARCH_DEPTH));
                aggstngs.ge.setTmpDir(prop.getProperty(GESettings.VALUE_TMP_DIR));
                aggstngs.ge.setMemoryLimit(prop.getProperty(GESettings.VALUE_MEM_LIMIT));
                aggstngs.ge.setInputDir(prop.getProperty(GESettings.VALUE_INPUT_DIR));
                aggstngs.ge.setOutputDir(prop.getProperty(GESettings.VALUE_OUTPUT_DIR));
                aggstngs.ge.setSelectedCommand(prop.getProperty(GESettings.VALUE_SELECTED_COMMAND));

            } catch (InfoException ex) {
                // TODO: indicate error
                System.out.println("corrupted settings file not all settings red");
            }
        } catch (IOException ex) {
            //TODO: indicate error
            System.out.println("settings could not be red everyting default");
        }
    }

    public void saveSettingsToFile() {
        prop.clear();

        if (aggstngs.dzt.overlapChanged()) {
            prop.put(DZTSettings.VALUE_OVERLAP, aggstngs.dzt.getOverlap());
        }
        if (aggstngs.dzt.tileSizeChanged()) {
            prop.put(DZTSettings.VALUE_TILE_SIZE, aggstngs.dzt.getTileSize());
        }
        if (aggstngs.dzt.qualityChanged()) {
            prop.put(DZTSettings.VALUE_QUALITY, aggstngs.dzt.getQuality());
        }
        if (aggstngs.dzt.jarDirChanged()) {
            prop.put(DZTSettings.VALUE_JAR_DIR, aggstngs.dzt.getJarDir());
        }

        if (aggstngs.ec.overlapChanged()) {
            prop.put(ECSettings.VALUE_OVERLAP, aggstngs.ec.getOverlap());
        }
        if (aggstngs.ec.interpolationChanged()) {
            prop.put(ECSettings.VALUE_INTERPOLATION, aggstngs.ec.getInterpolation());
        }
        if (aggstngs.ec.qualityChanged()) {
            prop.put(ECSettings.VALUE_QUALITY, aggstngs.ec.getQuality());
        }
        if (aggstngs.ec.jarDirChanged()) {
            prop.put(ECSettings.VALUE_JAR_DIR, aggstngs.ec.getJarDir());
        }

        if (aggstngs.ge.searchSubDirsChanged()) {
            prop.put(GESettings.VALUE_SEARCH_SUBDIR, aggstngs.ge.getSearchSubDirs());
        }
        if (aggstngs.ge.searchDepthChanged()) {
            prop.put(GESettings.VALUE_SEARCH_DEPTH, aggstngs.ge.getSearchDepth());
        }
        if (aggstngs.ge.tmpDirChanged()) {
            prop.put(GESettings.VALUE_TMP_DIR, aggstngs.ge.getTmpDir());
        }        
        if (aggstngs.ge.memoryLimitChanged()) {
            prop.put(GESettings.VALUE_MEM_LIMIT, aggstngs.ge.getMemoryLimit());
        }
        if (aggstngs.ge.outputDirChanged()) {
            prop.put(GESettings.VALUE_OUTPUT_DIR, aggstngs.ge.getOutputDir());
        }
        if (aggstngs.ge.inputDirChanged()) {
            prop.put(GESettings.VALUE_INPUT_DIR, aggstngs.ge.getInputDir());
        }
        if (aggstngs.ge.selectedCommandChanged()) {
            prop.put(GESettings.VALUE_SELECTED_COMMAND, aggstngs.ge.getSelectedCommand());
        }

        try {
            prop.store(new FileOutputStream(aggstngs.getCurrentDirectory() + File.separator + AggregatedSettings.FILE_PROPERTIES), null);
        } catch (IOException ex) {
            //TODO: indicate error
            System.out.println("settings could not be written");
        }
    }
}