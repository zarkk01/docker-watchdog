package gr.aueb.dmst.dockerWatchdog;

import java.util.Map;

public class MyInstance {
    private final String id;
    private String name;

    private long size;

    private Map<String ,String> labels;
    private final String image;
    private String status;
    private long pids;
    private long memoryUsage;
    private double cpuUsage;
    private double blockI;
    private double blockO;

    // constructor
    public MyInstance(String id , String name , String image , String status ,Map<String,String> labels ,long size,double cpuUsage, long memoryUsage,long pids,double blockI,double blockO) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.status = status;
        this.labels = labels;
        this.size = size;
        this.memoryUsage = memoryUsage;
        this.pids = pids;
        this.cpuUsage = cpuUsage;
        this.blockI = blockI;
        this.blockO = blockO;
    }

    @Override
    public String toString() {
        return "Name = " + name.substring(1) +" , ID = "+ id +", " +" , Image = " + image
                + " , Status = " + status + " , CPU Usage: " + String.format("%.2f", cpuUsage*100) +" %" + " , Memory usage : " +String.format("%.2f", (double)memoryUsage) + " MB" + " , PIDs : " + pids + " , Block I/0 : " + String.format("%.2f", blockI) + "MB/"+String.format("%.2f", blockO)+"MB";
    }

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

    public Map<String, String> getLabels() {
        return labels;
    }

    // Getter for size
    public long getSize() {
        return size;
    }

    public long getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    // Setter for name
    public void setName(String newName) { this.name = newName; }

    // Setter for image
    public void setStatus(String status) {
        this.status = status;
    }

    // Setter for labels
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    // Setter for size
    public void setSize(long size) {
        this.size = size;
    }

    public static MyInstance getInstanceByid(String id) {
        MyInstance instanceToReturn = null;
        for (MyInstance instance:Main.myInstancesList) {
            if (id.equals(instance.getId())) {
                instanceToReturn = instance;
            }
        }
        if (instanceToReturn != null) {
            return instanceToReturn;
        } else {
            return null;
        }
    }
    public long getPids() {
        return pids;
    }
    public void setPids(long pids) {
        this.pids = pids;
    }
    public void setBlockI(double blockI) {
        this.blockI = blockI;
    }
    public void setBlockO(double blockO) {
        this.blockO = blockO;
    }
    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }
}