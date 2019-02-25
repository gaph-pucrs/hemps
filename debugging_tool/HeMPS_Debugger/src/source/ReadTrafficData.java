/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import util.MPSoCConfig;

/**
 *
 * @author Marcelo
 */
public final class ReadTrafficData {

    private RandomAccessFile traffic;
    private int packetReadControl;
    private ArrayList<PacketInformation> allPackets;
    private MPSoCConfig mPSoCConfig;
    private RouterNeighbors n;

    public ReadTrafficData(MPSoCConfig mPSoCConfig, RouterNeighbors n) throws IOException {
        this.mPSoCConfig = mPSoCConfig;
        this.n = n;
        resetPacketCounter();
        allPackets = new ArrayList<PacketInformation>();
        
        File f = new File(this.mPSoCConfig.getDebugDirPath()+"/traffic_router.txt");
        if (f.exists()) {
            try {
                traffic = new RandomAccessFile(f, "r");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ReadTrafficData.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            throw new IOException();
        }
    }
    
    public void resetPacketCounter(){
        packetReadControl = 0;
    }
    
    public int getPacketCounterByTime(int time){
        int counter = 0;
        
        if (allPackets.get(allPackets.size()-1).getTime() < time || time < 0)
            return -1;
        
        counter = searchPacketTime(time, 0, allPackets.size()-1);
        
        
        while(allPackets.get(counter).getTime() <= time){
            counter++;
        }
        
        return counter--;
    }
    
    private int searchPacketTime(int time, int min, int max){
       if (max - min < 2)
           return min;
       
       if (allPackets.get(min+((max-min)/2)).getTime() < time)
           return searchPacketTime(time, min+((max-min)/2), max);
       
       return searchPacketTime(time, min, min+((max-min)/2));
    }
    
    
    public PacketInformation getNextPacket(){
        
        PacketInformation packet = null;
        
        if (packetReadControl == allPackets.size()){
            try {
                packet = getPacket(traffic.readLine());
                if (packet != null){
                    allPackets.add(packet);
                    packetReadControl++;
                }
            } catch (IOException ex) {}
        } else {
            packet = allPackets.get(packetReadControl);
            packetReadControl++;
        }
        
        
        
        return packet;
        
    }

    private PacketInformation getPacket(String line) {
        

        //System.out.println(line);
        
        if (line == null) {
            return null;
        }

        int router_address = 0;
        int time = 0;
        int service = 0;
        int size = 0;
        int bandwidthCycles = 0;
        int input_port = 0;
        int target_router = 0;
        int task_source = -1;
        int task_target = -1;

        String[] splitedLine = line.split("\t");
        
        time = Integer.parseInt(splitedLine[0]);
        router_address = extractRouterAddress(splitedLine[1]);
        service = Integer.parseInt(splitedLine[2]);
        size = Integer.parseInt(splitedLine[3]);
        bandwidthCycles = Integer.parseInt(splitedLine[4]);
        if (mPSoCConfig.getChannel_number() == 1)
            input_port = ((Integer.parseInt(splitedLine[5])) * 2) + 1 ; //Duplicated phsysical channel support
        else//Assuming always 2
            input_port = ((Integer.parseInt(splitedLine[5])));
        target_router = extractRouterAddress(splitedLine[6]);
        
        if (splitedLine.length > 7){
            task_source = Integer.parseInt(splitedLine[7]);
            if (splitedLine.length > 8){
                task_target = Integer.parseInt(splitedLine[8]);
            }
        }
        
        //System.out.println("**********\nline: "+line);
        //System.out.println(time+" "+router_address+" "+service+" "+size+" "+bandwidthCycles+" "+input_port+" "+target_router+" "+task_source+" "+task_target);
        
        return new PacketInformation(router_address, time, service, size, bandwidthCycles, input_port, target_router, task_source, task_target);

    }
    
    private int extractRouterAddress(String value){
        
         /*Trecho para representacao hexadecimal
                int a = packet_readed.getRouter_address();
                a = Integer.parseInt(Integer.toString(a), 16);
                packet_readed.setRouter_address(a);
                
                a = packet_readed.getTarget_router();
                a = Integer.parseInt(Integer.toString(a), 16);
                packet_readed.setTarget_router(a);
                 */
        
        int address = Integer.parseInt(value);
        if (mPSoCConfig.getRouterAddressing() == MPSoCConfig.XY){
            return n.xy_to_ham_addr(address);
        }
        return address;
    }

    public ArrayList<PacketInformation> getAllPackets() {
        return allPackets;
    }
}
