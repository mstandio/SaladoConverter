package com.panozona.converter.settings;

import com.panozona.converter.utils.InfoException;
import java.io.File;

/**
 *
 * @author Marek Standio
 */
public class DZTSettings {

    public static final String JAR_CLASSNAME = "deepzoomtiler.DeepZoomTiler";
    public static final String JAR_FILENAME = "DeepZoomTiler.jar";
    public static final String VALUE_OVERLAP = "DZT_overlap";
    public static final String VALUE_TILE_SIZE = "DZT_tileSize";
    public static final String VALUE_QUALITY = "DZT_quality";
    public static final String VALUE_JAR_DIR = "DZT_jarDir";
    private int overlap;
    private int tileSize;
    private float quality;
    private String jarDir;
    private final int defaultOverlap = 1;
    private final int defaultTileSize = 256;
    private final float defaultQuality = 0.8f;
    private String defaultJarDir = "";

    public DZTSettings(String currentDirectory) {
        if (currentDirectory != null) {
            defaultJarDir = currentDirectory
                    + File.separator
                    + AggregatedSettings.FOLDER_COMPONENTS
                    + File.separator
                    + JAR_FILENAME;
        }
        overlap = defaultOverlap;
        tileSize = defaultTileSize;
        quality = defaultQuality;
        jarDir = defaultJarDir;
    }

    public void setOverlap(String value, String errorMsg) throws InfoException {
        if (value != null) {
            try {
                overlap = Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                throw new InfoException(errorMsg);
            }
        }
    }

    public String getOverlap() {
        return Integer.toString(overlap);
    }

    public String getDefaultOverlap() {
        return Integer.toString(defaultOverlap);
    }

    public boolean overlapChanged() {
        return (overlap != defaultOverlap);
    }

    public void setTileSize(String value, String errorMsg) throws InfoException {
        if (value != null) {
            try {
                if (Integer.parseInt(value) != 0) {
                    tileSize = Integer.parseInt(value);
                } else {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                throw new InfoException(errorMsg);
            }
        }
    }

    public String getTileSize() {
        return Integer.toString(tileSize);
    }

    public String getDefaultTileSize() {
        return Integer.toString(defaultTileSize);
    }

    public boolean tileSizeChanged() {
        return (tileSize != defaultTileSize);
    }

    public void setQuality(String value, String errorMsg) throws InfoException {
        if (value != null) {
            try {
                if (Float.parseFloat(value) > 0 && Float.parseFloat(value) <= 1) {
                    quality = Float.parseFloat(value);
                } else {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                throw new InfoException(errorMsg);
            }
        }
    }

    public String getQuality() {
        return Float.toString(quality);
    }

    public String getDefaultQuality() {
        return Float.toString(defaultQuality);
    }

    public boolean qualityChanged() {
        return (quality != defaultQuality);
    }

    public void setJarDir(String value, String errorMsg) throws InfoException {
        if (value != null) {
            File tmp = new File(value);
            if (tmp.isFile() && value.endsWith(".jar")) {
                jarDir = value;
            } else {
                throw new InfoException(errorMsg);
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
