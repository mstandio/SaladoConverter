package com.panozona.converter.utils;

import java.io.File;
import com.panozona.converter.Starter;

/**
 *
 * @author Marek
 */
public class CurrentDirectoryFinder {
    
    public String currentDir;   
    
    public CurrentDirectoryFinder() {
        if (Starter.developmentMode)
            currentDir = System.getProperty("user.dir")+File.separator+"external";
        else{
            currentDir = System.getProperty("user.dir");
        }
    }
}