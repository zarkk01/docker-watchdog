package gr.aueb.dmst.dockerWatchdog.Models;

import gr.aueb.dmst.dockerWatchdog.Main;

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
        return String.format("Name = %s, ID = %s, Size = %.2f MB, Status = %s", name, id.substring(7), (double) size / (1024 * 1024), status);
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


    /**
     * Retrieves a MyImage instance from the myImagesList based on its name.
     *
     * @param name The name of the image to search for.
     * @return The MyImage instance with the specified name, or null if not found.
     */
    public static MyImage getImageByName(String name) {
        return Main.myImagesList.stream()
                .filter(image -> image.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves a MyImage instance from the myImagesList based on its ID.
     *
     * @param id The ID of the image to search for.
     * @return The MyImage instance with the specified ID, or null if not found.
     */
    public static MyImage getImageByID(String id) {
        return Main.myImagesList.stream()
                .filter(image -> image.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

}