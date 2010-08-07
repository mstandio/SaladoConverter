package com.panozona.converter.table;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;


/**
 *
 * @author Marek
 */
public class TaskTableModel extends AbstractTableModel{

    public String[] columnNames = {"R","Status","Directory"};

    public ArrayList<TaskData> rows;    

    public TaskTableModel(ArrayList<TaskData> rows){
        super();
        this.rows = rows;
    }

    @Override
    public Class getColumnClass(int column){
       return getValueAt(0, column).getClass();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
         return columnNames.length;
    }

    @Override
     public String getColumnName(int col) {
      return columnNames[col];
    }

    @Override
    public TaskData getValueAt(int rowIndex, int columnIndex) {
        return rows.get(rowIndex);
    }

    public void addRow(TaskData newTaskData){
        rows.add(newTaskData);        
        fireTableDataChanged();
    }

    public void removeItem(TaskData taskData){
        rows.remove(taskData);        
        fireTableDataChanged();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return (col == 0);
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        rows.set(row, (TaskData)value);
        fireTableCellUpdated(row, col);
    }
}
