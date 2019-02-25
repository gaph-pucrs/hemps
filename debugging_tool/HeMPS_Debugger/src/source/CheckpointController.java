/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package source;

/**
 *
 * @author mruaro
 */
public final class CheckpointController {

    private MainFrame mainFrame;
    private float windowsInit;
    private float windowsSizeInMs;
    private float currentTimeInMs;
    private int clockPeriod;

    public CheckpointController(MainFrame mainFrame, int clockPeriod) {
        this.mainFrame = mainFrame;
        this.clockPeriod = clockPeriod;
        windowsSizeInMs = 0.500f; //ms
        reset();
    }

    public void reset() {
        windowsInit = 0.0f;
        currentTimeInMs = 0.0f;
        
    }


    public void setTime(int timeInTicks) {

        this.currentTimeInMs = (float) timeInTicks * clockPeriod / 1000.0f / 1000.0f;
        
        if ((currentTimeInMs - windowsInit) > windowsSizeInMs) {
            //System.out.println("Checkpoint at: "+windowsInit+" - "+currentTimeInMs+" diff: "+(currentTimeInMs - windowsInit));
            windowsInit = currentTimeInMs;
            mainFrame.checkpointReached();
        }
    }

    public float getWindowsSizeInMs() {
        return windowsSizeInMs;
    }
    
    public float getWindowsSizeInUs() {
        return windowsSizeInMs*1000;
    }
    
    public float getWindowsSizeInNs() {
        return getWindowsSizeInUs()*1000;
    }

    public void setWindowsSizeinMs(float windowsSize) {
        this.windowsSizeInMs = windowsSize;
    }

    public void setClockPeriod(int clockPeriod) {
        this.clockPeriod = clockPeriod;
    }

    public float getWindowsInit() {
        return windowsInit;
    }

    public float getCurrentTimeInMs() {
        return currentTimeInMs;
    }

}
