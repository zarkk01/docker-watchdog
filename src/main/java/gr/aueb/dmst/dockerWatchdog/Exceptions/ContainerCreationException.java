package gr.aueb.dmst.dockerWatchdog.Exceptions;

/**
 * This class represents a custom exception that is thrown when a container fails to be created.
 * It includes the name of the image that was used in the attempt to create the container.
 */
public class ContainerCreationException extends Exception {
    // The name of the image that was used in the attempt to create the container
    private final String imageName;

    /**
     * Constructor for the ContainerCreationException class.
     *
     * @param imageName The name of the image that was used in the attempt to create the container.
     */
    public ContainerCreationException(String imageName) {
        super("Failed to create container.");
        this.imageName = imageName;
    }

    /**
     * Returns a detailed message about the exception with the name of the image.
     *
     * @return A string representation of the exception message.
     */
    @Override
    public String getMessage() {
        return "Failed to create container from " + imageName + " . Check if all requirements are met.";
    }
}
