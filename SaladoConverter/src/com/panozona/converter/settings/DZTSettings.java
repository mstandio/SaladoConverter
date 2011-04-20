package com.panozona.converter.settings;

import com.panozona.converter.utils.Messages;
import java.io.File;

/**
 * @author Marek Standio
 */
public class DZTSettings {

    public static final String JAR_CLASSNAME = "deepzoomtiler.DeepZoomTiler";
    public static final String JAR_FILENAME = "DeepZoomTiler.jar";
    public static final String VALUE_TILE_OVERLAP = "DZT_tileOverlap";
    public static final String VALUE_TILE_SIZE = "DZT_tileSize";
    public static final String VALUE_QUALITY = "DZT_quality";
    public static final String VALUE_JAR_DIR = "DZT_jarDir";
    private int tileOverlap;
    private int tileSize;
    private float quality;
    private String jarDir;
    private final int defaultTileOverlap = 1;
    private final int defaultTileSize = 512;
    private final float defaultQuality = 0.8f;
    private String defaultJarDir = "";

    public DZTSettings(String currentDirectory) {
        if (currentDirectory != null) {
            defaultJarDir = currentDirectory
                    + File.separator
                    + "components"
                    + File.separator
                    + JAR_FILENAME;
        }
        tileOverlap = defaultTileOverlap;
        tileSize = defaultTileSize;
        quality = defaultQuality;
        jarDir = defaultJarDir;
    }

    public void setTileOverlap(String value) throws IllegalArgumentException {
        if (value != null) {
            try {
                if (Integer.parseInt(value) >= 0) {
                    tileOverlap = Integer.parseInt(value);
                } else {
                    throw new IllegalArgumentException();
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException(Messages.DZT_TILE_OVERLAP_ERROR);
            }
        }
    }

    public void setTileOverlap(int value) throws IllegalArgumentException {
        if (value >= 0) {
            tileOverlap = value;
        } else {
            throw new IllegalArgumentException(Messages.DZT_TILE_OVERLAP_ERROR);
        }
    }

    public int getTileOverlap() {
        return tileOverlap;
    }

    public int getDefaultTileOverlap() {
        return defaultTileOverlap;
    }

    public boolean tileOverlapChanged() {
        return (tileOverlap != defaultTileOverlap);
    }

    public void setTileSize(String value) throws IllegalArgumentException {
        if (value != null) {
            try {
                if (Integer.parseInt(value) > 0) {
                    tileSize = Integer.parseInt(value);
                } else {
                    throw new IllegalArgumentException();
                }
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
                if (Float.parseFloat(value) > 0f && Float.parseFloat(value) <= 1f) {
                    quality = Float.parseFloat(value);
                } else {
                    throw new IllegalArgumentException();
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException(Messages.DZT_QUALITY_ERROR);
            }
        }
    }

    public void setQuality(float value) throws IllegalArgumentException {
        if (value > 0f && value <= 1f) {
            quality = value;
        } else {
            throw new IllegalArgumentException(Messages.DZT_QUALITY_ERROR);
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
