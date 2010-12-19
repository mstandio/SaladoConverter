package com.panozona.converter.settings;

import com.panozona.converter.utils.InfoException;
import java.io.File;

/**
 *
 * @author Marek Standio
 */
public class GESettings {

    public static final String VALUE_TMP_DIR = "GE_tmpdir";
    public static final String VALUE_MEM_LIMIT = "GE_memlimit";
    public static final String VALUE_INPUT_DIR = "GE_inputdir";
    public static final String VALUE_OUTPUT_DIR = "GE_outputdir";
    public static final String VALUE_SELECTED_COMMAND = "GE_sel_comm";
    public static final String EQUIRECTANGULAR_TO_DEEPZOOM_CUBIC = "Equirectangular to Deep Zoom cubic";
    public static final String CUBIC_TO_DEEPZOOM_CUBIC = "Cubic to Deep Zoom cubic";
    public static final String EQUIRECTANGULAR_TO_CUBIC = "Equirectangular to cubic";
    public static final String CUBIC_TO_RESIZED_CUBIC = "Cubic to resized cubic";
    private String tmpDir;
    private int memoryLimit;
    private String outputDir;
    private String inputDir;
    private String selectedCommand;
    private String defaultTmpDir = "";
    private int defaultMemoryLimit = 1024;
    private String defaultOutputDir = "";
    private String defaultInputDir = "";
    private final String defaultSelectedCommand = EQUIRECTANGULAR_TO_DEEPZOOM_CUBIC;

    public GESettings(String currentDirectory) {
        defaultTmpDir = currentDirectory
                + File.separator
                + AggregatedSettings.FOLDER_TMP;
        defaultOutputDir = currentDirectory
                + File.separator
                + AggregatedSettings.FOLDER_OUTPUT;
        defaultInputDir = currentDirectory
                + File.separator
                + AggregatedSettings.FOLDER_INPUT;
        tmpDir = defaultTmpDir;
        memoryLimit = defaultMemoryLimit;
        outputDir = defaultOutputDir;
        inputDir = defaultInputDir;
        selectedCommand = defaultSelectedCommand;
    }

    public String[] getCommandNames() {
        return new String[]{
                    EQUIRECTANGULAR_TO_DEEPZOOM_CUBIC,
                    CUBIC_TO_DEEPZOOM_CUBIC,
                    EQUIRECTANGULAR_TO_CUBIC,
                    CUBIC_TO_RESIZED_CUBIC
                };
    }

    public void setTmpDir(String value, String errorMsg) throws InfoException {
        if (value != null) {
            File tmp = new File(value);
            if (tmp.isDirectory()) {
                tmpDir = value;
            } else {
                throw new InfoException(errorMsg);
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

    public void setMemoryLimit(String value, String errorMsg) throws InfoException {
        if (value != null) {
            try {
                if (Integer.parseInt(value) > 253) {
                    memoryLimit = Integer.parseInt(value);
                } else {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                throw new InfoException(errorMsg);
            }
        }
    }

    public String getMemoryLimit() {
        return Integer.toString(memoryLimit);
    }

    public String getDefaultMemoryLimit() {
        return Integer.toString(defaultMemoryLimit);
    }

    public boolean memoryLimitChanged() {
        return !(memoryLimit == defaultMemoryLimit);
    }

    public void setOutputDir(String value, String errorMsg) throws InfoException {
        if (value != null) {
            File tmp = new File(value);
            if (tmp.isDirectory()) {
                outputDir = value;
            } else {
                throw new InfoException(errorMsg);
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

    public void setInputDir(String value, String errorMsg) throws InfoException {
        if (value != null) {
            File tmp = new File(value);
            if (tmp.isDirectory()) {
                inputDir = value;
            } else {
                throw new InfoException(errorMsg);
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

    public void setSelectedCommand(String value, String errorMsg) throws InfoException {
        if (value != null) {
            if (value.equals(EQUIRECTANGULAR_TO_DEEPZOOM_CUBIC)
                    || value.equals(CUBIC_TO_DEEPZOOM_CUBIC)
                    || value.equals(EQUIRECTANGULAR_TO_CUBIC)
                    || value.equals(CUBIC_TO_RESIZED_CUBIC)) {
                selectedCommand = value;
            } else {
                throw new InfoException(errorMsg);
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
