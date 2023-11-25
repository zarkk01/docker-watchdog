package gr.aueb.dmst.dockerWatchdog;

public class MyInstance {
    private final String id;
    private String name;
    private final String image;
    private String status;

    // constructor
    public MyInstance(String id ,String name ,String image ,String status){
        this.id = id;
        this.name = name;
        this.image = image;
        this.status = status;
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

    // Setter for name
    public void setName(String newName) { this.name = newName; }

    // Setter for image
    public void setStatus(String status) {
        this.status = status;
    }
}