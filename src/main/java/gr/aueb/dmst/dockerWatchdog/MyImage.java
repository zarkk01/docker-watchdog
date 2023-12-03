package gr.aueb.dmst.dockerWatchdog;

public class MyImage {

    private final String name;
    private final String id;
    private final Long size;
    private String status;

    public MyImage(String name,String id, Long size,String status) {
        this.name = name;
        this.id = id;
        this.size = size;
        this.status = status;
    }

    // Getter for id
    public String getId() {
        return id;
    }

    // Getter for size
    public Long getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "Name = " + name + " , ID = "+ id.substring(7) +  " , Size = " + String.format("%.2f", (double) size / (1024 * 1024)) + " MB"+ " , Status = " + status ;
    }

    public String getName() {
        return name;
    }

    public String getStatus(){
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }
}