package com.panozona.converter.settings;

import com.panozona.converter.utils.Info;
import com.panozona.converter.utils.InfoException;
import java.io.File;

/**
 *
 * @author Marek Standio
 */
public class GESettings {

    public static final String VALUE_SEARCH_SUBDIR = "GE_search_sub";
    public static final String VALUE_SEARCH_DEPTH = "GE_depth";
    public static final String VALUE_TMP_DIR = "GE_tmpdir";
    public static final String VALUE_INPUT_DIR = "GE_inputdir";
    public static final String VALUE_OUTPUT_DIR = "GE_outputdir";
    public static final String VALUE_SELECTED_COMMAND = "GE_sel_comm";
    
    public static final String EQUIRECTANGULAR_TO_DEEPZOOM_EQUIRECTANGULAR = "Equirectangular to DeepZoom equirectangular";
    public static final String EQUIRECTANGULAR_TO_DEEPZOOM_CUBIC = "Equirectangular to DeepZoom cubic";
    public static final String CUBIC_TO_DEEPZOOM_CUBIC = "Cubic to DeepZoom cubic";
    public static final String EQUIRECTANGULAR_TO_CUBIC = "Equirectangular to cubic";

    private Boolean searchSubDir;
    private Integer searchDepth;
    private String tmpDir;
    private String outputDir;
    private String inputDir;
    private String selectedCommand;
    private final Boolean defaultSearchSubDir = true;
    private final Integer defaultSearchDepth = 3;
    private String defaultTmpDir = "";
    private String defaultOutputDir = "";
    private String defaultInputDir ="";
    private final String defaultSelectedCommand = EQUIRECTANGULAR_TO_DEEPZOOM_CUBIC;

    public GESettings(String currentDirectory) {
        defaultTmpDir = currentDirectory +
                File.separator +
                AggregatedSettings.FOLDER_TMP;
        defaultOutputDir = currentDirectory +
                File.separator +
                AggregatedSettings.FOLDER_OUTPUT;
        defaultInputDir = currentDirectory +
                File.separator +
                AggregatedSettings.FOLDER_INPUT;
        searchSubDir = defaultSearchSubDir;
        searchDepth = defaultSearchDepth;
        tmpDir = defaultTmpDir;
        outputDir = defaultOutputDir;
        inputDir = defaultInputDir;
        selectedCommand = defaultSelectedCommand;
    }

    public String[] getCommandNames() {
        return new String[]{
                    EQUIRECTANGULAR_TO_DEEPZOOM_CUBIC,
                    EQUIRECTANGULAR_TO_DEEPZOOM_EQUIRECTANGULAR,
                    EQUIRECTANGULAR_TO_CUBIC,
                    CUBIC_TO_DEEPZOOM_CUBIC
                };
    }

    public void setSearchSubDirs(String value) throws InfoException {
        if (value != null){
            if (value.equals("true") || value.equals("false")) {
                searchSubDir = Boolean.parseBoolean(value);
            } else {
                throw new InfoException(Info.GE_SEARCH_SUB_ERROR);
            }
        }
    }
    public String getSearchSubDirs() {
        return searchSubDir.toString();
    }
    public String getDefaultSearchSubDirs() {
        return defaultSearchSubDir.toString();
    }
    public boolean searchSubDirsChanged() {
        return !(searchSubDir.equals(defaultSearchSubDir));
    }

    public void setSearchDepth(String value) throws InfoException {
        if (value != null) {
            try {
                if (Integer.parseInt(value) > 0 && Integer.parseInt(value) <= 10) {
                    searchDepth = Integer.parseInt(value);
                } else {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                throw new InfoException(Info.GE_DEPTH_ERROR);
            }
        }
    }
    public String getSearchDepth() {
        return searchDepth.toString();
    }
    public String getDefaultSearchDepth() {
        return defaultSearchDepth.toString();
    }
    public boolean searchDepthChanged() {
        return !(searchDepth.equals(defaultSearchDepth));
    }

    public void setTmpDir(String value) throws InfoException {
        if (value != null) {
            File tmp = new File(value);
            if (tmp.isDirectory()) {
                tmpDir = value;
            } else {
                throw new InfoException(Info.GE_TMP_DIR_ERROR);
            }
        }
    }
    public String getTmpDir() {
        return tmpDir;
    }
    public String getDefaultTmpDir() {
        return defaultTmpDir;
    }

    public boolean tmpDirChanged() {
        return !(tmpDir.equals(defaultTmpDir));
    }

    public void setOutputDir(String value) throws InfoException {
        if (value != null) {
            File tmp = new File(value);
            if (tmp.isDirectory()) {
                outputDir = value;
            } else {
                throw new InfoException(Info.GE_OUT_DIR_ERROR);
            }
        }
    }
    public String getOutputDir() {
        return outputDir;
    }
    public String getDefaultOutputDir() {
        return defaultOutputDir;
    }
    public boolean outputDirChanged() {
        return !(outputDir.equals(defaultOutputDir));
    }

    public void setInputDir(String value) throws InfoException {
        if (value != null) {
            File tmp = new File(value);
            if (tmp.isDirectory()) {
                inputDir = value;
            } else {
                throw new InfoException(Info.GE_IN_DIR_ERROR);
            }
        }
    }

    public String getInputDir() {
        return inputDir;
    }
    public String getDefaultInputDir() {
        return inputDir;
    }
    public boolean inputDirChanged() {
        return !(inputDir.equals(defaultInputDir));
    }

    public void setSelectedCommand(String value) throws InfoException {
        if (value != null) {
            if (value.equals(EQUIRECTANGULAR_TO_DEEPZOOM_EQUIRECTANGULAR) ||
                    value.equals(EQUIRECTANGULAR_TO_DEEPZOOM_CUBIC) ||
                    value.equals(CUBIC_TO_DEEPZOOM_CUBIC) ||
                    value.equals(EQUIRECTANGULAR_TO_CUBIC)) {
                selectedCommand = value;
            } else {
                throw new InfoException(Info.GE_COMMAND_ERROR);
            }
        }
    }
    public String getSelectedCommand() {
        return selectedCommand;
    }
    public String getDefaultSelectedCommand() {
        return defaultSelectedCommand;
    }
    public boolean selectedCommandChanged() {
        return !(selectedCommand.equals(defaultSelectedCommand));
    }
}