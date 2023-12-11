package gr.aueb.dmst.dockerWatchdog;

public class MyImage {

    private final String name; // Name of image final cause will not change
    private final String id; // ID of image final cause will not change
    private final Long size; // Size of image final cause will not change
    private String status; // Status of image

    // Constructor
    public MyImage(String name, String id, Long size, String status) {

        // Initialize images variables with the values of the parameters
        this.name = name;
        this.id = id;
        this.size = size;
        this.status = status;
    }

    // Method toString that returns a string with the values of the instance variables
    @Override
    public String toString() {
        return "Name = " + name + " , ID = " + id.substring(7) + " , Size = " + String.format("%.2f", (double) size / (1024 * 1024)) + " MB" + " , Status = " + status;
    }

    // Getter for id
    public String getId() {
        return id;
    }

    // Getter for size
    public Long getSize() {
        return size;
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Getter for status
    public String getStatus() {
        return status;
    }

    // Setter for status, only setter cause status can change
    public void setStatus(String status) {
        this.status = status;
    }

    public static MyImage getImageByName(String name){
        for(MyImage image : Main.myImagesList){
            if(image.getName().equals(name)){
                return image;
            }
        }
        return null;
    }

    public static MyImage getImageByID(String id){
        for(MyImage image : Main.myImagesList){
            if(image.getId().equals(id)){
                return image;
            }
        }
        return null;
    }
}