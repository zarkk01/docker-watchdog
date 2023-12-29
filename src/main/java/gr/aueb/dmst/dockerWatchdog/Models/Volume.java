package gr.aueb.dmst.dockerWatchdog.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "Volumes")
public class Volume {

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "driver")
    private String driver;

    @Column(name = "mountpoint")
    private String mountpoint;

    @Column(name = "containerNamesUsing")
    private String containerNamesUsing;

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