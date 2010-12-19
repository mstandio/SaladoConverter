package com.panozona.converter.settings;
import com.panozona.converter.utils.InfoException;
import java.io.File;

/**
 *
 * @author Marek Standio
 */
public class ECSettings {

    public static final String JAR_CLASSNAME = "equirectangulartocubic.EquirectangularToCubic";
    public static final String JAR_FILENAME = "EquirectangularToCubic.jar";
    public static final String VALUE_OVERLAP = "EC_overlap";
    public static final String VALUE_INTERPOLATION = "EC_interp";
    public static final String VALUE_JAR_DIR = "EC_jarDir";
    private static final String INTERP_LANCZOS2 = "lanczos2";
    private static final String INTERP_BILINEAR = "bilinear";
    private static final String INTERP_NEAREST = "nearest-neighbour";
    private int overlap;
    private String interpolation;
    private String jarDir;
    private final int defaultOverlap = 1;
    private final String defaultInterpolation = INTERP_LANCZOS2;
    private String defaultJarDir = "";

    public ECSettings(String currentDirectory) {
        if (currentDirectory != null) {
            defaultJarDir = currentDirectory
                    + File.separator
                    + AggregatedSettings.FOLDER_COMPONENTS
                    + File.separator
                    + JAR_FILENAME;
        }
        overlap = defaultOverlap;
        interpolation = defaultInterpolation;
        jarDir = defaultJarDir;
    }

    public String[] getInterpolationNames() {
        return new String[]{INTERP_LANCZOS2, INTERP_BILINEAR, INTERP_NEAREST};
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

    public void setInterpolation(String value, String errorMsg) throws InfoException {
        if (value != null) {
            if (value.equals(INTERP_BILINEAR) || value.equals(INTERP_LANCZOS2) || value.equals(INTERP_NEAREST)) {
                interpolation = value;
            } else {
                throw new InfoException(errorMsg);
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
