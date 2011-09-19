package com.panozona.converter.settings;

import com.panozona.converter.utils.Messages;
import java.io.File;

/**
 * @author Marek Standio
 */
public class SBMSettings {

    public static final String JAR_CLASSNAME = "skyboxmaker.SkyboxMaker";
    public static final String JAR_FILENAME = "SkyboxMaker.jar";    
    public static final String VALUE_PREVIEW_SIZE = "SBM_previewSize";
    public static final String VALUE_QUALITY = "ZBM_quality";
    public static final String VALUE_JAR_DIR = "SBM_jarDir";    
    private int previewSize;
    private float quality;
    private String jarDir;    
    private final int defaultPreviewSize = 200;
    private final float defaultQuality = 0.8f;
    private String defaultJarDir = "";

    public SBMSettings(String currentDirectory) {
        if (currentDirectory != null) {
            defaultJarDir = currentDirectory
                    + File.separator
                    + "components"
                    + File.separator
                    + JAR_FILENAME;
        }
        
        previewSize = defaultPreviewSize;
        quality = defaultQuality;
        jarDir = defaultJarDir;
    }    

    public void setPreviewSize(String value) throws IllegalArgumentException {
        if (value != null) {
            try {
                setPreviewSize(Integer.parseInt(value));
            } catch (Exception ex) {
                throw new IllegalArgumentException(Messages.SBM_PREVIEWSIZE_ERROR);
            }
        }
    }

    public void setPreviewSize(int value) throws IllegalArgumentException {
        if (value > 0) {
            previewSize = value;
        } else {
            throw new IllegalArgumentException(Messages.SBM_PREVIEWSIZE_ERROR);
        }
    }

    public int getPreviewSize() {
        return previewSize;
    }

    public int getDefaultPreviewSize() {
        return defaultPreviewSize;
    }

    public boolean previewSizeChanged() {
        return (previewSize != defaultPreviewSize);
    }

    public void setQuality(String value) throws IllegalArgumentException {
        if (value != null) {
            try {
                setQuality(Float.parseFloat(value));
            } catch (Exception ex) {
                throw new IllegalArgumentException(Messages.SBM_QUALITY_ERROR);
            }
        }
    }

    public void setQuality(float value) throws IllegalArgumentException {
        if (value > 0f && value <= 1f) {
            quality = value;
        } else {
            throw new IllegalArgumentException(Messages.SBM_QUALITY_ERROR);
        }
    }

    public float getQuality() {
        return quality;
    }

    public float getDefaultQuality() {
        return defaultQuality;
    }

    public boolean qualityChanged() {
        return (quality != defaultQuality);
    }

    public void setJarDir(String value) throws IllegalArgumentException {
        if (value != null) {
            File tmp = new File(value);
            if (tmp.isFile() && value.endsWith(".jar")) {
                jarDir = value;
            } else {
                throw new IllegalArgumentException(Messages.DZT_JAR_DIR_ERROR);
            }
        }
    }

    public String getJarDir() {
        return jarDir;
    }

    public String getDefaultJarDir() {
        return defaultJarDir;
    }

    public boolean jarDirChanged() {
        return !(jarDir.equals(defaultJarDir));
    }
}
