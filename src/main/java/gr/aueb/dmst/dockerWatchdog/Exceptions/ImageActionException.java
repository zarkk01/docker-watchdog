package gr.aueb.dmst.dockerWatchdog.Exceptions;

/**
 * The ImageActionException class is a custom exception used in the ImagesController.
 * It includes an additional field for the name of the image that caused the exception.
 * This image name is included in the exception message.
 */
public class ImageActionException extends Exception {
    // The name of the image that caused the exception
    private final String imageName;

    /**
     * Constructs a new ImageActionException with the specified detail message and image name.
     * The image name is appended to the detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method).
     * @param imageName the name of the image that caused the exception (which is saved for later retrieval by the getImageName() method).
     */
    public ImageActionException(String message, String imageName) {
        super(message + " Image name: " + imageName);
        this.imageName = imageName;
    }

    /**
     * Returns the name of the image that caused the exception.
     *
     * @return the name of the image that caused the exception.
     */
    public String getImageName() {
        return imageName;
    }
}