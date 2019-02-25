/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package source;

import util.MPSoCConfig;

/**
 *
 * @author Marcelo
 */
public class PacketInformation {

    private int router_address;
    private int time;
    private int service;
    private int size;
    private int bandwidthCycles;
    private int channel;
    private int input_port;
    private int target_router;
    private int task_source;
    private int task_target;

    public PacketInformation(int router_address, int time, int service, int size, int bandwidthCycles, int input_port, int target_router) {
        this.router_address = router_address;
        this.time = time;
        this.service = service;
        this.size = size;
        this.bandwidthCycles = bandwidthCycles;
        this.input_port = input_port;
        if(this.input_port == MPSoCConfig.LOCAL0 || this.input_port == MPSoCConfig.EAST0 || this.input_port == MPSoCConfig.NORTH0 || this.input_port == MPSoCConfig.EAST0 || this.input_port == MPSoCConfig.SOUTH0){
            this.channel = MPSoCConfig.HIGH;
        } else {
            this.channel = MPSoCConfig.LOW;
        }
        this.target_router = target_router;
        this.task_source = -1;
        this.task_target = -1;
    }

    public PacketInformation(int router_address, int time, int service, int size, int bandwidthCycles, int input_port, int target_router, int task_source, int task_target) {
        this.router_address = router_address;
        this.time = time;
        this.service = service;
        this.size = size;
        this.bandwidthCycles = bandwidthCycles;
        this.input_port = input_port;
        
        if(this.input_port == MPSoCConfig.LOCAL0 || this.input_port == MPSoCConfig.EAST0 || this.input_port == MPSoCConfig.NORTH0 || this.input_port == MPSoCConfig.EAST0 || this.input_port == MPSoCConfig.SOUTH0){
            this.channel = MPSoCConfig.HIGH;
        } else {
            this.channel = MPSoCConfig.LOW;
        }
        
        this.target_router = target_router;
        this.task_source = task_source;
        this.task_target = task_target;
    }
    
    public void printPacket(){
        System.out.println("*********PACKET************");
        System.out.println("router_address: "+router_address);
        System.out.println("time: "+time);
        System.out.println("service: "+service);
        System.out.println("size: "+size);
        System.out.println("bandwidth cycles: "+bandwidthCycles);
        System.out.println("channel: "+channel);
        System.out.println("input_port: "+input_port);
        System.out.println("target_router: "+target_router);
        System.out.println("task_source: "+task_source);
        System.out.println("task_target: "+task_target);
    }

    public int getRouter_address() {
        return router_address;
    }

    public void setRouter_address(int router_address) {
        this.router_address = router_address;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getService() {
        return service;
    }

    public void setService(int service) {
        this.service = service;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getInput_port() {
        return input_port;
    }
    
    public String getInput_portString(){
        switch(input_port){
            case MPSoCConfig.LOW:
                return "LOW";
            case MPSoCConfig.HIGH:
                return "HIGH";
        }
        return null;
    }

    public void setInput_port(int input_port) {
        this.input_port = input_port;
    }

    public int getTask_source() {
        return task_source;
    }

    public void setTask_source(int task_source) {
        this.task_source = task_source;
    }

    public int getTask_target() {
        return task_target;
    }

    public void setTask_target(int task_target) {
        this.task_target = task_target;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTarget_router() {
        return target_router;
    }

    public void setTarget_router(int target_router) {
        this.target_router = target_router;
    }

    public int getBandwidthCycles() {
        return bandwidthCycles;
    }

    public void setBandwidthCycles(int bandwidthCycles) {
        this.bandwidthCycles = bandwidthCycles;
    }
}
