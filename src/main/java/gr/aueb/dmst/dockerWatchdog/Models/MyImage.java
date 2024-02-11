package gr.aueb.dmst.dockerWatchdog.Models;

import gr.aueb.dmst.dockerWatchdog.Main;


/**
 * This class represents an image object.
 * MyImage is a custom class we created to represent
 * Docker's Java API original images. In this way, we have better control
 * over them, and we assign them specific properties and values we need.
 * A MyImage has a name, id, size, and status.
 */
public class MyImage {

    private final String name;
    private final String id;
    private final Long size;
    private String status;

    /**
     * Constructor for the MyImage class.
     *
     * @param name The name of the image.
     * @param id The id of the image.
     * @param size The size of the image.
     * @param status The status of the image.
     */
    public MyImage(String name, String id, Long size, String status) {
        this.name = name;
        this.id = id;
        this.size = size;
        this.status = status;
    }

    /**
     * Returns a string representation of the MyImage object.
     *
     * @return A string representation of the MyImage object.
     */
    @Override
    public String toString() {
        return String.format("Name = %s, ID = %s, Size = %.2f MB, Status = %s",
                name, id.substring(7), (double) size / (1024 * 1024), status);
    }

    /**
     * Returns the id of the image.
     *
     * @return The id of the image.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the size of the image.
     *
     * @return The size of the image.
     */
    public Long getSize() {
        return size;
    }

    /**
     * Returns the name of the image.
     *
     * @return The name of the image.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the status of the image.
     *
     * @return The status of the image.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the image.
     *
     * @param status The status to set.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Helper method that retrieves a MyImage instance
     * from the myImagesList based on its ID. This method is used
     * whenever we have only the ID of an image, and we want to find
     * the corresponding MyImage instance, especially in MonitorThread events.
     *
     * @param id The ID of the image to search for.
     * @return The MyImage instance with the specified ID, or null if not found.
     */
    public static MyImage getImageByID(String id) {
        // Search for the image with the specified ID in the myImagesList.
        return Main.myImages.stream()
                .filter(image -> image.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
