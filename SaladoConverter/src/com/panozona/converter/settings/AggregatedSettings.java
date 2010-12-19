
package com.panozona.converter.settings;

/**
 *
 * @author Marek Standio
 */
public class AggregatedSettings {

    public static final String FILE_PROPERTIES = "settings.properties";
    public static final String FOLDER_OUTPUT = "output";
    public static final String FOLDER_INPUT = "input";
    public static final String FOLDER_TMP = "tmp";
    public static final String FOLDER_COMPONENTS = "components";
    public RESSettings res;
    public DZTSettings dzt;
    public ECSettings ec;
    public GESettings ge;
    private String currentDirectory = "";
    
    public AggregatedSettings(String currentDirectory) {
        this.currentDirectory = currentDirectory;
        res = new RESSettings(currentDirectory);
        dzt = new DZTSettings(currentDirectory);
        ec = new ECSettings(currentDirectory);
        ge = new GESettings(currentDirectory);
    }

    public String getCurrentDirectory(){
        return currentDirectory;
    }
}