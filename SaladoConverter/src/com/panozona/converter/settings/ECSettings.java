package com.panozona.converter.settings;

import com.panozona.converter.utils.Messages;
import java.io.File;

/**
 * @author Marek Standio
 */
public class ECSettings {

    public static final String JAR_CLASSNAME = "equirectangulartocubic.EquirectangularToCubic";
    public static final String JAR_FILENAME = "EquirectangularToCubic.jar";
    public static final String VALUE_WALL_OVERLAP = "EC_wallOverlap";
    public static final String VALUE_INTERPOLATION = "EC_interpolation";
    public static final String VALUE_JAR_DIR = "EC_jarDir";
    private static final String INTERPOLATION_LANCZOS2 = "lanczos2";
    private static final String INTERPOLATION_BILINEAR = "bilinear";
    private static final String INTERPOLATION_NEAREST_NEIGHBOUR = "nearest-neighbour";
    private int wallOverlap;
    private String interpolation;
    private String jarDir;
    private final int defaultWallOverlap = 0;
    private final String defaultInterpolation = INTERPOLATION_LANCZOS2;
    private String defaultJarDir = "";

    public ECSettings(String currentDirectory) {
        if (currentDirectory != null) {
            defaultJarDir = currentDirectory
                    + File.separator
                    + "components"
                    + File.separator
                    + JAR_FILENAME;
        }
        wallOverlap = defaultWallOverlap;
        interpolation = defaultInterpolation;
        jarDir = defaultJarDir;
    }

    public String[] getInterpolationNames() {
        return new String[]{INTERPOLATION_LANCZOS2, INTERPOLATION_BILINEAR, INTERPOLATION_NEAREST_NEIGHBOUR};
    }

    public void setWallOverlap(String value) throws IllegalArgumentException {
        if (value != null) {
            try {
                setWallOverlap(Integer.parseInt(value));
            } catch (Exception ex) {
                throw new IllegalArgumentException(Messages.EC_WALL_OVERLAP_ERROR);
            }
        }
    }

    public void setWallOverlap(int value) throws IllegalArgumentException {
        if (value >= 0) {
            wallOverlap = value;
        } else {
            throw new IllegalArgumentException(Messages.EC_WALL_OVERLAP_ERROR);
        }
    }

    public int getWallOverlap() {
        return wallOverlap;
    }

    public int getDefaultWallOverlap() {
        return defaultWallOverlap;
    }

    public boolean wallOverlapChanged() {
        return (wallOverlap != defaultWallOverlap);
    }

    public void setInterpolation(String value) throws IllegalArgumentException {
        if (value != null) {
            if (value.equals(INTERPOLATION_BILINEAR) || value.equals(INTERPOLATION_LANCZOS2) || value.equals(INTERPOLATION_NEAREST_NEIGHBOUR)) {
                interpolation = value;
            } else {
                throw new IllegalArgumentException(Messages.EC_INTERPOLATION_ERROR);
            }
        }
    }

    public String getInterpolation() {
        return interpolation;
    }

    public String getDefaultInterpolation() {
        return defaultInterpolation;
    }

    public boolean interpolationChanged() {
        return !(interpolation.equals(defaultInterpolation));
    }

    public void setJarDir(String value) throws IllegalArgumentException {
        if (value != null) {
            File tmp = new File(value);
            if (tmp.isFile() && value.endsWith(".jar")) {
                jarDir = value;
            } else {
                throw new IllegalArgumentException(Messages.EC_JAR_DIR_ERROR);
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
