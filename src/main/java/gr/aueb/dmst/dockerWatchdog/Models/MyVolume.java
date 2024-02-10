package gr.aueb.dmst.dockerWatchdog.Models;

import java.util.ArrayList;

import gr.aueb.dmst.dockerWatchdog.Main;

/**
 * This class represents a volume object.
 * ΜyVolume is a custom class we created to represent
 * Docker's Java API original volumes. In this way, we have better control
 * over them, and we assign them specific properties and values we need.
 */
public class MyVolume {

    private final String name;
    private final String driver;
    private final String mountpoint;
    private ArrayList<String> containerNamesUsing;

    /**
     * Constructor for the MyVolume class.
     *
     * @param name The name of the volume.
     * @param driver The driver of the volume.
     * @param mountpoint The mountpoint of the volume.
     * @param containerNamesUsing The list of container names using the volume.
     */
    public MyVolume(
            String name,
            String driver,
            String mountpoint,
            ArrayList<String> containerNamesUsing
    ) {
        this.name = name;
        this.driver = driver;
        this.mountpoint = mountpoint;
        this.containerNamesUsing = containerNamesUsing;
    }

    /**
     * Returns a string representation of the MyVolume object.
     *
     * @return A string representation of the MyVolume object.
     */
    @Override
    public String toString() {
        return "Name = " + name
                + " , Driver = " + driver
                + " , Mountpoint = " + mountpoint;
    }

    /**
     * Returns the name of the volume.
     *
     * @return The name of the volume.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the driver of the volume.
     *
     * @return The driver of the volume.
     */
    public String getDriver() {
        return driver;
    }

    /**
     * Returns the mountpoint of the volume.
     *
     * @return The mountpoint of the volume.
     */
    public String getMountpoint() {
        return mountpoint;
    }

    /**
     * Returns the list of container names using the volume.
     *
     * @return The list of container names using the volume.
     */
    public ArrayList<String> getContainerNamesUsing() {
        return containerNamesUsing;
    }

    /**
     * Adds a container name to the list of container names using the volume.
     *
     * @param containerName The name of the container to add.
     */
    public void addContainerNameUsing(String containerName) {
        this.containerNamesUsing.add(containerName);
    }

    /**
     * Removes a container name from the list of container names using the volume.
     *
     * @param containerName The name of the container to remove.
     */
    public void removeContainerNameUsing(String containerName) {
        this.containerNamesUsing.remove(containerName);
    }

    /**
     * Retrieves a MyVolume instance from the myVolumesList based on its name.
     *
     * @param name The name of the volume to search for.
     * @return The MyVolume instance with the specified name, or null if not found.
     */
    public static MyVolume getVolumeByName(String name) {
        MyVolume volumeToReturn = null;
        for (MyVolume volume: Main.myVolumes) {
            if (name.equals(volume.getName())) {
                volumeToReturn = volume;
            }
        }
        return volumeToReturn;
    }
}
