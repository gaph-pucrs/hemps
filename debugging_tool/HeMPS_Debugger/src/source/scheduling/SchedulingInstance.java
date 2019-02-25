package source.scheduling;


import java.awt.Color;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mruaro
 */
public class SchedulingInstance {
    
    private String name;
    private int y_position;
    private Color color;
    private int period;
    private int deadline;
    private int total_cpu_utilization;
    
    
    public SchedulingInstance(String name, int y_position, Color c) {
        this.name = name;
        this.y_position = y_position;
        this.color = c;
        this.period = 0;
        this.deadline = 0;
        this.total_cpu_utilization = 0;
    }
    
    public String getName() {
        return name;
    }

    public int getY_position() {
        return y_position;
    }

    public Color getColor() {
        return color;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getDeadline() {
        return deadline;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }

    public int getTotal_cpu_utilization() {
        return total_cpu_utilization;
    }

    public void setTotal_cpu_utilization(int total_cpu_utilization) {
        this.total_cpu_utilization = total_cpu_utilization;
    }

   
    
}
