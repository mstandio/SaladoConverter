package com.panozona.converter.settings;

import com.panozona.converter.utils.InfoException;
import java.io.File;

/**
 *
 * @author Marek Standio
 */
public class RESSettings {

    public static final String JAR_CLASSNAME = "resizer.Resizer";
    public static final String JAR_FILENAME = "Resizer.jar";    
    public static final String VALUE_JAR_DIR = "RES_jarDir";
    
    private String jarDir;    
    private String defaultJarDir = "";

    public RESSettings(String currentDirectory) {
        if (currentDirectory != null) {
            defaultJarDir = currentDirectory
                    + File.separator
                    + AggregatedSettings.FOLDER_COMPONENTS
                    + File.separator
                    + JAR_FILENAME;
        }        
        jarDir = defaultJarDir;
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