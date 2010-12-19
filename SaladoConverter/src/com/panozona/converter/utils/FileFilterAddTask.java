package com.panozona.converter.utils;

import java.io.File;

/**
 *
 * @author Marek Standio
 */
public class FileFilterAddTask extends javax.swing.filechooser.FileFilter implements java.io.FileFilter {

    @Override
    public boolean accept(File f) {
        return (f.isDirectory() || (f.isFile()
                && (f.getName().toLowerCase()).endsWith(".jpg")
                || (f.getName().toLowerCase()).endsWith(".jpeg")
                || (f.getName().toLowerCase()).endsWith(".png")
                || (f.getName().toLowerCase()).endsWith(".bmp")
                || (f.getName().toLowerCase()).endsWith(".gif")
                || (f.getName().toLowerCase()).endsWith(".tif")
                || (f.getName().toLowerCase()).endsWith(".tiff")));
    }

    @Override
    public String getDescription() {
        // hard-coded = ugly, should be done via I18N
        return "Direcrories or image files";
    }
}
