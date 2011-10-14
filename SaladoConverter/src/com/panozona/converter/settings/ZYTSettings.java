package com.panozona.converter.settings;

import com.panozona.converter.utils.Messages;
import java.io.File;

/**
 * @author Marek Standio
 */
public class ZYTSettings {

    public static final String JAR_CLASSNAME = "zoomifytiler.ZoomifyTiler";
    public static final String JAR_FILENAME = "ZoomifyTiler.jar";
    public static final String VALUE_TILE_SIZE = "ZYT_tileSize";
    public static final String VALUE_QUALITY = "ZYT_quality";
    public static final String VALUE_JAR_DIR = "ZYT_jarDir";
    private int tileSize;
    private float quality;
    private String jarDir;
    private final int defaultTileSize = 896;
    private final float defaultQuality = 0.8f;
    private String defaultJarDir = "";

    public ZYTSettings(String currentDirectory) {
        if (currentDirectory != null) {
            defaultJarDir = currentDirectory + File.separator + "components" + File.separator + JAR_FILENAME;
        }
        tileSize = defaultTileSize;
        quality = defaultQuality;
        jarDir = defaultJarDir;
    }

    public void setTileSize(String value) throws IllegalArgumentException {
        if (value != null) {
            try {
                setTileSize(Integer.parseInt(value));
            } catch (Exception ex) {
                throw new IllegalArgumentException(Messages.DZT_TILESIZE_ERROR);
            }
        }
    }

    public void setTileSize(int value) throws IllegalArgumentException {
        if (value > 0) {
            tileSize = value;
        } else {
            throw new IllegalArgumentException(Messages.DZT_TILESIZE_ERROR);
        }
    }

    public int getTileSize() {
        return tileSize;
    }

    public int getDefaultTileSize() {
        return defaultTileSize;
    }

    public boolean tileSizeChanged() {
        return (tileSize != defaultTileSize);
    }

    public void setQuality(String value) throws IllegalArgumentException {
        if (value != null) {
            try {
                setQuality(Float.parseFloat(value));
            } catch (Exception ex) {
                throw new IllegalArgumentException(Messages.ZYT_QUALITY_ERROR);
            }
        }
    }

    public void setQuality(float value) throws IllegalArgumentException {
        if (value > 0f && value <= 1f) {
            quality = value;
        } else {
            throw new IllegalArgumentException(Messages.ZYT_QUALITY_ERROR);
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
                throw new IllegalArgumentException(Messages.ZYT_JAR_DIR_ERROR);
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
