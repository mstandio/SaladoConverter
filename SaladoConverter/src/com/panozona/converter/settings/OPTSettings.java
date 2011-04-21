package com.panozona.converter.settings;

public class OPTSettings {

    public static final String VALUE_ENABLED = "OPT_enabled";

    private boolean optEnabled;
    private boolean optEnabledDefault = true;

    public OPTSettings(){
        optEnabled = optEnabledDefault;
    }

    public void setOptEnabled(boolean value) {
        optEnabled = value;
    }

    public void setOptEnabled(String value) {
        if(value != null){
            optEnabled = value.equals("true");
        }
    }

    public boolean getOptEnabled() {
        return optEnabled;
    }

    public boolean getDefaultOptEnabled() {
        return optEnabledDefault;
    }

    public boolean optEnabledChanged() {
        return (optEnabled != optEnabledDefault);
    }
}