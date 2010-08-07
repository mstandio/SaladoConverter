package com.panozona.converter.settings;

import com.panozona.converter.utils.Info;
import com.panozona.converter.utils.InfoException;
import java.io.File;

/**
 *
 * @author Marek Standio
 */
public class ECSettings {

    public static final String JAR_CLASSNAME = "EquirectangularToCubic";
    public static final String JAR_FILENAME = "EquirectangularToCubic.jar";
    public static final String VALUE_OVERLAP = "EC_overlap";
    public static final String VALUE_INTERPOLATION = "EC_interp";
    public static final String VALUE_QUALITY = "EC_quality";
    public static final String VALUE_JAR_DIR = "EC_jarDir";
    private final String INTERP_LANCZOS2 = "lanczos2";
    private final String INTERP_BILINEAR = "bilinear";
    private final String INTERP_NEAREST = "nearest-neighbour";
    private Integer overlap;
    private String interpolation;
    private Float quality;
    private String jarDir;
    private final Integer defaultOverlap = 1;
    private final String defaultInterpolation = INTERP_LANCZOS2;
    private final Float defaultQuality = 0.8f;
    private String defaultJarDir ="";

    public ECSettings(String currentDirectory) {
        defaultJarDir = currentDirectory +
                File.separator +
                AggregatedSettings.FOLDER_COMPONENTS +
                File.separator +
                JAR_FILENAME;
        overlap = defaultOverlap;
        interpolation = defaultInterpolation;
        quality = defaultQuality;
        jarDir = defaultJarDir;
    }

    public String[] getInterpolationNames() {
        return new String[]{INTERP_LANCZOS2, INTERP_BILINEAR, INTERP_NEAREST};
    }

    public void setOverlap(String value) throws InfoException {
        if (value != null) {
            try {
                overlap = Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                throw new InfoException(Info.EC_OVERLAP_ERROR);
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

    public void setInterpolation(String value) throws InfoException {
        if (value != null) {
            if (value.equals(INTERP_BILINEAR) || value.equals(INTERP_LANCZOS2) || value.equals(INTERP_NEAREST)) {
                interpolation = value;
            } else {
                throw new InfoException(Info.EC_INTERPOLATION_ERROR);
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

    public void setQuality(String value) throws InfoException {
        if (value != null) {
            try {
                if (Float.parseFloat(value) > 0 && Float.parseFloat(value) <= 1) {
                    quality = Float.parseFloat(value);
                } else {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                throw new InfoException(Info.EC_QUALITY_ERROR);
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
                throw new InfoException(Info.EC_JAR_DIR_ERROR);
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
