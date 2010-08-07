package com.panozona.converter.utils;

import java.io.File;

/**
 *
 * @author Marek
 */
public class FileFilterAddTask extends javax.swing.filechooser.FileFilter implements java.io.FileFilter{

    @Override
    public boolean accept(File f) {
        return (f.isDirectory() || (f.isFile() && (
                (f.getName()).endsWith(".jpg") ||
                (f.getName()).endsWith(".jpeg") || 
                (f.getName()).endsWith(".JPG") || 
                (f.getName()).endsWith(".JPEG") )));
    }

    @Override
    public String getDescription() {
        // hard-coded = ugly, should be done via I18N
        return "Direcrories or *.jpg files";
    }
}
