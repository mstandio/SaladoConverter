package com.panozona.converter.utils;

import java.io.File;

/**
 *
 * @author Marek
 */
public class FileFilterDir extends javax.swing.filechooser.FileFilter{

    @Override
    public boolean accept(File f) {
        return (f.isDirectory());
    }

    @Override
    public String getDescription() {
        // hard-coded = ugly, should be done via I18N
        return "Directory";
    }
}