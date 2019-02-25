/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

/**
 *
 * @author mruaro
 */
public class SimpleProgressBar {

    private JFrame frame;
    private JFrame mainFrame;
    private String text;
    
    public SimpleProgressBar(String text, JFrame mainFrame){
        this.text = text;
        this.mainFrame = mainFrame;
    }
    
    public SimpleProgressBar(JFrame mainFrame){
        this.text = "Wait...";
        this.mainFrame = mainFrame;
    }
    
    
    public void start(){
                
        frame = new JFrame();

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setString(text);
        bar.setStringPainted(true);

        frame.setUndecorated(true);
        frame.getContentPane().add(bar);
        frame.setSize(150, 20);
        frame.setLocationRelativeTo(mainFrame);
        frame.setVisible(true);
        
       
    }
    
    public void kill(){
        frame.dispose();
    }
}
