/*
 * TaskSettingsView.java
 *
 * Created on 2011-04-17, 21:41:19
 */
package com.panozona.converter;

import com.panozona.converter.maintable.TaskTableModel;
import com.panozona.converter.settings.AggregatedSettings;
import com.panozona.converter.task.TaskData;
import javax.swing.JOptionPane;

/**
 * @author Marek
 */
public class TaskSettingsView extends javax.swing.JFrame {

    private Controller controller;
    private TaskTableModel taskTableModel;
    private TaskData currentTaskData;
    private boolean allowCloseFlag;

    /** Creates new form TaskSettingsView */
    public TaskSettingsView(TaskTableModel taskTableModel) {
        initComponents();
        setTitle("Edit task");
        this.taskTableModel = taskTableModel;
        controller = Controller.getInstance();
        allowCloseFlag = true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupCubeTileSize = new javax.swing.ButtonGroup();
        jButtonTaskOK = new javax.swing.JButton();
        jButtonTaskCancel = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jRadioButtonAutosize = new javax.swing.JRadioButton();
        jRadioButtonCustom = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldCubeSize = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldTileSize = new javax.swing.JTextField();
        jLabelTileDefaultCubeSize = new javax.swing.JLabel();
        jLabelTileDefaultTileSize = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setName("Form"); // NOI18N
        setResizable(false);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.panozona.converter.SaladoConverter.class).getContext().getResourceMap(TaskSettingsView.class);
        jButtonTaskOK.setText(resourceMap.getString("jButtonTaskOK.text")); // NOI18N
        jButtonTaskOK.setName("jButtonTaskOK"); // NOI18N
        jButtonTaskOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTaskOKActionPerformed(evt);
            }
        });

        jButtonTaskCancel.setText(resourceMap.getString("jButtonTaskCancel.text")); // NOI18N
        jButtonTaskCancel.setName("jButtonTaskCancel"); // NOI18N
        jButtonTaskCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTaskCancelActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Cube and tile size"));
        jPanel1.setName("jPanel1"); // NOI18N

        buttonGroupCubeTileSize.add(jRadioButtonAutosize);
        jRadioButtonAutosize.setText(resourceMap.getString("jRadioButtonAutosize.text")); // NOI18N
        jRadioButtonAutosize.setName("jRadioButtonAutosize"); // NOI18N
        jRadioButtonAutosize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonAutosizeActionPerformed(evt);
            }
        });

        buttonGroupCubeTileSize.add(jRadioButtonCustom);
        jRadioButtonCustom.setText(resourceMap.getString("jRadioButtonCustom.text")); // NOI18N
        jRadioButtonCustom.setName("jRadioButtonCustom"); // NOI18N
        jRadioButtonCustom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonCustomActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioButtonCustom)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButtonAutosize)
                .addContainerGap(77, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonCustom)
                    .addComponent(jRadioButtonAutosize, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setName("jPanel2"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jTextFieldCubeSize.setName("jTextFieldCubeSize"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jTextFieldTileSize.setName("jTextFieldTileSize"); // NOI18N

        jLabelTileDefaultCubeSize.setText(resourceMap.getString("jLabelTileDefaultCubeSize.text")); // NOI18N
        jLabelTileDefaultCubeSize.setName("jLabelTileDefaultCubeSize"); // NOI18N

        jLabelTileDefaultTileSize.setText(resourceMap.getString("jLabelTileDefaultTileSize.text")); // NOI18N
        jLabelTileDefaultTileSize.setName("jLabelTileDefaultTileSize"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabelTileDefaultTileSize)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabelTileDefaultCubeSize)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldTileSize, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                    .addComponent(jTextFieldCubeSize, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldCubeSize, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jLabelTileDefaultCubeSize)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldTileSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabelTileDefaultTileSize)
                    .addComponent(jLabel2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButtonTaskOK, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonTaskCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonTaskCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonTaskOK, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonTaskOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTaskOKActionPerformed
        collectAllData();
        if (allowCloseFlag) {
            this.dispose();            
            taskTableModel.fireTableDataChanged();
            controller.applyCommand();
        }
        allowCloseFlag = true;
}//GEN-LAST:event_jButtonTaskOKActionPerformed

    private void jButtonTaskCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTaskCancelActionPerformed
        this.dispose();
}//GEN-LAST:event_jButtonTaskCancelActionPerformed

    private void jRadioButtonCustomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonCustomActionPerformed
        jTextFieldCubeSize.setEditable(jRadioButtonCustom.isSelected());
        jTextFieldTileSize.setEditable(jRadioButtonCustom.isSelected());
        if (jRadioButtonAutosize.isSelected()) {
            Optimizer.optimize(currentTaskData);
            displayTaskData(currentTaskData);
        }
}//GEN-LAST:event_jRadioButtonCustomActionPerformed

    private void jRadioButtonAutosizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonAutosizeActionPerformed
        jTextFieldCubeSize.setEditable(jRadioButtonCustom.isSelected());
        jTextFieldTileSize.setEditable(jRadioButtonCustom.isSelected());
        if (jRadioButtonAutosize.isSelected()) {
            Optimizer.optimize(currentTaskData);
            displayTaskData(currentTaskData);
        }
}//GEN-LAST:event_jRadioButtonAutosizeActionPerformed

    public void displayTaskData(TaskData taskData) {
        currentTaskData = taskData;

        jLabelTileDefaultCubeSize.setText(Integer.toString(currentTaskData.getPanorama().getCubeSize()));
        jLabelTileDefaultTileSize.setText(Integer.toString(AggregatedSettings.getInstance().dzt.getTileSize()));

        jTextFieldCubeSize.setText(Integer.toString(currentTaskData.getNewCubeSize()));
        jTextFieldTileSize.setText(Integer.toString(currentTaskData.getNewTileSize()));

        if (currentTaskData.autosize) {
            jRadioButtonAutosize.setSelected(true);
        } else {
            jRadioButtonCustom.setSelected(false);
        }
        jTextFieldCubeSize.setEditable(jRadioButtonCustom.isSelected());
        jTextFieldTileSize.setEditable(jRadioButtonCustom.isSelected());
    }

    private void collectAllData() {
        try {
            currentTaskData.autosize = jRadioButtonAutosize.isSelected();
            if (!currentTaskData.autosize) {
                currentTaskData.setNewCubeSize(jTextFieldCubeSize.getText());
                currentTaskData.setNewTileSize(jTextFieldTileSize.getText());
            }
        } catch (IllegalArgumentException ex) {
            showOptionPane(ex.getMessage());
        }
    }

    private void showOptionPane(String message) {
        JOptionPane.showMessageDialog(this, message);
        allowCloseFlag = false;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupCubeTileSize;
    private javax.swing.JButton jButtonTaskCancel;
    private javax.swing.JButton jButtonTaskOK;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabelTileDefaultCubeSize;
    private javax.swing.JLabel jLabelTileDefaultTileSize;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButtonAutosize;
    private javax.swing.JRadioButton jRadioButtonCustom;
    private javax.swing.JTextField jTextFieldCubeSize;
    private javax.swing.JTextField jTextFieldTileSize;
    // End of variables declaration//GEN-END:variables
}
