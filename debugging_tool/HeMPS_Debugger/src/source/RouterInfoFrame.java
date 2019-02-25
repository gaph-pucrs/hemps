/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package source;

import componentes.RouterInfoTableCellRender;
import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.table.DefaultTableModel;
import util.MPSoCConfig;

/**
 *
 * @author mruaro
 */
public final class RouterInfoFrame extends javax.swing.JFrame {

    private int router_address;
    private RouterInformation routerInfo;
    private int services[];
    private MPSoCConfig mPSoCConfig;
    private Image im;

    public RouterInfoFrame(RouterInformation routerInfo, MPSoCConfig mPSoCConfig, Image im) {
        this.setIconImage(im);
        this.im = im;
        this.routerInfo = routerInfo;
        this.mPSoCConfig = mPSoCConfig;
        this.router_address = routerInfo.getRouter_address();
        this.services = new int[0];
        if (this.mPSoCConfig.getRouterAddressing() == MPSoCConfig.HAMILTONIAN)
            this.setTitle("Router " + router_address + " Information");
        else
            this.setTitle("Router " + this.mPSoCConfig.HamAdressToXYLabel(this.router_address) + " Information");
        this.setLocation((int) (1 + Math.random() * 768), (int) (1 + Math.random() * 200));
        this.setVisible(true);
        initComponents();
        taskTable.setDefaultRenderer(Object.class, new RouterInfoTableCellRender());
        initServiceComboBox();
        updateRouterInfoTable();
        servicesAddedCombo.addItem("Show all");
        updateTaskTable();
    }

    public void initServiceComboBox() {

        servicesToAddCombo.addItem("None");
        
        TreeMap<Integer, String> servicesHash = mPSoCConfig.getServicesHash();
        
        Set<Integer> keys = servicesHash.keySet();
                
        for (Integer service : keys) {
            servicesToAddCombo.addItem(servicesHash.get(service));
        }
        
    }
    
    private void updateTaskTable(){
        
        String taskName;
        String task;
        String service;
        String time;
        
        TreeMap<Integer, String> taskNameHash = mPSoCConfig.getTaskNameHash();
        
        DefaultTableModel model = (DefaultTableModel) taskTable.getModel();

        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
        
        ArrayList<TaskInformation> taskInfoList = routerInfo.getTasksInformation();
       
        String tableServiceName;
        boolean running;
        for (int i = 0; i < taskInfoList.size(); i++) {

            TaskInformation taskInfo = taskInfoList.get(i);

            if (taskInfo.getService().equals("ALLOCATED")) {
                
                tableServiceName = "*";
                running = true;

                if (taskNameHash != null && taskNameHash.get(taskInfo.getID()) != null) {
                    
                    taskName = taskNameHash.get(taskInfo.getID());
                    
                    for (int j = 0; j < taskInfoList.size(); j++) {
                        
                       TaskInformation taskInfo2 = taskInfoList.get(j);
                       
                       if (taskInfo2.getID() == taskInfo.getID() && taskInfo2.getService().equals("TERMINATED")){
                           running = false;
                           tableServiceName = "";
                           break;
                       }
                        
                    }
                } else {
                    taskName = Integer.toString(taskInfo.getID());
                }

                task = Integer.toString(taskInfo.getAppId() << 8 | taskInfo.getTaskId());
                service = taskInfo.getService() + tableServiceName;
                time = Integer.toString(taskInfo.getTime());

                model.addRow(new String[]{taskName, task, service, time});
            }
        }
        
        for (int i = 0; i < taskInfoList.size(); i++) {

            TaskInformation taskInfo = taskInfoList.get(i);

            if (taskInfo.getService().equals("TERMINATED")) {

                if (taskNameHash != null && taskNameHash.get(taskInfo.getID()) != null) {
                    taskName = taskNameHash.get(taskInfo.getID());
                } else {
                    taskName = Integer.toString(taskInfo.getID());
                }

                task = Integer.toString(taskInfo.getAppId() << 8 | taskInfo.getTaskId());
                service = taskInfo.getService();
                time = Integer.toString(taskInfo.getTime());

                model.addRow(new String[]{taskName, task, service, time});
            }
        }
    }

    private void updateRouterInfoTable() {

        String port;
        String totalVolume;
        String serviceVolume;
        String percent;

        int total_router_volume = 0;
        int total_service_volume = 0;

        int total_volume = 0;
        int service_volume = 0;

        DefaultTableModel model = (DefaultTableModel) jTableRouterInfo.getModel();

        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }

        for (int i = 0; i < MPSoCConfig.NPORT; i++) {

            port = MPSoCConfig.getPortString(i);
            total_volume = routerInfo.getPortTotalVolumeInFlits(i);
            totalVolume = Integer.toString(total_volume).concat(" flits");
            service_volume = 0;
            if (services.length > 0) {
                service_volume = routerInfo.getPortVolumeInFlit(i, services);
            }
            serviceVolume = Integer.toString(service_volume).concat(" flits");
            if (total_volume != 0) {
                percent = Integer.toString((service_volume * 100) / total_volume);
            } else {
                percent = "0";
            }
            percent += "%";
            total_router_volume += total_volume;
            total_service_volume += service_volume;

            model.addRow(new String[]{port, totalVolume, serviceVolume, percent});
        }

        port = "TOTAL";
        totalVolume = Integer.toString(total_router_volume).concat(" flits");
        serviceVolume = Integer.toString(total_service_volume).concat(" flits");
        if (total_router_volume != 0) {
            percent = Integer.toString((total_service_volume * 100) / total_router_volume);
        } else {
            percent = "0";
        }
        percent += "%";

        model.addRow(new String[]{port, totalVolume, serviceVolume, percent});

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        int router_addr = router_address;
        if (mPSoCConfig.getRouterAddressing() == MPSoCConfig.XY){
            router_addr = mPSoCConfig.ham_to_xy_addr(router_address);
        }
        schedulingPanel = new SchedulingTab(router_addr, mPSoCConfig);
        PELogPanel = new PETextLog(router_addr, mPSoCConfig);
        jScrollPane1 = new javax.swing.JScrollPane();
        taskTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jScrollPaneRouterInfo = new javax.swing.JScrollPane();
        jTableRouterInfo = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        servicesToAddCombo = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        AddButton = new javax.swing.JButton();
        RemoveButton = new javax.swing.JButton();
        servicesAddedCombo = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAutoRequestFocus(false);
        setMinimumSize(new java.awt.Dimension(440, 365));
        setResizable(true);

        taskTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Task Name", "Task", "Service", "Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        taskTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                taskTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(taskTable);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
        );
        
        
        jTabbedPane1.addTab("Log", PELogPanel);

        jTabbedPane1.addTab("Applications", jPanel3);

        jTableRouterInfo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Input Port", "Total Volume", "Service Volume", "Percentual"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        
        
        jTabbedPane1.addTab("Scheduling", schedulingPanel);
         
        jScrollPaneRouterInfo.setViewportView(jTableRouterInfo);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Service Filter"));
        jPanel1.setToolTipText("");

        jLabel1.setText("Services to add");

        AddButton.setText("Add >>");
        AddButton.setMaximumSize(new java.awt.Dimension(86, 29));
        AddButton.setMinimumSize(new java.awt.Dimension(86, 29));
        AddButton.setPreferredSize(new java.awt.Dimension(86, 29));
        AddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddButtonActionPerformed(evt);
            }
        });

        RemoveButton.setText("Remove <<");
        RemoveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RemoveButtonActionPerformed(evt);
            }
        });

        servicesAddedCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                servicesAddedComboActionPerformed(evt);
            }
        });

        jLabel2.setText("Added services");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(servicesToAddCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(RemoveButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(AddButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(servicesAddedCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(AddButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(RemoveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(servicesAddedCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(servicesToAddCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(31, 31, 31))))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPaneRouterInfo)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPaneRouterInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(63, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Traffic", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void servicesAddedComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_servicesAddedComboActionPerformed
        if (servicesAddedCombo.getSelectedIndex() != 0){
            services = new int[]{mPSoCConfig.getServiceValue(servicesAddedCombo.getSelectedItem().toString())};
            
        } else {
            services = new int[servicesAddedCombo.getItemCount()-1];
            
            for (int i = 0; i < services.length; i++) {
                services[i] = mPSoCConfig.getServiceValue(servicesAddedCombo.getItemAt(i+1).toString());
            }
        }
        updateRouterInfoTable();
    }//GEN-LAST:event_servicesAddedComboActionPerformed

    private void AddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddButtonActionPerformed
        if (servicesToAddCombo.getSelectedIndex() != 0) {

            String toAddService = servicesToAddCombo.getSelectedItem().toString();

            for (int i = 0; i < servicesAddedCombo.getItemCount(); i++) {
                if (toAddService.equals(servicesAddedCombo.getItemAt(i))) {
                    toAddService = null;
                }
            }

            if (toAddService != null) {
                
                servicesAddedCombo.addItem(toAddService);
                servicesAddedCombo.setSelectedIndex(0);
                services = new int[servicesAddedCombo.getItemCount()-1];
                
                for (int i = 0; i < services.length; i++) {
                    services[i] = mPSoCConfig.getServiceValue(servicesAddedCombo.getItemAt(i+1).toString());
                }
                
                updateRouterInfoTable();

            }


        } else if (services.length > 0){
            services = new int[0];
            updateRouterInfoTable();
        }
        
    }//GEN-LAST:event_AddButtonActionPerformed

    private void RemoveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RemoveButtonActionPerformed
        if (servicesAddedCombo.getSelectedIndex() != 0) {

            servicesAddedCombo.removeItemAt(servicesAddedCombo.getSelectedIndex());

            services = new int[servicesAddedCombo.getItemCount() - 1];

            for (int i = 0; i < services.length; i++) {
                services[i] = mPSoCConfig.getServiceValue(servicesAddedCombo.getItemAt(i + 1).toString());
            }
            
            updateRouterInfoTable();
        }
    }//GEN-LAST:event_RemoveButtonActionPerformed

    private void taskTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_taskTableMouseClicked
        if (evt.getClickCount() > 1) {

            int taskID = Integer.parseInt(taskTable.getValueAt(taskTable.getSelectedRow(), 1).toString());
            
            boolean terminated = false;

            ArrayList<TaskInformation> taskInfo = routerInfo.getTasksInformation();

            int allocationTime = 0;
            int finishTIme = 0;

            for (TaskInformation taskInformation : taskInfo) {
                if (taskInformation.getID() == taskID) {
                    
                    switch (taskInformation.getService()) {
                        case "ALLOCATED":
                            allocationTime = taskInformation.getTime();
                            break;
                        case "TERMINATED":
                            finishTIme = taskInformation.getTime();
                            break;
                    }
                }

                if (finishTIme != 0 && allocationTime != 0) {
                    terminated = true;
                    break;
                }


            }
            
            TaskInfoFrame t = new TaskInfoFrame(taskID, terminated, (finishTIme - allocationTime), mPSoCConfig, routerInfo.getTasksInformation(), im, router_address);
            t.setLocationRelativeTo(this);
            t.setVisible(true);
            
        }
    }//GEN-LAST:event_taskTableMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddButton;
    private javax.swing.JButton RemoveButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel schedulingPanel;
    private javax.swing.JPanel PELogPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPaneRouterInfo;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableRouterInfo;
    private javax.swing.JComboBox servicesAddedCombo;
    private javax.swing.JComboBox servicesToAddCombo;
    private javax.swing.JTable taskTable;
    // End of variables declaration//GEN-END:variables
}
