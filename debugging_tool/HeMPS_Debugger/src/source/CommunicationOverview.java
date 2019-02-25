/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package source;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import util.MPSoCConfig;

/**
 *
 * @author Marcelo
 */
public final class CommunicationOverview extends javax.swing.JFrame {

   
    private JPanel hotspot;
    private MPSoCConfig mPSoCConfig;
    private MPSoCInformation mPSoCInformation;
    private int[] routersFlitsVolume;
    
    public CommunicationOverview(MPSoCConfig mPSoCConfig, MPSoCInformation mPSoCInformation, Image im) {
        this.setLocation(400, 100);
        this.setIconImage(im);
        this.mPSoCConfig = mPSoCConfig;
        this.mPSoCInformation = mPSoCInformation;
        this.setTitle("Communication Overview");
        hotspot = new JPanel();
        hotspot.setBackground(Color.WHITE);
        hotspot.setLayout(new GridLayout(mPSoCConfig.getY_dimension(), mPSoCConfig.getX_dimension(), 8, 8));
        initComponents();
        colorSpectrumPanel.setImagem("/images/color_spectrum.png");
        jCheckBoxFIlterAll.setSelected(true);
        underNoCRadioButton.setEnabled(false);
        underNoCRadioButton.setSelected(true);
        underRouterRadioButton.setEnabled(false);
        underAllTrafficRadioButton.setEnabled(false);
        volumeCheckBox.setSelected(true);
        createHotSpotPanel(new int[0]);
        initServiceComboBox();
        underAllTrafficRadioButton.setToolTipText("(Total NoC traffic) / (total SERVICE router traffic)");
        underNoCRadioButton.setToolTipText("(Total SERVICE NoC traffic) / (total SERVICE router traffic)");
        underRouterRadioButton.setToolTipText("(Total router traffic) / (total SERVICE router traffic)");
        
    }
    
    public void initServiceComboBox(){
        
        String aux;
        
        jComboBoxServiceSelection.setEnabled(false);

        int serviceRef[] = mPSoCConfig.getServiceReference();
        
        for (int i = 0; i < serviceRef.length; i++) {
            aux = mPSoCConfig.getStringServiceName(serviceRef[i]);
            if (aux != null){
                jComboBoxServiceSelection.addItem(aux);
            }
            
        }
        
    }
    
    public void updateStatisticsTable(float smaller, float bigger, float averange, int routerOfBigger, int routerOfSmaller){
        DefaultTableModel model = (DefaultTableModel) statisticsJTable.getModel();
        
        //String smallerColunm = "Router "+routerOfSmaller +" ="+ new DecimalFormat("0.000%").format(smaller);
        String smallerRouter, biggerRouter;
        
        if (mPSoCConfig.getRouterAddressing() == MPSoCConfig.XY){
           smallerRouter = mPSoCConfig.HamAdressToXYLabel(routerOfSmaller);
           biggerRouter = mPSoCConfig.HamAdressToXYLabel(routerOfBigger);
        } else {
            smallerRouter = Integer.toString(routerOfSmaller);
            biggerRouter = Integer.toString(routerOfBigger);
        }
        
        String smallerColunm = "R. "+smallerRouter+": "+new DecimalFormat("0.000").format(smaller)+"%";
        String biggerColunm = "R. "+biggerRouter+": "+new DecimalFormat("0.000").format(bigger)+"%";
        
        
        model.setValueAt(smallerColunm, 0, 0);
        model.setValueAt(biggerColunm, 0, 1);
        model.setValueAt(new DecimalFormat("0.000").format(averange)+"%", 0, 2);
    }
    
    public void createHotSpotPanel(int[] services) {
        
        float smaller = -1f, bigger = 0f, averange = 0f;
        int peOfSmaller = 0, peOfBigger = 0;

        RouterNeighbors n = new RouterNeighbors(mPSoCConfig);
        this.routersFlitsVolume = new int[mPSoCConfig.getX_dimension() * mPSoCConfig.getY_dimension()];
        
        hotspot.removeAll();
        
        int totalVolume = 0;
        
        for (int y = mPSoCConfig.getY_dimension() - 1; y >= 0; y--) {
            for (int x = 0; x < mPSoCConfig.getX_dimension(); x++) {
                int router_addr = n.xy_to_ham_addr((x << 8) | y);
                if (volumeCheckBox.isSelected()){
                    if (services.length == 0)
                        routersFlitsVolume[router_addr] = mPSoCInformation.getRouterInformation(router_addr).getRouterTotalVolumeInFlits();
                    else 
                        routersFlitsVolume[router_addr] = mPSoCInformation.getRouterInformation(router_addr).getRouterTotalServicesVolumeInFlits(services);
                } else {
                    if (services.length == 0)
                        routersFlitsVolume[router_addr] = mPSoCInformation.getRouterInformation(router_addr).getRouterTotalBandwidthInCycles();
                    else 
                        routersFlitsVolume[router_addr] = mPSoCInformation.getRouterInformation(router_addr).getRouterTotalServicesBandwidthInCycles(services);
                }
                totalVolume += routersFlitsVolume[router_addr];
            }
        }
        
        if (underAllTrafficRadioButton.isSelected()){
            if (volumeCheckBox.isSelected())
                totalVolume = mPSoCInformation.getTotalNoCVolume();
            else
                totalVolume = mPSoCInformation.getTotalNoCBandwidth();
        }
        
        for (int y = mPSoCConfig.getY_dimension() - 1; y >= 0; y--) {
            for (int x = 0; x < mPSoCConfig.getX_dimension(); x++) {
                int ham_addr = n.xy_to_ham_addr((x << 8) | y);
                
                JPanel panel = new JPanel(new GridLayout(3, 3));
                
                float percent = 0f;
                
                if (underRouterRadioButton.isSelected()){
                    if (volumeCheckBox.isSelected())
                        totalVolume = mPSoCInformation.getRouterInformation(ham_addr).getRouterTotalVolumeInFlits();
                    else
                        totalVolume = mPSoCInformation.getRouterInformation(ham_addr).getRouterTotalBandwidthInCycles();
                }              
                
                String volume = "0%";
                if (totalVolume != 0){
                    percent = routersFlitsVolume[ham_addr]*100.0f/totalVolume;
                    volume = new DecimalFormat("0.000").format(percent);
                   /* if (volumeCheckBox.isSelected())
                        volume +="% filts";
                    else 
                        volume +="% cycles";*/
                    volume +="%";
                }
                
                panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
                
                
                /*int c = (percent*255)/100;
                panel.setBackground(new Color(c, 0, 255-c));*/
                
                if (smaller == -1f)
                    smaller = percent;
                
                if (bigger < percent){
                    bigger = percent;
                    peOfBigger = ham_addr;
                }
                if (smaller > percent){
                    smaller = percent;
                    peOfSmaller = ham_addr;
                }
                averange+=percent;
                
                
                float h = (percent*0.65f)/100.0f;
                panel.setBackground(Color.getHSBColor((0.65f- h), 1f, 0.8f));
                
                Color foreground = Color.WHITE;
               
                /*if (c < (256/2))
                    foreground = Color.BLACK;*/
                String router_name = null;
                int peType = mPSoCConfig.getPEType(ham_addr);
                switch(peType){
                    case MPSoCConfig.GLOBAL_MASTER:
                        router_name = "Global M ";
                        break;
                    case MPSoCConfig.CLUSTER_MASTER:
                        router_name = "Cluster M ";
                        break;
                    case MPSoCConfig.SLAVE:
                        router_name = "Slave ";
                        break;
                }
                        
                JLabel labelRouter = null;
                if (mPSoCConfig.getRouterAddressing() == MPSoCConfig.HAMILTONIAN)
                    labelRouter = new JLabel(router_name+ham_addr);
                else
                    labelRouter = new JLabel(router_name+this.mPSoCConfig.HamAdressToXYLabel(ham_addr));
                
                labelRouter.setFont(new Font("Verdana",1,18));
                labelRouter.setForeground(foreground);
                labelRouter.setHorizontalAlignment(SwingConstants.CENTER);
                labelRouter.setVerticalTextPosition(SwingConstants.CENTER);
                
                
                //JLabel data = new JLabel("Volume: "+routersFlitsVolume[ham_addr] + " flits = "+volume);
                JLabel data = new JLabel(volume);
                data.setFont(new Font("Verdana",0,18));
                data.setForeground(foreground);
                data.setHorizontalAlignment(SwingConstants.CENTER);
                data.setVerticalTextPosition(SwingConstants.CENTER);
                
                JLabel dataType = null;
                if (volumeCheckBox.isSelected())
                    dataType = new JLabel("flits");
                else 
                    dataType = new JLabel("cycles");
                dataType.setFont(new Font("Verdana",0,18));
                dataType.setForeground(foreground);
                dataType.setHorizontalAlignment(SwingConstants.CENTER);
                dataType.setVerticalTextPosition(SwingConstants.CENTER);
                
                panel.add(labelRouter);
                panel.add(data);
                panel.add(dataType);
                
                hotspot.add(panel);
            }

        }
        
        averange = averange / (float)mPSoCConfig.getPENumber();
        
        updateStatisticsTable(smaller, bigger, averange, peOfBigger, peOfSmaller);
        
        hotspot.revalidate();
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane(hotspot);
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jCheckBoxFIlterAll = new javax.swing.JCheckBox();
        jComboBoxServiceSelection = new javax.swing.JComboBox();
        underNoCRadioButton = new javax.swing.JRadioButton();
        underRouterRadioButton = new javax.swing.JRadioButton();
        underAllTrafficRadioButton = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        volumeCheckBox = new javax.swing.JCheckBox();
        bandwidthCheckBox = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        statisticsJTable = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        colorSpectrumPanel = new componentes.UJPanelImagem();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(100, 100));

        jScrollPane1.setMinimumSize(new java.awt.Dimension(100, 100));

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Service Filter"));

        jCheckBoxFIlterAll.setText("All Services");
        jCheckBoxFIlterAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxFIlterAllActionPerformed(evt);
            }
        });

        jComboBoxServiceSelection.setModel(new javax.swing.DefaultComboBoxModel());
        jComboBoxServiceSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxServiceSelectionActionPerformed(evt);
            }
        });

        underNoCRadioButton.setText("Global");
        underNoCRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                underNoCRadioButtonActionPerformed(evt);
            }
        });

        underRouterRadioButton.setText(" Router ");
        underRouterRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                underRouterRadioButtonActionPerformed(evt);
            }
        });

        underAllTrafficRadioButton.setText("All traffic");
        underAllTrafficRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                underAllTrafficRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxFIlterAll)
                    .addComponent(jComboBoxServiceSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(underNoCRadioButton)
                    .addComponent(underRouterRadioButton)
                    .addComponent(underAllTrafficRadioButton))
                .addGap(27, 27, 27))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {underNoCRadioButton, underRouterRadioButton});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(underAllTrafficRadioButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jCheckBoxFIlterAll))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(underNoCRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(underRouterRadioButton))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jComboBoxServiceSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {underNoCRadioButton, underRouterRadioButton});

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Data Filter"));

        volumeCheckBox.setText("Volume");
        volumeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                volumeCheckBoxActionPerformed(evt);
            }
        });

        bandwidthCheckBox.setText("Bandwidth");
        bandwidthCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bandwidthCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(volumeCheckBox)
                    .addComponent(bandwidthCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(volumeCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bandwidthCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Statistics"));

        statisticsJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null}
            },
            new String [] {
                "Smaller %", "Bigger %", "Averange"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(statisticsJTable);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 379, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(73, 73, 73))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(392, 392, 392))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Color Legend"));

        javax.swing.GroupLayout colorSpectrumPanelLayout = new javax.swing.GroupLayout(colorSpectrumPanel);
        colorSpectrumPanel.setLayout(colorSpectrumPanelLayout);
        colorSpectrumPanelLayout.setHorizontalGroup(
            colorSpectrumPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        colorSpectrumPanelLayout.setVerticalGroup(
            colorSpectrumPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );

        jLabel1.setText("high");

        jLabel2.setText("low");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(colorSpectrumPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                        .addComponent(jLabel1)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(colorSpectrumPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 416, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 632, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void underRouterRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_underRouterRadioButtonActionPerformed
        underNoCRadioButton.setSelected(false);
        underAllTrafficRadioButton.setSelected(false);
        int[] service = {mPSoCConfig.getServiceValue(jComboBoxServiceSelection.getSelectedItem().toString())};
        createHotSpotPanel(service);
    }//GEN-LAST:event_underRouterRadioButtonActionPerformed

    private void underNoCRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_underNoCRadioButtonActionPerformed
        underRouterRadioButton.setSelected(false);
        underAllTrafficRadioButton.setSelected(false);
        int[] service = {mPSoCConfig.getServiceValue(jComboBoxServiceSelection.getSelectedItem().toString())};
        createHotSpotPanel(service);
    }//GEN-LAST:event_underNoCRadioButtonActionPerformed

    private void jComboBoxServiceSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxServiceSelectionActionPerformed
        if (jComboBoxServiceSelection.isEnabled()){
            int[] service = {mPSoCConfig.getServiceValue(jComboBoxServiceSelection.getSelectedItem().toString())};
            createHotSpotPanel(service);
        }
    }//GEN-LAST:event_jComboBoxServiceSelectionActionPerformed

    private void jCheckBoxFIlterAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxFIlterAllActionPerformed
        if (jCheckBoxFIlterAll.isSelected()){
            underNoCRadioButton.setSelected(true);
            underRouterRadioButton.setSelected(false);
            underNoCRadioButton.setEnabled(false);
            underRouterRadioButton.setEnabled(false);
            underAllTrafficRadioButton.setEnabled(false);
            jComboBoxServiceSelection.setEnabled(false);
            createHotSpotPanel(new int[0]);
        } else {
            underNoCRadioButton.setEnabled(true);
            underRouterRadioButton.setEnabled(true);
            underAllTrafficRadioButton.setEnabled(true);
            jComboBoxServiceSelection.setEnabled(true);
            int[] service = {mPSoCConfig.getServiceValue(jComboBoxServiceSelection.getSelectedItem().toString())};
            createHotSpotPanel(service);
        }
    }//GEN-LAST:event_jCheckBoxFIlterAllActionPerformed

    private void volumeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_volumeCheckBoxActionPerformed
        bandwidthCheckBox.setSelected(false);
        if (jCheckBoxFIlterAll.isSelected()){
            createHotSpotPanel(new int[0]);
        } else {
            int[] service = {mPSoCConfig.getServiceValue(jComboBoxServiceSelection.getSelectedItem().toString())};
            createHotSpotPanel(service);
        }
    }//GEN-LAST:event_volumeCheckBoxActionPerformed

    private void bandwidthCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bandwidthCheckBoxActionPerformed
        volumeCheckBox.setSelected(false);
        int[] service = {mPSoCConfig.getServiceValue(jComboBoxServiceSelection.getSelectedItem().toString())};
        createHotSpotPanel(service);
    }//GEN-LAST:event_bandwidthCheckBoxActionPerformed

    private void underAllTrafficRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_underAllTrafficRadioButtonActionPerformed
        underNoCRadioButton.setSelected(false);
        underRouterRadioButton.setSelected(false);
        int[] service = {mPSoCConfig.getServiceValue(jComboBoxServiceSelection.getSelectedItem().toString())};
        createHotSpotPanel(service);
    }//GEN-LAST:event_underAllTrafficRadioButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox bandwidthCheckBox;
    private componentes.UJPanelImagem colorSpectrumPanel;
    private javax.swing.JCheckBox jCheckBoxFIlterAll;
    private javax.swing.JComboBox jComboBoxServiceSelection;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable statisticsJTable;
    private javax.swing.JRadioButton underAllTrafficRadioButton;
    private javax.swing.JRadioButton underNoCRadioButton;
    private javax.swing.JRadioButton underRouterRadioButton;
    private javax.swing.JCheckBox volumeCheckBox;
    // End of variables declaration//GEN-END:variables
}
