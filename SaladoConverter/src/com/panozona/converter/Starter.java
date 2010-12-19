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
 *
 * @author Marek
 */
public class Starter {

    //hey you change developmentMode value

    public final static boolean developmentMode = true;

    public static void main(String[] args) throws Exception {

        Properties prop = new Properties();

        // TODO: DO SOMETHING WIT THIS MESS
        AggregatedSettings aggstngs = new AggregatedSettings((new CurrentDirectoryFinder()).currentDir);
        prop.load(new FileInputStream(aggstngs.getCurrentDirectory() + File.separator + AggregatedSettings.FILE_PROPERTIES));
        aggstngs.ge.setMemoryLimit(prop.getProperty(GESettings.VALUE_MEM_LIMIT),"on strat: corrupted file");

        if (developmentMode) {
            SaladoConverter.main(args);
        } else {
            String pathToJar = SaladoConverter.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            ProcessBuilder pb = new ProcessBuilder("java", "-Xmx" + aggstngs.ge.getMemoryLimit() + "m", "-classpath", pathToJar, "com.panozona.converter.SaladoConverter");
            pb.start();
        }
    }
}