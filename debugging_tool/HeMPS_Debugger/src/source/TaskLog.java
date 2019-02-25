/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package source;

/**
 *
 * @author mruaro
 */
public class TaskLog {
    
    private String processor;
    private String taskName;
    private int taskID;
    private String applicationName;
    private int applicationID;

    public TaskLog(String processor, String taskName, int taskID, String applicationName, int applicationID) {
        this.processor = processor;
        this.taskName = taskName;
        this.taskID = taskID;
        this.applicationName = applicationName;
        this.applicationID = applicationID;
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getTaskID() {
        return taskID;
    }

    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public int getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(int applicationID) {
        this.applicationID = applicationID;
    }
    
}
