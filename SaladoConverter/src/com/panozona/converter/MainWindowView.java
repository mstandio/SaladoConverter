/*
 * MainWindowView.java
 */
package com.panozona.converter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.event.ListSelectionEvent;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import com.panozona.converter.settings.AggregatedSettings;
import com.panozona.converter.maintable.TaskTableCheckBoxCellEditor;
import com.panozona.converter.maintable.TaskTableCheckBoxCellRenderer;
import com.panozona.converter.maintable.TaskTableModel;
import com.panozona.converter.task.TaskData;
import com.panozona.converter.utils.CurrentDirectoryFinder;
import com.panozona.converter.utils.FileFilterAddTask;
import com.panozona.converter.utils.FileFilterDir;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import javax.swing.event.ListSelectionListener;

/**
 * The application's main frame.
 */
public class MainWindowView extends FrameView {
    
    public MainWindowView(SingleFrameApplication app) {
        super(app);
        initComponents();
        CurrentDirectoryFinder finder = new CurrentDirectoryFinder();
        
        aggstngs = AggregatedSettings.getInstance();
        aggstngs.setCurrentDirectory(finder.currentDir);
        
        taskTableModel = new TaskTableModel();
        jTableTasks.getTableHeader().setReorderingAllowed(false);
        jTableTasks.setModel(taskTableModel);
        
        TaskTableCheckBoxCellRenderer taskTableCheckBoxCellRenderer = new TaskTableCheckBoxCellRenderer();
        TaskTableCheckBoxCellEditor taskTableCheckBoxCellEditor = new TaskTableCheckBoxCellEditor();
        taskTableCheckBoxCellEditor.setMainWindowViewReference(this);
        
        jTableTasks.getColumn(taskTableModel.columnNames[0]).setCellRenderer(taskTableCheckBoxCellRenderer);
        jTableTasks.getColumn(taskTableModel.columnNames[0]).setCellEditor(taskTableCheckBoxCellEditor);
        jTableTasks.getColumn(taskTableModel.columnNames[0]).setMaxWidth(20);
        jTableTasks.getColumn(taskTableModel.columnNames[0]).setMinWidth(20);
        jTableTasks.getColumn(taskTableModel.columnNames[0]).setResizable(false);
        
        jTableTasks.getColumn(taskTableModel.columnNames[1]).setMaxWidth(60);
        jTableTasks.getColumn(taskTableModel.columnNames[1]).setMinWidth(60);        
        jTableTasks.getColumn(taskTableModel.columnNames[1]).setResizable(false);
        
        jTableTasks.getColumn(taskTableModel.columnNames[2]).setMaxWidth(100);
        jTableTasks.getColumn(taskTableModel.columnNames[2]).setMinWidth(100);
        jTableTasks.getColumn(taskTableModel.columnNames[2]).setResizable(false);
        
        jTableTasks.getColumn(taskTableModel.columnNames[3]).setMaxWidth(60);
        jTableTasks.getColumn(taskTableModel.columnNames[3]).setMinWidth(60);
        jTableTasks.getColumn(taskTableModel.columnNames[3]).setResizable(false);
        
        jTableTasks.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        
        jTableTasks.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (jTableTasks.getSelectionModel().isSelectionEmpty()) {
                    jButtonEdit.setEnabled(false);
                    jButtonRemoveTask.setEnabled(false);
                } else {
                    jButtonEdit.setEnabled(true);
                    jButtonRemoveTask.setEnabled(true);
                }
            }
        });
        
        jTableTasks.addKeyListener(new java.awt.event.KeyAdapter() {
            
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    evt.consume();
                    showTaskSettings();
                }
                if (evt.getKeyCode() == KeyEvent.VK_DELETE || evt.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    evt.consume();
                    jButtonRemoveTaskActionPerformed(null);
                }
            }
        });
        
        jButtonEdit.setEnabled(false);
        jButtonRemoveTask.setEnabled(false);
        jButtonClearTasks.setEnabled(false);
        jButtonRunTasks.setEnabled(false);
        
        controller = Controller.getInstance();
        controller.setTaskTableModel(taskTableModel);
        controller.setMainWindowView(this);
        controller.readSettingsFromFile();
        
        jComboBoxCommand.setModel(new DefaultComboBoxModel(aggstngs.ge.getCommandNames()));
        jComboBoxCommand.setSelectedItem(aggstngs.ge.getSelectedCommand());
        
        jTextFieldOutputDir.setText(aggstngs.ge.getOutputDir());
        
        this.getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getFrame().addWindowListener(new WindowListener() {
            
            @Override
            public void windowOpened(WindowEvent e) {
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
                controller.saveSettingsToFile();
                SaladoConverter.getApplication().exit();
            }
            
            @Override
            public void windowClosed(WindowEvent e) {
            }
            
            @Override
            public void windowIconified(WindowEvent e) {
            }
            
            @Override
            public void windowDeiconified(WindowEvent e) {
            }
            
            @Override
            public void windowActivated(WindowEvent e) {
            }
            
            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });
        
        redirectSystemStreams();
        logWindowView = new LogWindowView(this);
        logWindowView.setLocationRelativeTo(SaladoConverter.getApplication().getMainFrame());

        // set application icon
        //this.getFrame().setIconImage(Toolkit.getDefaultToolkit().getImage(MainWindowView.class.getResource("resources/icons/appicon.png")));

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                    InterfaceEnable();
                    logWindowView.setRunning(false);
                    logWindowView.setVisible(false);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
        controller.setTaskMonitor(taskMonitor);
    }
    
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = SaladoConverter.getApplication().getMainFrame();
            aboutBox = new AboutWindowView(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        SaladoConverter.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableTasks = new javax.swing.JTable();
        jComboBoxCommand = new javax.swing.JComboBox();
        jTextFieldOutputDir = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jButtonAddTask = new javax.swing.JButton();
        jButtonRemoveTask = new javax.swing.JButton();
        jButtonClearTasks = new javax.swing.JButton();
        jButtonSelectOutput = new javax.swing.JButton();
        jButtonRunTasks = new javax.swing.JButton();
        jButtonEdit = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        fileMenuLog = new javax.swing.JMenuItem();
        fileMenuSettings = new javax.swing.JMenuItem();
        fileMenuSeparator = new javax.swing.JSeparator();
        javax.swing.JMenuItem fileMenuExit = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        jMenuItemOnlineHelp = new javax.swing.JMenuItem();
        javax.swing.JMenuItem jMenuItemAbout = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jFileChooser = new javax.swing.JFileChooser();

        mainPanel.setName("mainPanel"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTableTasks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jTableTasks.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        jTableTasks.setName("jTableTasks"); // NOI18N
        jTableTasks.setRowHeight(20);
        jTableTasks.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jTableTasks);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.panozona.converter.SaladoConverter.class).getContext().getActionMap(MainWindowView.class, this);
        jComboBoxCommand.setAction(actionMap.get("chooseCommandActionPerformed")); // NOI18N
        jComboBoxCommand.setName("jComboBoxCommand"); // NOI18N
        jComboBoxCommand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxCommandActionPerformed(evt);
            }
        });

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.panozona.converter.SaladoConverter.class).getContext().getResourceMap(MainWindowView.class);
        jTextFieldOutputDir.setText(resourceMap.getString("jTextFieldOutputDir.text")); // NOI18N
        jTextFieldOutputDir.setName("jTextFieldOutputDir"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jButtonAddTask.setText(resourceMap.getString("jButtonAddTask.text")); // NOI18N
        jButtonAddTask.setName("jButtonAddTask"); // NOI18N
        jButtonAddTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddTaskActionPerformed(evt);
            }
        });

        jButtonRemoveTask.setText(resourceMap.getString("jButtonRemoveTask.text")); // NOI18N
        jButtonRemoveTask.setName("jButtonRemoveTask"); // NOI18N
        jButtonRemoveTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveTaskActionPerformed(evt);
            }
        });

        jButtonClearTasks.setText(resourceMap.getString("jButtonClearTasks.text")); // NOI18N
        jButtonClearTasks.setName("jButtonClearTasks"); // NOI18N
        jButtonClearTasks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearTasksActionPerformed(evt);
            }
        });

        jButtonSelectOutput.setText(resourceMap.getString("jButtonSelectOutput.text")); // NOI18N
        jButtonSelectOutput.setName("jButtonSelectOutput"); // NOI18N
        jButtonSelectOutput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectOutputActionPerformed(evt);
            }
        });

        jButtonRunTasks.setText(resourceMap.getString("jButtonRunTasks.text")); // NOI18N
        jButtonRunTasks.setName("jButtonRunTasks"); // NOI18N
        jButtonRunTasks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRunTasksActionPerformed(evt);
            }
        });

        jButtonEdit.setText(resourceMap.getString("jButtonEdit.text")); // NOI18N
        jButtonEdit.setName("jButtonEdit"); // NOI18N
        jButtonEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jButtonAddTask, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                        .addGap(12, 12, 12))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButtonSelectOutput, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButtonRunTasks, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButtonRemoveTask, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(12, 12, 12))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButtonClearTasks, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                        .addGap(12, 12, 12))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButtonEdit, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jButtonAddTask)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonRemoveTask)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonClearTasks)
                .addGap(18, 18, 18)
                .addComponent(jButtonEdit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 85, Short.MAX_VALUE)
                .addComponent(jButtonSelectOutput)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonRunTasks)
                .addContainerGap())
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
                    .addComponent(jComboBoxCommand, 0, 421, Short.MAX_VALUE)
                    .addComponent(jTextFieldOutputDir, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                        .addGap(13, 13, 13)
                        .addComponent(jTextFieldOutputDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBoxCommand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        fileMenuLog.setText(resourceMap.getString("fileMenuLog.text")); // NOI18N
        fileMenuLog.setName("fileMenuLog"); // NOI18N
        fileMenuLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileMenuLogActionPerformed(evt);
            }
        });
        fileMenu.add(fileMenuLog);

        fileMenuSettings.setText(resourceMap.getString("fileMenuSettings.text")); // NOI18N
        fileMenuSettings.setName("fileMenuSettings"); // NOI18N
        fileMenuSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileMenuSettingsActionPerformed(evt);
            }
        });
        fileMenu.add(fileMenuSettings);
        fileMenu.add(fileMenuSeparator);

        fileMenuExit.setText(resourceMap.getString("fileMenuExit.text")); // NOI18N
        fileMenuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileMenuExitActionPerformed(evt);
            }
        });
        fileMenu.add(fileMenuExit);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        jMenuItemOnlineHelp.setText(resourceMap.getString("jMenuItemOnlineHelp.text")); // NOI18N
        jMenuItemOnlineHelp.setName("jMenuItemOnlineHelp"); // NOI18N
        jMenuItemOnlineHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOnlineHelpActionPerformed(evt);
            }
        });
        helpMenu.add(jMenuItemOnlineHelp);

        jMenuItemAbout.setAction(actionMap.get("showAboutBox")); // NOI18N
        jMenuItemAbout.setName("jMenuItemAbout"); // NOI18N
        helpMenu.add(jMenuItemAbout);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 368, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        jFileChooser.setAcceptAllFileFilterUsed(false);
        jFileChooser.setDialogTitle(resourceMap.getString("jFileChooser.dialogTitle")); // NOI18N
        jFileChooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
        jFileChooser.setMultiSelectionEnabled(true);
        jFileChooser.setName("jFileChooser"); // NOI18N

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAddTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddTaskActionPerformed
        if (interfaceLocked) {
            return;
        }
        jFileChooser.setDialogTitle("Select files and/or directories");
        jFileChooser.resetChoosableFileFilters();
        jFileChooser.setSelectedFile(new File(""));
        jFileChooser.setFileFilter(new FileFilterAddTask());
        jFileChooser.setMultiSelectionEnabled(true);
        jFileChooser.setCurrentDirectory(new File(aggstngs.ge.getInputDir()));
        int returnVal = jFileChooser.showOpenDialog(this.getFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] files = jFileChooser.getSelectedFiles();
            controller.addTask(files);
            try {
                aggstngs.ge.setInputDir(jFileChooser.getSelectedFile().getParentFile().getAbsolutePath());
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this.getFrame(), ex.getMessage());
            }
        }
        jTableTasks.removeEditor();
        controller.applyCommand();        
        analyseTasks();
    }//GEN-LAST:event_jButtonAddTaskActionPerformed
    
    private void jButtonClearTasksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearTasksActionPerformed
        if (interfaceLocked) {
            return;
        }
        if (taskSettingsView != null) {
            taskSettingsView.dispose();
        }
        controller.clearTasks();
        analyseTasks();
        jButtonAddTask.requestFocusInWindow();
    }//GEN-LAST:event_jButtonClearTasksActionPerformed
    
    private void jButtonRemoveTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveTaskActionPerformed
        if (interfaceLocked) {
            return;
        }
        int[] selectedRows = jTableTasks.getSelectedRows();
        int beginIndex = -1;
        if (selectedRows.length > 0) {
            beginIndex = selectedRows[0];
        }
        TaskData[] tasksToRemove = new TaskData[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
            tasksToRemove[i] = (TaskData) taskTableModel.rows.get(jTableTasks.convertRowIndexToModel(selectedRows[i]));            
            if (taskSettingsView != null) {
                taskSettingsView.removeTaskData(tasksToRemove[i]);
            }
        }
        
        controller.removeTasks(tasksToRemove);
        analyseTasks();
        jButtonAddTask.requestFocusInWindow();
        
        if (beginIndex >= 0) {
            jTableTasks.getSelectionModel().setSelectionInterval(beginIndex, beginIndex);
            jTableTasks.requestFocusInWindow();
        }
        
    }//GEN-LAST:event_jButtonRemoveTaskActionPerformed
    
    private void fileMenuSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileMenuSettingsActionPerformed
        if (interfaceLocked) {
            return;
        }
        if (settingsWindowView == null) {
            JFrame mainFrame = SaladoConverter.getApplication().getMainFrame();
            settingsWindowView = new SettingsWindowView(taskTableModel);
            settingsWindowView.setLocationRelativeTo(mainFrame);
        }
        SaladoConverter.getApplication().show(settingsWindowView);
        settingsWindowView.displayAggregatedSettings();
    }//GEN-LAST:event_fileMenuSettingsActionPerformed
    
    private void fileMenuLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileMenuLogActionPerformed
        showLog();
    }//GEN-LAST:event_fileMenuLogActionPerformed
    
    private void showLog() {
        SaladoConverter.getApplication().show(logWindowView);
    }
    
    private void showTaskSettings() {
        if (interfaceLocked) {
            return;
        }
        if (jTableTasks.getSelectedRow() >= 0) {
            TaskData taskData = (TaskData) taskTableModel.rows.get(jTableTasks.convertRowIndexToModel(jTableTasks.getSelectedRow()));
            if (taskSettingsView == null) {
                taskSettingsView = new TaskSettingsView(taskTableModel);
                taskSettingsView.setLocationRelativeTo(SaladoConverter.getApplication().getMainFrame());
            }
            SaladoConverter.getApplication().show(taskSettingsView);
            taskSettingsView.displayTaskData(taskData);
        }
        
    }

    public void rfreshTaskSettings() {
        if (interfaceLocked || taskSettingsView == null) {
            return;
        } else {
            taskSettingsView.refreshTaskData();
        }
    }
    
    private void jComboBoxCommandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxCommandActionPerformed
        if (interfaceLocked) {
            return;
        }
        try {
            aggstngs.ge.setSelectedCommand(jComboBoxCommand.getSelectedItem().toString());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this.getFrame(), ex.getMessage());
        }
        jTableTasks.removeEditor();
        controller.applyCommand();
        analyseTasks();
    }//GEN-LAST:event_jComboBoxCommandActionPerformed
    
    private void jButtonSelectOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectOutputActionPerformed
        if (interfaceLocked) {
            return;
        }
        jFileChooser.resetChoosableFileFilters();
        jFileChooser.setSelectedFile(new File(""));
        jFileChooser.setFileFilter(new FileFilterDir());
        jFileChooser.setDialogTitle("Browse for output directory");
        jFileChooser.setMultiSelectionEnabled(false);
        jFileChooser.setCurrentDirectory(new File(aggstngs.ge.getOutputDir()));
        int returnVal = jFileChooser.showOpenDialog(this.getFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                jTextFieldOutputDir.setText(aggstngs.ge.getOutputDir());
                aggstngs.ge.setOutputDir(jFileChooser.getSelectedFile().getAbsolutePath());
                jTextFieldOutputDir.setText(aggstngs.ge.getOutputDir());
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this.getFrame(), ex.getMessage());
            }
        }
    }//GEN-LAST:event_jButtonSelectOutputActionPerformed
    
    private void fileMenuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileMenuExitActionPerformed
        controller.saveSettingsToFile();
        SaladoConverter.getApplication().exit();
    }//GEN-LAST:event_fileMenuExitActionPerformed
    
    private void jButtonRunTasksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRunTasksActionPerformed
        if (interfaceLocked) {
            return;
        }
        try {
            aggstngs.ge.setOutputDir(jTextFieldOutputDir.getText());
            InterfaceDisable();
            showLog();
            logWindowView.setRunning(true);
            controller.applyCommand();
            controller.generateOperations();
            controller.executeTasks();
        } catch (IllegalArgumentException ex) {
            showOptionPane(ex.getMessage());
        }
    }//GEN-LAST:event_jButtonRunTasksActionPerformed
    
    private void jMenuItemOnlineHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOnlineHelpActionPerformed
        String url = "http://panozona.com/saladoconverter";
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this.getFrame(), "Could not openurl: " + url);
        }
    }//GEN-LAST:event_jMenuItemOnlineHelpActionPerformed
    
    private void jButtonEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditActionPerformed
        showTaskSettings();
    }//GEN-LAST:event_jButtonEditActionPerformed
    
    public void cancelRunningTasks() {
        controller.cancelRunningTasks();
        if (logWindowView != null) {
            logWindowView.setRunning(false);
        }
        InterfaceEnable();
    }
    
    private void InterfaceDisable() {
        //TODO: disable table sorting
        interfaceLocked = true;
        if (settingsWindowView != null) {
            settingsWindowView.dispose();
        }
        if (taskSettingsView != null) {
            taskSettingsView.dispose();
        }
        jButtonAddTask.setEnabled(false);
        jButtonRemoveTask.setEnabled(false);
        jButtonClearTasks.setEnabled(false);
        jButtonEdit.setEnabled(false);
        jButtonSelectOutput.setEnabled(false);
        jButtonRunTasks.setEnabled(false);
        jTextFieldOutputDir.setEditable(false);
        jComboBoxCommand.setEditable(false);
        fileMenuSettings.setEnabled(false);
        jTableTasks.setEnabled(false);
    }
    
    private void InterfaceEnable() {
        // TODO: enable table sorting
        interfaceLocked = false;
        jButtonAddTask.setEnabled(true);
        jButtonRemoveTask.setEnabled(true);
        jButtonClearTasks.setEnabled(true);
        jButtonEdit.setEnabled(true);
        jButtonSelectOutput.setEnabled(true);
        jButtonRunTasks.setEnabled(true);
        jTextFieldOutputDir.setEditable(true);
        jComboBoxCommand.setEditable(true);
        fileMenuSettings.setEnabled(true);
        jTableTasks.setEnabled(true);
        analyseTasks();
    }
    
    public void analyseTasks() {
        jTableTasks.getColumn(taskTableModel.columnNames[0]).getCellEditor().stopCellEditing();
        if (taskTableModel.getRowCount() > 0) {
            jButtonClearTasks.setEnabled(true);
        } else {
            jButtonClearTasks.setEnabled(false);
        }
        
        if (taskTableModel.hasActiveTasks()) {
            jButtonRunTasks.setEnabled(true);
        } else {
            jButtonRunTasks.setEnabled(false);
        }
    }
    
    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                if (logWindowView != null) {
                    logWindowView.append(text);
                }
            }
        });
    }
    
    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            
            @Override
            public void write(int b) throws IOException {
                updateTextArea(String.valueOf((char) b));
            }
            
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                updateTextArea(new String(b, off, len));
            }
            
            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }
    
    public void showOptionPane(String message) {
        JOptionPane.showMessageDialog(this.getFrame(), message);
        
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem fileMenuLog;
    private javax.swing.JSeparator fileMenuSeparator;
    private javax.swing.JMenuItem fileMenuSettings;
    private javax.swing.JButton jButtonAddTask;
    private javax.swing.JButton jButtonClearTasks;
    private javax.swing.JButton jButtonEdit;
    private javax.swing.JButton jButtonRemoveTask;
    private javax.swing.JButton jButtonRunTasks;
    private javax.swing.JButton jButtonSelectOutput;
    private javax.swing.JComboBox jComboBoxCommand;
    private javax.swing.JFileChooser jFileChooser;
    private javax.swing.JMenuItem jMenuItemOnlineHelp;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableTasks;
    private javax.swing.JTextField jTextFieldOutputDir;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
    private SettingsWindowView settingsWindowView;
    private LogWindowView logWindowView;
    private TaskSettingsView taskSettingsView;
    private TaskTableModel taskTableModel;
    private Controller controller;
    private AggregatedSettings aggstngs;
    private boolean interfaceLocked;
}
