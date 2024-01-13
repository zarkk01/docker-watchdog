package gr.aueb.dmst.dockerWatchdog.Models;

import java.util.ArrayList;

import gr.aueb.dmst.dockerWatchdog.Main;

/**
 * This class represents a container object.
 * ΜyInstance is a custom class we created to represent
 * Docker's Java API original containers. In this way, we have better control
 * over them, and we assign them specific properties and values we need.
 */
public class MyInstance {
    private final String id;
    private String name;
    private final String image;
    private String status;
    private long pids;
    private long memoryUsage;
    private double cpuUsage;
    private double blockI;
    private double blockO;
    private ArrayList<String> volumes;
    private String subnet;
    private String gateway;
    private int prefixLen;

    /**
     * Constructor for the MyInstance class.
     *
     * @param id The id of the instance.
     * @param name The name of the instance.
     * @param image The image of the instance.
     * @param status The status of the instance.
     * @param cpuUsage The CPU usage of the instance.
     * @param memoryUsage The memory usage of the instance.
     * @param pids The pids of the instance.
     * @param blockI The block I of the instance.
     * @param blockO The block O of the instance.
     * @param volumes The volumes of the instance.
     * @param subnet The subnet of the instance.
     * @param gateway The gateway of the instance.
     * @param prefixLen The prefix length of the instance.
     */
    public MyInstance(String id,
                      String name,
                      String image,
                      String status,
                      double cpuUsage,
                      long memoryUsage,
                      long pids,
                      double blockI,
                      double blockO,
                      ArrayList<String> volumes,
                      String subnet,
                      String gateway,
                      int prefixLen) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.status = status;
        this.memoryUsage = memoryUsage;
        this.pids = pids;
        this.cpuUsage = cpuUsage;
        this.blockI = blockI;
        this.blockO = blockO;
        this.volumes = volumes;
        this.subnet = subnet;
        this.gateway = gateway;
        this.prefixLen = prefixLen;
    }

    /**
     * Returns a string representation of the MyInstance object.
     *
     * @return A string representation of the MyInstance object.
     */
    @Override
    public String toString() {
        return "Name = " + name.substring(1) + " , ID = " + id + ", " + " , Image = " + image
                + " , Status = " + status + " , CPU Usage: " + String.format("%.2f", cpuUsage * 100)
                + " %" + " , Memory usage : " + String.format("%.2f", (double) memoryUsage) + " MB"
                + " , PIDs : " + pids + " , Block I/0 : " + String.format("%.2f", blockI)
                + "MB/" + String.format("%.2f", blockO) + "MB" + " , Volumes : " + volumes
                + " , Subnet : " + subnet + " , Gateway : " + gateway + " , PrefixLen : " + prefixLen;
    }

    /**
     * Returns the id of the instance.
     *
     * @return The id of the instance.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the image of the instance.
     *
     * @return The image of the instance.
     */
    public String getImage() {
        return image;
    }

    /**
     * Returns the status of the instance.
     *
     * @return The status of the instance.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the name of the instance.
     *
     * @return The name of the instance.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the memory usage of the instance.
     *
     * @return The memory usage of the instance.
     */
    public long getMemoryUsage() {
        return memoryUsage;
    }

    /**
     * Returns the CPU usage of the instance.
     *
     * @return The CPU usage of the instance.
     */
    public double getCpuUsage() {
        return cpuUsage;
    }

    /**
     * Returns the pids of the instance.
     *
     * @return The pids of the instance.
     */
    public long getPids() {
        return pids;
    }

    /**
     * Returns the block I of the instance.
     *
     * @return The block I of the instance.
     */
    public double getBlockI() {
        return blockI;
    }

    /**
     * Returns the block O of the instance.
     *
     * @return The block O of the instance.
     */
    public double getBlockO() {
        return blockO;
    }

    /**
     * Returns the volumes of the instance.
     *
     * @return The volumes of the instance.
     */
    public ArrayList<String> getVolumes() {
        return volumes;
    }

    /**
     * Returns the subnet of the instance.
     *
     * @return The subnet of the instance.
     */
    public String getSubnet() {
        return subnet;
    }

    /**
     * Returns the gateway of the instance.
     *
     * @return The gateway of the instance.
     */
    public String getGateway() {
        return gateway;
    }

    /**
     * Returns the prefix length of the instance.
     *
     * @return The prefix length of the instance.
     */
    public int getPrefixLen() {
        return prefixLen;
    }

    /**
     * Sets the status of the instance.
     *
     * @param status The status to set.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Sets the name of the instance.
     *
     * @param newName The name to set.
     */
    public void setName(String newName) {
        this.name = newName;
    }

    /**
     * Sets the memory usage of the instance.
     *
     * @param memoryUsage The memory usage to set.
     */
    public void setMemoryUsage(long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    /**
     * Sets the CPU usage of the instance.
     *
     * @param cpuUsage The CPU usage to set.
     */
    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    /**
     * Sets the pids of the instance.
     *
     * @param pids The pids to set.
     */
    public void setPids(long pids) {
        this.pids = pids;
    }

    /**
     * Sets the block I of the instance.
     *
     * @param blockI The block I to set.
     */
    public void setBlockI(double blockI) {
        this.blockI = blockI;
    }

    /**
     * Sets the block O of the instance.
     *
     * @param blockO The block O to set.
     */
    public void setBlockO(double blockO) {
        this.blockO = blockO;
    }

    /**
     * Sets the volumes of the instance.
     *
     * @param volumes The volumes to set.
     */
    public void setVolume(ArrayList<String> volumes) {
        this.volumes = volumes;
    }

    /**
     * Adds a volume to the instance.
     *
     * @param volumeName The name of the volume to add.
     */
    public void addVolume(String volumeName) {
        this.volumes.add(volumeName);
    }

    /**
     * Sets the subnet of the instance.
     *
     * @param subnet The subnet to set.
     */
    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    /**
     * Sets the gateway of the instance.
     *
     * @param gateway The gateway to set.
     */
    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    /**
     * Sets the prefix length of the instance.
     *
     * @param prefixLen The prefix length to set.
     */
    public void setPrefixLen(int prefixLen) {
        this.prefixLen = prefixLen;
    }

    /**
     * Retrieves a MyInstance instance from the myInstancesList based on its ID.
     *
     * @param id The ID of the instance to search for.
     * @return The MyInstance instance with the specified ID, or null if not found.
     */
    public static MyInstance getInstanceByid(String id) {
        MyInstance instanceToReturn = null;
        for (MyInstance instance: Main.myInstances) {
            if (id.equals(instance.getId())) {
                instanceToReturn = instance;
            }
        }
        return instanceToReturn;
    }
}
