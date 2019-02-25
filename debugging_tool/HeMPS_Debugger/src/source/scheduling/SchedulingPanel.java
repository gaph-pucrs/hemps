package source.scheduling;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.Border;
import util.MPSoCConfig;

/**
 *
 * @author mruaro
 */
public final class SchedulingPanel extends JPanel implements KeyListener, MouseListener, MouseWheelListener, MouseMotionListener{
    
    private static final int INITI_Y = 50;
    private static final int NAME_SPACE = 26;
    private int MAX_NAME_LENGHT;
    private int REPORT_INIT;
    private int TIME_LINE;
    private int recommended_heigth;
    
    private HashMap<Integer, SchedulingInstance> codeMap;
    private ArrayList<SchedulingEvent> schedulingEvents;
    private MPSoCConfig mPSoCConfig;
    private int appIDFilter;
    
    private String PE_address;
    
    private float total_cpu_time;
    private boolean crtl_pressed;
    private boolean report_enabled;
    private SchedulingEvent report_event;
    private boolean display_mouse_line;
    private int mouse_x;
    private HashMap<Integer, String> cpu_events; 
    
    

    public SchedulingPanel(int PE_address, MPSoCConfig mPSoCConfig, int appIDFilter) {
        this.mPSoCConfig = mPSoCConfig;
        this.PE_address = Integer.toString(PE_address);
        this.appIDFilter = appIDFilter;
        this.schedulingEvents = new ArrayList<>();
        this.codeMap = new HashMap<>();
        this.report_enabled = false;
        this.crtl_pressed = false;
        this.display_mouse_line = false;
        try {
            readCPUInfo();
        } catch (Exception ex) {
            cpu_events = new HashMap<>();
            //JOptionPane.showMessageDialog(this, "Warning: file cpu.cfg not found in dir: /debug or the file is out of format\n Using default values", "Warning", JOptionPane.WARNING_MESSAGE);
            cpu_events.put(65536, "Interruption");
            cpu_events.put(262144, "Scheduler");
            cpu_events.put(524288, "Idle");
        }
        this.setBackground(Color.white);
        if (appIDFilter > -1){
            readlAllPESchedulingLog(appIDFilter);
        } else {
            readSchedulingLog();
        }
        
        initialize();
        this.recommended_heigth = ((codeMap.size() * NAME_SPACE)+INITI_Y+NAME_SPACE/2) + 250;
        this.setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width, codeMap.size()*NAME_SPACE*3));
        this.addMouseListener(this);
        this.addKeyListener(this);
        this.addMouseWheelListener(this);
        this.addMouseMotionListener(this);
        this.setFocusable(true);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        
        //Defines the max lenght of instances name
        MAX_NAME_LENGHT = 0;
        Iterator<SchedulingInstance> instances = codeMap.values().iterator();
        while (instances.hasNext()){
            String  n = instances.next().getName();
            if (MAX_NAME_LENGHT < n.length()){
                MAX_NAME_LENGHT = n.length();
            }
        }
        MAX_NAME_LENGHT*=10;
        
        //Draw the X and Y lines
        TIME_LINE = (codeMap.size() * NAME_SPACE)+INITI_Y+NAME_SPACE/2;
        REPORT_INIT = TIME_LINE + 50;
        
        
        boolean change_line_heigth = false;
        
        if(!schedulingEvents.isEmpty()){
            
            //COnfigures the time proportion
            float proportion = (float)(width-MAX_NAME_LENGHT) / getLastEventTime();
            
            //Draw the instances name
            g.setColor(Color.black);
            
            if (appIDFilter == -1){
                g.setFont(new Font("Arial", Font.BOLD, 16)); 
                if (mPSoCConfig.getRouterAddressing() == MPSoCConfig.XY){
                    int pe_addr = Integer.parseInt(PE_address);
                    g.drawString("PE "+(pe_addr >> 8)+"x"+(pe_addr & 0xFF)+" Scheduling Graph", width/2-50, 20);
                } else {
                    g.drawString("PE "+PE_address+" Scheduling Graph", width/2-50, 20);
                }
            }
            
            g.setFont(new Font("Arial", Font.PLAIN, 12)); 
            
            instances = codeMap.values().iterator();
            while (instances.hasNext()){
                SchedulingInstance si = instances.next();
                g.drawString(si.getName(), 10, INITI_Y+ si.getY_position() +5);
            }
            
            //Draw the scheduling slots
            for (SchedulingEvent schedulingEvent : schedulingEvents) {
                 
                 float start_time = schedulingEvent.getStart_time_ticks();
                 float finish_time = schedulingEvent.getFinish_time_ticks();

                 int code = schedulingEvent.getCode();
                
                 //Draws the rectangle
                 int x = (int)Math.floor(start_time * proportion)+MAX_NAME_LENGHT;
                 int y = INITI_Y + codeMap.get(code).getY_position() - (NAME_SPACE/2);
                 int rec_width = (int)Math.floor((finish_time - start_time) * proportion);
                 int rec_heigth = NAME_SPACE;
                 
                 if (cpu_events.containsKey(code)){
                     g.setColor(new Color(21, 119, 40));
                 } else {
                     g.setColor(Color.BLUE);
                 }
                 
                 g.fillRect(x, y, rec_width, rec_heigth);
                 
                 schedulingEvent.setXi(x);
                 schedulingEvent.setYi(y);
                 schedulingEvent.setXf(x+rec_width);
                 schedulingEvent.setYf(y+rec_heigth);
                 
                 //Draw the horizontal lines
                 g.setColor(Color.gray);
                 g.drawLine(MAX_NAME_LENGHT, y, width, y);
                 //Draw the vertical lines
                 g.setColor(Color.black);
                 
                 change_line_heigth = !change_line_heigth;
            } // end FOR
            
            //Draw X and Y reference lines
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.black);
            g2.drawLine(MAX_NAME_LENGHT, INITI_Y, MAX_NAME_LENGHT, TIME_LINE+10);
            g2.drawLine(MAX_NAME_LENGHT, TIME_LINE, width, TIME_LINE);

            
            int ms = 100000; //ticks
            int us = 1000; //ticks
            int ns = 10; //ticks
            int tick_scale = ms;
            String scale_name = "ms";
            int total_ticks = (int)getLastEventTime();
            if (total_ticks < ms){
                if (total_ticks < us){
                    tick_scale = ns;
                    scale_name = "ns";
                } else {
                    tick_scale = us;
                    scale_name = "us";
                }
            }
            int tick = 0;
            int scale_ref = 1;
            int last_scale = 0;
            for (int time_line_count = 0; time_line_count < (width-MAX_NAME_LENGHT); time_line_count++) {
               tick = (int)((float)(time_line_count) / proportion);
               if (tick > (tick_scale * scale_ref)){
                   String scale_leg = scale_ref+" "+scale_name;
                   g.drawString(scale_leg, ((time_line_count-last_scale)/2+last_scale+MAX_NAME_LENGHT)-scale_leg.length()*2, TIME_LINE+12);
                   g.drawLine(time_line_count+MAX_NAME_LENGHT, TIME_LINE, time_line_count+MAX_NAME_LENGHT, TIME_LINE+10);
                   last_scale = time_line_count;
                   scale_ref++;
               } 
               
            }
            

            if (display_mouse_line){
                int tick_x = (int)((float)(mouse_x-MAX_NAME_LENGHT) / proportion);
                g.setColor(Color.red);
                g.drawLine(mouse_x, INITI_Y, mouse_x, TIME_LINE);
                String value = Integer.toString(tick_x) + " ticks";
                if (mouse_x >= (width - 100)) {
                    g.drawString(value, mouse_x - (value.length()*2) - (value.length()*4), INITI_Y - 10);
                } else { 
                    g.drawString(value, mouse_x - (value.length()*2), INITI_Y - 10);
                }
            }
            g.setColor(Color.black);
            //Utilization
            int last_report_x = 200;
            int y_increment = 15;
            
            if (appIDFilter == -1){
                g.drawString("----------------------- CPU utilization ------------------------", 10, REPORT_INIT);
                g.drawString("Total CPU time: ", 10, REPORT_INIT+y_increment);
                g.drawString(total_cpu_time+" ticks", last_report_x, REPORT_INIT+y_increment);
                instances = codeMap.values().iterator();
                while (instances.hasNext()){
                    y_increment+=15;
                    SchedulingInstance si = instances.next();
                    g.drawString(si.getName()+" : ", 10, REPORT_INIT+y_increment);
                    g.drawString((si.getTotal_cpu_utilization()*100.0f/total_cpu_time)+"%", last_report_x, REPORT_INIT+y_increment);
                }
            }
            y_increment = 0;
            if (report_enabled){
                int first_x = last_report_x + 200;
                if (mouse_x > first_x){
                    if (mouse_x >= (width - 300)) {
                         first_x = mouse_x - 300;
                    } else {
                        first_x = mouse_x;
                    }
                }
                g.drawString("----------------------- Execution Slice------------------------", first_x, REPORT_INIT);
                
                y_increment+=15;
                g.drawString("Name : ", first_x, REPORT_INIT+y_increment);
                g.drawString(report_event.getName(), first_x+200, REPORT_INIT+y_increment);
                y_increment+=15;
                g.drawString("Start time : ", first_x, REPORT_INIT+y_increment);
                g.drawString(report_event.getStart_time_ticks()+" ticks" , first_x+200, REPORT_INIT+y_increment);
                y_increment+=15;
                g.drawString("Finish time : ", first_x, REPORT_INIT+y_increment);
                g.drawString(report_event.getFinish_time_ticks()+" ticks", first_x+200, REPORT_INIT+y_increment);
                y_increment+=15;
                g.drawString("Slice Time : ", first_x, REPORT_INIT+y_increment);
                g.drawString(report_event.getFinish_time_ticks()-report_event.getStart_time_ticks()+" ticks", first_x+200, REPORT_INIT+y_increment);
                /*y_increment+=15;
                g.drawString("Period : ", first_x, REPORT_INIT+y_increment);
                g.drawString(codeMap.get(report_event.getCode()).getPeriod()+" ticks", first_x+200, REPORT_INIT+y_increment);
                y_increment+=15;
                g.drawString("Deadline : ", first_x, REPORT_INIT+y_increment);
                g.drawString(codeMap.get(report_event.getCode()).getDeadline()+" ticks", first_x+200, REPORT_INIT+y_increment);*/
                
                report_enabled = false;
            }
            
        } // end IF
        
    }
    
    public float getLastEventTime(){
        if (!schedulingEvents.isEmpty()){
            return (float)schedulingEvents.get(schedulingEvents.size()-1).getFinish_time_ticks();
        }
        return 0.0f;
    }
    
    
    private void readCPUInfo() throws Exception{
        
            
        RandomAccessFile rf = new RandomAccessFile(mPSoCConfig.getDebugDirPath()+"/cpu.cfg", "r");

        String line = null;

        cpu_events = new HashMap<>();
        
        boolean nonEmpty = false;
        
        while ((line = rf.readLine()) != null) {
            
                nonEmpty = true;

                String[] splitedLine = line.split("\t");

                String name = splitedLine[0];
                Integer code = Integer.parseInt(splitedLine[1]);

                cpu_events.put(code, name);

        }
        
        if (nonEmpty == false){
            throw new Exception();
        }
            
    }
    
    private void readlAllPESchedulingLog(int appFilter){
        
         try{
             
             
            JFrame fb = new JFrame("Reading scheduler report file");
            fb.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            Container content = fb.getContentPane();
            JProgressBar progressBar = new JProgressBar();
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            Border border = BorderFactory.createTitledBorder("Reading...");
            progressBar.setBorder(border);
            content.add(progressBar, BorderLayout.NORTH);
            fb.setSize(300, 70);
            fb.setLocationRelativeTo(this);  
            fb.setVisible(true);
            
            int peNumber = mPSoCConfig.getX_dimension() * mPSoCConfig.getY_dimension();
            float incrementCoef = 100.0f / peNumber; 
            peNumber = 0;
            
            
            RandomAccessFile rf = new RandomAccessFile(mPSoCConfig.getDebugDirPath()+"/scheduling_report.txt", "r");
            
            ArrayList<SchedulingEvent> tempEvents = new ArrayList<>();
            
            String line;
            
            int code = 0;
            int start_time;
            int finish_time;
            int proc;
            String procStr;
            
            
             for (int x = 0; x < mPSoCConfig.getX_dimension(); x++) {
                 for (int y = 0; y < mPSoCConfig.getY_dimension(); y++) {
                     
                     progressBar.setValue((int) (peNumber * incrementCoef));
                     peNumber++;
                     
                     proc = (x << 8) | y;
                     if (mPSoCConfig.getRouterAddressing() == MPSoCConfig.HAMILTONIAN){
                        procStr = Integer.toString(mPSoCConfig.xy_to_ham_addr(proc));
                     } else {
                        procStr = Integer.toString(proc);
                     }
                     start_time = -1;
                     while ((line = rf.readLine()) != null) {

                         String[] splitedLine = line.split("\t");

                         if (!splitedLine[0].equals(procStr)) {
                             continue;
                         }

                         if (start_time == -1) {
                             code = Integer.parseInt(splitedLine[1]);
                             start_time = Integer.parseInt(splitedLine[2]);
                             continue;
                         }

                         finish_time = Integer.parseInt(splitedLine[2]);
                         
                         tempEvents.add(new SchedulingEvent(code, "", start_time, finish_time));
                         
                         code = Integer.parseInt(splitedLine[1]);
                         start_time = Integer.parseInt(splitedLine[2]);
                     }
                     
                     if (start_time != -1){
                         tempEvents.add(new SchedulingEvent(code, "", start_time, start_time + 10));
                    }
                    rf.seek(0);
                 }

             }
             rf.close();
             fb.dispose();
             
             for (SchedulingEvent schedulingEvent : tempEvents) {
                 if ((schedulingEvent.getCode() >> 16) == 0 && (schedulingEvent.getCode() >> 8) == appFilter){
                     addSchedulingEvent(schedulingEvent.getCode(), schedulingEvent.getStart_time_ticks(), schedulingEvent.getFinish_time_ticks());
                 }
             }

        }catch(Exception ex){
            JOptionPane.showMessageDialog(this, mouse_x, PE_address, mouse_x);
        }
    }
    
    private void readSchedulingLog(){
        try{
            
            RandomAccessFile rf = new RandomAccessFile(mPSoCConfig.getDebugDirPath()+"/scheduling_report.txt", "r");
            
            String line;
            
            int code = 0;
            int start_time;
            int finish_time;
            
            start_time = -1;
            
            while((line = rf.readLine()) != null){
                
                String[] splitedLine = line.split("\t");
                
                if (!splitedLine[0].equals(PE_address)){
                    continue;
                }
                
                if (start_time == -1){
                    code = Integer.parseInt(splitedLine[1]);
                    start_time = Integer.parseInt(splitedLine[2]);
                    continue;
                }
                
                finish_time = Integer.parseInt(splitedLine[2]);
                addSchedulingEvent(code, start_time, finish_time);
                code = Integer.parseInt(splitedLine[1]);
                start_time = Integer.parseInt(splitedLine[2]);
            }
            
            if (start_time != -1){
                addSchedulingEvent(code, start_time, start_time + 10);
            }
            
            
        }catch(Exception ex){
            JOptionPane.showMessageDialog(this, mouse_x, PE_address, mouse_x);
        }
    }
    
    private void initialize(){
        
        ArrayList<Color> colors = new ArrayList<>();
        int instances_count = 1;
        
        //Generate codeMap
        for (SchedulingEvent se : schedulingEvents) {
            int code = se.getCode();
            
            if (!codeMap.containsKey(code)){
                Color c = generateNextValidColor(colors);
                int y = (instances_count * NAME_SPACE);
                SchedulingInstance si = new SchedulingInstance(se.getName(), y, c);
                instances_count++;
                codeMap.put(code, si);
            }
        }
        
        //Initialize Utilization
        total_cpu_time = 0.0f;
        for (SchedulingEvent se : schedulingEvents) {
            int partial_utilization = se.getFinish_time_ticks() - se.getStart_time_ticks();
            total_cpu_time += (float)partial_utilization;
            int current_utilization = codeMap.get(se.getCode()).getTotal_cpu_utilization();
            current_utilization+=partial_utilization;
            codeMap.get(se.getCode()).setTotal_cpu_utilization(current_utilization);
        }
        
        
        
    }
    
    private Color generateNextValidColor(ArrayList<Color> input){
        
        Random rand = new Random();
        boolean validColor;
        
        while(true){
            Color newColor = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
            validColor = true;
            for (Color color : input) {
                if (color.getRGB() == newColor.getRGB()){
                    validColor = false;
                    break;
                }
            }
            if(validColor){
                input.add(newColor);
                return newColor;
            }
        }
    }
    
    
    public void addSchedulingEvent(int code, int start_time_ticks, int finish_time_ticks){
        
        String name = "Undefined";
        if (mPSoCConfig.getTaskNameHash().containsKey(code)){
            name = mPSoCConfig.getTaskNameHash().get(code);
        }
        
        //String name = codeNameMap.get(code);
        
        if (cpu_events.containsKey(code)){
            name = cpu_events.get(code);
        } else {
            if (mPSoCConfig.getTaskNameHash().containsKey(code)) {
                name = mPSoCConfig.getTaskNameHash().get(code);
            }
        }
      
        schedulingEvents.add(new SchedulingEvent(code, name, start_time_ticks, finish_time_ticks));
        
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL){
            crtl_pressed = true;
        }
        
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL){
            crtl_pressed = false;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!crtl_pressed){
            report_enabled = false;
            for (SchedulingEvent schedulingEvent : schedulingEvents) {
                if (schedulingEvent.getXi() <= e.getX() && schedulingEvent.getYi() <= e.getY() && schedulingEvent.getXf() >= e.getX() && schedulingEvent.getYf() >= e.getY()){
                    report_enabled = true;
                    report_event = schedulingEvent;
                    this.repaint();
                }
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        display_mouse_line = false;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        
        if (crtl_pressed){
            if (e.getPreciseWheelRotation() > 0){
                this.setPreferredSize(new Dimension((int) (this.getPreferredSize().getWidth()/1.1d), codeMap.size()*NAME_SPACE));
            } else {
                this.setPreferredSize(new Dimension((int) (this.getPreferredSize().getWidth()*1.1d), codeMap.size()*NAME_SPACE));
            }
            this.revalidate();
        }
        
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        display_mouse_line = false;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        boolean default_cursor = true;
        display_mouse_line = false;
        if (!crtl_pressed){
            for (SchedulingEvent schedulingEvent : schedulingEvents) {
                if (schedulingEvent.getXi() <= e.getX() && schedulingEvent.getYi() <= e.getY() && schedulingEvent.getXf() >= e.getX() && schedulingEvent.getYf() >= e.getY()){
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    default_cursor = false;
                }
            }
            
        } 
        if (e.getX() >= MAX_NAME_LENGHT && e.getY() >= INITI_Y && e.getY() <= TIME_LINE){
            display_mouse_line = true;
            mouse_x = e.getX();
            this.repaint();
        }
        
        
        if (default_cursor){
            setCursor(Cursor.getDefaultCursor());
        }
    
    }

    public int getRecommended_heigth() {
        return recommended_heigth;
    }
}
