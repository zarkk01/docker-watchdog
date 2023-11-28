package gr.aueb.dmst.dockerWatchdog;

import java.util.Map;

public class MyInstance {
    private final String id;
    private String name;

    private long size;

    private Map<String ,String> labels;
    private final String image;
    private String status;

    // constructor
    public MyInstance(String id , String name , String image , String status ,Map<String,String> labels ,long size) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.status = status;
        this.labels = labels;
        this.size = size;
    }

    @Override
    public String toString() {
        return "Name = " + name.substring(1) +" , ID = "+ id +", " +" , Image = " + image.substring(7)
                + " , Status = " + status;
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


}