package com.panozona.converter;

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
import java.util.regex.Pattern;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import org.jdesktop.application.TaskService;
import org.jdesktop.application.ApplicationContext;
import com.panozona.converter.settings.DZTSettings;
import com.panozona.converter.settings.ECSettings;
import com.panozona.converter.settings.AggregatedSettings;
import com.panozona.converter.settings.GESettings;
import com.panozona.converter.task.TaskData;
import com.panozona.converter.task.TaskOperation;
import com.panozona.converter.task.TaskImages;
import com.panozona.converter.task.ImageData;
import com.panozona.converter.utils.InfoException;
import com.panozona.converter.utils.FileFilterAddTask;
import com.panozona.converter.utils.TasksExecutor;
import com.panozona.converter.maintable.TaskTableModel;
import com.panozona.converter.settings.RESSettings;
import com.panozona.converter.utils.ImageDimensionsChecker;
import com.panozona.converter.utils.Info;

/**
 *
 * @author Marek Standio
 */
public class Controller {

    private TaskTableModel taskTableModel;
    private MainWindowView mainWindowView;
    private AggregatedSettings aggstngs;
    private TaskMonitor taskMonitor;
    private static Controller instance;

    private Controller() {
    } // hide default constructor

    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    public void setTaskTableModel(TaskTableModel taskTableModel) {
        this.taskTableModel = taskTableModel;
    }

    public void setMainWindowView(MainWindowView mainWindowView) {
        this.mainWindowView = mainWindowView;
    }

    public void setAggregatedSettings(AggregatedSettings aggstngs) {
        this.aggstngs = aggstngs;
    }

    public void setTaskMonitor(TaskMonitor taskMonitor) {
        this.taskMonitor = taskMonitor;
    }

    //##########################################################################
    // managing tasks
    public void addTask(File[] selectedFiles) {

        HashMap<String, ArrayList<ImageData>> m = new HashMap<String, ArrayList<ImageData>>();
        for (File file : selectedFiles) {
            addTaskR(file, m, true);
        }

        // analyse hashmap and add to tasks every directory containing 6 square images
        Iterator it = m.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            if (((ArrayList<ImageData>) pairs.getValue()).size() == 6) {
                TaskData newTask = new TaskData(new TaskImages((ArrayList<ImageData>) pairs.getValue()), aggstngs);
                newTask.taskState = TaskData.STATE_READY;
                appendTask(newTask);
            }
        }
    }

    private void addTaskR(File selectedFile, HashMap m, boolean searchSubDirectories) {

        // if it is single image
        if (selectedFile.isFile()) {
            ImageData imageData = ImageDimensionsChecker.analise(selectedFile);
            if (imageData == null) {
                return;
            }

            // equirectangular file
            if (imageData.width == imageData.height * 2) {
                TaskData newTask = new TaskData(new TaskImages(imageData), aggstngs);
                newTask.taskState = TaskData.STATE_READY;
                appendTask(newTask);
                return;

                // possibly cube wall
            } else if (imageData.width == imageData.height) {
                ArrayList<ImageData> contents = (ArrayList<ImageData>) m.get(selectedFile.getParent());
                if (contents == null) {
                    contents = new ArrayList<ImageData>();
                    m.put(selectedFile.getParent(), contents);
                }
                if ((contents.isEmpty()) || (contents.size() > 0 && contents.get(0).width == imageData.width)) {
                    contents.add(imageData);
                }
                return;
            }

            // file is directory, so it needs to iterate through its content
        } else if (searchSubDirectories) {
            File files[] = selectedFile.listFiles((FileFilter) new FileFilterAddTask());
            for (int i = 0; i < files.length; i++) {
                addTaskR(files[i], m, false);
            }
        }
    }

    private void appendTask(TaskData newTask) {
        for (int i = 0; i < taskTableModel.getRowCount(); i++) {
            if (taskTableModel.rows.get(i).taskSettings.getTaskImages().getTaskPathDescription().equals(newTask.taskSettings.getTaskImages().getTaskPathDescription())) {
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
        boolean cubic = aggstngs.ge.getSelectedCommand().equals(GESettings.CUBIC_TO_DEEPZOOM_CUBIC) || aggstngs.ge.getSelectedCommand().equals(GESettings.CUBIC_TO_RESIZED_CUBIC);

        for (int i = 0; i < taskTableModel.getRowCount(); i++) {
            if (taskTableModel.rows.get(i).taskSettings.getTaskImages().getPanoType() == TaskImages.panoType.equirectangular) {
                taskTableModel.rows.get(i).checkBoxEnabled = !cubic;
            } else {
                if (aggstngs.ge.getSelectedCommand().equals(GESettings.CUBIC_TO_RESIZED_CUBIC)) {
                    if (taskTableModel.rows.get(i).taskSettings.CubeNewSizeChanged()) {
                        taskTableModel.rows.get(i).checkBoxEnabled = cubic;
                    } else {
                        taskTableModel.rows.get(i).checkBoxEnabled = false;
                    }
                } else {
                    taskTableModel.rows.get(i).checkBoxEnabled = cubic;
                }
            }
        }
        taskTableModel.fireTableDataChanged();
        mainWindowView.analyseTasks();
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
                taskData.taskState = TaskData.STATE_READY;
                if (selection.equals(GESettings.CUBIC_TO_RESIZED_CUBIC)) {
                    generateOpCRES(taskData);
                } else if (selection.equals(GESettings.CUBIC_TO_DEEPZOOM_CUBIC)) {
                    generateOpCTDZC(taskData);
                } else if (selection.equals(GESettings.EQUIRECTANGULAR_TO_CUBIC)) {
                    generateOpETC(taskData);
                } else if (selection.equals(GESettings.EQUIRECTANGULAR_TO_DEEPZOOM_CUBIC)) {
                    generateOpETDZC(taskData);
                }
            }
            taskTableModel.fireTableDataChanged();
        }
    }

    // Cubic to resized Cubic
    private void generateOpCRES(TaskData taskData) {
        String parentFolderName;
        for (ImageData imagedata : taskData.getTaskSettings().getTaskImages().getImagesData()) {
            String[] tmp = imagedata.path.split(Pattern.quote(File.separator));
            parentFolderName = tmp[tmp.length - 2];
            taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_RES, generateArgsRES(imagedata.path, aggstngs.ge.getOutputDir() + File.separator + "resized_" + parentFolderName, Integer.parseInt(taskData.getTaskSettings().getCubeNewSize()), true)));
        }
    }

    //Cubic to DeepZoom cubic
    private void generateOpCTDZC(TaskData taskData) {
        String parentFolderName;
        String nameWithExtension;
        String[] tmp;
        int newSize;
        String resDir = aggstngs.ge.getTmpDir() + File.separator + "res";
        for (ImageData imagedata : taskData.getTaskSettings().getTaskImages().getImagesData()) {
            tmp = imagedata.path.split(Pattern.quote(File.separator));
            parentFolderName = tmp[tmp.length - 2];
            if (taskData.getTaskSettings().CubeNewSizeChanged()) {
                newSize = Integer.parseInt(taskData.getTaskSettings().getCubeNewSize());
                nameWithExtension = imagedata.path.substring(imagedata.path.lastIndexOf(File.separator) + 1, imagedata.path.length());
                
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_RES, generateArgsRES(imagedata.path, resDir, newSize, true)));

                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(resDir + File.separator + nameWithExtension, aggstngs.ge.getOutputDir() + File.separator + "dz_" + parentFolderName, taskData.taskSettings.getTileNewSize(), true)));

                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{resDir + File.separator + nameWithExtension}));
            } else {
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(imagedata.path, aggstngs.ge.getOutputDir() + File.separator + "dz_" + parentFolderName, taskData.taskSettings.getTileNewSize(), true)));
            }
        }
    }

    //Equirectangular to cubic
    private void generateOpETC(TaskData taskData) {
        String nameWithoutExtension;
        String cubeFile;
        String outputDir;
        String resDir = aggstngs.ge.getTmpDir() + File.separator + "res";
        int newSize;
        // there is only one imagedata
        for (ImageData imagedata : taskData.getTaskSettings().getTaskImages().getImagesData()) {
            if (taskData.taskSettings.CubeNewSizeChanged()) {
                nameWithoutExtension = imagedata.path.substring(imagedata.path.lastIndexOf(File.separator) + 1, imagedata.path.lastIndexOf('.'));
                cubeFile = aggstngs.ge.getTmpDir() + File.separator + "res" + File.separator + nameWithoutExtension;
                outputDir = aggstngs.ge.getOutputDir() + File.separator + "cubic_" + nameWithoutExtension;
                newSize = Integer.parseInt(taskData.getTaskSettings().getCubeNewSize());

                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_EC, generateArgsEC(imagedata.path, resDir, true)));

                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_RES, generateArgsRES(cubeFile + "_b.tif", outputDir, newSize, true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_RES, generateArgsRES(cubeFile + "_d.tif", outputDir, newSize, true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_RES, generateArgsRES(cubeFile + "_f.tif", outputDir, newSize, true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_RES, generateArgsRES(cubeFile + "_l.tif", outputDir, newSize, true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_RES, generateArgsRES(cubeFile + "_r.tif", outputDir, newSize, true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_RES, generateArgsRES(cubeFile + "_u.tif", outputDir, newSize, true)));

                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{cubeFile + "_b.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{cubeFile + "_d.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{cubeFile + "_f.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{cubeFile + "_l.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{cubeFile + "_r.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{cubeFile + "_u.tif"}));

            } else {
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_EC, generateArgsEC(imagedata.path, aggstngs.ge.getOutputDir(), false)));
            }
        }
    }

    //Equirectangular to DeepZoom cubic
    private void generateOpETDZC(TaskData taskData) {
        String nameWithoutExtension;
        String cubeFile;
        String outputDir;
        String resizedFile;
        int newSize;
        String resDir = aggstngs.ge.getTmpDir() + File.separator + "res";

        for (ImageData imagedata : taskData.getTaskSettings().getTaskImages().getImagesData()) {
            nameWithoutExtension = imagedata.path.substring(imagedata.path.lastIndexOf(File.separator) + 1, imagedata.path.lastIndexOf('.'));
            cubeFile = aggstngs.ge.getTmpDir() + File.separator + nameWithoutExtension;

            taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_EC, generateArgsEC(imagedata.path, aggstngs.ge.getTmpDir(), true)));

            outputDir = aggstngs.ge.getOutputDir() + File.separator + "dz_" + nameWithoutExtension;

            if (taskData.getTaskSettings().CubeNewSizeChanged()) {
                resizedFile = resDir + File.separator + nameWithoutExtension;
                newSize = Integer.parseInt(taskData.getTaskSettings().getCubeNewSize());

                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_RES, generateArgsRES(cubeFile + "_b.tif", resDir, newSize, true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_RES, generateArgsRES(cubeFile + "_d.tif", resDir, newSize, true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_RES, generateArgsRES(cubeFile + "_f.tif", resDir, newSize, true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_RES, generateArgsRES(cubeFile + "_l.tif", resDir, newSize, true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_RES, generateArgsRES(cubeFile + "_r.tif", resDir, newSize, true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_RES, generateArgsRES(cubeFile + "_u.tif", resDir, newSize, true)));

                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{cubeFile + "_b.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{cubeFile + "_d.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{cubeFile + "_f.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{cubeFile + "_l.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{cubeFile + "_r.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{cubeFile + "_u.tif"}));

                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(resizedFile + "_b.tif", outputDir, taskData.taskSettings.getTileNewSize(), true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(resizedFile + "_d.tif", outputDir, taskData.taskSettings.getTileNewSize(), true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(resizedFile + "_f.tif", outputDir, taskData.taskSettings.getTileNewSize(), true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(resizedFile + "_l.tif", outputDir, taskData.taskSettings.getTileNewSize(), true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(resizedFile + "_r.tif", outputDir, taskData.taskSettings.getTileNewSize(), true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(resizedFile + "_u.tif", outputDir, taskData.taskSettings.getTileNewSize(), true)));

                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{resizedFile + "_b.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{resizedFile + "_d.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{resizedFile + "_f.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{resizedFile + "_l.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{resizedFile + "_r.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{resizedFile + "_u.tif"}));

            } else {
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(cubeFile + "_b.tif", outputDir, taskData.taskSettings.getTileNewSize(), true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(cubeFile + "_d.tif", outputDir, taskData.taskSettings.getTileNewSize(), true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(cubeFile + "_f.tif", outputDir, taskData.taskSettings.getTileNewSize(), true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(cubeFile + "_l.tif", outputDir, taskData.taskSettings.getTileNewSize(), true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(cubeFile + "_r.tif", outputDir, taskData.taskSettings.getTileNewSize(), true)));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DZT, generateArgsDZT(cubeFile + "_u.tif", outputDir, taskData.taskSettings.getTileNewSize(), true)));

                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{cubeFile + "_b.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{cubeFile + "_d.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{cubeFile + "_f.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{cubeFile + "_l.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{cubeFile + "_r.tif"}));
                taskData.operations.add(new TaskOperation(TaskOperation.OPERATION_DEL, new String[]{cubeFile + "_u.tif"}));
            }
        }
    }

    private String[] generateArgsRES(String input, String output, int width, boolean simpleOutput) {
        ArrayList tmpArgsRES = new ArrayList();
        tmpArgsRES.add("-verbose");
        if (simpleOutput) {
            tmpArgsRES.add("-simpleoutput");
        }
        tmpArgsRES.add("-width");
        tmpArgsRES.add(Integer.toString(width));
        tmpArgsRES.add("-outputformat");
        tmpArgsRES.add("tif");
        tmpArgsRES.add("-outputdir");
        tmpArgsRES.add(output);
        tmpArgsRES.add(input);

        String[] argsRES = new String[tmpArgsRES.size()];
        tmpArgsRES.toArray(argsRES);
        return argsRES;
    }

    private String[] generateArgsDZT(String input, String output, String tileSize, boolean simpleOutput) {
        ArrayList tmpArgsDZT = new ArrayList();
        tmpArgsDZT.add("-verbose");
        if (simpleOutput) {
            tmpArgsDZT.add("-simpleoutput");
        }
        tmpArgsDZT.add("-overlap");
        tmpArgsDZT.add(aggstngs.dzt.getOverlap());
        tmpArgsDZT.add("-quality");
        tmpArgsDZT.add(aggstngs.dzt.getQuality());
        tmpArgsDZT.add("-tilesize");
        tmpArgsDZT.add(tileSize);
        tmpArgsDZT.add("-outputdir");
        tmpArgsDZT.add(output);
        tmpArgsDZT.add(input);
        String argsDZT[] = new String[tmpArgsDZT.size()];
        tmpArgsDZT.toArray(argsDZT);
        return argsDZT;
    }

    private String[] generateArgsEC(String input, String output, boolean simpleOutput) {
        ArrayList tmpArgsEC = new ArrayList();
        tmpArgsEC.add("-verbose");
        if (simpleOutput) {
            tmpArgsEC.add("-simpleoutput");
        }
        tmpArgsEC.add("-overlap");
        tmpArgsEC.add(aggstngs.ec.getOverlap());
        tmpArgsEC.add("-interpolation");
        tmpArgsEC.add(aggstngs.ec.getInterpolation());
        tmpArgsEC.add("-outputdir");
        tmpArgsEC.add(output);
        tmpArgsEC.add(input);
        String argsEC[] = new String[tmpArgsEC.size()];
        tmpArgsEC.toArray(argsEC);
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
            Properties prop = new Properties();
            prop.load(new FileInputStream(aggstngs.getCurrentDirectory() + File.separator + AggregatedSettings.FILE_PROPERTIES));
            try {
                aggstngs.res.setJarDir(prop.getProperty(RESSettings.VALUE_JAR_DIR), Info.CONFIGURATIN_READ_ERROR);

                aggstngs.dzt.setOverlap(prop.getProperty(DZTSettings.VALUE_OVERLAP), Info.CONFIGURATIN_READ_ERROR);
                aggstngs.dzt.setTileSize(prop.getProperty(DZTSettings.VALUE_TILE_SIZE), Info.CONFIGURATIN_READ_ERROR);
                aggstngs.dzt.setQuality(prop.getProperty(DZTSettings.VALUE_QUALITY), Info.CONFIGURATIN_READ_ERROR);
                aggstngs.dzt.setJarDir(prop.getProperty(DZTSettings.VALUE_JAR_DIR), Info.CONFIGURATIN_READ_ERROR);

                aggstngs.ec.setOverlap(prop.getProperty(ECSettings.VALUE_OVERLAP), Info.CONFIGURATIN_READ_ERROR);
                aggstngs.ec.setInterpolation(prop.getProperty(ECSettings.VALUE_INTERPOLATION), Info.CONFIGURATIN_READ_ERROR);
                aggstngs.ec.setJarDir(prop.getProperty(ECSettings.VALUE_JAR_DIR), Info.CONFIGURATIN_READ_ERROR);

                aggstngs.ge.setTmpDir(prop.getProperty(GESettings.VALUE_TMP_DIR), Info.CONFIGURATIN_READ_ERROR);
                aggstngs.ge.setMemoryLimit(prop.getProperty(GESettings.VALUE_MEM_LIMIT), Info.CONFIGURATIN_READ_ERROR);
                aggstngs.ge.setInputDir(prop.getProperty(GESettings.VALUE_INPUT_DIR), Info.CONFIGURATIN_READ_ERROR);
                aggstngs.ge.setOutputDir(prop.getProperty(GESettings.VALUE_OUTPUT_DIR), Info.CONFIGURATIN_READ_ERROR);
                aggstngs.ge.setSelectedCommand(prop.getProperty(GESettings.VALUE_SELECTED_COMMAND), Info.CONFIGURATIN_READ_ERROR);

            } catch (InfoException ex) {
                // TODO: indicate error
                System.out.println("corrupted settings file not all settings red.");
            }
        } catch (IOException ex) {
            //TODO: indicate error
            System.out.println("settings could not be red everything default.");
        }
    }

    public void saveSettingsToFile() {
        Properties prop = new Properties();
        prop.clear();

        if (aggstngs.res.jarDirChanged()) {
            prop.put(RESSettings.VALUE_JAR_DIR, aggstngs.res.getJarDir());
        }

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

        if (aggstngs.ec.jarDirChanged()) {
            prop.put(ECSettings.VALUE_JAR_DIR, aggstngs.ec.getJarDir());
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
            System.out.println("Settings could not be written.");
        }
    }
}
