/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package source;

import java.util.HashMap;
import util.MPSoCConfig;

/**
 *
 * @author mruaro
 */
public class PortInformation {
    
    private int channel;
    private int totalVolume;
    private int totalBandwidth;
    private HashMap<Integer, Integer> serviceVolume;
    private HashMap<Integer, Integer> serviceBandwidth;
    private int throughput;
    private int bandwidth_throughput;
    
    
    public PortInformation(MPSoCConfig mPSoCConfig, int channel){
        this.channel = channel;
        totalVolume = 0;
        totalBandwidth = 0;
        throughput = 0;
        bandwidth_throughput = 0;
        serviceVolume = new HashMap<Integer, Integer>();
        serviceBandwidth = new HashMap<Integer, Integer>();
        
        int[] serviceRef = mPSoCConfig.getServiceReference();
        
        for (int i = 0; i < serviceRef.length; i++) {
            serviceVolume.put(serviceRef[i], 0);
            serviceBandwidth.put(serviceRef[i], 0);
        }
    }
    
    public void addNewInformation(PacketInformation inputPacket){
        totalVolume += inputPacket.getSize() + 1; //+1 por causa do header
        throughput += inputPacket.getSize() + 1;
        
        totalBandwidth += inputPacket.getBandwidthCycles();
        bandwidth_throughput += inputPacket.getBandwidthCycles();
        
        int service = inputPacket.getService();
        
        int volume_aux = serviceVolume.get(service);
        volume_aux += inputPacket.getSize() + 1;
        serviceVolume.put(service, volume_aux);
        
        int bandwidth_aux = serviceBandwidth.get(service);
        bandwidth_aux += inputPacket.getBandwidthCycles();
        serviceBandwidth.put(service, bandwidth_aux);
    }
    
    public int getServiceVolumeInFlits(int service){
        return serviceVolume.get(service);
    }
    
    public int getServiceBandwidthInCycles(int service){
        return serviceBandwidth.get(service);
    }

    public int getChannel() {
        return channel;
    }

    public int getTotalVolumeInFlits() {
        return totalVolume;
    }
    
    public int getTotalBandwidthInCycles(){
        return totalBandwidth;
    }

    void resetPortThroughput() {
        throughput = 0;
    }
    
    void resetPortBandwidthThroughput() {
        bandwidth_throughput = 0;
    }
    
    public int getPortThroughputInFlits(){
        return throughput;
    }
    
    public int getPortBandwidthThroughputInCycles(){
        return bandwidth_throughput;
    }

    
    
}
