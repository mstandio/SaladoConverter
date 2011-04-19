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
import com.panozona.converter.task.Operation;
import com.panozona.converter.task.Panorama;
import com.panozona.converter.task.Image;
import com.panozona.converter.utils.FileFilterAddTask;
import com.panozona.converter.utils.TasksExecutor;
import com.panozona.converter.maintable.TaskTableModel;
import com.panozona.converter.settings.RESSettings;
import com.panozona.converter.task.PanoramaTypes;
import com.panozona.converter.utils.ImageDimensionsChecker;

/** 
 * @author Marek Standio
 */
public class Controller {

    private TaskTableModel taskTableModel;
    private MainWindowView mainWindowView;
    private AggregatedSettings aggstngs;
    private TaskMonitor taskMonitor;
    private static Controller instance;

    private Controller() {
        aggstngs = AggregatedSettings.getInstance();
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

    public void setMainWindowView(MainWindowView mainWindowView) {
        this.mainWindowView = mainWindowView;
    }

    public void setTaskMonitor(TaskMonitor taskMonitor) {
        this.taskMonitor = taskMonitor;
    }

    //##########################################################################
    // managing tasks
    public void addTask(File[] selectedFiles) {

        HashMap<String, ArrayList<Image>> m = new HashMap<String, ArrayList<Image>>();
        for (File file : selectedFiles) {
            addTaskR(file, m, true);
        }

        // analyse hashmap and add to tasks every directory containing 6 square images
        Iterator it = m.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            if (((ArrayList<Image>) pairs.getValue()).size() == 6) {
                TaskData newTask = new TaskData(new Panorama((ArrayList<Image>) pairs.getValue()));
                newTask.state = TaskData.STATE_READY;
                appendTask(newTask);
            }
        }
    }

    private void addTaskR(File selectedFile, HashMap m, boolean searchSubDirectories) {

        // if it is single image
        if (selectedFile.isFile()) {
            Image image = ImageDimensionsChecker.analise(selectedFile);
            if (image == null) {
                return;
            }

            // equirectangular file
            if (image.width == image.height * 2) {
                TaskData newTask = new TaskData(new Panorama(image));
                newTask.state = TaskData.STATE_READY;
                appendTask(newTask);
                return;

                // possibly cube wall
            } else if (image.width == image.height) {
                ArrayList<Image> contents = (ArrayList<Image>) m.get(selectedFile.getParent());
                if (contents == null) {
                    contents = new ArrayList<Image>();
                    m.put(selectedFile.getParent(), contents);
                }
                if ((contents.isEmpty()) || (contents.size() > 0 && contents.get(0).width == image.width)) {
                    contents.add(image);
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
            if (taskTableModel.rows.get(i).getPathDescription().equals(newTask.getPathDescription())) {
                taskTableModel.rows.get(i).state = TaskData.STATE_READY;
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
        boolean cubic = aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_CUBIC_TO_DEEPZOOM_CUBIC) || aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_CUBIC_TO_RESIZED_CUBIC);

        for (int i = 0; i < taskTableModel.getRowCount(); i++) {
            if (taskTableModel.rows.get(i).getPanorama().getPanoramaType() == PanoramaTypes.equirectangular) {
                taskTableModel.rows.get(i).checkBoxEnabled = !cubic;
            } else {
                if (aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_CUBIC_TO_RESIZED_CUBIC)) {
                    if (taskTableModel.rows.get(i).cubeSizeChanged()) {
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
                taskData.state = TaskData.STATE_READY;
                if (selection.equals(GESettings.COMMAND_CUBIC_TO_RESIZED_CUBIC)) {
                    generateOpCRES(taskData);
                } else if (selection.equals(GESettings.COMMAND_CUBIC_TO_DEEPZOOM_CUBIC)) {
                    generateOpCTDZC(taskData);
                } else if (selection.equals(GESettings.COMMAND_EQUIRECTANGULAR_TO_CUBIC)) {
                    generateOpETC(taskData);
                } else if (selection.equals(GESettings.COMMAND_EQUIRECTANGULAR_TO_DEEPZOOM_CUBIC)) {
                    generateOpETDZC(taskData);
                }
            }
            taskTableModel.fireTableDataChanged();
        }
    }

    private String getOutputFolderName(String outputFolderName) {
        if (aggstngs.ge.getOverwriteOutput() || !new File(outputFolderName).exists()) {
            return outputFolderName;
        } else {
            String result = outputFolderName;
            if (outputFolderName.matches(".+\\((\\d+)\\)$")) {
                result = outputFolderName.substring(0, outputFolderName.lastIndexOf("("));
            }
            int counter = 2;
            while (new File(result + "(" + counter + ")").exists()) {
                counter++;
            }
            return result + "(" + counter + ")";
        }
    }

    // Cubic to resized Cubic
    private void generateOpCRES(TaskData taskData) {
        String parentFolderName;
        for (Image image : taskData.getPanorama().getImages()) {
            String[] tmp = image.path.split(Pattern.quote(File.separator));
            parentFolderName = tmp[tmp.length - 2];
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(image.path, getOutputFolderName(aggstngs.ge.getOutputDir() + File.separator + parentFolderName), taskData.getNewCubeSize(), true)));
        }
    }

    //Cubic to DeepZoom cubic
    private void generateOpCTDZC(TaskData taskData) {
        String parentFolderName;
        String nameWithExtension;
        String nameWithoutExtension;
        String[] tmp;
        String outputDir;
        String resDir = aggstngs.ge.getTmpDir() + File.separator + "res";
        for (Image image : taskData.getPanorama().getImages()) {
            tmp = image.path.split(Pattern.quote(File.separator));
            parentFolderName = tmp[tmp.length - 2];

            outputDir = getOutputFolderName(aggstngs.ge.getOutputDir() + File.separator + "dz_" + parentFolderName);
            nameWithExtension = image.path.substring(image.path.lastIndexOf(File.separator) + 1, image.path.length());
            nameWithoutExtension = image.path.substring(image.path.lastIndexOf(File.separator) + 1, image.path.lastIndexOf('.'));

            if (taskData.cubeSizeChanged()) {

                taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(image.path, resDir, taskData.getNewCubeSize(), true)));

                taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(resDir + File.separator + nameWithoutExtension + ".tif", outputDir, taskData.getNewTileSize(), true)));

                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resDir + File.separator + nameWithoutExtension + ".tif"}));

            } else {
                taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(image.path, outputDir, taskData.getNewTileSize(), true)));
            }

            if (aggstngs.ge.getRemoveObsolete()) {
                if (!nameWithoutExtension.endsWith("_f")) {
                    taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{outputDir + File.separator + nameWithoutExtension + ".xml"}));
                }
                removeObsoleteDeepZoomImages(taskData, outputDir + File.separator + nameWithoutExtension);
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
        // there is only one image
        for (Image image : taskData.getPanorama().getImages()) {
            nameWithoutExtension = image.path.substring(image.path.lastIndexOf(File.separator) + 1, image.path.lastIndexOf('.'));
            cubeFile = aggstngs.ge.getTmpDir() + File.separator + "res" + File.separator + nameWithoutExtension;
            outputDir = getOutputFolderName(aggstngs.ge.getOutputDir() + File.separator + nameWithoutExtension);
            newSize = taskData.getNewCubeSize();

            if (taskData.cubeSizeChanged()) {
                taskData.operations.add(new Operation(Operation.TYPE_EC, generateArgsEC(image.path, resDir, true)));

                taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + "_b.tif", outputDir, newSize, true)));
                taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + "_d.tif", outputDir, newSize, true)));
                taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + "_f.tif", outputDir, newSize, true)));
                taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + "_l.tif", outputDir, newSize, true)));
                taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + "_r.tif", outputDir, newSize, true)));
                taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + "_u.tif", outputDir, newSize, true)));

                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + "_b.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + "_d.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + "_f.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + "_l.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + "_r.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + "_u.tif"}));

            } else {
                taskData.operations.add(new Operation(Operation.TYPE_EC, generateArgsEC(image.path, outputDir, true)));
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

        for (Image image : taskData.getPanorama().getImages()) {
            nameWithoutExtension = image.path.substring(image.path.lastIndexOf(File.separator) + 1, image.path.lastIndexOf('.'));
            cubeFile = aggstngs.ge.getTmpDir() + File.separator + nameWithoutExtension;

            taskData.operations.add(new Operation(Operation.TYPE_EC, generateArgsEC(image.path, aggstngs.ge.getTmpDir(), true)));

            outputDir = getOutputFolderName(aggstngs.ge.getOutputDir() + File.separator + "dz_" + nameWithoutExtension);

            if (taskData.cubeSizeChanged()) {
                resizedFile = resDir + File.separator + nameWithoutExtension;
                newSize = taskData.getNewCubeSize();

                taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + "_b.tif", resDir, newSize, true)));
                taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + "_d.tif", resDir, newSize, true)));
                taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + "_f.tif", resDir, newSize, true)));
                taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + "_l.tif", resDir, newSize, true)));
                taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + "_r.tif", resDir, newSize, true)));
                taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + "_u.tif", resDir, newSize, true)));

                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + "_b.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + "_d.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + "_f.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + "_l.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + "_r.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + "_u.tif"}));

                taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(resizedFile + "_b.tif", outputDir, taskData.getNewTileSize(), true)));
                taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(resizedFile + "_d.tif", outputDir, taskData.getNewTileSize(), true)));
                taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(resizedFile + "_f.tif", outputDir, taskData.getNewTileSize(), true)));
                taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(resizedFile + "_l.tif", outputDir, taskData.getNewTileSize(), true)));
                taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(resizedFile + "_r.tif", outputDir, taskData.getNewTileSize(), true)));
                taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(resizedFile + "_u.tif", outputDir, taskData.getNewTileSize(), true)));

                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + "_b.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + "_d.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + "_f.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + "_l.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + "_r.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + "_u.tif"}));

            } else {
                taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(cubeFile + "_b.tif", outputDir, taskData.getNewTileSize(), true)));
                taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(cubeFile + "_d.tif", outputDir, taskData.getNewTileSize(), true)));
                taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(cubeFile + "_f.tif", outputDir, taskData.getNewTileSize(), true)));
                taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(cubeFile + "_l.tif", outputDir, taskData.getNewTileSize(), true)));
                taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(cubeFile + "_r.tif", outputDir, taskData.getNewTileSize(), true)));
                taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(cubeFile + "_u.tif", outputDir, taskData.getNewTileSize(), true)));

                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + "_b.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + "_d.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + "_f.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + "_l.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + "_r.tif"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + "_u.tif"}));
            }

            if (aggstngs.ge.getRemoveObsolete()) {
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{outputDir + File.separator + nameWithoutExtension + "_b.xml"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{outputDir + File.separator + nameWithoutExtension + "_d.xml"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{outputDir + File.separator + nameWithoutExtension + "_l.xml"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{outputDir + File.separator + nameWithoutExtension + "_r.xml"}));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{outputDir + File.separator + nameWithoutExtension + "_u.xml"}));

                removeObsoleteDeepZoomImages(taskData, outputDir + File.separator + nameWithoutExtension + "_b");
                removeObsoleteDeepZoomImages(taskData, outputDir + File.separator + nameWithoutExtension + "_d");
                removeObsoleteDeepZoomImages(taskData, outputDir + File.separator + nameWithoutExtension + "_f");
                removeObsoleteDeepZoomImages(taskData, outputDir + File.separator + nameWithoutExtension + "_l");
                removeObsoleteDeepZoomImages(taskData, outputDir + File.separator + nameWithoutExtension + "_r");
                removeObsoleteDeepZoomImages(taskData, outputDir + File.separator + nameWithoutExtension + "_u");
            }
        }
    }

    private void removeObsoleteDeepZoomImages(TaskData taskData, String path) {
        double cubeSize = taskData.getNewCubeSize();
        double tileSize = taskData.getNewTileSize();
        int numLevels = (int) Math.ceil(Math.log(cubeSize) / Math.log(2));
        double levelSize = cubeSize;
        for (int i = numLevels; i > 0; i--) {
            if (levelSize < tileSize) {
                for (int j = i - 1; j >= 0; j--) {
                    taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{path + File.separator + j}));
                }
                return;
            }
            levelSize = Math.ceil(levelSize / 2);
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

    private String[] generateArgsDZT(String input, String output, int tileSize, boolean simpleOutput) {
        ArrayList tmpArgsDZT = new ArrayList();
        tmpArgsDZT.add("-verbose");
        if (simpleOutput) {
            tmpArgsDZT.add("-simpleoutput");
        }
        tmpArgsDZT.add("-overlap");
        tmpArgsDZT.add(aggstngs.dzt.getTileOverlap());
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
        tmpArgsEC.add(aggstngs.ec.getWallOverlap());
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
            prop.load(new FileInputStream(aggstngs.currentDirectory + File.separator + AggregatedSettings.FILE_PROPERTIES));
            try {
                aggstngs.res.setJarDir(prop.getProperty(RESSettings.VALUE_JAR_DIR));

                aggstngs.dzt.setTileOverlap(prop.getProperty(DZTSettings.VALUE_TILE_OVERLAP));
                aggstngs.dzt.setTileSize(prop.getProperty(DZTSettings.VALUE_TILE_SIZE));
                aggstngs.dzt.setQuality(prop.getProperty(DZTSettings.VALUE_QUALITY));
                aggstngs.dzt.setJarDir(prop.getProperty(DZTSettings.VALUE_JAR_DIR));

                aggstngs.ec.setWallOverlap(prop.getProperty(ECSettings.VALUE_WALL_OVERLAP));
                aggstngs.ec.setInterpolation(prop.getProperty(ECSettings.VALUE_INTERPOLATION));
                aggstngs.ec.setJarDir(prop.getProperty(ECSettings.VALUE_JAR_DIR));

                aggstngs.ge.setTmpDir(prop.getProperty(GESettings.VALUE_TMP_DIR));
                aggstngs.ge.setMemoryLimit(prop.getProperty(GESettings.VALUE_MEMORY_LIMIT));
                aggstngs.ge.setRemoveObsolete(prop.getProperty(GESettings.VALUE_REMOVE_OBSOLETE));
                aggstngs.ge.setOverwriteOutput(prop.getProperty(GESettings.VALUE_OVERWRITE_OUTPUT));
                aggstngs.ge.setInputDir(prop.getProperty(GESettings.VALUE_INPUT_DIR));
                aggstngs.ge.setOutputDir(prop.getProperty(GESettings.VALUE_OUTPUT_DIR));
                aggstngs.ge.setSelectedCommand(prop.getProperty(GESettings.VALUE_SELECTED_COMMAND));

            } catch (IllegalArgumentException ex) {
                // TODO: indicate error
                System.out.println("corrupted settings file not all settings red.");
            }
        } catch (IOException ex) {
            //TODO: indicate error
            System.out.println("Settings could not be red everything default.");
        }
    }

    public void saveSettingsToFile() {
        Properties prop = new Properties();
        prop.clear();

        if (aggstngs.res.jarDirChanged()) {
            prop.put(RESSettings.VALUE_JAR_DIR, aggstngs.res.getJarDir());
        }

        if (aggstngs.dzt.tileOverlapChanged()) {
            prop.put(DZTSettings.VALUE_TILE_OVERLAP, Integer.toString(aggstngs.dzt.getTileOverlap()));
        }
        if (aggstngs.dzt.tileSizeChanged()) {
            prop.put(DZTSettings.VALUE_TILE_SIZE, Integer.toString(aggstngs.dzt.getTileSize()));
        }
        if (aggstngs.dzt.qualityChanged()) {
            prop.put(DZTSettings.VALUE_QUALITY, Float.toString(aggstngs.dzt.getQuality()));
        }
        if (aggstngs.dzt.jarDirChanged()) {
            prop.put(DZTSettings.VALUE_JAR_DIR, aggstngs.dzt.getJarDir());
        }

        if (aggstngs.ec.wallOverlapChanged()) {
            prop.put(ECSettings.VALUE_WALL_OVERLAP, Integer.toString(aggstngs.ec.getWallOverlap()));
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
            prop.put(GESettings.VALUE_MEMORY_LIMIT, Integer.toString(aggstngs.ge.getMemoryLimit()));
        }
        if (aggstngs.ge.removeObsoleteChanged()) {
            prop.put(GESettings.VALUE_REMOVE_OBSOLETE, aggstngs.ge.getRemoveObsolete() ? "true" : "false");
        }
        if (aggstngs.ge.overwriteOutputChanged()) {
            prop.put(GESettings.VALUE_OVERWRITE_OUTPUT, aggstngs.ge.getOverwriteOutput() ? "true" : "false");
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
            prop.store(new FileOutputStream(aggstngs.currentDirectory + File.separator + AggregatedSettings.FILE_PROPERTIES), null);
        } catch (IOException ex) {
            //TODO: indicate error
            System.out.println("Settings could not be written.");
        }
    }
}
