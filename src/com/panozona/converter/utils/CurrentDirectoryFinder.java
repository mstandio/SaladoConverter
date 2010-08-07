package com.panozona.converter.utils;

import java.io.File;


/**
 *
 * @author Marek
 */
public class CurrentDirectoryFinder {
    
    public String currentDir;   
    
    public CurrentDirectoryFinder() {

        hey check this out before compiling

        currentDir = System.getProperty("user.dir"); // release

        //currentDir = System.getProperty("user.dir")+File.separator+"external"; // development
        
        // second version uses "external" folder in project directory

    }
}