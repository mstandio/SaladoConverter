package com.panozona.converter.utils;

import java.io.File;

/**
 *
 * @author Marek
 */
public class FileFilterJar extends javax.swing.filechooser.FileFilter{

    @Override
    public boolean accept(File f) {
        return (f.isFile() && f.getName().endsWith(".jar"));
    }

    @Override
    public String getDescription() {
        // hard-coded = ugly, should be done via I18N
        return "*.jar";
    }
}