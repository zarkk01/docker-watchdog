package gr.aueb.dmst.dockerWatchdog;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Immutable
@Entity
@Table(name = "Instances")
public class Instans {
    @Id
    @Column(name = "id")
    private String id; // ID of instance is final

    @Column(name = "name")
    private String name; // Instance name

    @Column(name = "image")
    private String image; // Image of instance

    @Column(name = "status")
    private String status; // Status of instance

    @Column(name = "pids")
    private long pids; // PIDs of instance

    @Column(name = "memoryUsage")
    private long memoryUsage; // Memory usage of instance in MB

    @Column(name = "cpuUsage")
    private double cpuUsage; // CPU usage of instance in %

    @Column(name = "blockI")
    private double blockI; // Block I of instance in MB

    @Column(name = "blockO")
    private double blockO; // Block O of instance in MB

    @Column(name = "ports")
    private String ports; // Ports of instance

    // Getter for id
    public String getId() {
        return id;
    }

    // Getter for image
    public String getImage() {
        return image;
    }

    // Getter for status
    public String getStatus() {
        return status;
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Getter for memoryUsage
    public long getMemoryUsage() {
        return memoryUsage;
    }

    // Getter for cpuUsage
    public void setMemoryUsage(long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    // Getter for PIDs
    public long getPids() {
        return pids;
    }

    // Getter for blockI
    public double getBlockI() {
        return blockI;
    }

    // Getter for blockO
    public double getBlockO() {
        return blockO;
    }

    //Getter for cpuUsage
    public double getCpuUsage() {
        return cpuUsage;
    }

    //Getter for ports
    public String getPorts() {
        return ports;
    }

    // Setter for cpuUsage
    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    // Setter for name
    public void setName(String newName) {
        this.name = newName;
    }

    // Setter for image
    public void setStatus(String status) {
        this.status = status;
    }

    // Setter for PIDs
    public void setPids(long pids) {
        this.pids = pids;
    }

    // Setter for blockI
    public void setBlockI(double blockI) {
        this.blockI = blockI;
    }

    // Setter for blockO
    public void setBlockO(double blockO) {
        this.blockO = blockO;
    }

}