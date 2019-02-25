package source.scheduling;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mruaro
 */
public class SchedulingEvent {
    
    private int code;
    private String name;
    private int start_time_ticks;
    private int finish_time_ticks;
    private int xi;
    private int yi;
    private int xf;
    private int yf;
    
    public SchedulingEvent(int code, String name, int start_time_ticks, int finish_time_ticks) {
        this.code = code;
        this.name = name;
        this.start_time_ticks = start_time_ticks;
        this.finish_time_ticks = finish_time_ticks;
        this.xi = -1;
        this.yi = -1;
        this.xf = -1;
        this.yf = -1;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getStart_time_ticks() {
        return start_time_ticks;
    }

    public int getFinish_time_ticks() {
        return finish_time_ticks;
    }
    
    public int getXi() {
        return xi;
    }

    public void setXi(int xi) {
        this.xi = xi;
    }

    public int getYi() {
        return yi;
    }

    public void setYi(int yi) {
        this.yi = yi;
    }

    public int getXf() {
        return xf;
    }

    public void setXf(int xf) {
        this.xf = xf;
    }

    public int getYf() {
        return yf;
    }

    public void setYf(int yf) {
        this.yf = yf;
    }
    
   
    
}
