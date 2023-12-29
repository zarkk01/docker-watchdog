package gr.aueb.dmst.dockerWatchdog.Models;

public class VolumeScene {
    private String name;
    private String driver;
    private String mountpoint;
    private String containernamesusing;

    public VolumeScene(String name, String driver,String mountpoint, String containernamesusing) {
        this.name = name;
        this.driver = driver;
        this.mountpoint = mountpoint;
        this.containernamesusing = containernamesusing;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getMountpoint() {
        return mountpoint;
    }

    public void setMountpoint(String mountpoint) {
        this.mountpoint = mountpoint;
    }

    public String getContainerNamesUsing() {
        return containernamesusing;
    }

    public void setContainerNamesUsing(String containernamesusing) {
        this.containernamesusing = containernamesusing;
    }
}
