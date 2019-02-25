/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package source;

/**
 *
 * @author mruaro
 */
public class TaskInformation {
    
    private int id;
    private String service;
    private int time;
    private int remote_task_id;
    
    
    public TaskInformation(int id, String service, int time){
        this.id = id;
        this.service = service;
        this.time = time;
    }
    
    
    public TaskInformation(int id, String service, int time, int remote_task_id) {
        this.id = id;
        this.service = service;
        this.time = time;
        this.remote_task_id = remote_task_id;
    }

    
    public int getTime() {
        return time;
    }
    
    public int getID(){
        return id;
    }

    public int getTaskId() {
        return (id & 0xFF);
    }
    
    public int getAppId(){
        return (id >> 8);
    }

    public String getService() {
        return service;
    }

    public int getRemote_task_id() {
        return remote_task_id;
    }

}
