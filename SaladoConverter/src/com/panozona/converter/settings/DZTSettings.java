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
    public static final String VALUE_TILE_PART = "DZT_tilePart";
    public static final String VALUE_QUALITY = "DZT_quality";
    public static final String VALUE_JAR_DIR = "DZT_jarDir";
    private static final String TILE_PART_1 = "1/1";
    private static final String TILE_PART_2 = "1/2";
    private static final String TILE_PART_3 = "1/3";
    private static final String TILE_PART_4 = "1/4";
    private static final String TILE_PART_5 = "1/5";
    private static final String TILE_PART_C = "custom";
    private int overlap;
    private int tileSize;
    private int tilePart;
    private float quality;
    private String jarDir;
    private final int defaultOverlap = 1;
    private final int defaultTileSize = 256;
    private final int defaultTilePart = 4;
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
        tilePart = defaultTilePart;
        quality = defaultQuality;
        jarDir = defaultJarDir;
    }

    public String[] getTilePartNames() {
        return new String[]{TILE_PART_1, TILE_PART_2, TILE_PART_3,
                    TILE_PART_4, TILE_PART_5, TILE_PART_C};
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

    public void setTilePart(String value, String errorMsg) throws InfoException {
        if (value != null) {
            if (value.equals(TILE_PART_1)) {
                tilePart = 1;
            } else if (value.equals(TILE_PART_2)) {
                tilePart = 2;
            } else if (value.equals(TILE_PART_3)) {
                tilePart = 3;
            } else if (value.equals(TILE_PART_4)) {
                tilePart = 4;
            } else if (value.equals(TILE_PART_5)) {
                tilePart = 5;
            } else if (value.equals(TILE_PART_C)) {
                tilePart = 0;
            } else {
                throw new InfoException(errorMsg);
            }
        }
    }

    public String getTilePart() {
        switch (tilePart) {
            case 1:
                return TILE_PART_1;
            case 2:
                return TILE_PART_2;
            case 3:
                return TILE_PART_3;
            case 4:
                return TILE_PART_4;
            case 5:
                return TILE_PART_5;
            default:
                return TILE_PART_C;
        }
    }

    public String getDefaultTilePart() {
        switch (defaultTilePart) {
            case 1:
                return TILE_PART_1;
            case 2:
                return TILE_PART_2;
            case 3:
                return TILE_PART_3;
            case 4:
                return TILE_PART_4;
            case 5:
                return TILE_PART_5;
            default:
                return TILE_PART_C;
        }
    }

    public boolean tilePartChanged() {
        return (tilePart != defaultTilePart);
    }

    public boolean tilePartDisabled(String value) {
        return value.equals(TILE_PART_C);
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

    public String getTilePartValue(){
        return Integer.toString(tilePart);
    }
}
