package com.panozona.converter.table;

import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Marek
 */
public class TaskDataDirectoryCellRenderer extends DefaultTableCellRenderer{

    @Override
    public void setValue(Object value){               
        setText(" "+((TaskData)value).getTaskPaths()); // yeah i know
        
    }
}
