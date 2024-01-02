package gr.aueb.dmst.dockerWatchdog.Models;

import gr.aueb.dmst.dockerWatchdog.Main;

import java.util.ArrayList;

public class MyInstance {
    private final String id; // ID of instance is final
    private String name; // Instance name
    private final String image; // Image of instance
    private String status; // Status of instance
    private long pids; // PIDs of instance
    private long memoryUsage; // Memory usage of instance in MB
    private double cpuUsage; // CPU usage of instance in %
    private double blockI; // Block I of instance in MB
    private double blockO; // Block O of instance in MB
    private String ports; // Ports of instance
    private ArrayList<String> volumes; // Volumes of instance
    private String subnet;
    private String gateway;
    private int prefixLen;

    // Constructor
    public MyInstance(String id, String name, String image, String status, double cpuUsage, long memoryUsage, long pids, double blockI, double blockO,String ports,ArrayList<String> volumes,String subnet,String gateway,int prefixLen) {

        // Initialize instance variables with the values of the parameters
        this.id = id;
        this.name = name;
        this.image = image;
        this.status = status;
        this.memoryUsage = memoryUsage;
        this.pids = pids;
        this.cpuUsage = cpuUsage;
        this.blockI = blockI;
        this.blockO = blockO;
        this.ports = ports;
        this.volumes = volumes;
        this.subnet = subnet;
        this.gateway = gateway;
        this.prefixLen = prefixLen;
    }

    // Method toString that returns a string with the values of the instance variables
    @Override
    public String toString() {
        return "Name = " + name.substring(1) + " , ID = " + id + ", " + " , Image = " + image +
                " , Status = " + status + " , Port(s) : " + ports + " , CPU Usage: " + String.format("%.2f", cpuUsage * 100) +
                " %" + " , Memory usage : " + String.format("%.2f", (double) memoryUsage) + " MB" +
                " , PIDs : " + pids + " , Block I/0 : " + String.format("%.2f", blockI) +
                "MB/" + String.format("%.2f", blockO) + "MB" + " , Volumes : " + volumes + " , Subnet : " + subnet + " , Gateway : " + gateway + " , PrefixLen : " + prefixLen;
    }

    // Getter for id
    public String getId() {
        return id;
    }

    // Getter for image
    public String getImage() {
        return image;
    }

    // Getter for status
    public String getStatus() {
        return status;
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Getter for memoryUsage
    public long getMemoryUsage() {
        return memoryUsage;
    }

    // Getter for cpuUsage
    public void setMemoryUsage(long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    // Getter for PIDs
    public long getPids() {
        return pids;
    }

    // Getter for blockI
    public double getBlockI() {
        return blockI;
    }

    // Getter for blockO
    public double getBlockO() {
        return blockO;
    }

    //Getter for cpuUsage
    public double getCpuUsage() {
        return cpuUsage;
    }

    //Getter for ports
    public String getPorts() {return ports;}

    // Setter for cpuUsage
    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    // Setter for name
    public void setName(String newName) {
        this.name = newName;
    }

    // Setter for image
    public void setStatus(String status) {
        this.status = status;
    }

    // Setter for PIDs
    public void setPids(long pids) {
        this.pids = pids;
    }

    // Setter for blockI
    public void setBlockI(double blockI) {
        this.blockI = blockI;
    }

    // Setter for blockO
    public void setBlockO(double blockO) {
        this.blockO = blockO;
    }

    public ArrayList<String> getVolumes() {
        return volumes;
    }

    public void setVolume(ArrayList<String> volumes) {
        this.volumes = volumes;
    }

    public void addVolume(String volumeName) {
        this.volumes.add(volumeName);
    }

    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public int getPrefixLen() {
        return prefixLen;
    }

    public void setPrefixLen(int prefixLen) {
        this.prefixLen = prefixLen;
    }

    // Given an ID of an instance, return the instance
    public static MyInstance getInstanceByid(String id) {
        MyInstance instanceToReturn = null;
        for (MyInstance instance: Main.myInstancesList) {
            if (id.equals(instance.getId())) {
                instanceToReturn = instance;
            }
        }
        if (instanceToReturn != null) {
            return instanceToReturn;
        } else {
            return null;
        }
    }
}