package com.panozona.converter.settings;

import com.panozona.converter.utils.Info;
import com.panozona.converter.utils.InfoException;
import java.io.File;

/**
 *
 * @author Marek Standio
 */
public class DZTSettings {

    public static final String JAR_CLASSNAME = "DeepZoomTiler";
    public static final String JAR_FILENAME = "DeepZoomTiler.jar";
    public static final String VALUE_OVERLAP = "DZT_overlap";
    public static final String VALUE_TILE_SIZE = "DZT_tileSize";
    public static final String VALUE_QUALITY = "DZT_quality";
    public static final String VALUE_JAR_DIR = "DZT_jarDir";
    private Integer overlap;
    private Integer tileSize;
    private Float quality;
    private String jarDir;
    private final Integer defaultOverlap = 1;
    private final Integer defaultTileSize = 256;    
    private final Float defaultQuality = 0.8f;   
    private String defaultJarDir = "";

    public DZTSettings(String currentDirectory) {        
        defaultJarDir = currentDirectory +
                File.separator +
                AggregatedSettings.FOLDER_COMPONENTS +
                File.separator +
                JAR_FILENAME;
        overlap = defaultOverlap;
        tileSize = defaultTileSize;
        quality = defaultQuality;        
        jarDir = defaultJarDir;
    }

    public void setOverlap(String value) throws InfoException {
        if (value != null) {
            try {
                overlap = Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                throw new InfoException(Info.DZT_OVERLAP_ERROR);
            }
        }
    }
    public String getOverlap() {
        return overlap.toString();
    }    
    public String getDefaultOverlap() {
        return defaultOverlap.toString();
    }
    public boolean overlapChanged() {
        return !(overlap.equals(defaultOverlap));
    }

    public void setTileSize(String value) throws InfoException {
        if (value != null) {
            try {
                if (Integer.parseInt(value) != 0) {
                    tileSize = Integer.parseInt(value);
                } else {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                throw new InfoException(Info.DZT_TILESIZE_ERROR);
            }
        }
    }
    public String getTileSize() {
        return tileSize.toString();
    }
    public String getDefaultTileSize() {
        return defaultTileSize.toString();
    }
    public boolean tileSizeChanged() {
        return !(tileSize.equals(defaultTileSize));
    }

    public void setQuality(String value) throws InfoException {
        if (value != null) {
            try {
                if (Float.parseFloat(value) > 0 && Float.parseFloat(value) <= 1) {
                    quality = Float.parseFloat(value);
                } else {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                throw new InfoException(Info.DZT_QUALITY_ERROR);
            }
        }
    }
    public String getQuality() {
        return quality.toString();
    }
    public String getDefaultQuality() {
        return defaultQuality.toString();
    }

    public boolean qualityChanged() {
        return !(quality.equals(defaultQuality));
    }   
    public void setJarDir(String value) throws InfoException {
        if (value != null) {
            File tmp = new File(value);
            if (tmp.isFile() && value.endsWith(".jar")) {
                jarDir = value;
            } else {
                throw new InfoException(Info.DZT_JAR_DIR_ERROR);
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
