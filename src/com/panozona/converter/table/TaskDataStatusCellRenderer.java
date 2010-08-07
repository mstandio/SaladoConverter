package com.panozona.converter.table;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Marek
 */
public class TaskDataStatusCellRenderer extends DefaultTableCellRenderer{

    @Override
    public void setValue(Object value){
        setHorizontalAlignment(JLabel.CENTER);
        setText(((TaskData)value).taskState);
    }
}