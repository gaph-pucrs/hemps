/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deloream;

/**
 *
 * @author mruaro
 */
public class TaskMessage implements Comparable<TaskMessage>{

    
    private final int time;
    private String message;
    private final String proc;
    
    public TaskMessage(String messagelog) {
        String[] sp = messagelog.split("_");
        this.time = 0;//Integer.parseInt(sp[0]);
        this.proc = sp[1];
        this.message = "";
        if (sp.length >= 5){
            for (int i = 4; i < sp.length; i++) {
                this.message += sp[i];
            }
        }
            
           
    }

    public int getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }

    public String getProc() {
        return proc;
    }
    
    public String getProcNumber(){
        String[] xy = proc.split("x");
        int x = Integer.parseInt(xy[0]);
        int y = Integer.parseInt(xy[1]);
        
        return Integer.toString(x << 8 | y);
    }

    @Override
    public int compareTo(TaskMessage other) {
        if (this.time < other.getTime()) {
            return -1;
        }
        if (this.time > other.getTime()) {
            return 1;
        }
        return 0;
    }
    
   
    
    
}
