package com.panozona.converter.maintable;

import com.panozona.converter.task.TaskData;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Marek
 */
public class TaskTableModel extends AbstractTableModel {

    public String[] columnNames = {"R", "Status", "Cube size", "Tile size", "Directory"};
    public ArrayList<TaskData> rows;

    public TaskTableModel() {
        super();
        this.rows = new ArrayList<TaskData>();
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
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return rows.get(rowIndex);
            case 1:
                return rows.get(rowIndex).state;
            case 2:
                return rows.get(rowIndex).getCubeSizeDescription();
            case 3:
                return rows.get(rowIndex).getTileSizeDescription();
            case 4:
                return rows.get(rowIndex).getPathDescription();
            default:
                return null;
        }
    }

    public void addRow(TaskData newTaskData) {
        rows.add(newTaskData);
        fireTableDataChanged();
    }

    public void removeItem(TaskData taskData) {
        rows.remove(taskData);
        fireTableDataChanged();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return (col == 0);
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        rows.set(row, (TaskData) value);
        fireTableCellUpdated(row, col);
    }

    public boolean hasActiveTasks() {
        for (TaskData taskData : rows) {
            if (taskData.checkBoxEnabled && taskData.checkBoxSelected) {
                return true;
            }
        }
        return false;
    }   
}
