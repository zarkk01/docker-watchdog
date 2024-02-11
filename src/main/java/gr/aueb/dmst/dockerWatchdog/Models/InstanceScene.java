package gr.aueb.dmst.dockerWatchdog.Models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;


/**
 * This class represents an Instance that be displayed in GUI panels.
 * An InstanceScene has an id, name, status, memoryUsage, pids, cpuUsage, blockI, blockO,
 * image, volumes, subnet, gateway, prefixLen, and select.
 * It is used as part of our JavaFX application to represent instances in a scene.
 */
public class InstanceScene {
    private String id;
    private String name;
    private String status;
    private String memoryUsage;
    private String pids;
    private String cpuUsage;
    private String blockI;
    private String blockO;
    private String image;
    private String volumes;
    private String subnet;
    private String gateway;
    private Integer prefixLen;
    private final BooleanProperty select;

    /**
     * Constructor for the InstanceScene class.
     *
     * @param id The id of the instance.
     * @param name The name of the instance.
     * @param image The image of the instance.
     * @param status The status of the instance.
     * @param memoryUsage The memory usage of the instance.
     * @param pids The pids of the instance.
     * @param cpuUsage The CPU usage of the instance.
     * @param blockI The block I of the instance.
     * @param blockO The block O of the instance.
     * @param volumes The volumes of the instance.
     * @param subnet The subnet of the instance.
     * @param gateway The gateway of the instance.
     * @param prefixLen The prefix length of the instance.
     * @param select The select property of the instance.
     */
    public InstanceScene(String id,
                         String name,
                         String image,
                         String status,
                         String memoryUsage,
                         String pids,
                         String cpuUsage,
                         String blockI,
                         String blockO,
                         String volumes,
                         String subnet,
                         String gateway,
                         Integer prefixLen,
                         boolean select) {
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
        this.select = new SimpleBooleanProperty(select);
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
     * Returns the name of the instance.
     *
     * @return The name of the instance.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the instance.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
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
     * Sets the status of the instance.
     *
     * @param status The status to set.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the memory usage of the instance.
     *
     * @return The memory usage of the instance.
     */
    public String getMemoryUsage() {
        return memoryUsage;
    }

    /**
     * Returns the pids of the instance.
     *
     * @return The pids of the instance.
     */
    public String getPids() {
        return pids;
    }

    /**
     * Sets the pids of the instance.
     *
     * @param pids The pids to set.
     */
    public void setPids(String pids) {
        this.pids = pids;
    }

    /**
     * Returns the CPU usage of the instance.
     *
     * @return The CPU usage of the instance.
     */
    public String getCpuUsage() {
        return cpuUsage;
    }

    /**
     * Returns the block I of the instance.
     *
     * @return The block I of the instance.
     */
    public String getBlockI() {
        return blockI;
    }

    /**
     * Returns the block O of the instance.
     *
     * @return The block O of the instance.
     */
    public String getBlockO() {
        return blockO;
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
     * Returns the volumes of the instance.
     *
     * @return The volumes of the instance.
     */
    public String getVolumes() {
        return volumes;
    }

    /**
     * Sets the volumes of the instance.
     *
     * @param volumes The volumes to set.
     */
    public void setVolumes(String volumes) {
        this.volumes = volumes;
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
    public Integer getPrefixLen() {
        return prefixLen;
    }

    /**
     * Returns the select property of the instance.
     *
     * @return The select property of the instance.
     */
    public boolean isSelect() {
        return select.get();
    }

    /**
     * Sets the select property of the instance.
     *
     * @param select The select property to set.
     */
    public void setSelect(boolean select) {
        this.select.set(select);
    }
}
