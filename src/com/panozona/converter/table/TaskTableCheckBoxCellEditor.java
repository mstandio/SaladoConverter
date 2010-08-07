package com.panozona.converter.table;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Marek
 */
public class TaskTableCheckBoxCellEditor extends AbstractCellEditor implements TableCellEditor,ActionListener {

    private TaskData taskData;
    private JCheckBox jcheckbox;

    @Override
    public Object getCellEditorValue() {        
        return taskData;
    }    

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        taskData = (TaskData)value;
                
        jcheckbox = new JCheckBox();
        jcheckbox.addActionListener(this);        
        jcheckbox.setSelected(taskData.checkBoxSelected);
        jcheckbox.setEnabled(taskData.checkBoxEnabled);
        jcheckbox.setHorizontalAlignment(SwingConstants.CENTER);
        jcheckbox.setVerticalAlignment(SwingConstants.CENTER);        
        return jcheckbox;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(taskData.checkBoxEnabled){
            taskData.checkBoxSelected = jcheckbox.isSelected();
        }
        jcheckbox.setSelected(taskData.checkBoxSelected);
        jcheckbox.setEnabled(taskData.checkBoxEnabled);
    }
}