package com.panozona.converter.settings;

/** 
 * @author Marek Standio
 */
public class AggregatedSettings {
    
    public GESettings ge;
    public OPTSettings opt;
    public ECSettings ec;
    public RESSettings res;
    public DZTSettings dzt;
    public ZYTSettings zyt;
    public String currentDirectory = "";
    
    public static final String FILE_PROPERTIES = "settings.properties"; // GAH

    private static AggregatedSettings instance;

    private AggregatedSettings() {
    }

    public void setCurrentDirectory(String value) {
        currentDirectory = value;
        ge = new GESettings(value);
        opt = new OPTSettings();
        ec = new ECSettings(value);
        res = new RESSettings(value);
        dzt = new DZTSettings(value);
        zyt = new ZYTSettings(value);
    }

    public static AggregatedSettings getInstance() {
        if (instance == null) {
            instance = new AggregatedSettings();
        }
        return instance;
    }
}
