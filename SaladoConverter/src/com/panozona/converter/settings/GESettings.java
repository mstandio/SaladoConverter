package com.panozona.converter.settings;

import com.panozona.converter.utils.Messages;
import java.io.File;

/**
 * @author Marek Standio
 */
public class GESettings {

    public static final String VALUE_TMP_DIR = "GE_tmp_dir";
    public static final String VALUE_MEMORY_LIMIT = "GE_memory_limit";
    public static final String VALUE_REMOVE_OBSOLETE = "GE_remove_obsolete";
    public static final String VALUE_OVERWRITE_OUTPUT = "GE_overwrite_output";
    public static final String VALUE_INPUT_DIR = "GE_input_dir";
    public static final String VALUE_OUTPUT_DIR = "GE_output_dir";
    public static final String VALUE_SELECTED_COMMAND = "GE_selected_commmand";
    public static final String COMMAND_EQUIRECTANGULAR_TO_DEEPZOOM_CUBIC = "Equirectangular to Deep Zoom cubic";
    public static final String COMMAND_CUBIC_TO_DEEPZOOM_CUBIC = "Cubic to Deep Zoom cubic";
    public static final String COMMAND_EQUIRECTANGULAR_TO_CUBIC = "Equirectangular to cubic";
    public static final String COMMAND_CUBIC_TO_RESIZED_CUBIC = "Cubic to resized cubic";
    private String tmpDir;
    private int memoryLimit;
    private String outputDir;
    private boolean remObsolete;
    private boolean overwriteOutput;
    private String inputDir;
    private String selectedCommand;
    private String defaultTmpDir = "";
    private int defaultMemoryLimit = 1024;
    private boolean defaultRemObsolete = true;
    private boolean defaultOverwriteOutput = false;
    private String defaultOutputDir = "";
    private String defaultInputDir = "";
    private final String defaultSelectedCommand = COMMAND_EQUIRECTANGULAR_TO_DEEPZOOM_CUBIC;

    public GESettings(String currentDirectory) {
        defaultTmpDir = currentDirectory
                + File.separator
                + "tmp";
        defaultOutputDir = currentDirectory
                + File.separator
                + "output";
        defaultInputDir = currentDirectory
                + File.separator
                + "input";
        tmpDir = defaultTmpDir;
        memoryLimit = defaultMemoryLimit;
        remObsolete = defaultRemObsolete;
        overwriteOutput = defaultOverwriteOutput;
        outputDir = defaultOutputDir;
        inputDir = defaultInputDir;
        selectedCommand = defaultSelectedCommand;
    }

    public String[] getCommandNames() {
        return new String[]{
                    COMMAND_EQUIRECTANGULAR_TO_DEEPZOOM_CUBIC,
                    COMMAND_CUBIC_TO_DEEPZOOM_CUBIC,
                    COMMAND_EQUIRECTANGULAR_TO_CUBIC,
                    COMMAND_CUBIC_TO_RESIZED_CUBIC
                };
    }

    public void setTmpDir(String value) throws IllegalArgumentException {
        if (value != null) {
            File tmp = new File(value);
            if (tmp.isDirectory()) {
                tmpDir = value;
            } else {
                throw new IllegalArgumentException(Messages.GE_TMP_DIR_ERROR);
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

    public void setMemoryLimit(String value) throws IllegalArgumentException {
        if (value != null) {
            try {
                if (Integer.parseInt(value) > 253) {
                    memoryLimit = Integer.parseInt(value);
                } else {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(Messages.GE_MEMORY_LIMIT_ERROR);
            }
        }
    }

    public void setMemoryLimit(int value) throws IllegalArgumentException {
        if (value > 253) {
            memoryLimit = value;
        } else {
            throw new IllegalArgumentException(Messages.GE_MEMORY_LIMIT_ERROR);
        }
    }

    public int getMemoryLimit() {
        return memoryLimit;
    }

    public int getDefaultMemoryLimit() {
        return defaultMemoryLimit;
    }

    public boolean memoryLimitChanged() {
        return (memoryLimit != defaultMemoryLimit);
    }

    public void setRemoveObsolete(boolean value) {
        remObsolete = value;
    }

    public void setRemoveObsolete(String value) {
        if (value != null) {
            remObsolete = value.equals("true");
        }
    }

    public boolean getRemoveObsolete() {
        return remObsolete;
    }

    public boolean getDefaultRemoveObsolete() {
        return defaultRemObsolete;
    }

    public boolean removeObsoleteChanged() {
        return (remObsolete != defaultRemObsolete);
    }

    public void setOverwriteOutput(boolean value) {
        overwriteOutput = value;
    }

    public void setOverwriteOutput(String value) {
        if (value != null) {
            overwriteOutput = value.equals("true");
        }
    }

    public boolean getOverwriteOutput() {
        return overwriteOutput;
    }

    public boolean getDefaultOverwriteOutput() {
        return defaultOverwriteOutput;
    }

    public boolean overwriteOutputChanged() {
        return (overwriteOutput != defaultOverwriteOutput);
    }

    public void setOutputDir(String value) throws IllegalArgumentException {
        if (value != null) {
            File tmp = new File(value);
            if (tmp.exists() && tmp.isDirectory()) {
                outputDir = value;
            } else {
                throw new IllegalArgumentException(Messages.GE_OUTPUT_DIR_ERROR);
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

    public void setInputDir(String value) throws IllegalArgumentException {
        if (value != null) {
            File tmp = new File(value);
            if (tmp.isDirectory()) {
                inputDir = value;
            } else {
                throw new IllegalArgumentException(Messages.GE_INPUT_DIR_ERROR);
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

    public void setSelectedCommand(String value) throws IllegalArgumentException {
        if (value != null) {
            if (value.equals(COMMAND_EQUIRECTANGULAR_TO_DEEPZOOM_CUBIC)
                    || value.equals(COMMAND_CUBIC_TO_DEEPZOOM_CUBIC)
                    || value.equals(COMMAND_EQUIRECTANGULAR_TO_CUBIC)
                    || value.equals(COMMAND_CUBIC_TO_RESIZED_CUBIC)) {
                selectedCommand = value;
            } else {
                throw new IllegalArgumentException(Messages.GE_COMMAND_ERROR);
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
