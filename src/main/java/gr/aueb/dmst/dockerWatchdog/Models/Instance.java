package gr.aueb.dmst.dockerWatchdog.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.IdClass;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

/**
 * This class represents an Instance entity.
 * An Instance has an id, metricId, name, image, status, memoryUsage, pids, cpuUsage,
 * blockI, blockO, volumes, subnet, gateway, and prefixLen.
 * It is used as part of our REST API in our Spring Boot application.
 * It is, basically, the model of what we get from Instances table on our database.
 */
@Entity
@Table(name = "Instances")
@IdClass(InstanceId.class)
public class Instance {

    @Id
    @Column(name = "id")
    // Unique identifier for the instance
    private String id;

    @Id
    @Column(name = "metricid")
    // Metric id for the instance
    private Integer metricid;

    @Column(name = "name")
    // Name of the instance
    private String name;

    @Column(name = "image")
    // Image of the instance
    private String image;

    @Column(name = "status")
    // Status of the instance
    private String status;

    @Column(name = "memoryusage")
    // Memory usage of the instance
    private Long memoryUsage;

    @Column(name = "pids")
    // Pids of the instance
    private Long pids;

    @Column(name = "cpuusage")
    // CPU usage of the instance
    private Double cpuUsage;

    @Column(name = "blockI")
    // Block I of the instance
    private Double blockI;

    @Column(name = "blockO")
    // Block O of the instance
    private Double blockO;

    @Column(name = "volumes")
    // Volumes of the instance
    private String volumes;

    @Column(name = "subnet")
    // Subnet of the instance
    private String subnet;

    @Column(name = "gateway")
    // Gateway of the instance
    private String gateway;

    @Column(name = "prefixlen")
    // Prefix length of the instance
    private Integer prefixLen;

    /**
     * Returns the id of the instance.
     * @return The id of the instance.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the instance.
     * @param id The id to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the metric id of the instance.
     * @return The metric id of the instance.
     */
    public Integer getMetricid() {
        return metricid;
    }

    /**
     * Sets the metric id of the instance.
     * @param metricid The metric id to set.
     */
    public void setMetricId(Integer metricid) {
        this.metricid = metricid;
    }

    /**
     * Returns the name of the instance.
     * @return The name of the instance.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the instance.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the image of the instance.
     * @return The image of the instance.
     */
    public String getImage() {
        return image;
    }

    /**
     * Sets the image of the instance.
     * @param image The image to set.
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Returns the status of the instance.
     * @return The status of the instance.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the instance.
     * @param status The status to set.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the memory usage of the instance.
     * @return The memory usage of the instance.
     */
    public Long getMemoryUsage() {
        return memoryUsage;
    }

    /**
     * Sets the memory usage of the instance.
     * @param memoryUsage The memory usage to set.
     */
    public void setMemoryUsage(Long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    /**
     * Returns the pids of the instance.
     * @return The pids of the instance.
     */
    public Long getPids() {
        return pids;
    }

    /**
     * Sets the pids of the instance.
     * @param pids The pids to set.
     */
    public void setPids(Long pids) {
        this.pids = pids;
    }

    /**
     * Returns the CPU usage of the instance.
     * @return The CPU usage of the instance.
     */
    public Double getCpuUsage() {
        return cpuUsage;
    }

    /**
     * Sets the CPU usage of the instance.
     * @param cpuUsage The CPU usage to set.
     */
    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    /**
     * Returns the block I of the instance.
     * @return The block I of the instance.
     */
    public Double getBlockI() {
        return blockI;
    }

    /**
     * Sets the block I of the instance.
     * @param blockI The block I to set.
     */
    public void setBlockI(Double blockI) {
        this.blockI = blockI;
    }

    /**
     * Returns the block O of the instance.
     * @return The block O of the instance.
     */
    public Double getBlockO() {
        return blockO;
    }

    /**
     * Sets the block O of the instance.
     * @param blockO The block O to set.
     */
    public void setBlockO(Double blockO) {
        this.blockO = blockO;
    }

    /**
     * Returns the volumes of the instance.
     * @return The volumes of the instance.
     */
    public String getVolumes() {
        return volumes;
    }

    /**
     * Sets the volumes of the instance.
     * @param volumes The volumes to set.
     */
    public void setVolumes(String volumes) {
        this.volumes = volumes;
    }

    /**
     * Returns the subnet of the instance.
     * @return The subnet of the instance.
     */
    public String getSubnet() {
        return subnet;
    }

    /**
     * Sets the subnet of the instance.
     * @param subnet The subnet to set.
     */
    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    /**
     * Returns the gateway of the instance.
     * @return The gateway of the instance.
     */
    public String getGateway() {
        return gateway;
    }

    /**
     * Sets the gateway of the instance.
     * @param gateway The gateway to set.
     */
    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    /**
     * Returns the prefix length of the instance.
     * @return The prefix length of the instance.
     */
    public Integer getPrefixLen() {
        return prefixLen;
    }

    /**
     * Sets the prefix length of the instance.
     * @param prefixLen The prefix length to set.
     */
    public void setPrefixLen(Integer prefixLen) {
        this.prefixLen = prefixLen;
    }
}
