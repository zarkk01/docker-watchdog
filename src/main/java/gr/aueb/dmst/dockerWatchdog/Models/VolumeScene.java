package gr.aueb.dmst.dockerWatchdog.Models;

public class VolumeScene {
    private String name;
    private String driver;
    private String mountpoint;
    private String containerNamesUsing;

    public VolumeScene(String name, String driver,String mountpoint, String containerNamesUsing) {
        this.name = name;
        this.driver = driver;
        this.mountpoint = mountpoint;
        this.containerNamesUsing = containerNamesUsing;
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
        return containerNamesUsing;
    }

    public void setContainerNamesUsing(String containerNamesUsing) {
        this.containerNamesUsing = containerNamesUsing;
    }
}
