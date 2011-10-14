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
import com.panozona.converter.settings.ERFSettings;
import com.panozona.converter.settings.OPTSettings;
import com.panozona.converter.settings.RESSettings;
import com.panozona.converter.settings.SBMSettings;
import com.panozona.converter.settings.ZYTSettings;
import com.panozona.converter.task.TaskDataCubic;
import com.panozona.converter.task.TaskDataEquirectangular;
import com.panozona.converter.task.TaskDataStates;
import com.panozona.converter.utils.ImageDimensionsChecker;
import com.panozona.converter.utils.Naming;
import java.util.Arrays;

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
        aggstngs.ge.naming.setType(Naming.NUMBERS);
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

        // analyse hashmap and add to tasks every 6 squareimages
        // with same size and matching given regex
        Iterator it = m.entrySet().iterator();
        ArrayList<Image> collectedImages;
        ArrayList<Image> cubeWalls;
        int firstWallsize;
        String wallsRegex;
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            collectedImages = (ArrayList<Image>) pairs.getValue();
            cubeWalls = new ArrayList<Image>();

            firstWallsize = 0;
            wallsRegex = null;

            for (Image image : collectedImages) {
                if (image.path.toLowerCase().matches(".+_" + aggstngs.ge.naming.getSelection() + "\\.(tif{1,2}|jpg|jpeg|gif|bmp|png)$")) {
                    firstWallsize = image.width;
                    wallsRegex = image.path.substring(image.path.lastIndexOf(File.separator) + 1, image.path.lastIndexOf("_") + 1)
                            + aggstngs.ge.naming.getSelection()
                            + image.path.substring(image.path.lastIndexOf("."), image.path.length());
                    break;
                }
            }

            if (wallsRegex != null) {
                for (Image image : collectedImages) {
                    if (image.width == firstWallsize
                            && image.path.substring(image.path.lastIndexOf(File.separator) + 1).matches(wallsRegex)) {
                        cubeWalls.add(image);
                        System.out.println("Cube wall: " + image.path);
                    }
                }
                if (cubeWalls.size() == 6) {
                    TaskData newTask = new TaskDataCubic(new Panorama(collectedImages));
                    newTask.state = TaskDataStates.READY;
                    appendTask(newTask);
                } else {
                    //StringBuilder sb = new StringBuilder("Not enough walls (" + cubeWalls.size() + ")");
                    //for (Image cubeWall : cubeWalls) {
                    //    sb.append("\n");
                    //    sb.append(cubeWall.path.substring(cubeWall.path.lastIndexOf(File.separator) + 1));
                    //}
                    //mainWindowView.showOptionPane(sb.toString());
                    for (Image image : collectedImages) {
                        TaskData newTask = new TaskDataEquirectangular(new Panorama(image));
                        newTask.state = TaskDataStates.READY;
                        appendTask(newTask);
                    }
                }
            } else {
                for (Image image : collectedImages) {
                    TaskData newTask = new TaskDataEquirectangular(new Panorama(image));
                    newTask.state = TaskDataStates.READY;
                    appendTask(newTask);
                }
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
            if (image.width != image.height) {
                TaskData newTask = new TaskDataEquirectangular(new Panorama(image));
                newTask.state = TaskDataStates.READY;
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
                taskTableModel.rows.get(i).state = TaskDataStates.READY;
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

        boolean cubic = aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_CUBIC_TO_DEEPZOOM_CUBIC)
                || aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_CUBIC_TO_ZOOMIFY_CUBIC)
                || aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_CUBIC_TO_RESIZED_CUBIC)
                || aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_CUBIC_TO_SKYBOX)
                || aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_CUBIC_TO_SKYBOX_PREVIEW);

        boolean surpressOptimalisationEqui = aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_EQUIRECTANGULAR_TO_CUBIC)
                || aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_EQUIRECTANGULAR_TO_SKYBOX)
                || aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_EQUIRECTANGULAR_TO_SKYBOX_PREVIEW)
                || aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_FLAT_TO_ZOOMIFY);
        boolean surpressOptimalisationCubic = aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_CUBIC_TO_SKYBOX)
                || aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_CUBIC_TO_SKYBOX_PREVIEW)
                || aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_CUBIC_TO_RESIZED_CUBIC);

        boolean ignoreTileSizeEqui = aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_EQUIRECTANGULAR_TO_CUBIC)
                || aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_EQUIRECTANGULAR_TO_SKYBOX)
                || aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_EQUIRECTANGULAR_TO_SKYBOX_PREVIEW);

        boolean ignoreTileSizeCubic = aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_CUBIC_TO_SKYBOX)
                || aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_CUBIC_TO_SKYBOX_PREVIEW)
                || aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_CUBIC_TO_RESIZED_CUBIC);

        boolean ignoreCubeSizeEqui = aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_FLAT_TO_ZOOMIFY)
                || aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_EQUIRECTANGULAR_TO_SKYBOX_PREVIEW);

        boolean ignoreCubeSizeCubic = aggstngs.ge.getSelectedCommand().equals(GESettings.COMMAND_CUBIC_TO_SKYBOX_PREVIEW);
        
        for (int i = 0; i < taskTableModel.getRowCount(); i++) {
            if (taskTableModel.rows.get(i) instanceof TaskDataEquirectangular) {
                taskTableModel.rows.get(i).checkBoxEnabled = !cubic;

                taskTableModel.rows.get(i).surpressOptimalisation = surpressOptimalisationEqui;
                taskTableModel.rows.get(i).showTizeSize = !ignoreTileSizeEqui;
                taskTableModel.rows.get(i).showCubeSize = !ignoreCubeSizeEqui;

            } else {

                taskTableModel.rows.get(i).surpressOptimalisation = surpressOptimalisationCubic;
                taskTableModel.rows.get(i).showTizeSize = !ignoreTileSizeCubic;
                taskTableModel.rows.get(i).showCubeSize = !ignoreCubeSizeCubic;

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
            taskTableModel.rows.get(i).optimalize();
        }

        taskTableModel.fireTableDataChanged();
        mainWindowView.analyseTasks();
        mainWindowView.rfreshTaskSettings();
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
                taskData.state = TaskDataStates.READY;
                if (selection.equals(GESettings.COMMAND_CUBIC_TO_RESIZED_CUBIC)) {
                    generateOpCRES(taskData);
                } else if (selection.equals(GESettings.COMMAND_CUBIC_TO_DEEPZOOM_CUBIC)) {
                    generateOpCTDZC(taskData);
                } else if (selection.equals(GESettings.COMMAND_EQUIRECTANGULAR_TO_CUBIC)) {
                    generateOpETC(taskData);
                } else if (selection.equals(GESettings.COMMAND_EQUIRECTANGULAR_TO_DEEPZOOM_CUBIC)) {
                    generateOpETDZC(taskData);
                } else if (selection.equals(GESettings.COMMAND_CUBIC_TO_ZOOMIFY_CUBIC)) {
                    generateOpCTZYC(taskData);
                } else if (selection.equals(GESettings.COMMAND_EQUIRECTANGULAR_TO_ZOOMIFY_CUBIC)) {
                    generateOpETZYC(taskData);
                } else if (selection.equals(GESettings.COMMAND_EQUIRECTANGULAR_TO_SKYBOX)) {
                    generateOpETSB(taskData, false);
                } else if (selection.equals(GESettings.COMMAND_EQUIRECTANGULAR_TO_SKYBOX_PREVIEW)) {
                    generateOpETSB(taskData, true);
                } else if (selection.equals(GESettings.COMMAND_CUBIC_TO_SKYBOX)) {
                    generateOpCTSB(taskData, false);
                } else if (selection.equals(GESettings.COMMAND_CUBIC_TO_SKYBOX_PREVIEW)) {
                    generateOpCTSB(taskData, true);
                } else if (selection.equals(GESettings.COMMAND_FLAT_TO_ZOOMIFY)) {
                    generateOpFTZ(taskData);
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
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(image.path, getOutputFolderName(aggstngs.ge.getOutputDir() + File.separator + parentFolderName), taskData.getNewCubeSize())));
        }
    }

    //Cubic to DeepZoom cubic
    private void generateOpCTDZC(TaskData taskData) {
        String parentFolderName;
        String nameWithoutExtension;
        String[] tmp;
        String outputDir;
        String resDir = aggstngs.ge.getTmpDir() + File.separator + "res";
        for (Image image : taskData.getPanorama().getImages()) {
            tmp = image.path.split(Pattern.quote(File.separator));
            parentFolderName = tmp[tmp.length - 2];

            outputDir = getOutputFolderName(aggstngs.ge.getOutputDir() + File.separator + "dz_" + parentFolderName);
            nameWithoutExtension = image.path.substring(image.path.lastIndexOf(File.separator) + 1, image.path.lastIndexOf('.'));

            if (taskData.cubeSizeChanged()) {

                taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(image.path, resDir, taskData.getNewCubeSize())));

                taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(resDir + File.separator + nameWithoutExtension + ".tif", outputDir, taskData.getNewTileSize())));

                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resDir + File.separator + nameWithoutExtension + ".tif"}));

            } else {
                taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(image.path, outputDir, taskData.getNewTileSize())));
            }

            if (aggstngs.ge.getRemoveObsolete()) {
                if (!nameWithoutExtension.endsWith(aggstngs.ge.naming.getFront())) {
                    taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{outputDir + File.separator + nameWithoutExtension + ".xml"}));
                }
                removeObsoleteDeepZoomImages(taskData, outputDir + File.separator + nameWithoutExtension);
            }
        }
    }

    //Equirectangular to cubic
    private void generateOpETC(TaskData taskData) {
        Image image = taskData.getPanorama().getImages().get(0);
        TaskDataEquirectangular taskDataEquirectangular = (TaskDataEquirectangular) taskData;

        String nameWithoutExtension = image.path.substring(image.path.lastIndexOf(File.separator) + 1, image.path.lastIndexOf('.'));
        String tmpFilePath = aggstngs.ge.getTmpDir() + File.separator + nameWithoutExtension + ".tif";
        String outputDir = getOutputFolderName(aggstngs.ge.getOutputDir() + File.separator + nameWithoutExtension);
        String cubeFileRes = aggstngs.ge.getTmpDir() + File.separator + "res" + File.separator + nameWithoutExtension;

        if (taskData.cubeSizeChanged()) {

            String resDir = aggstngs.ge.getTmpDir() + File.separator + "res";

            if (taskDataEquirectangular.requiresFilling()) {
                taskData.operations.add(new Operation(Operation.TYPE_ERF, generateArgsERF(image.path, aggstngs.ge.getTmpDir(), taskDataEquirectangular.getFov(), taskDataEquirectangular.getOffset())));
                taskData.operations.add(new Operation(Operation.TYPE_EC, generateArgsEC(tmpFilePath, resDir)));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{tmpFilePath}));
            } else {
                taskData.operations.add(new Operation(Operation.TYPE_EC, generateArgsEC(image.path, resDir)));
            }

            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFileRes + aggstngs.ge.naming.getFront() + ".tif", outputDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFileRes + aggstngs.ge.naming.getRight() + ".tif", outputDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFileRes + aggstngs.ge.naming.getBack() + ".tif", outputDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFileRes + aggstngs.ge.naming.getLeft() + ".tif", outputDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFileRes + aggstngs.ge.naming.getUp() + ".tif", outputDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFileRes + aggstngs.ge.naming.getDown() + ".tif", outputDir, taskData.getNewCubeSize())));

            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFileRes + aggstngs.ge.naming.getFront() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFileRes + aggstngs.ge.naming.getRight() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFileRes + aggstngs.ge.naming.getBack() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFileRes + aggstngs.ge.naming.getLeft() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFileRes + aggstngs.ge.naming.getUp() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFileRes + aggstngs.ge.naming.getDown() + ".tif"}));

        } else {

            if (taskDataEquirectangular.requiresFilling()) {
                taskData.operations.add(new Operation(Operation.TYPE_ERF, generateArgsERF(image.path, aggstngs.ge.getTmpDir(), taskDataEquirectangular.getFov(), taskDataEquirectangular.getOffset())));
                taskData.operations.add(new Operation(Operation.TYPE_EC, generateArgsEC(tmpFilePath, outputDir)));
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{tmpFilePath}));
            } else {
                taskData.operations.add(new Operation(Operation.TYPE_EC, generateArgsEC(image.path, outputDir)));
            }
        }
    }

    //Equirectangular to DeepZoom cubic
    private void generateOpETDZC(TaskData taskData) {

        Image image = taskData.getPanorama().getImages().get(0);
        TaskDataEquirectangular taskDataEquirectangular = (TaskDataEquirectangular) taskData;

        String nameWithoutExtension = image.path.substring(image.path.lastIndexOf(File.separator) + 1, image.path.lastIndexOf('.'));
        String cubeFile = aggstngs.ge.getTmpDir() + File.separator + nameWithoutExtension;
        String outputDir = getOutputFolderName(aggstngs.ge.getOutputDir() + File.separator + "dz_" + nameWithoutExtension);

        if (taskDataEquirectangular.requiresFilling()) {
            String tmpFilePath = aggstngs.ge.getTmpDir() + File.separator + nameWithoutExtension + ".tif";
            taskData.operations.add(new Operation(Operation.TYPE_ERF, generateArgsERF(image.path, aggstngs.ge.getTmpDir(), taskDataEquirectangular.getFov(), taskDataEquirectangular.getOffset())));
            taskData.operations.add(new Operation(Operation.TYPE_EC, generateArgsEC(tmpFilePath, aggstngs.ge.getTmpDir())));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{tmpFilePath}));
        } else {
            taskData.operations.add(new Operation(Operation.TYPE_EC, generateArgsEC(image.path, aggstngs.ge.getTmpDir())));
        }

        if (taskData.cubeSizeChanged()) {
            String resDir = aggstngs.ge.getTmpDir() + File.separator + "res";
            String resizedFile = resDir + File.separator + nameWithoutExtension;

            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + aggstngs.ge.naming.getFront() + ".tif", resDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + aggstngs.ge.naming.getRight() + ".tif", resDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + aggstngs.ge.naming.getBack() + ".tif", resDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + aggstngs.ge.naming.getLeft() + ".tif", resDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + aggstngs.ge.naming.getUp() + ".tif", resDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + aggstngs.ge.naming.getDown() + ".tif", resDir, taskData.getNewCubeSize())));

            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getFront() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getRight() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getBack() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getLeft() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getUp() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getDown() + ".tif"}));

            taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(resizedFile + aggstngs.ge.naming.getFront() + ".tif", outputDir, taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(resizedFile + aggstngs.ge.naming.getRight() + ".tif", outputDir, taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(resizedFile + aggstngs.ge.naming.getBack() + ".tif", outputDir, taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(resizedFile + aggstngs.ge.naming.getLeft() + ".tif", outputDir, taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(resizedFile + aggstngs.ge.naming.getUp() + ".tif", outputDir, taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(resizedFile + aggstngs.ge.naming.getDown() + ".tif", outputDir, taskData.getNewTileSize())));

            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + aggstngs.ge.naming.getFront() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + aggstngs.ge.naming.getRight() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + aggstngs.ge.naming.getBack() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + aggstngs.ge.naming.getLeft() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + aggstngs.ge.naming.getUp() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + aggstngs.ge.naming.getDown() + ".tif"}));

        } else {
            taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(cubeFile + aggstngs.ge.naming.getFront() + ".tif", outputDir, taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(cubeFile + aggstngs.ge.naming.getRight() + ".tif", outputDir, taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(cubeFile + aggstngs.ge.naming.getBack() + ".tif", outputDir, taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(cubeFile + aggstngs.ge.naming.getLeft() + ".tif", outputDir, taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(cubeFile + aggstngs.ge.naming.getUp() + ".tif", outputDir, taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_DZT, generateArgsDZT(cubeFile + aggstngs.ge.naming.getDown() + ".tif", outputDir, taskData.getNewTileSize())));

            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getFront() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getRight() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getBack() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getLeft() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getUp() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getDown() + ".tif"}));
        }

        if (aggstngs.ge.getRemoveObsolete()) {
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getRight() + ".xml"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getBack() + ".xml"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getLeft() + ".xml"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getUp() + ".xml"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getDown() + ".xml"}));

            removeObsoleteDeepZoomImages(taskData, outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getFront());
            removeObsoleteDeepZoomImages(taskData, outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getRight());
            removeObsoleteDeepZoomImages(taskData, outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getBack());
            removeObsoleteDeepZoomImages(taskData, outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getLeft());
            removeObsoleteDeepZoomImages(taskData, outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getUp());
            removeObsoleteDeepZoomImages(taskData, outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getDown());
        }
    }

    //Equirectangular to Zoomify cubic
    private void generateOpETZYC(TaskData taskData) {

        Image image = taskData.getPanorama().getImages().get(0);
        TaskDataEquirectangular taskDataEquirectangular = (TaskDataEquirectangular) taskData;

        String nameWithoutExtension = image.path.substring(image.path.lastIndexOf(File.separator) + 1, image.path.lastIndexOf('.'));
        String cubeFile = aggstngs.ge.getTmpDir() + File.separator + nameWithoutExtension;
        String outputDir = getOutputFolderName(aggstngs.ge.getOutputDir() + File.separator + "zoomify_" + nameWithoutExtension);

        if (taskDataEquirectangular.requiresFilling()) {
            String tmpFilePath = aggstngs.ge.getTmpDir() + File.separator + nameWithoutExtension + ".tif";
            taskData.operations.add(new Operation(Operation.TYPE_ERF, generateArgsERF(image.path, aggstngs.ge.getTmpDir(), taskDataEquirectangular.getFov(), taskDataEquirectangular.getOffset())));
            taskData.operations.add(new Operation(Operation.TYPE_EC, generateArgsEC(tmpFilePath, aggstngs.ge.getTmpDir())));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{tmpFilePath}));
        } else {
            taskData.operations.add(new Operation(Operation.TYPE_EC, generateArgsEC(image.path, aggstngs.ge.getTmpDir())));
        }

        new File(outputDir).mkdir();

        if (taskData.cubeSizeChanged()) {
            String resDir = aggstngs.ge.getTmpDir() + File.separator + "res";
            String resizedFile = resDir + File.separator + nameWithoutExtension;

            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + aggstngs.ge.naming.getFront() + ".tif", resDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + aggstngs.ge.naming.getRight() + ".tif", resDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + aggstngs.ge.naming.getBack() + ".tif", resDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + aggstngs.ge.naming.getLeft() + ".tif", resDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + aggstngs.ge.naming.getUp() + ".tif", resDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + aggstngs.ge.naming.getDown() + ".tif", resDir, taskData.getNewCubeSize())));

            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getFront() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getRight() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getBack() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getLeft() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getUp() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getDown() + ".tif"}));

            taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(resizedFile + aggstngs.ge.naming.getFront() + ".tif", outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getFront(), taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(resizedFile + aggstngs.ge.naming.getRight() + ".tif", outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getRight(), taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(resizedFile + aggstngs.ge.naming.getBack() + ".tif", outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getBack(), taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(resizedFile + aggstngs.ge.naming.getLeft() + ".tif", outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getLeft(), taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(resizedFile + aggstngs.ge.naming.getUp() + ".tif", outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getUp(), taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(resizedFile + aggstngs.ge.naming.getDown() + ".tif", outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getDown(), taskData.getNewTileSize())));

            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + aggstngs.ge.naming.getFront() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + aggstngs.ge.naming.getRight() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + aggstngs.ge.naming.getBack() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + aggstngs.ge.naming.getLeft() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + aggstngs.ge.naming.getUp() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + aggstngs.ge.naming.getDown() + ".tif"}));

        } else {

            taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(cubeFile + aggstngs.ge.naming.getFront() + ".tif", outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getFront(), taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(cubeFile + aggstngs.ge.naming.getRight() + ".tif", outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getRight(), taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(cubeFile + aggstngs.ge.naming.getBack() + ".tif", outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getBack(), taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(cubeFile + aggstngs.ge.naming.getLeft() + ".tif", outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getLeft(), taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(cubeFile + aggstngs.ge.naming.getUp() + ".tif", outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getUp(), taskData.getNewTileSize())));
            taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(cubeFile + aggstngs.ge.naming.getDown() + ".tif", outputDir + File.separator + nameWithoutExtension + aggstngs.ge.naming.getDown(), taskData.getNewTileSize())));

            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getFront() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getRight() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getBack() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getLeft() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getUp() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getDown() + ".tif"}));
        }
    }

    //Equirectangular to Skybox
    private void generateOpETSB(TaskData taskData, boolean previewOnly) {

        Image image = taskData.getPanorama().getImages().get(0);
        TaskDataEquirectangular taskDataEquirectangular = (TaskDataEquirectangular) taskData;

        String nameWithoutExtension = image.path.substring(image.path.lastIndexOf(File.separator) + 1, image.path.lastIndexOf('.'));
        String cubeFile = aggstngs.ge.getTmpDir() + File.separator + nameWithoutExtension;
        String outputDir = getOutputFolderName(aggstngs.ge.getOutputDir() + File.separator + "skybox_" + nameWithoutExtension);

        if (taskDataEquirectangular.requiresFilling()) {
            String tmpFilePath = aggstngs.ge.getTmpDir() + File.separator + nameWithoutExtension + ".tif";
            taskData.operations.add(new Operation(Operation.TYPE_ERF, generateArgsERF(image.path, aggstngs.ge.getTmpDir(), taskDataEquirectangular.getFov(), taskDataEquirectangular.getOffset())));
            taskData.operations.add(new Operation(Operation.TYPE_EC, generateArgsEC(tmpFilePath, aggstngs.ge.getTmpDir())));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{tmpFilePath}));
        } else {
            taskData.operations.add(new Operation(Operation.TYPE_EC, generateArgsEC(image.path, aggstngs.ge.getTmpDir())));
        }

        new File(outputDir).mkdir();
        
        if (taskData.getNewCubeSize()*3 > 8000){
            taskData.setNewCubeSize(2666);
        }

        if (taskData.cubeSizeChanged() && !previewOnly) {
            String resDir = aggstngs.ge.getTmpDir() + File.separator + "res";
            String resizedFile = resDir + File.separator + nameWithoutExtension;

            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + aggstngs.ge.naming.getFront() + ".tif", resDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + aggstngs.ge.naming.getRight() + ".tif", resDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + aggstngs.ge.naming.getBack() + ".tif", resDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + aggstngs.ge.naming.getLeft() + ".tif", resDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + aggstngs.ge.naming.getUp() + ".tif", resDir, taskData.getNewCubeSize())));
            taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(cubeFile + aggstngs.ge.naming.getDown() + ".tif", resDir, taskData.getNewCubeSize())));

            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getFront() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getRight() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getBack() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getLeft() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getUp() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getDown() + ".tif"}));

            taskData.operations.add(new Operation(Operation.TYPE_SB, generateArgsSBM(new String[]{resDir}, outputDir, false)));

            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + aggstngs.ge.naming.getFront() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + aggstngs.ge.naming.getRight() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + aggstngs.ge.naming.getBack() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + aggstngs.ge.naming.getLeft() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + aggstngs.ge.naming.getUp() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resizedFile + aggstngs.ge.naming.getDown() + ".tif"}));

        } else {

            String[] input = new String[]{cubeFile + aggstngs.ge.naming.getFront() + ".tif",
                cubeFile + aggstngs.ge.naming.getRight() + ".tif",
                cubeFile + aggstngs.ge.naming.getBack() + ".tif",
                cubeFile + aggstngs.ge.naming.getLeft() + ".tif",
                cubeFile + aggstngs.ge.naming.getUp() + ".tif",
                cubeFile + aggstngs.ge.naming.getDown() + ".tif"};

            taskData.operations.add(new Operation(Operation.TYPE_SB, generateArgsSBM(input, outputDir, true)));

            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getFront() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getRight() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getBack() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getLeft() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getUp() + ".tif"}));
            taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{cubeFile + aggstngs.ge.naming.getDown() + ".tif"}));
        }
    }

    //Cubic to Zoomify cubic
    private void generateOpCTZYC(TaskData taskData) {
        String parentFolderName;
        String nameWithoutExtension;
        String nameWithoutDescription;
        String[] tmp;
        String outputDir;
        String resDir = aggstngs.ge.getTmpDir() + File.separator + "res";

        tmp = taskData.getPanorama().getImages().get(0).path.split(Pattern.quote(File.separator));
        parentFolderName = tmp[tmp.length - 2];
        outputDir = getOutputFolderName(aggstngs.ge.getOutputDir() + File.separator + "zoomify_" + parentFolderName);
        new File(outputDir).mkdir();

        for (Image image : taskData.getPanorama().getImages()) {
            nameWithoutExtension = image.path.substring(image.path.lastIndexOf(File.separator) + 1, image.path.lastIndexOf('.'));
            nameWithoutDescription = nameWithoutExtension.substring(0, nameWithoutExtension.lastIndexOf("_"));

            if (Naming.recognizeFront(nameWithoutExtension)) {
                if (taskData.cubeSizeChanged()) {
                    taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(image.path, resDir, taskData.getNewCubeSize())));
                    taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(resDir + File.separator + nameWithoutExtension + ".tif", outputDir + File.separator + nameWithoutDescription + aggstngs.ge.naming.getFront(), taskData.getNewTileSize())));
                    taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resDir + File.separator + nameWithoutExtension + ".tif"}));
                } else {
                    taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(image.path, outputDir + File.separator + nameWithoutDescription + aggstngs.ge.naming.getFront(), taskData.getNewTileSize())));
                }
            } else if (Naming.recognizeRight(nameWithoutExtension)) {
                if (taskData.cubeSizeChanged()) {
                    taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(image.path, resDir, taskData.getNewCubeSize())));
                    taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(resDir + File.separator + nameWithoutExtension + ".tif", outputDir + File.separator + nameWithoutDescription + aggstngs.ge.naming.getRight(), taskData.getNewTileSize())));
                    taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resDir + File.separator + nameWithoutExtension + ".tif"}));
                } else {
                    taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(image.path, outputDir + File.separator + nameWithoutDescription + aggstngs.ge.naming.getRight(), taskData.getNewTileSize())));
                }
            } else if (Naming.recognizeBack(nameWithoutExtension)) {
                if (taskData.cubeSizeChanged()) {
                    taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(image.path, resDir, taskData.getNewCubeSize())));
                    taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(resDir + File.separator + nameWithoutExtension + ".tif", outputDir + File.separator + nameWithoutDescription + aggstngs.ge.naming.getBack(), taskData.getNewTileSize())));
                    taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resDir + File.separator + nameWithoutExtension + ".tif"}));
                } else {
                    taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(image.path, outputDir + File.separator + nameWithoutDescription + aggstngs.ge.naming.getBack(), taskData.getNewTileSize())));
                }
            } else if (Naming.recognizeLeft(nameWithoutExtension)) {
                if (taskData.cubeSizeChanged()) {
                    taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(image.path, resDir, taskData.getNewCubeSize())));
                    taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(resDir + File.separator + nameWithoutExtension + ".tif", outputDir + File.separator + nameWithoutDescription + aggstngs.ge.naming.getLeft(), taskData.getNewTileSize())));
                    taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resDir + File.separator + nameWithoutExtension + ".tif"}));
                } else {
                    taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(image.path, outputDir + File.separator + nameWithoutDescription + aggstngs.ge.naming.getLeft(), taskData.getNewTileSize())));
                }
            } else if (Naming.recognizeUp(nameWithoutExtension)) {
                if (taskData.cubeSizeChanged()) {
                    taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(image.path, resDir, taskData.getNewCubeSize())));
                    taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(resDir + File.separator + nameWithoutExtension + ".tif", outputDir + File.separator + nameWithoutDescription + aggstngs.ge.naming.getUp(), taskData.getNewTileSize())));
                    taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resDir + File.separator + nameWithoutExtension + ".tif"}));
                } else {
                    taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(image.path, outputDir + File.separator + nameWithoutDescription + aggstngs.ge.naming.getUp(), taskData.getNewTileSize())));
                }
            } else if (Naming.recognizeDown(nameWithoutExtension)) {
                if (taskData.cubeSizeChanged()) {
                    taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(image.path, resDir, taskData.getNewCubeSize())));
                    taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(resDir + File.separator + nameWithoutExtension + ".tif", outputDir + File.separator + nameWithoutDescription + aggstngs.ge.naming.getDown(), taskData.getNewTileSize())));
                    taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resDir + File.separator + nameWithoutExtension + ".tif"}));
                } else {
                    taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(image.path, outputDir + File.separator + nameWithoutDescription + aggstngs.ge.naming.getDown(), taskData.getNewTileSize())));
                }
            }
        }
    }

    //Cubic to Skybox
    private void generateOpCTSB(TaskData taskData, boolean previewOnly) {
        String parentFolderName;
        String[] tmp;
        String outputDir;
        String resDir = aggstngs.ge.getTmpDir() + File.separator + "res";
        String nameWithoutExtension;

        tmp = taskData.getPanorama().getImages().get(0).path.split(Pattern.quote(File.separator));
        parentFolderName = tmp[tmp.length - 2];
        outputDir = getOutputFolderName(aggstngs.ge.getOutputDir() + File.separator + "skybox_" + parentFolderName);
        
        if (taskData.getNewCubeSize()*3 > 8000){
            taskData.setNewCubeSize(2666);
        }

        if (taskData.cubeSizeChanged() && !previewOnly) {
            String[] input = new String[6];
            for (int i = 0; i < taskData.getPanorama().getImages().size(); i++) {
                taskData.operations.add(new Operation(Operation.TYPE_RES, generateArgsRES(taskData.getPanorama().getImages().get(i).path, resDir, taskData.getNewCubeSize())));
                nameWithoutExtension = taskData.getPanorama().getImages().get(i).path.substring(taskData.getPanorama().getImages().get(i).path.lastIndexOf(File.separator) + 1, taskData.getPanorama().getImages().get(i).path.lastIndexOf('.'));
                input[i] = resDir + File.separator + nameWithoutExtension + ".tif";
            }
            taskData.operations.add(new Operation(Operation.TYPE_SB, generateArgsSBM(input, outputDir, false)));
            for (String resized : input) {
                taskData.operations.add(new Operation(Operation.TYPE_DEL, new String[]{resized}));
            }
        } else {
            String[] input = new String[6];
            for (int i = 0; i < taskData.getPanorama().getImages().size(); i++) {
                input[i] = taskData.getPanorama().getImages().get(i).path;
            }
            taskData.operations.add(new Operation(Operation.TYPE_SB, generateArgsSBM(input, outputDir, true)));
        }
    }

    //Flat to Zoomify 
    private void generateOpFTZ(TaskData taskData) {
        Image image = taskData.getPanorama().getImages().get(0);
        String[] tmp = image.path.split(Pattern.quote(File.separator));
        String parentFolderName = tmp[tmp.length - 2];
        String outputDir = getOutputFolderName(aggstngs.ge.getOutputDir() + File.separator + "zoomify_" + parentFolderName);
        new File(outputDir).mkdir();
        String nameWithoutExtension = image.path.substring(image.path.lastIndexOf(File.separator) + 1, image.path.lastIndexOf('.'));
        taskData.operations.add(new Operation(Operation.TYPE_ZYT, generateArgsZYT(image.path, outputDir, taskData.getNewTileSize())));
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

    private String[] generateArgsRES(String input, String output, int width) {
        ArrayList tmpArgsRES = new ArrayList();
        tmpArgsRES.add("-simpleoutput");
        tmpArgsRES.add("-width");
        tmpArgsRES.add(Integer.toString(width));
        tmpArgsRES.add("-outputdir");
        tmpArgsRES.add(output);
        tmpArgsRES.add(input);
        String[] argsRES = new String[tmpArgsRES.size()];
        tmpArgsRES.toArray(argsRES);
        return argsRES;
    }

    private String[] generateArgsERF(String input, String output, int fov, int offset) {
        ArrayList tmpArgsRES = new ArrayList();
        tmpArgsRES.add("-simpleoutput");
        tmpArgsRES.add("-fov");
        tmpArgsRES.add(Integer.toString(fov));
        tmpArgsRES.add("-offset");
        tmpArgsRES.add(Integer.toString(offset));
        tmpArgsRES.add("-outputdir");
        tmpArgsRES.add(output);
        tmpArgsRES.add(input);
        String[] argsRES = new String[tmpArgsRES.size()];
        tmpArgsRES.toArray(argsRES);
        return argsRES;
    }

    private String[] generateArgsDZT(String input, String output, int tileSize) {
        ArrayList tmpArgsDZT = new ArrayList();
        tmpArgsDZT.add("-verbose");
        tmpArgsDZT.add("-simpleoutput");
        tmpArgsDZT.add("-overlap");
        tmpArgsDZT.add(Integer.toString(aggstngs.dzt.getTileOverlap()));
        tmpArgsDZT.add("-quality");
        tmpArgsDZT.add(Float.toString(aggstngs.dzt.getQuality()));
        tmpArgsDZT.add("-tilesize");
        tmpArgsDZT.add(Integer.toString(tileSize));
        tmpArgsDZT.add("-outputdir");
        tmpArgsDZT.add(output);
        tmpArgsDZT.add(input);
        String argsDZT[] = new String[tmpArgsDZT.size()];
        tmpArgsDZT.toArray(argsDZT);
        return argsDZT;
    }

    private String[] generateArgsZYT(String input, String output, int tileSize) {
        ArrayList tmpArgsZYT = new ArrayList();
        tmpArgsZYT.add("-verbose");
        tmpArgsZYT.add("-simpleoutput");
        tmpArgsZYT.add("-quality");
        tmpArgsZYT.add(Float.toString(aggstngs.zyt.getQuality()));
        tmpArgsZYT.add("-tilesize");
        tmpArgsZYT.add(Integer.toString(tileSize));
        tmpArgsZYT.add("-outputdir");
        tmpArgsZYT.add(output);
        tmpArgsZYT.add(input);
        String argsZYT[] = new String[tmpArgsZYT.size()];
        tmpArgsZYT.toArray(argsZYT);
        return argsZYT;
    }

    private String[] generateArgsSBM(String[] input, String output, boolean previewOnly) {
        ArrayList tmpArgsSBM = new ArrayList();
        if (previewOnly) {
            tmpArgsSBM.add("-previewonly");
        }
        tmpArgsSBM.add("-quality");
        tmpArgsSBM.add(Float.toString(aggstngs.sbm.getQuality()));
        tmpArgsSBM.add("-previewsize");
        tmpArgsSBM.add(Integer.toString(aggstngs.sbm.getPreviewSize()));
        tmpArgsSBM.add("-outputdir");
        tmpArgsSBM.add(output);
        tmpArgsSBM.addAll(new ArrayList<String>(Arrays.asList(input)));
        String argsSBM[] = new String[tmpArgsSBM.size()];
        tmpArgsSBM.toArray(argsSBM);
        return argsSBM;
    }

    private String[] generateArgsEC(String input, String output) {
        ArrayList tmpArgsEC = new ArrayList();
        tmpArgsEC.add("-verbose");
        tmpArgsEC.add("-simpleoutput");
        tmpArgsEC.add("-naming");
        tmpArgsEC.add(aggstngs.ge.naming.getType());
        tmpArgsEC.add("-overlap");
        tmpArgsEC.add(Integer.toString(aggstngs.ec.getWallOverlap()));
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
                aggstngs.erf.setJarDir(prop.getProperty(ERFSettings.VALUE_JAR_DIR));

                aggstngs.dzt.setTileOverlap(prop.getProperty(DZTSettings.VALUE_TILE_OVERLAP));
                aggstngs.dzt.setTileSize(prop.getProperty(DZTSettings.VALUE_TILE_SIZE));
                aggstngs.dzt.setQuality(prop.getProperty(DZTSettings.VALUE_QUALITY));
                aggstngs.dzt.setJarDir(prop.getProperty(DZTSettings.VALUE_JAR_DIR));

                aggstngs.zyt.setTileSize(prop.getProperty(ZYTSettings.VALUE_TILE_SIZE));
                aggstngs.zyt.setQuality(prop.getProperty(ZYTSettings.VALUE_QUALITY));
                aggstngs.zyt.setJarDir(prop.getProperty(ZYTSettings.VALUE_JAR_DIR));

                aggstngs.sbm.setPreviewSize(prop.getProperty(SBMSettings.VALUE_PREVIEW_SIZE));
                aggstngs.sbm.setQuality(prop.getProperty(SBMSettings.VALUE_QUALITY));
                aggstngs.sbm.setJarDir(prop.getProperty(SBMSettings.VALUE_JAR_DIR));

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

                aggstngs.opt.setResizePercent(prop.getProperty(OPTSettings.VALUE_RESIZE_PERCENT));
                aggstngs.opt.setMaxTileSize(prop.getProperty(OPTSettings.VALUE_MAX_TILE_SIZE));
                aggstngs.opt.setMinTileSize(prop.getProperty(OPTSettings.VALUE_MIN_TILE_SIZE));

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

        if (aggstngs.erf.jarDirChanged()) {
            prop.put(ERFSettings.VALUE_JAR_DIR, aggstngs.erf.getJarDir());
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

        if (aggstngs.zyt.tileSizeChanged()) {
            prop.put(ZYTSettings.VALUE_TILE_SIZE, Integer.toString(aggstngs.zyt.getTileSize()));
        }
        if (aggstngs.zyt.qualityChanged()) {
            prop.put(ZYTSettings.VALUE_QUALITY, Float.toString(aggstngs.zyt.getQuality()));
        }
        if (aggstngs.zyt.jarDirChanged()) {
            prop.put(ZYTSettings.VALUE_JAR_DIR, aggstngs.zyt.getJarDir());
        }

        if (aggstngs.sbm.previewSizeChanged()) {
            prop.put(SBMSettings.VALUE_PREVIEW_SIZE, Integer.toString(aggstngs.sbm.getPreviewSize()));
        }
        if (aggstngs.sbm.qualityChanged()) {
            prop.put(SBMSettings.VALUE_QUALITY, Float.toString(aggstngs.sbm.getQuality()));
        }
        if (aggstngs.sbm.jarDirChanged()) {
            prop.put(SBMSettings.VALUE_JAR_DIR, aggstngs.sbm.getJarDir());
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

        if (aggstngs.opt.resizePercentChanged()) {
            prop.put(OPTSettings.VALUE_RESIZE_PERCENT, Integer.toString(aggstngs.opt.getResizePercent()));
        }
        if (aggstngs.opt.maxTileSizeChanged()) {
            prop.put(OPTSettings.VALUE_MAX_TILE_SIZE, Integer.toString(aggstngs.opt.getMaxTileSize()));
        }
        if (aggstngs.opt.minTileSizeChanged()) {
            prop.put(OPTSettings.VALUE_MIN_TILE_SIZE, Integer.toString(aggstngs.opt.getMinTileSize()));
        }

        try {
            prop.store(new FileOutputStream(aggstngs.currentDirectory + File.separator + AggregatedSettings.FILE_PROPERTIES), null);
        } catch (IOException ex) {
            //TODO: indicate error
            System.out.println("Settings could not be written.");
        }
    }
}
