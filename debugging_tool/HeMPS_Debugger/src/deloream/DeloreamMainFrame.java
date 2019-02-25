/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deloream;

import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import util.AboutFrame;

/**
 *
 * @author mruaro
 */
public final class DeloreamMainFrame extends javax.swing.JFrame {

    private DefaultMutableTreeNode root;
    
    private HashMap<Integer, String> taskNameHash;
    private HashMap<Integer, String> appHash;
    private String testcasePath;
    
    public DeloreamMainFrame(String hmpPath) {
        this.testcasePath = hmpPath;
        this.setLocationRelativeTo(null);
        this.setLocation(200,200);
        this.setTitle("Deloream");
        if (initTaskNameRelation(hmpPath)){
            initComponents();
            this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            initTree();
        } else {
            System.exit(0);
        }
        this.setSize(800,700);
        URL url = this.getClass().getResource("/icon/delorean.png");    
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(url));
    }
    
    private boolean initTaskNameRelation(String hmpPath) {
        try {
        
            File f = new File(hmpPath+"/debug/platform.cfg");
            
            RandomAccessFile file = new RandomAccessFile(f, "r");
            
            String line = null; 
            boolean start_read = false;
            
            taskNameHash = null;
            appHash = null;
            taskNameHash = new HashMap<>();
            appHash = new HashMap<>();
            
            
            while ((line = file.readLine()) != null) {
                if (!start_read && line.contains("BEGIN_task_name_relation")){
                    start_read = true;
                    continue;
                }
                
                if (start_read){
                    
                    if (line.contains("END_task_name_relation")){
                        break;
                    }
                    String splitedLine[] = line.split(" ");

                    String taskName = splitedLine[0];
                    Integer taskID = Integer.parseInt(splitedLine[1]);

                    taskNameHash.put(taskID, taskName+"["+taskID+"]");
                }
            }
            
            file.close();
            
            f = new File(hmpPath+"/debug/platform.cfg");
            file = new RandomAccessFile(f, "r");
            start_read = false;
            
            while ((line = file.readLine()) != null) {
                if (!start_read && line.contains("BEGIN_app_name_relation")){
                    start_read = true;
                    continue;
                }
                
                if (start_read){
                    
                    if (line.contains("END_app_name_relation")){
                        break;
                    }
                    String app_name = line.split("\t")[0];
                    int app_id = Integer.parseInt(line.split("\t")[1]);
                    
                    appHash.put(app_id, app_name+"["+app_id+"]");
                }
            }
            
            file.close();
            
            //System.out.println(appHash);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return true;
    }
    
    private void initTree(){
        
        root = new DefaultMutableTreeNode("Applications");
        
        LinkedList<DefaultMutableTreeNode> appList = new LinkedList<>();
        
        Set<Integer> appKeys = appHash.keySet();
        for (Integer appID : appKeys) {
            DefaultMutableTreeNode appNode = new DefaultMutableTreeNode(appHash.get(appID));
            
            Set<Integer> taskKeys = taskNameHash.keySet();
            for (Integer taskID : taskKeys) {
                if (taskID >> 8 == appID){
                    appNode.add(new DefaultMutableTreeNode(taskNameHash.get(taskID)));
                }
            }
            appList.add(appNode);
        }
        
        for (DefaultMutableTreeNode appInstance : appList) {
            root.add(appInstance);
        }
        
        DefaultTreeModel model = new DefaultTreeModel(root);
        
        applicationTree.setModel(model);
        
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        applicationTreePanel = new javax.swing.JScrollPane();
        applicationTree = new javax.swing.JTree();
        jScrollPane1 = new javax.swing.JScrollPane();
        textLabel = new javax.swing.JTextPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        optionMenuItem = new javax.swing.JMenu();
        newTestcaseMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Deloream - Debug Log Reader for MPSoCs");
        setBackground(new java.awt.Color(255, 255, 255));

        applicationTreePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        applicationTreePanel.setAlignmentX(1.0F);

        applicationTree.setAlignmentX(Component.CENTER_ALIGNMENT);
        applicationTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                applicationTreeMouseClicked(evt);
            }
        });
        applicationTreePanel.setViewportView(applicationTree);
        //applicationTree.setRootVisible(false);

        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));

        textLabel.setEditable(false);
        textLabel.setBackground(Color.WHITE);
        textLabel.setContentType("text/html"); // NOI18N
        textLabel.setFont(new java.awt.Font("DejaVu Sans Mono", 0, 14)); // NOI18N
        textLabel.setToolTipText("");
        jScrollPane1.setViewportView(textLabel);

        optionMenuItem.setText("Options");

        newTestcaseMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newTestcaseMenuItem.setText("New testcase");
        newTestcaseMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newTestcaseMenuItemActionPerformed(evt);
            }
        });
        optionMenuItem.add(newTestcaseMenuItem);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        optionMenuItem.add(exitMenuItem);

        jMenuBar1.add(optionMenuItem);

        jMenu2.setText("Help");

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(aboutMenuItem);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(applicationTreePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
            .addComponent(applicationTreePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 532, Short.MAX_VALUE)
        );

        jScrollPane1.getVerticalScrollBar().setUnitIncrement(12);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void applicationTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_applicationTreeMouseClicked
        TreePath path = applicationTree.getSelectionPath();

        if (evt.getClickCount() == 2 && path.getPath().length >= 3) {
            String appName = path.getPath()[1].toString();
            String taskName = path.getPath()[2].toString();
            String appID = "";
            String taskId = "";
            
            Set<Integer> keys = appHash.keySet();
            for (Integer id : keys) {
                if (appHash.get(id).equals(appName)){
                    appID = Integer.toString(id);
                }
            }
            int applicID = Integer.parseInt(appID);
            keys = taskNameHash.keySet();
            for (Integer id : keys) {
                if (id >> 8 == applicID && taskNameHash.get(id).equals(taskName)){
                    taskId = Integer.toString(id & 0xFF);
                }
            }
            
            String sentence = "_"+appID+"_"+taskId+"_";
            
            File logDir = new File(testcasePath + "/log");
            File[] filesInDir = logDir.listFiles();
            
            String text = "<html>";
            String proc = "";
            
            LinkedList<TaskMessage> messages = new LinkedList<>();
            
            for (File logFile : filesInDir) {

                String fileName = logFile.getName();

                if (fileName.contains("log")) {
                    String line = null;
                    
                    try {
                        
                        RandomAccessFile fileRead = new RandomAccessFile(logFile, "r");

                        while ((line = fileRead.readLine()) != null) {
                            
                            if (line.contains(sentence) && line.contains("$$$")){
                                messages.add(new TaskMessage(line));
                            }
                        }
                        
                        fileRead.close();

                    } catch (Exception ex) {
                        System.out.println("line: "+line);
                        ex.printStackTrace();
                    }

                }
            }
            
            if (messages.size() == 0){
                text += "<b><span style=\"color:#FF0000\"> Log file not created yet or no message found! </b> <P></html>";
                textLabel.setText(text);
                return;
            }
            
            Collections.sort(messages);
            
            
            for (TaskMessage taskMessage : messages) {
                if (!taskMessage.getProc().equals(proc)){
                    text += "<b>-- Processor :"+taskMessage.getProc()+"</b> <P>";
                    proc = taskMessage.getProc();
                }
                //text += taskMessage.getTime()+": "+taskMessage.getMessage()+"<P style=\"margin-top: 0\">";
                text += taskMessage.getMessage()+"<P style=\"margin-top: 0\">";
                
            }
            
            text += "</html>";
            
            textLabel.setText(text);
        }
    }//GEN-LAST:event_applicationTreeMouseClicked

    private void newTestcaseMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newTestcaseMenuItemActionPerformed
        if (initTaskNameRelation(null)){
            initTree();
            textLabel.setText("");
        }
    }//GEN-LAST:event_newTestcaseMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        if (JOptionPane.showConfirmDialog(this, "You are sure?", "", JOptionPane.YES_NO_OPTION) == 0){
            System.exit(0);
        }
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        new AboutFrame().setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JTree applicationTree;
    private javax.swing.JScrollPane applicationTreePanel;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuItem newTestcaseMenuItem;
    private javax.swing.JMenu optionMenuItem;
    private javax.swing.JTextPane textLabel;
    // End of variables declaration//GEN-END:variables
}
