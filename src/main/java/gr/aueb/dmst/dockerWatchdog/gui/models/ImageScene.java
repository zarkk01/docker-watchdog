package gr.aueb.dmst.dockerWatchdog.gui.models;


/**
 * This class represents an Image that be displayed in GUI panels.
 * An ImageScene has an id, name, status, and size, and it is what the user
 * actually sees on the Images panel.
 */
public class ImageScene {
    // ID of the image
    private String id;
    // Name of the image
    private String name;
    // Status of the image ("In use" / "Unused")
    private String status;
    // Size of the image in bytes
    private Long size;

    /**
     * Constructor for the ImageScene class.
     *
     * @param id The id of the image.
     * @param name The name of the image.
     * @param size The size of the image.
     * @param status The status of the image.
     */
    public ImageScene(String id, String name, Long size, String status) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.status = status;
    }

    /**
     * This method returns the id of the image
     * but as substring starting from index 7 so to not include
     * sha256 hashing function in the start.
     *
     * @return The id of the image scene.
     */
    public String getId() {
        return id.substring(7);
    }

    /**
     * This method sets the id of the image scene.
     *
     * @param id The id to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * This method returns the name of the image with the tag of it.
     *
     * @return The name of the image scene.
     */
    public String getName() {
        return name;
    }

    /**
     * This method sets the name of the image scene.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This method returns the status of the image scene
     * whether it is "In use" or "Unused".
     *
     * @return The status of the image scene.
     */
    public String getStatus() {
        return status;
    }

    /**
     * This method sets the status of the image scene.
     *
     * @param status The status to set.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * This method returns the size of the image scene in GB.
     * It converts our size from bytes to GB so be more readable.
     *
     * @return The size of the image scene in GB.
     */
    public String getSize() {
        double sizeInGb = size / (1024.0 * 1024.0 * 1024.0);
        return String.format("%.2f", sizeInGb) + " GB";
    }

    /**
     * This method sets the size of the image scene.
     *
     * @param size The size to set.
     */
    public void setSize(Long size) {
        this.size = size;
    }
}
