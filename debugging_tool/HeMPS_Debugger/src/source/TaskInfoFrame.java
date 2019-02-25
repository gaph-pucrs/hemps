/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package source;

import componentes.CellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import source.communication_debug.CommDebbugInstance;
import util.MPSoCConfig;

/**
 *
 * @author mruaro
 */
public final class TaskInfoFrame extends javax.swing.JFrame {

    private int task_ID;
    private boolean status;
    private int executionTime;
    private TreeMap<Integer, String> tasksName;
    private ArrayList<TaskInformation> taskInformation;
    private boolean running;
    private int router_address;
    private MPSoCConfig mPSoCConfig;
    
    public TaskInfoFrame(int task_ID, boolean status, int executionTime, MPSoCConfig mPSoCConfig, ArrayList<TaskInformation> taskInformation, Image im, int router_address) {
        initComponents();
        this.setIconImage(im);
        if (mPSoCConfig.getRouterAddressing() == MPSoCConfig.XY){
            this.router_address = mPSoCConfig.ham_to_xy_addr(router_address);
        } else {
            this.router_address = router_address;
        }
        this.task_ID = task_ID;
        this.status = status;
        this.executionTime = executionTime;
        this.mPSoCConfig = mPSoCConfig;
        this.tasksName = mPSoCConfig.getTaskNameHash();
        this.taskInformation = taskInformation;
        this.running = true;
        this.setTitle("Task "+tasksName.get(task_ID)+" Information");
        this.setLocation((int) (1 + Math.random() * 768), (int) (1 + Math.random() * 200));
        messageTable.setDefaultRenderer(Object.class, new CellRenderer());
        updateLabels();
        updateMessageTable();
        updateThread();
        
    }
    
    void updateThread(){
        new Thread() {
            @Override
            public void run() {
                while(running){
                    try { sleep(500); } catch (Exception ex) {}
                    updateLabels();
                    updateMessageTable();
                }
            }
        }.start();
    }
    
    
  
    

    private void updateLabels(){
        taskIDLabel.setText(Integer.toString(task_ID));
        if (status){
            taskStatusLabel.setText("Terminated");
            taskRunningTimeLabel.setText(Integer.toString(executionTime));
        } else {
            taskStatusLabel.setText("Running");
            taskRunningTimeLabel.setText("-");
        }
        taskNameLabel.setText(tasksName.get(task_ID));
    }
    
    private void updateMessageTable(){
        
        
        ArrayList<TaskInformation> pending = new ArrayList<>();
        ArrayList<Object[]> checked = new ArrayList<>();
        String req_time, del_time, remote_task;
        
        for (TaskInformation taskInfo : taskInformation) {
            if (taskInfo.getID() == task_ID && (taskInfo.getService().equals("MESSAGE_DELIVERY") || taskInfo.getService().equals("MESSAGE_REQUEST"))){
                
                
                if (taskInfo.getService().equals("MESSAGE_REQUEST")){
                    
                    pending.add(taskInfo);
                    
                } else if (taskInfo.getService().equals("MESSAGE_DELIVERY")) {
                    
                    for (int i = pending.size()-1; i >= 0; i--) {
                        
                        TaskInformation tf = pending.get(i);
                        
                        if (tf.getRemote_task_id() == taskInfo.getRemote_task_id()){
                            tf = pending.remove(i);
                            req_time = Integer.toString(tf.getTime());
                            del_time = Integer.toString(taskInfo.getTime());
                            remote_task = tasksName.get(taskInfo.getRemote_task_id());
                            
                            checked.add(new Object[]{"Requested: "+req_time+"  Delivered: "+del_time, remote_task, "checked"});
                        }
                        
                    }
                    
                }
                
            }
        }
        
        
        for (TaskInformation taskInfo : pending) {
            remote_task = tasksName.get(taskInfo.getRemote_task_id());
            
            checked.add(new Object[]{"Requested: "+taskInfo.getTime()+"  Delivered: -", remote_task, "pending"});
        }
        
        DefaultTableModel model = (DefaultTableModel) messageTable.getModel();
        model.setRowCount(checked.size());
        
        TreeMap<String, Integer> packet_counter = new TreeMap<>();
        
        for (int i = 0; i < checked.size(); i++) {
            Object[] in = checked.get(i);
            String r_task = in[1].toString();
            int v = 1;
            if (packet_counter.containsKey(r_task)){
                v = packet_counter.get(r_task) + 1;
                packet_counter.put(r_task, v);
            } else {
                packet_counter.put(r_task, 0);
            }
            
            messageTable.setValueAt(in[0], i, 0);
            messageTable.setValueAt(in[1], i, 1);
            messageTable.setValueAt(in[2], i, 2);
            messageTable.setValueAt(v, i, 3);
            
        }
        
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        taskNameLabel = new javax.swing.JLabel();
        taskIDLabel = new javax.swing.JLabel();
        taskStatusLabel = new javax.swing.JLabel();
        saa = new javax.swing.JLabel();
        taskRunningTimeLabel = new javax.swing.JLabel();
        pipeJButton = new javax.swing.JButton();
        requestedJButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        messageTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel2.setText("Task Name:");

        jLabel3.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel3.setText("Task ID:");

        jLabel4.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel4.setText("Task Status:");

        taskNameLabel.setText("jLabel5");

        taskIDLabel.setText("jLabel5");

        taskStatusLabel.setText("jLabel5");

        saa.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        saa.setText("Executed Time:");

        taskRunningTimeLabel.setText("jLabel5");

        pipeJButton.setText("View PIPE");
        pipeJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pipeJButtonActionPerformed(evt);
            }
        });

        requestedJButton.setText("Requested");
        requestedJButton.setMaximumSize(new java.awt.Dimension(42, 29));
        requestedJButton.setMinimumSize(new java.awt.Dimension(42, 29));
        requestedJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                requestedJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(taskNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24)
                        .addComponent(taskIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saa))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(taskStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(taskRunningTimeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(requestedJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pipeJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saa, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(taskRunningTimeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(taskNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pipeJButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(taskStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(requestedJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(taskIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel1.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel1.setText("Sent Message Requests:");

        messageTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null}
            },
            new String [] {
                "Time: Requested /  Deliverd", "Remote task", "Delivered", "Num"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
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
        messageTable.setColumnSelectionAllowed(true);
        jScrollPane1.setViewportView(messageTable);
        messageTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        messageTable.getColumnModel().getColumn(0).setMinWidth(250);
        messageTable.getColumnModel().getColumn(1).setMinWidth(50);
        messageTable.getColumnModel().getColumn(3).setResizable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        this.running = false;
    }//GEN-LAST:event_formWindowClosed

    private void pipeJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pipeJButtonActionPerformed
       
       try{
           
            String columnNames[] = { "Prod Task", "Cons Task", "Time" };

            String fileName = mPSoCConfig.getDebugDirPath()+"/pipe/"+router_address+".txt";
            
            String rowData[][] = createCommTable(fileName);
            
            showPIPEorRequestTable("Stored PIPE Messages", columnNames, rowData);
            
           
       } catch(Exception e){
           JOptionPane.showMessageDialog(this, "No PIPE information", "", JOptionPane.INFORMATION_MESSAGE);
       }
                
    }//GEN-LAST:event_pipeJButtonActionPerformed

    private void requestedJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_requestedJButtonActionPerformed
        try{
           
            String columnNames[] = { "Prod Task", "Cons Task", "Time" };

            String fileName = mPSoCConfig.getDebugDirPath()+"/request/"+router_address+".txt";
            
            String rowData[][] = createCommTable(fileName);
            
            showPIPEorRequestTable("PIPE", columnNames, rowData);
            
           
       } catch(Exception e){
           JOptionPane.showMessageDialog(this, "No request information", "", JOptionPane.INFORMATION_MESSAGE);
       }
    }//GEN-LAST:event_requestedJButtonActionPerformed

    String [][] createCommTable(String fileName) throws FileNotFoundException, IOException{
        
        String dataValues[][] = null;
        
        RandomAccessFile rf = new RandomAccessFile(fileName, "r");

        ArrayList<CommDebbugInstance> comm_instances = new ArrayList<>();

        String line;
        String splitLine[];
        String prodName, consName, time;
        CommDebbugInstance nextIntance;

        while ((line = rf.readLine()) != null) {

            splitLine = line.split("\t");
            prodName = tasksName.get(Integer.parseInt(splitLine[1]));
            consName = tasksName.get(Integer.parseInt(splitLine[2]));
            time = splitLine[3];

            nextIntance = new CommDebbugInstance(prodName, consName, time);

            if (line.contains("add")) {
                comm_instances.add(nextIntance);
            } else {
                for (CommDebbugInstance commInst : comm_instances) {
                    if (nextIntance.equals(commInst)) {
                        comm_instances.remove(commInst);
                        break;
                    }
                }
            }
        }

        dataValues = new String[comm_instances.size()][3];

        for (int i = 0; i < dataValues.length; i++) {
            CommDebbugInstance commInst = comm_instances.get(i);
            String[] stringPIPE = {commInst.getProducer(), commInst.getConsumer(), commInst.getTime()};

            dataValues[i] = stringPIPE;

        }

        return dataValues;
    }
    
    void showPIPEorRequestTable(String title, String columnNames[], String dataValues[][]){
        JFrame tableFrame = new JFrame(title);
        tableFrame.setSize(280, 300);
        tableFrame.setBackground(Color.white);
        tableFrame.setLocationRelativeTo(this);
        
        JPanel topPanel = new JPanel();
        topPanel.setLayout( new BorderLayout() );
        tableFrame.getContentPane().add( topPanel );
        
        JTable  table = new JTable( dataValues, columnNames );
        JScrollPane scrollPane = new JScrollPane( table );
        topPanel.add( scrollPane, BorderLayout.CENTER );
        tableFrame.setVisible(true);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable messageTable;
    private javax.swing.JButton pipeJButton;
    private javax.swing.JButton requestedJButton;
    private javax.swing.JLabel saa;
    private javax.swing.JLabel taskIDLabel;
    private javax.swing.JLabel taskNameLabel;
    private javax.swing.JLabel taskRunningTimeLabel;
    private javax.swing.JLabel taskStatusLabel;
    // End of variables declaration//GEN-END:variables

}


