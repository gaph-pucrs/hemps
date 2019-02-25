/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package source;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import util.MPSoCConfig;

/**
 *
 * @author mruaro
 */
public final class TaskMappingFrame extends javax.swing.JFrame implements WindowListener{

   
    private JPanel taskMapping;
    private MPSoCConfig mPSoCConfig;
    private MPSoCInformation mPSoCInformation;
    private boolean running;
    private HashMap<Integer, Color> colorsHash;
    private Random colorGenerator;
    private boolean firsExecution;
    
    public TaskMappingFrame(MPSoCConfig mPSoCConfig, MPSoCInformation mPSoCInformation, Image im) {
        this.setIconImage(im);
        this.showTaskMappingFrame(mPSoCConfig, mPSoCInformation);
    }
    
    public void setInformation(MPSoCConfig mPSoCConfig, MPSoCInformation mPSoCInformation){
        this.mPSoCConfig = mPSoCConfig;
        this.mPSoCInformation = mPSoCInformation;
    }
    
    public void showTaskMappingFrame(MPSoCConfig noCConfig, MPSoCInformation mPSoCInformation){
        this.mPSoCConfig = noCConfig;
        this.mPSoCInformation = mPSoCInformation;
        this.running = true;
        this.colorsHash = new HashMap<>();
        this.colorGenerator = new Random(20);
        this.addWindowListener(this);
        taskMapping = new JPanel(true);
        taskMapping.setBackground(Color.LIGHT_GRAY);
        taskMapping.setLayout(new GridLayout(mPSoCConfig.getY_dimension(), mPSoCConfig.getX_dimension(), 8, 8));
        initComponents();
        this.setTitle("Task Mapping Overview: "+mPSoCConfig.getTestcasePath());
        this.setVisible(true);
        this.setLocation(100,100);
        this.firsExecution = true;
        this.onlyRunningCheckBox.setSelected(true);
        this.updateButton.setSelected(true);
        realTime();
    }
    
    public void realTime(){
        new Thread() {
            @Override
            public void run() {
                while (running) {
                    try {
                        createTaskMapping();
                        sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TaskMappingFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }.start();
        
       
    }
    
    public void createTaskMapping() {

        try {

            taskMapping.removeAll();

            RouterNeighbors n = new RouterNeighbors(mPSoCConfig);

            //-----------TASK NAME IMPLEMENTATION -----------------
            TreeMap<Integer, String> taskNameHash = mPSoCConfig.getTaskNameHash();
            if (taskNameHash == null || taskNameHash.isEmpty()) {
                if (firsExecution) {
                    if (firsExecution) {
                        JOptionPane.showMessageDialog(this, "Task name representation not informed\nThe task ID will be represented by numbers", "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
            firsExecution = false;
            //-----------END TASK NAME IMPLEMENTATION -----------------

            for (int y = mPSoCConfig.getY_dimension() - 1; y >= 0; y--) {
                for (int x = 0; x < mPSoCConfig.getX_dimension(); x++) {
                    int ham_addr = n.xy_to_ham_addr((x << 8) | y);

                    JPanel panel = new JPanel(new GridBagLayout());
                    panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
                    panel.setBackground(Color.WHITE);
                    //panel.setPreferredSize(new Dimension(80, 100));

                    GridBagConstraints cons = new GridBagConstraints();
                    
                    cons.anchor = GridBagConstraints.CENTER;
                    int yCounter = 0;


                    Color foreground = Color.BLACK;

                    String peName = null;
                    int peType = mPSoCConfig.getPEType(ham_addr);
                    switch (peType) {
                        case MPSoCConfig.GLOBAL_MASTER:
                            peName = "Global M ";
                            break;
                        case MPSoCConfig.CLUSTER_MASTER:
                            peName = "Cluster M ";
                            break;
                        case MPSoCConfig.SLAVE:
                            peName = "Slave ";
                            break;
                    }

                    JLabel labelRouter = null;
                    if (mPSoCConfig.getRouterAddressing() == MPSoCConfig.HAMILTONIAN) {
                        labelRouter = new JLabel(peName + ham_addr);
                    } else {
                        labelRouter = new JLabel(peName + this.mPSoCConfig.HamAdressToXYLabel(ham_addr));
                    }

                    labelRouter.setFont(new Font("Verdana", 1, 12));
                    labelRouter.setForeground(foreground);
                    labelRouter.setHorizontalAlignment(SwingConstants.CENTER);
                    labelRouter.setVerticalAlignment(SwingConstants.CENTER);
                    labelRouter.setVerticalTextPosition(SwingConstants.TOP);

                    ArrayList<TaskInformation> taskInformationList = mPSoCInformation.getRouterInformation(ham_addr).getTasksInformation();

                    HashMap<Integer, String> taskStatusHash = new HashMap<>();

                    for (TaskInformation taskInformation : taskInformationList) {
                        switch (taskInformation.getService()) {
                            case "ALLOCATED":
                                taskStatusHash.put(taskInformation.getID(), "RUN");
                                break;
                            case "TERMINATED":
                                taskStatusHash.put(taskInformation.getID(), "TER");
                                break;
                        }
                    }
                    cons.fill = GridBagConstraints.NONE;
                    cons.weightx = 1;
                    cons.weighty = 1;
                    cons.insets = new Insets(1, 0, 1, 0);
                    cons.gridy = yCounter;
                    yCounter++;
                    panel.add(labelRouter, cons);
                    cons.weighty = 0;
                    cons.fill = GridBagConstraints.BOTH;

                    Set<Integer> keys = taskStatusHash.keySet();

                    for (Integer taskID : keys) {

                        if (onlyRunningCheckBox.isSelected() && taskStatusHash.get(taskID).equals("TER")) {
                            continue;
                        }
                        if (onlyTerminatedCheckBox.isSelected() && taskStatusHash.get(taskID).equals("RUN")) {
                            continue;
                        }

                        if (colorsHash.get(taskID >> 8) == null) {
                            Color c = new Color(colorGenerator.nextInt(0xFFFFFF));
                            colorsHash.put(taskID >> 8, c);
                        }

                        JPanel p = new JPanel(true);
                        p.setBackground(colorsHash.get(taskID >> 8));

                        JLabel data = new JLabel();
                        String taskName = (taskID >> 8) + "  " + (taskID & 0xFF);
                        if (taskNameHash != null && taskNameHash.get(taskID) != null) {
                            taskName = taskNameHash.get(taskID);
                        }
                        if (withoutTaskIDitem.isSelected())
                            data.setText(taskName +"  " + taskStatusHash.get(taskID));
                        else
                            data.setText(taskName + "  " +taskID+ "  " + taskStatusHash.get(taskID));
                        data.setFont(new Font("Verdana", 1, 12));
                        data.setForeground(Color.BLACK);
                        data.setHorizontalAlignment(SwingConstants.CENTER);
                        data.setVerticalTextPosition(SwingConstants.CENTER);

                        p.add(data);

                        cons.gridy = yCounter;
                        yCounter++;
                        panel.add(p, cons);
                    }

                    //scroll.add(panel);

                    taskMapping.add(panel);
                }

            }

            taskMapping.revalidate();
            jScrollPane1.revalidate();

        } catch (Exception ex) {
            return;
        }


    }
    

    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane(taskMapping);
        jPanel1 = new javax.swing.JPanel();
        allTasksCheckBox = new javax.swing.JCheckBox();
        onlyRunningCheckBox = new javax.swing.JCheckBox();
        onlyTerminatedCheckBox = new javax.swing.JCheckBox();
        updateButton = new javax.swing.JToggleButton();
        withoutTaskIDitem = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        jScrollPane1.setBackground(java.awt.Color.GRAY);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 200));
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(10);
        jScrollPane1.getHorizontalScrollBar().setUnitIncrement(10);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        allTasksCheckBox.setText("All tasks status");
        allTasksCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allTasksCheckBoxActionPerformed(evt);
            }
        });

        onlyRunningCheckBox.setText("Only running");
        onlyRunningCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onlyRunningCheckBoxActionPerformed(evt);
            }
        });

        onlyTerminatedCheckBox.setText("Only terminated");
        onlyTerminatedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onlyTerminatedCheckBoxActionPerformed(evt);
            }
        });

        updateButton.setText("Updating");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        withoutTaskIDitem.setText("Without Task ID");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(allTasksCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(onlyRunningCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(onlyTerminatedCheckBox)
                .addGap(34, 34, 34)
                .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(withoutTaskIDitem)
                .addContainerGap(205, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {allTasksCheckBox, onlyRunningCheckBox, onlyTerminatedCheckBox});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(onlyRunningCheckBox)
                    .addComponent(allTasksCheckBox)
                    .addComponent(onlyTerminatedCheckBox)
                    .addComponent(updateButton)
                    .addComponent(withoutTaskIDitem))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void onlyRunningCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onlyRunningCheckBoxActionPerformed
        allTasksCheckBox.setSelected(false);
        onlyTerminatedCheckBox.setSelected(false);
        createTaskMapping();
    }//GEN-LAST:event_onlyRunningCheckBoxActionPerformed

    private void allTasksCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allTasksCheckBoxActionPerformed
        onlyRunningCheckBox.setSelected(false);
        onlyTerminatedCheckBox.setSelected(false);
        createTaskMapping();
    }//GEN-LAST:event_allTasksCheckBoxActionPerformed

    private void onlyTerminatedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onlyTerminatedCheckBoxActionPerformed
        onlyRunningCheckBox.setSelected(false);
        allTasksCheckBox.setSelected(false);
        createTaskMapping();
    }//GEN-LAST:event_onlyTerminatedCheckBoxActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        if (updateButton.isSelected()){
            System.out.println("Selecionado");
            updateButton.setText("Updating");
            running = true;
            realTime();
        } else {
            running = false;
            updateButton.setText("Update");
            createTaskMapping();
        }
    }//GEN-LAST:event_updateButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox allTasksCheckBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox onlyRunningCheckBox;
    private javax.swing.JCheckBox onlyTerminatedCheckBox;
    private javax.swing.JToggleButton updateButton;
    private javax.swing.JCheckBox withoutTaskIDitem;
    // End of variables declaration//GEN-END:variables

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        this.running = false;
        this.setVisible(false);
    }

    @Override
    public void windowClosed(WindowEvent e) {
        this.running = false;
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
}
