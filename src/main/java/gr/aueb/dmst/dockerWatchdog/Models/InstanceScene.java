package gr.aueb.dmst.dockerWatchdog.Models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

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


    public InstanceScene(String id, String name,String image, String status, String memoryUsage, String pids, String cpuUsage, String blockI, String blockO, String volumes, String subnet, String gateway, Integer prefixLen ,boolean select) {
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

    public String getMemoryUsage() {
        return memoryUsage;
    }

    public String getPids() {
        return pids;
    }

    public String getCpuUsage() {
        return cpuUsage;
    }

    public String getBlockI() {
        return blockI;
    }

    public String getBlockO() {
        return blockO;
    }
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public void setMemoryUsage(String memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public void setPids(String pids) {
        this.pids = pids;
    }
    public String getImage() {
        return image;
    }

    public void setCpuUsage(String cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public void setBlockI(String blockI) {
        this.blockI = blockI;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setBlockO(String blockO) {
        this.blockO = blockO;
    }
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

    public BooleanProperty selectProperty() {
        return select;
    }

    public boolean isSelect() {
        return select.get();
    }

    public void setSelect(boolean select) {
        this.select.set(select);
    }
}
