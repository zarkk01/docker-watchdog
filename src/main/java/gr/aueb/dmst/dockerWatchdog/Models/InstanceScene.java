package gr.aueb.dmst.dockerWatchdog.Models;

public class InstanceScene {
    private String id;
    private String name;
    private String status;
    private Long memoryUsage;
    private Long pids;
    private Double cpuUsage;
    private Double blockI;
    private Double blockO;
    private String image;

    public InstanceScene(String id, String name,String image, String status, Long memoryUsage, Long pids, Double cpuUsage, Double blockI, Double blockO) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.status = status;
        this.memoryUsage = memoryUsage;
        this.pids = pids;
        this.cpuUsage = cpuUsage;
        this.blockI = blockI;
        this.blockO = blockO;
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
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public void setMemoryUsage(Long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public void setPids(Long pids) {
        this.pids = pids;
    }
    public String getImage() {
        return image;
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
