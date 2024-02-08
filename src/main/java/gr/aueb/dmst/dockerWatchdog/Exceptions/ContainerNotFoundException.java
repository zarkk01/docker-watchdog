package gr.aueb.dmst.dockerWatchdog.Exceptions;

/**
 * This class represents a custom exception that is thrown when we make an action
 * on a container, but it is not found.
 * It includes the ID of the container that was not found.
 */
public class ContainerNotFoundException extends Exception {
    // The ID of the container that was not found
    private final String containerId;

    /**
     * Constructor for the ContainerNotFoundException class.
     *
     * @param containerId The ID of the container that was not found.
     */
    public ContainerNotFoundException(String containerId) {
        super("Container not found.");
        this.containerId = containerId;
    }

    /**
     * Returns a detailed message about the exception with the container ID.
     *
     * @return A string representation of the exception message.
     */
    @Override
    public String getMessage() {
        return "Container with ID " + containerId + " not found. Please check the container ID and try again.";
    }
}
