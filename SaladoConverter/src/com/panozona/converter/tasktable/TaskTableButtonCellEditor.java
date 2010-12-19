package com.panozona.converter.tasktable;

import com.panozona.converter.task.TaskData;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Marek
 */
public class TaskTableButtonCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

    private TaskData taskData;
    private JButton jbutton;
    protected static final String EDIT = "edit";

    public TaskTableButtonCellEditor(){
        jbutton = new JButton("...");
        jbutton.setActionCommand(EDIT);
        jbutton.addActionListener(this);        
    }


    @Override
    public Object getCellEditorValue() {    
        return taskData;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        taskData = (TaskData)value;        
        return jbutton;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (EDIT.equals(e.getActionCommand())) {            
        }        
        fireEditingStopped();
    }
}
