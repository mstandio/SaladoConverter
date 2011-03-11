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
public class TaskTableCheckBoxCellRenderer implements TableCellRenderer{

    private JCheckBox jcheckbox;

    public TaskTableCheckBoxCellRenderer(){
        jcheckbox = new JCheckBox();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        jcheckbox.setSelected(((TaskData)value).checkBoxSelected);
        jcheckbox.setEnabled(((TaskData)value).checkBoxEnabled);
        jcheckbox.setHorizontalAlignment(SwingConstants.CENTER);
        jcheckbox.setVerticalAlignment(SwingConstants.CENTER);
        return jcheckbox;
    }
}
