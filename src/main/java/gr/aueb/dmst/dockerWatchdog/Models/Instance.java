package gr.aueb.dmst.dockerWatchdog.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "Instances")
@IdClass(InstanceId.class)
public class Instance {

    @Id
    @Column(name = "id")
    private String id;

    @Id
    @Column(name = "metricid")
    private Integer metricid;

    @Column(name = "name")
    private String name;

    @Column(name = "image")
    private String image;

    @Column(name = "status")
    private String status;

    @Column(name = "memoryusage")
    private Long memoryUsage;

    @Column(name = "pids")
    private Long pids;

    @Column(name = "cpuusage")
    private Double cpuUsage;

    @Column(name = "blockI")
    private Double blockI;

    @Column(name = "blockO")
    private Double blockO;

    @Column(name = "volumes")
    private String volumes;

    @Column(name = "subnet")
    private String subnet;

    @Column(name = "gateway")
    private String gateway;

    @Column(name = "prefixlen")
    private Integer prefixLen;

    public String getVolumes() {
        return volumes;
    }

    public void setVolumes(String volumes) {
        this.volumes = volumes;
    }

    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public Integer getPrefixLen() {
        return prefixLen;
    }

    public void setPrefixLen(Integer prefixLen) {
        this.prefixLen = prefixLen;
    }

    public String getId() {
        return id;
    }

    public Integer getMetricid() {
        return metricid;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getStatus() {
        return status;
    }

    public Long getMemoryUsage() {
        return memoryUsage;
    }

    public Long getPids() {
        return pids;
    }

    public Double getCpuUsage() {
        return cpuUsage;
    }

    public Double getBlockI() {
        return blockI;
    }

    public Double getBlockO() {
        return blockO;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setMetricId(Integer metricId) {
        this.metricid = metricId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMemoryUsage(Long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public void setPids(Long pids) {
        this.pids = pids;
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public void setBlockI(Double blockI) {
        this.blockI = blockI;
    }

    public void setBlockO(Double blockO) {
        this.blockO = blockO;
    }
}