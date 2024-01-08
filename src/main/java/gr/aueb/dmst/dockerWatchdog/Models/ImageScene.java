package gr.aueb.dmst.dockerWatchdog.Models;

public class ImageScene {
    private String id;
    private String name;
    private String status;
    private Long size;

    public ImageScene(String id, String name,Long size, String status) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.status = status;
    }

    public String getId() {
        return id.substring(7);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSize() {
        double sizeInGb = size / (1024.0 * 1024.0 * 1024.0);
        return String.format("%.2f", sizeInGb)+" GB";
    }

    public void setSize(Long size) {
        this.size = size;
    }
}
