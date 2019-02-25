/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package source;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;
import source.scheduling.SchedulingPanel;
import util.MPSoCConfig;

/**
 *
 * @author mruaro
 */
public class SchedulingTab extends JPanel {

    private int PE;
    private MPSoCConfig mPSoCConfig;
    int appID;

    public SchedulingTab(int PE, MPSoCConfig mPSoCConfig) {
        this.PE = PE;
        this.mPSoCConfig = mPSoCConfig;
        //this.setBackground(Color.white);
        Button b = new Button("Open Scheduling Graph");
        //b.setBackground(Color.gray);
        b.setSize(70, 40);

        setLayout(new AbsoluteLayout());

        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bActionPerformed(false);
            }
        });

        add(b, new AbsoluteConstraints(170, 50, 207, 39));


        Button b2 = new Button("Open Application Scheduling ");

        b2.setSize(100, 40);

        b2.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bActionPerformed(true);
            }
        });

        add(b2, new AbsoluteConstraints(170, 100, 217, 39));

    }

    private void bActionPerformed(final boolean onlyApp) {


        appID = -1;
        while (onlyApp) {

            String appIDString = JOptionPane.showInputDialog(this, "Please, inform the application ID: ");
            if (appIDString == null || appIDString.equals("-1")) {
                return;
            }
            try {
                appID = Integer.parseInt(appIDString);
                break;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Enters only valid ID", "", JOptionPane.ERROR_MESSAGE);
            }

        }
                
        new Thread() {
            @Override
            public void run() {
                
                JFrame f = new JFrame();
                SchedulingPanel schedulingPanel = new SchedulingPanel(PE, mPSoCConfig, appID);
                JScrollPane jp = new JScrollPane(schedulingPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                jp.getHorizontalScrollBar().setUnitIncrement(200);
                f.getContentPane().add(jp);

                String title = "Scheduling Graph " + PE;
                if (mPSoCConfig.getRouterAddressing() == MPSoCConfig.XY) {
                    title = "Scheduling Graph " + (PE >> 8) + "x" + (PE & 0xFF);
                }
                f.setTitle(title);
                URL url = this.getClass().getResource("/icon/scheduling_icon.png");
                f.setIconImage(Toolkit.getDefaultToolkit().getImage(url));

                f.setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width, schedulingPanel.getRecommended_heigth()));
                f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                f.pack();
                f.setVisible(true);

            }
        }.start();



    }
}
