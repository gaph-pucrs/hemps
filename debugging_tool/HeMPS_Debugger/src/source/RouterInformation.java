/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package source;

import java.util.ArrayList;
import util.MPSoCConfig;

/**
 *
 * @author Marcelo
 */
public class RouterInformation {
    
    private int router_address;
    private ArrayList<PacketInformation> packets;
    private ArrayList<TaskInformation> tasks;
    private PortInformation[] portInformations = new PortInformation[MPSoCConfig.NPORT];
    private int taskCounter;
    private MPSoCConfig mPSoCConfig;
    
    public RouterInformation(MPSoCConfig mPSoCConfig, int router_addres){
        this.router_address = router_addres;
        this.taskCounter = 0;
        this.packets = new ArrayList<PacketInformation>();
        this.tasks = new ArrayList<TaskInformation>();
        this.mPSoCConfig = mPSoCConfig;
        
        for (int i = 0; i < MPSoCConfig.NPORT; i++) {
            portInformations[i] = new PortInformation(mPSoCConfig, MPSoCConfig.getChannel(i));
        }
    }
    
    
    public void addPacket(PacketInformation inputPacket){
        packets.add(inputPacket);
        updatePortInformations(inputPacket);
        updateTaskInformation(inputPacket);
        
        //Ordena a lista
            /*Collections.sort(packets, new Comparator() {
            public int compare(Object o1, Object o2) {
                PacketInformation p1 = (PacketInformation) o1;
                PacketInformation p2 = (PacketInformation) o2;
                return p1.getTime() < p2.getTime() ? -1 : (p1.getTime() > p2.getTime() ? +1 : 0);
            }
        });*/
            
    }
    
    public ArrayList<TaskInformation> getTasksInformation(){
        return tasks;
    }
    
    public int getRouter_address() {
        return router_address;
    }

    private void updatePortInformations(PacketInformation inputPacket) {
        portInformations[inputPacket.getInput_port()].addNewInformation(inputPacket);
    }
    
    public int getRouterTotalThroughputInFlits(){
        int total = 0;
        for (int i = 0; i < MPSoCConfig.NPORT; i++) {
            total += getPortThroughputInFlits(i);
        }
        
        return total;
    }
    
    public int getRouterTotalBandwidthThroughputInFlits(){
        int total = 0;
        for (int i = 0; i < MPSoCConfig.NPORT; i++) {
            total += getPortBandwidthThroughputInCycles(i);
        }
        
        return total;
    }
    
    public int getRouterTotalVolumeInFlits(){
        
        int total = 0;
        for (int i = 0; i < MPSoCConfig.NPORT; i++) {
            total += getPortTotalVolumeInFlits(i);
        }
        
        return total;
    }
    
    public int getRouterTotalBandwidthInCycles(){
        
        int total = 0;
        for (int i = 0; i < MPSoCConfig.NPORT; i++) {
            total += getPortTotalBadwidthInCycles(i);
        }
        
        return total;
    }
    
    public int getRouterTotalServicesVolumeInFlits(int[] services){
        
        int total = 0;
        for (int i = 0; i < MPSoCConfig.NPORT; i++) {
            total += getPortVolumeInFlit(i, services);
        }
        
        return total;
    }
    
    public int getRouterTotalServicesBandwidthInCycles(int[] services){
        
        int total = 0;
        for (int i = 0; i < MPSoCConfig.NPORT; i++) {
            total += getPortBandwidthInCycles(i, services);
        }
        
        return total;
    }
    
    public int getPortTotalVolumeInFlits(int port){
        return portInformations[port].getTotalVolumeInFlits();
    }
    
    public int getPortTotalBadwidthInCycles(int port){
        return portInformations[port].getTotalBandwidthInCycles();
    }
    
    
    
    public int getPortVolumeInFlit(int port, int[] services){
        
        PortInformation portInfo = portInformations[port];
        
        int servicesVolume = 0;
        
        for (int i = 0; i < services.length; i++) {
            servicesVolume += portInfo.getServiceVolumeInFlits(services[i]);
        }
        
        return servicesVolume;
    }
    
    public int getPortBandwidthInCycles(int port, int[] services){
        
        PortInformation portInfo = portInformations[port];
        
        int servicesBandwidth = 0;
        
        for (int i = 0; i < services.length; i++) {
            servicesBandwidth += portInfo.getServiceBandwidthInCycles(services[i]);
        }
        
        return servicesBandwidth;
    }
    

    private void updateTaskInformation(PacketInformation inputPacket) {
        
        //Pacote estra entrando no PE
        if (inputPacket.getTarget_router() == router_address){
            
            if (MPSoCConfig.TASK_ALLOCATION_SERVICES.contains(inputPacket.getService())){
                taskCounter++;
                tasks.add(new TaskInformation(inputPacket.getTask_source(), "ALLOCATED", inputPacket.getTime()));
            } else if (inputPacket.getService() == mPSoCConfig.getServiceValue("MESSAGE_DELIVERY")){
                tasks.add(new TaskInformation(inputPacket.getTask_target(), "MESSAGE_DELIVERY", inputPacket.getTime(), inputPacket.getTask_source()));
                //System.out.println("MESSAGE_DELIVERY received from task: prod: "+inputPacket.getTask_source());
            }
            
                   
        //Pacote esta saindo do PE
        } else if ((inputPacket.getInput_port() == MPSoCConfig.LOCAL1 || inputPacket.getInput_port() == MPSoCConfig.LOCAL0)){ 
            
            if (MPSoCConfig.TASK_TERMINATED_SERVICES.contains(inputPacket.getService())){
                tasks.add(new TaskInformation(inputPacket.getTask_source(), "TERMINATED", inputPacket.getTime()));
            } else if (inputPacket.getService() == mPSoCConfig.getServiceValue("MESSAGE_REQUEST")){
                tasks.add(new TaskInformation(inputPacket.getTask_target(), "MESSAGE_REQUEST", inputPacket.getTime(), inputPacket.getTask_source()));
                //System.out.println("MESSAGE_REQUEST send to task: prod: "+inputPacket.getTask_source());
            } 
        }
    }
    
    public int getPortThroughputInFlits(int port){
        return portInformations[port].getPortThroughputInFlits();
    }
    
    public int getPortBandwidthThroughputInCycles(int port){
        return portInformations[port].getPortBandwidthThroughputInCycles();
    }

    
    public void resetThroughput() {
        for (int i = 0; i < MPSoCConfig.NPORT; i++) {
            portInformations[i].resetPortThroughput();
        }
    }
    
    public void resetBandwidthThroughput() {
        for (int i = 0; i < MPSoCConfig.NPORT; i++) {
            portInformations[i].resetPortBandwidthThroughput();
        }
    }
    

    public int getTaskNumber() {
        return taskCounter;
    }
    
}
