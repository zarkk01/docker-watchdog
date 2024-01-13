package gr.aueb.dmst.dockerWatchdog.Exceptions;

/**
 * This class represents a custom exception that is thrown when an image is not found.
 * It includes the name of the image that was not found.
 */
public class ImageNotFoundException extends Exception {
    // The name of the image that was not found.
    private final String imageName;

    /**
     * Constructor for the ImageNotFoundException class.
     *
     * @param imageName The name of the image that was not found.
     */
    public ImageNotFoundException(String imageName) {
        super("Image not found.");
        this.imageName = imageName;
    }

    /**
     * Returns a detailed message about the exception with the image name.
     *
     * @return A string representation of the exception message.
     */
    @Override
    public String getMessage() {
        return "Image " + imageName + " not found. Please check the image name and try again.";
    }
}
