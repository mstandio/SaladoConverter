package com.panozona.converter.maintable;

import com.panozona.converter.task.TaskData;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Marek
 */
public class TaskTableCheckBoxCellRenderer extends JCheckBox implements TableCellRenderer{

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setSelected(((TaskData)value).checkBoxSelected);
        setEnabled(((TaskData)value).checkBoxEnabled);
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        return this;
    }
}
