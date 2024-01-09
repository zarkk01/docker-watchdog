package gr.aueb.dmst.dockerWatchdog.Models;

import gr.aueb.dmst.dockerWatchdog.Main;

import java.util.ArrayList;

public class MyVolume {

    private final String name;
    private final String driver;
    private final String mountpoint;
    private ArrayList<String> containerNamesUsing;

    public MyVolume(String name, String driver, String mountpoint,ArrayList<String> containerNamesUsing) {
        this.name = name;
        this.driver = driver;
        this.mountpoint = mountpoint;
        this.containerNamesUsing = containerNamesUsing;
    }

    @Override
    public String toString() {
        return "Name = " + name + " , Driver = " + driver + " , Mountpoint = " + mountpoint;
    }

    public String getName() {
        return name;
    }

    public String getDriver() {
        return driver;
    }

    public String getMountpoint() {
        return mountpoint;
    }

    public ArrayList<String> getContainerNamesUsing() {
        return containerNamesUsing;
    }

    public void addContainerNameUsing(String containerName) {
        this.containerNamesUsing.add(containerName);
    }

    public void setContainerNamesUsing(ArrayList<String> containerNamesUsing) {
        this.containerNamesUsing = containerNamesUsing;
    }

    public void removeContainerNameUsing(String containerName) {
        this.containerNamesUsing.remove(containerName);
    }

    public static MyVolume getVolumeByName(String name) {
        MyVolume volumeToReturn = null;
        for (MyVolume volume: Main.myVolumes) {
            if (name.equals(volume.getName())) {
                volumeToReturn = volume;
            }
        }
        if (volumeToReturn != null) {
            return volumeToReturn;
        } else {
            return null;
        }
    }
}