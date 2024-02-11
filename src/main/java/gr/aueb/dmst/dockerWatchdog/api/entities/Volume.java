package gr.aueb.dmst.dockerWatchdog.api.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;


/**
 * This class represents a Volume entity.
 * * It is used as part of our REST API in our Spring Boot application.
 * A Volume has a name, driver, mountpoint, and a list of container names that are using it.
 * It is, basically, the model of what we get from Volumes table on our database.
 */
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

    @Column(name = "containernamesusing")
    private String containernamesusing;

    /**
     * Returns the name of the volume.
     *
     * @return The name of the volume.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the volume.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
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
     * Sets the driver of the volume.
     *
     * @param driver The driver to set.
     */
    public void setDriver(String driver) {
        this.driver = driver;
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
     * Sets the mountpoint of the volume.
     *
     * @param mountpoint The mountpoint to set.
     */
    public void setMountpoint(String mountpoint) {
        this.mountpoint = mountpoint;
    }

    /**
     * Returns the list of container names using the volume.
     *
     * @return The list of container names using the volume.
     */
    public String getContainerNamesUsing() {
        return containernamesusing;
    }

    /**
     * Sets the list of container names using the volume.
     *
     * @param containernamesusing The list of container names to set.
     */
    public void setContainerNamesUsing(String containernamesusing) {
        this.containernamesusing = containernamesusing;
    }
}
