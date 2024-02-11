package gr.aueb.dmst.dockerWatchdog.Models;


/**
 * This class represents a Volume that can be displayed in our GUI panels.
 * A VolumeScene has a name, driver, mountpoint, and a list of container names using it
 * , and it is what the user actually sees on the Volumes panel.
 */
public class VolumeScene {
    private String name;
    private String driver;
    private String mountpoint;
    private String containernamesusing;

    /**
     * Constructor for the VolumeScene class.
     *
     * @param name The name of the volume.
     * @param driver The driver of the volume.
     * @param mountpoint The mountpoint of the volume.
     * @param containernamesusing The list of container names using the specific volume.
     */
    public VolumeScene(String name, String driver, String mountpoint, String containernamesusing) {
        this.name = name;
        this.driver = driver;
        this.mountpoint = mountpoint;
        this.containernamesusing = containernamesusing;
    }

    /**
     * Returns the name of the volume scene.
     *
     * @return The name of the volume scene.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the volume scene.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the driver of the volume scene.
     *
     * @return The driver of the volume scene.
     */
    public String getDriver() {
        return driver;
    }

    /**
     * Sets the driver of the volume scene.
     *
     * @param driver The driver to set.
     */
    public void setDriver(String driver) {
        this.driver = driver;
    }

    /**
     * Returns the mountpoint of the volume scene.
     *
     * @return The mountpoint of the volume scene.
     */
    public String getMountpoint() {
        return mountpoint;
    }

    /**
     * Sets the mountpoint of the volume scene.
     *
     * @param mountpoint The mountpoint to set.
     */
    public void setMountpoint(String mountpoint) {
        this.mountpoint = mountpoint;
    }

    /**
     * Returns the list of container names using the volume scene.
     *
     * @return The list of container names using the volume scene.
     */
    public String getContainerNamesUsing() {
        return containernamesusing;
    }

    /**
     * Sets the list of container names using the volume scene.
     *
     * @param containernamesusing The list of container names to set.
     */
    public void setContainerNamesUsing(String containernamesusing) {
        this.containernamesusing = containernamesusing;
    }
}
