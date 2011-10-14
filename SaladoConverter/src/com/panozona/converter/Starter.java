/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panozona.converter;

import com.panozona.converter.settings.AggregatedSettings;
import com.panozona.converter.settings.GESettings;
import com.panozona.converter.utils.CurrentDirectoryFinder;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/** 
 * @author Marek
 */
public class Starter {

    //hey change developmentMode value
    public final static boolean developmentMode = true;

    public static void main(String[] args) {

        Properties prop = new Properties();

        // TODO: DO SOMETHING WIT THIS MESS
        AggregatedSettings aggstngs = AggregatedSettings.getInstance();
        aggstngs.setCurrentDirectory((new CurrentDirectoryFinder()).currentDir);

        try {
            prop.load(new FileInputStream(aggstngs.currentDirectory + File.separator + AggregatedSettings.FILE_PROPERTIES));
            if (prop.getProperty(GESettings.VALUE_MEMORY_LIMIT) != null) {
                aggstngs.ge.setMemoryLimit(Integer.parseInt(prop.getProperty(GESettings.VALUE_MEMORY_LIMIT)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (developmentMode) {
            SaladoConverter.main(args);
        } else {
            try {                
                String pathToJar = SaladoConverter.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                ProcessBuilder pb = new ProcessBuilder("java", "-Xmx" + aggstngs.ge.getMemoryLimit() + "m", "-classpath", pathToJar, "com.panozona.converter.SaladoConverter");
                pb.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
